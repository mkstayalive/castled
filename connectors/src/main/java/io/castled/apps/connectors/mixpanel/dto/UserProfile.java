package io.castled.apps.connectors.mixpanel.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.castled.apps.connectors.activecampaign.dto.FieldValue;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfile {

    private List<Map<String,Object>> data ;
    private Integer verbose;
}
