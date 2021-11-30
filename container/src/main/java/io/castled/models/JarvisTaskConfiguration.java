package io.castled.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class JarvisTaskConfiguration {

    private List<JarvisGroupConfig> groupConfig;
    private int priorityCoolDownMins = 60;
}
