package io.castled.jarvis.scheduler.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class JarvisSchedulerConfig {

    private Map<String, String> quartzConfig;

    private Map<String, String> scheduleOverrides;
}
