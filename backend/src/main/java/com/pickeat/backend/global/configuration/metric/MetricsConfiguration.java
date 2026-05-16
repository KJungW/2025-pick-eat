package com.pickeat.backend.global.configuration.metric;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MetricsConfiguration {

    private static final Set<String> INFRA_URI_EXACT = Set.of("/", "/error");
    private static final List<String> INFRA_URI_PREFIXES = List.of(
            "/actuator", "/swagger", "/v3/api-docs", "/static", "/webjars", "/favicon"
    );

    private static final Pattern ID_PATTERN = Pattern.compile("/\\d+(/|$)");
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}(/|$)"
    );

    @Bean
    MeterRegistryCustomizer<MeterRegistry> goldenSignalsCustomizer() {
        return registry -> {
            registry.config()
                    .meterFilter(MeterFilter.deny(this::isDenyRequest))
                    .meterFilter(MeterFilter.replaceTagValues("uri", this::normalizeUri));
        };
    }

    private boolean isDenyRequest(Meter.Id id) {
        // HTTP 요청이 아닌 경우 허용
        if (!"http.server.requests".equals(id.getName())) {
            return false;
        }

        String uri = id.getTag("uri");

        // uri가 null인 경우 거부
        if (uri == null) {
            return true;
        }

        // 인프라 경로 거부
        boolean isInfraRequest = INFRA_URI_EXACT.contains(uri) ||
                INFRA_URI_PREFIXES.stream().anyMatch(uri::startsWith);

        if (isInfraRequest) {
            log.debug("Filtering infrastructure request: {}", uri);
            return true;
        }

        // 그외 모두 허용
        return false;
    }

    private String normalizeUri(String uri) {
        // null 또는 UNKNOWN은 정규화 진행하지 않음
        if (uri == null || "UNKNOWN".equals(uri)) {
            return uri;
        }

        try {
            // 쿼리 파라미터 제거
            int queryIndex = uri.indexOf('?');
            if (queryIndex != -1) {
                uri = uri.substring(0, queryIndex);
            }

            // ID 정규화: /123/ -> /{id}/
            uri = ID_PATTERN.matcher(uri).replaceAll("/{id}$1");

            // UUID 정규화: /uuid/ -> /{uuid}/
            uri = UUID_PATTERN.matcher(uri).replaceAll("/{uuid}$1");

            return uri;

        } catch (Exception e) {
            log.warn("Failed to normalize URI: {}, error: {}", uri, e.getMessage());
            return uri;
        }
    }
}
