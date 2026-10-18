# Questions & Answers

Technical questions and design decisions for the Technical Weather App.

## 1. ¿Cómo mejoraríamos el tiempo de respuesta del servicio de municipios?

La principal optimización sería introducir una **caché en el backend** para evitar consultar a AEMET cada vez que un usuario realiza una búsqueda.

El listado de municipios es información que cambia con muy poca frecuencia. Por tanto, no tiene sentido recuperar el catálogo completo en cada petición y posteriormente filtrarlo.

En la implementación actual del MVP he optado por cargar el catálogo una única vez en memoria en el frontend y realizar las búsquedas localmente. Esta decisión permite reducir las peticiones durante una misma sesión sin introducir dependencias adicionales, aunque no elimina las consultas iniciales de distintos usuarios.

Para evolucionar esta solución implementaría:

- **Caché en el backend:** utilizaría Spring Cache con Caffeine, almacenando el catálogo completo con un TTL configurable, por ejemplo de 24 horas.
- **Filtrado en memoria:** el servicio de búsqueda trabajaría sobre los datos cacheados, evitando nuevas llamadas a AEMET para cada término introducido.
- **Precarga opcional:** cargaría el catálogo al iniciar la aplicación o mediante un proceso de actualización para evitar que el primer usuario soporte la latencia de AEMET.
- **Caché distribuida:** si la aplicación escalase horizontalmente, valoraría Redis para compartir los datos entre instancias.

Con esto, la mayoría de las búsquedas se resolverían directamente desde nuestro backend. Además de reducir la latencia, disminuiríamos considerablemente las peticiones realizadas a AEMET.

## 2. ¿Por qué AEMET devuelve una URL en lugar de los datos directamente?

Considero que esta decisión responde principalmente a la **separación entre la solicitud de acceso a un recurso y la descarga de su contenido**.

La primera petición permite validar la solicitud y devuelve información sobre el recurso, incluyendo una URL para descargar los datos y otra para consultar sus metadatos.

AEMET documenta que estas URLs son temporales y tienen una validez aproximada de cinco minutos.

Este diseño presenta varias ventajas:

- **Separación de responsabilidades:** la API gestiona la solicitud y proporciona la ubicación del recurso, mientras que la descarga se realiza mediante una segunda petición.
- **Flexibilidad:** permite ofrecer recursos de distintos tamaños y formatos, como JSON, CSV o imágenes, manteniendo una estructura de respuesta común.
- **Escalabilidad:** separar el acceso a la API de la descarga del contenido podría facilitar optimizaciones de almacenamiento, distribución y entrega de los datos.
- **Control de acceso:** la API puede autorizar inicialmente una petición y proporcionar un enlace de duración limitada para acceder al resultado.
- **Metadatos independientes:** el consumidor puede consultar información sobre la estructura y el significado de los datos sin mezclarla con el contenido principal.

No puedo asegurar cuál fue la motivación interna exacta de AEMET, pero considero que este diseño facilita la distribución y reutilización de sus recursos.

En nuestra aplicación, esta particularidad queda encapsulada en `AemetOpenDataClient`, de forma que los casos de uso desconocen que son necesarias dos peticiones HTTP.

## 3. ¿Cómo recordaríamos el último municipio seleccionado?

Para este requisito utilizaría **localStorage**, ya que necesitamos conservar una preferencia sencilla entre sesiones sin requerir autenticación ni almacenamiento en el servidor.

Cuando el usuario seleccionase un municipio, guardaríamos su identificador y nombre. Al volver a acceder a la aplicación, recuperaríamos esa información y cargaríamos automáticamente la predicción correspondiente al día siguiente.

La implementación sería:

1. Guardar el municipio seleccionado en `localStorage`.
2. Al inicializar la aplicación, comprobar si existe un municipio guardado.
3. Si existe, restaurar la selección en el estado de la aplicación.
4. Ejecutar automáticamente el caso de uso de consulta de la predicción.
5. Actualizar el valor almacenado cada vez que el usuario seleccione otro municipio.

En el frontend actual, esta funcionalidad podría incorporarse mediante un pequeño servicio de persistencia integrado con `ForecastStore`, manteniendo separada la gestión del estado de la persistencia.

**No almacenaría la predicción meteorológica como parte de esta preferencia**, ya que podría quedar desactualizada. Únicamente persistiría la selección del municipio y solicitaría los datos correspondientes al día actual de consulta.

Esta solución es suficiente para usuarios anónimos y evita introducir una base de datos innecesariamente. Si en el futuro incorporásemos cuentas de usuario y sincronización entre dispositivos, podríamos trasladar esta preferencia al backend.

## 4. ¿Cómo reduciríamos la carga sobre AEMET sin perjudicar a nuestros usuarios?

El objetivo principal sería **desacoplar el número de consultas de nuestros usuarios del número de peticiones realizadas a AEMET**.

Actualmente, cada consulta de predicción puede generar dos peticiones HTTP a AEMET. Si varios usuarios solicitan la misma predicción, estamos recuperando repetidamente información equivalente.

Implementaría una estrategia combinada de caché, control de peticiones y tolerancia a fallos.

**Primero: caché de predicciones.**

Introduciría una caché en el backend utilizando Caffeine inicialmente, con claves basadas en el municipio y la fecha de predicción.

Almacenaría los datos meteorológicos normalizados en Celsius y aplicaría posteriormente la conversión de unidades solicitada por el usuario. De esta forma, una consulta en Fahrenheit podría reutilizar exactamente los mismos datos cacheados.

Utilizaría un TTL configurable, inicialmente de 30 a 60 minutos, ajustándolo según la frecuencia de actualización de los datos y el volumen de peticiones observado.

**Segundo: evitar peticiones duplicadas concurrentes.**

Si varios usuarios solicitan simultáneamente una predicción que todavía no está cacheada, intentaría que únicamente una petición llegase a AEMET y que las restantes compartiesen su resultado.

Esto evitaría que una entrada de caché vacía o recién expirada provocase múltiples llamadas idénticas al proveedor.

**Tercero: limitar y controlar las llamadas externas.**

Establecería un límite de peticiones salientes hacia AEMET para respetar sus restricciones de uso.

También incorporaría una política de reintentos acotados, con exponential backoff y jitter para errores transitorios, respetando el encabezado `Retry-After` cuando estuviese disponible.

Ante respuestas 429 persistentes, suspendería temporalmente nuevas solicitudes al proveedor mediante un mecanismo de circuit breaker o control equivalente, evitando agravar la situación.

**Cuarto: mantener el servicio disponible ante incidencias.**

Si AEMET no estuviese disponible, priorizaría servir una predicción previamente cacheada, siempre que correspondiese a la fecha solicitada y su antigüedad fuese aceptable. En ese caso, informaría al usuario de que los datos podrían no estar completamente actualizados.

Si no existiese información válida, mostraríamos un mensaje de indisponibilidad en lugar de realizar reintentos constantes.

**Quinto: monitorización y escalabilidad.**

Registraría métricas como la tasa de aciertos de caché, el número de llamadas reales a AEMET, sus tiempos de respuesta y la frecuencia de errores 429.

Si el tráfico requiriese varias instancias del backend, sustituiría o complementaría la caché local con una solución compartida como Redis, manteniendo también un control global de peticiones al proveedor.

**Conclusión**

Para el MVP mantendría una solución sencilla, pero ante el escenario de sobrecarga descrito priorizaría la caché backend, la eliminación de peticiones duplicadas y el control de solicitudes externas.

De esta manera, podríamos atender un número elevado de consultas reutilizando información meteorológica ya obtenida, reduciendo la dependencia inmediata de AEMET y protegiendo su infraestructura sin perjudicar significativamente la experiencia del usuario.
