package io.castled.apps.connectors.intercom.client.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Optional;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginationObject {

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Data
    private static class PaginationOffset {
        private String startingAfter;
    }

    private int page;
    private int perPage;
    private int totalPages;
    private PaginationOffset next;

    public String getNextOffset() {
        return Optional.ofNullable(next).map(PaginationOffset::getStartingAfter).orElse(null);
    }
}
