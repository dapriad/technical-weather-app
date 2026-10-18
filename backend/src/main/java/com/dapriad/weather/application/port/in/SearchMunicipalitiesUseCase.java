package com.dapriad.weather.application.port.in;

import java.util.List;
import com.dapriad.weather.domain.model.Municipality;

public interface SearchMunicipalitiesUseCase {

    List<Municipality> searchByName(String name);
}