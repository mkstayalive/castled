package io.castled.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.castled.apps.models.PrimaryKeyEligibles;
import io.castled.schema.SimpleSchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PipelineSchema {
    private SimpleSchema warehouseSchema;
    private SimpleSchema appSchema;
    private PrimaryKeyEligibles pkEligibles;
}

