package com.pickeat.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;

@Configuration
public class VirtualThreadExecutorConfig {

    @Bean
    public TaskExecutor virtualThreadExecutor() {
        return new VirtualThreadTaskExecutor("pickeat-vthread-");
    }
}
