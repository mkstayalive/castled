package io.castled.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ResetPasswordRequest {
    @NotNull
    private String password;

    @NotNull
    private String token;
}
