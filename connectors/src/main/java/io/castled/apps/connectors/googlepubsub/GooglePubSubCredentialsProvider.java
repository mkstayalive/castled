package io.castled.apps.connectors.googlepubsub;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import io.castled.commons.models.ServiceAccountDetails;
import io.castled.commons.util.GoogleAuthUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class GooglePubSubCredentialsProvider implements CredentialsProvider {

    private ServiceAccountDetails serviceAccountDetails;

    public GooglePubSubCredentialsProvider(ServiceAccountDetails serviceAccountDetails) {
        this.serviceAccountDetails = serviceAccountDetails;
    }

    @Override
    public Credentials getCredentials() throws IOException {
        return GoogleAuthUtil.getCredentials(this.serviceAccountDetails);
    }
}
