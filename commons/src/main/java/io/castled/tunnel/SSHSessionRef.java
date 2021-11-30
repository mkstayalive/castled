package io.castled.tunnel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SSHSessionRef {
    private String remoteHost;
    private int remotePort;
    private SSHTunnelParams sshTunnelParams;
}
