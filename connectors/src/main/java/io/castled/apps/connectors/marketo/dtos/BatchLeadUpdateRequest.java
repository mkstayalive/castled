package io.castled.apps.connectors.marketo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchLeadUpdateRequest {

    private String action;
    private String lookupField;
    private List<Map<String, Object>> input;
}
