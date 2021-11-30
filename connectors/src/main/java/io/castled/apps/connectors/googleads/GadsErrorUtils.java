package io.castled.apps.connectors.googleads;

import com.google.ads.googleads.v7.errors.GoogleAdsError;
import com.google.ads.googleads.v7.errors.GoogleAdsFailure;
import com.google.ads.googleads.v7.utils.ErrorUtils;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.rpc.Status;

import java.util.List;

@Singleton
public class GadsErrorUtils extends ErrorUtils {

    public List<GoogleAdsError> getErrors(long operationIndex, Status partialFailure) throws InvalidProtocolBufferException {
        List<GoogleAdsError> result = Lists.newArrayList();
        for (Any detail : partialFailure.getDetailsList()) {
            GoogleAdsFailure failure = getGoogleAdsFailure(detail);
            result.addAll(getAdsErrors(operationIndex, failure));
        }
        return result;
    }

    public List<GoogleAdsError> getAdsErrors(
            long operationIndex, GoogleAdsFailure googleAdsFailure) {
        List<GoogleAdsError> result = Lists.newArrayList();
        // Searches all the errors for one relating to the specified operation.
        for (ErrorPath<GoogleAdsError> path : getErrorPaths(googleAdsFailure)) {
            if (path.getIndex().isPresent()
                    && path.getIndex().get() == operationIndex) {
                GoogleAdsError error = path.getError();
                if (!result.contains(error)) {
                    result.add(path.getError());
                }
            }
        }
        return result;
    }

}
