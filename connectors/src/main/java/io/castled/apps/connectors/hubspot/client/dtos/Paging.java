package io.castled.apps.connectors.hubspot.client.dtos;

import lombok.Data;

@Data
public class Paging {

    private Offset next;

    @Data
    public static class Offset {
        private String after;
    }
}
