package io.castled.apps.models;

import com.google.api.client.util.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrimaryKeyEligibles {

    private List<String> eligibles;
    private boolean autoDetect;

    public static PrimaryKeyEligibles eligibles(List<String> eligibles) {
        return new PrimaryKeyEligibles(eligibles, false);
    }

    public static PrimaryKeyEligibles autoDetect() {
        return new PrimaryKeyEligibles(Lists.newArrayList(), true);
    }
}
