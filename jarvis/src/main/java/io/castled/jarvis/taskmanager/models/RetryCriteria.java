package io.castled.jarvis.taskmanager.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetryCriteria {

    private int maxRetries = 3;
    private boolean retryOnExpiry = true;
}
