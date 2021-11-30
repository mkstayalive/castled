package io.castled.errors;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.castled.commons.errors.CastledErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PipelineErrorAndSample {
    private CastledErrorCode errorCode;
    private String description;
    private Map<String, String> record;
    private long recordCount;

    public void incrementRecordCount() {
        recordCount++;
    }
}
