package io.castled.apps.connectors.googleads;

import com.google.ads.googleads.v7.errors.GoogleAdsError;
import com.google.inject.Singleton;
import io.castled.commons.errors.CastledError;
import io.castled.commons.errors.errorclassifications.ExternallyCategorizedError;


@Singleton
public class GadsErrorParser {
    public CastledError parseGadsError(GoogleAdsError googleAdsError) {
        return new ExternallyCategorizedError(googleAdsError.getErrorCode().getErrorCodeCase().name(),
                googleAdsError.getMessage());
    }
}
