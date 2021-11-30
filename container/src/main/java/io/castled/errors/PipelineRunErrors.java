package io.castled.errors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PipelineRunErrors {

    private List<String> sampleFields;
    private List<PipelineErrorAndSample> errorAndSamples;
}
