package io.castled.warehouses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.castled.forms.FormField;
import io.castled.forms.FormFieldSchema;
import io.castled.forms.FormFieldType;
import io.castled.forms.GroupActivator;
import io.castled.tunnel.SSHTunnelParams;
import lombok.Getter;
import lombok.Setter;

import static io.castled.forms.FormGroups.TUNNEL_GROUP;

@Getter
@Setter
@GroupActivator(dependencies = {"tunnelEnabled"}, group = TUNNEL_GROUP)
public class TunneledWarehouseConfig extends WarehouseConfig {

    @FormField(description = "SSH Host", schema = FormFieldSchema.STRING, group = TUNNEL_GROUP, title = "SSH Host", placeholder = "", type = FormFieldType.TEXT_BOX)
    private String sshHost;

    @FormField(description = "SSH Port", title = "SSH Port", placeholder = "", schema = FormFieldSchema.NUMBER, group = TUNNEL_GROUP, type = FormFieldType.TEXT_BOX)
    private int sshPort;

    @FormField(description = "SSH User", title = "SSH User", placeholder = "", schema = FormFieldSchema.STRING, group = TUNNEL_GROUP, type = FormFieldType.TEXT_BOX)
    private String sshUser;

    @FormField(description = "Enable SSH Tunnel", title = "Enable Tunnel", schema = FormFieldSchema.BOOLEAN, type = FormFieldType.CHECK_BOX)
    private boolean tunnelEnabled;

    @FormField(description = "Private Key file", title = "Private Key file", schema = FormFieldSchema.STRING, group = TUNNEL_GROUP, type = FormFieldType.TEXT_FILE)
    private String privateKey;

    @FormField(description = "SSH passphrase", title = "SSH passphrase", schema = FormFieldSchema.STRING, group = TUNNEL_GROUP, type = FormFieldType.TEXT_BOX)
    private String passPhrase;

    @JsonIgnore
    public SSHTunnelParams getSSHTunnelParams() {
        if (!tunnelEnabled) {
            return null;
        }
        String sshHost = getSshHost();
        int sshPort = getSshPort();
        String sshUser = getSshUser();
        return new SSHTunnelParams(sshHost, sshUser, sshPort, privateKey, passPhrase);
    }
}
