package io.castled.models.users;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.security.Principal;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Builder
@AllArgsConstructor
//@NoArgsConstructor
public class User implements Principal {

    private Long id;

    private String email;
    private String firstName;
    private String lastName;

    private Long teamId;
    private boolean isDeleted;

    private Long createdTs;


    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String getName() {
        return getFullName();
    }
}
