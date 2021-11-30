package io.castled.tunnel;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SSHTunnelParams {

    private String host;
    private String user;
    private int port;

    private String privateKey;
    private String passPhrase;

}
