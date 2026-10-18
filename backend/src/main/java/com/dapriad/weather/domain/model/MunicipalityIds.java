package com.dapriad.weather.domain.model;

import java.util.Locale;

public final class MunicipalityIds {

    private static final String ID_PREFIX = "id";

    private MunicipalityIds() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }

        String trimmed = raw.trim();
        if (trimmed.toLowerCase(Locale.ROOT).startsWith(ID_PREFIX)) {
            return trimmed.substring(ID_PREFIX.length()).trim();
        }

        return trimmed;
    }
}