package io.castled.apps.connectors.hubspot.client.dtos;

import lombok.Data;

import java.util.List;

@Data
public class PaginatedObjects {

    private List<HubspotObject> results;
    private Paging paging;

    public boolean hasMore() {
        return paging != null;
    }

}
