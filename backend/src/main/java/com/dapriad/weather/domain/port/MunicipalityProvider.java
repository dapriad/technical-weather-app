package com.dapriad.weather.domain.port;

import java.util.List;
import com.dapriad.weather.domain.model.Municipality;

public interface MunicipalityProvider {

    List<Municipality> findAll();
}