package com.pickeat.backend.global.log;

import com.pickeat.backend.global.log.model.Log;
import net.logstash.logback.marker.Markers;
import org.slf4j.LoggerFactory;

public final class LogWriter {

    private LogWriter() {
        throw new AssertionError();
    }

    public static void error(Class<?> clazz, Log log) {
        LoggerFactory.getLogger(clazz)
                .error(Markers.appendEntries(log.fields()), log.summary());
    }

    public static void warn(Class<?> clazz, Log log) {
        LoggerFactory.getLogger(clazz)
                .warn(Markers.appendEntries(log.fields()), log.summary());
    }

    public static void info(Class<?> clazz, Log log) {
        LoggerFactory.getLogger(clazz)
                .info(Markers.appendEntries(log.fields()), log.summary());
    }

    public static void debug(Class<?> clazz, Log log) {
        LoggerFactory.getLogger(clazz)
                .debug(Markers.appendEntries(log.fields()), log.summary());
    }

    public static void trace(Class<?> clazz, Log log) {
        LoggerFactory.getLogger(clazz)
                .trace(Markers.appendEntries(log.fields()), log.summary());
    }
}
