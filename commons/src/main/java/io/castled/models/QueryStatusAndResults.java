package io.castled.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryStatusAndResults {

    private QueryStatus status;
    private String failureMessage;
    private QueryResults queryResults;
}
