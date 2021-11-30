package io.castled.tunnel;

import com.google.common.collect.Maps;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
public class SSHTunnelManager implements AutoCloseable {
    private final Map<SSHSessionRef, Session> sessions = Maps.newHashMap();


    public HostAndPort getTunneledHostAndPort(SSHSessionRef sshSessionRef) throws JSchException {
        SSHTunnelParams sshTunnelParams = sshSessionRef.getSshTunnelParams();
        String remoteHost = sshSessionRef.getRemoteHost();
        int remotePort = sshSessionRef.getRemotePort();
        if (sshTunnelParams == null) {
            return new HostAndPort(remoteHost, remotePort);
        }
        return new HostAndPort("localhost", getTunneledPort(sshSessionRef));
    }

    private int getTunneledPort(SSHSessionRef sshSessionRef) throws JSchException {
        if (sessions.containsKey(sshSessionRef) && hasTunneledPort(sshSessionRef)) {
            return Integer.parseInt(sessions.get(sshSessionRef).getPortForwardingL()[0].split(":")[0]);
        }
        Session sshSession = createSecureSession(sshSessionRef.getSshTunnelParams());
        int localPort = sshSession.setPortForwardingL(0, sshSessionRef.getRemoteHost(), sshSessionRef.getRemotePort());
        this.sessions.put(sshSessionRef, sshSession);
        return localPort;
    }

    private boolean hasTunneledPort(SSHSessionRef sessionRef) throws JSchException {
        return sessions.get(sessionRef).getPortForwardingL().length > 0;
    }


    private Session createSecureSession(SSHTunnelParams sshTunnelParams) throws JSchException {
        JSch jsch = new JSch();
        com.jcraft.jsch.Session session = jsch.getSession(sshTunnelParams.getUser(),
                sshTunnelParams.getHost(), sshTunnelParams.getPort());
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("PreferredAuthentications", "publickey");
        jsch.addIdentity(UUID.randomUUID().toString(), sshTunnelParams.getPrivateKey().getBytes(), null,
                Optional.ofNullable(sshTunnelParams.getPassPhrase()).map(String::getBytes).orElse(null));
        session.connect(60000);
        return session;
    }

    @Override
    public void close() {
        for (Map.Entry<SSHSessionRef, Session> sessionEntry : sessions.entrySet()) {
            sessionEntry.getValue().disconnect();
        }
    }
}
