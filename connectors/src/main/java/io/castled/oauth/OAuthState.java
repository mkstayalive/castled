package io.castled.oauth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuthState {

    private String successUrl;
    private String failureUrl;
    private String serverUrl;
}
