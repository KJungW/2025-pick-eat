package com.pickeat.backend.global.utility;

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SystemRequestChecker {

    private static final Set<String> INFRA_URI_EXACT = Set.of("/", "/error");
    private static final List<String> INFRA_URI_PREFIX = List.of(
            "/actuator", "/swagger", "/v3/api-docs", "/static", "/webjars", "/favicon"
    );

    public boolean isSystemRequest(String uri) {
        if (uri == null || uri.isBlank()) {
            return false;
        }

        if (INFRA_URI_EXACT.contains(uri)) {
            return true;
        }

        if (INFRA_URI_PREFIX.stream().anyMatch(uri::startsWith)) {
            return true;
        }

        return false;
    }
}
