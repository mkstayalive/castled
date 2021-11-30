package io.castled.tunnel;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HostAndPort {
    private String host;
    private int port;
}
