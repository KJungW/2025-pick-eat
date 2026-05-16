package com.pickeat.backend.global.log.model;

import java.util.Map;

public interface Log {

    Map<String, Object> fields();

    String summary();
}
