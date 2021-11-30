package io.castled.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaitTimeAndRetry {

    private long waitTimeMs;
    private boolean shouldRetry;

}
