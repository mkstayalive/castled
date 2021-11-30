package io.castled.apps.connectors.sendgrid;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendgridUpsertError {

    private String email;
    private String message;
}
