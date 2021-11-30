package io.castled.apps.connectors.googleads;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v7.resources.ConversionCustomVariable;
import com.google.ads.googleads.v7.resources.CustomerName;
import com.google.ads.googleads.v7.services.*;
import com.google.api.client.util.Sets;
import com.google.common.collect.Lists;
import io.castled.ObjectRegistry;
import io.castled.oauth.OAuthDetails;
import io.castled.services.OAuthService;

import java.util.List;
import java.util.Properties;
import java.util.Set;

public class GoogleAdUtils {

    public static Long convertCustomerIdToLong(String customerId) {
        return Long.parseLong(customerId.replaceAll("-", ""));
    }

    public static Properties getClientProperties(GoogleAdsAppConfig googleAdsAppConfig, String refreshToken, String loginCustomerId) {
        Properties properties = new Properties();

        properties.put("api.googleads.clientId", googleAdsAppConfig.getClientId());
        properties.put("api.googleads.clientSecret", googleAdsAppConfig.getClientSecret());
        properties.put("api.googleads.refreshToken", refreshToken);
        properties.put("api.googleads.developerToken", googleAdsAppConfig.getDeveloperToken());
        if (loginCustomerId != null) {
            properties.put("api.googleads.loginCustomerId", loginCustomerId);
        }
        return properties;
    }

    public static GoogleAdsClient getGoogleAdsClient(GoogleAdsAppConfig googleAdsAppConfig, String loginCustomerId) {
        OAuthDetails oAuthDetails = ObjectRegistry.getInstance(OAuthService.class).getOAuthDetails(googleAdsAppConfig.getOAuthToken());
        return GoogleAdsClient.newBuilder().fromProperties(GoogleAdUtils.getClientProperties(googleAdsAppConfig, oAuthDetails.getAccessConfig().getRefreshToken(),
                loginCustomerId)).build();
    }

    public static List<ConversionCustomVariable> getCustomVariables(GoogleAdsAppConfig googleAdsAppConfig,
                                                                    GoogleAdsAppSyncConfig mappingConfig) {

        List<ConversionCustomVariable> conversionCustomVariables = Lists.newArrayList();
        OAuthDetails oAuthDetails = ObjectRegistry.getInstance(OAuthService.class).getOAuthDetails(googleAdsAppConfig.getOAuthToken());
        GoogleAdsClient googleAdsClient =
                GoogleAdsClient.newBuilder().fromProperties(GoogleAdUtils.getClientProperties(googleAdsAppConfig, oAuthDetails.getAccessConfig().getRefreshToken(),
                        mappingConfig.getLoginCustomerId())).build();

        try (GoogleAdsServiceClient googleAdsServiceClient = googleAdsClient.getLatestVersion().createGoogleAdsServiceClient()) {
            GoogleAdsServiceClient.SearchPagedResponse searchPagedResponse = googleAdsServiceClient.search(
                    String.valueOf(mappingConfig.getAccountId()),
                    "select conversion_custom_variable.name, conversion_custom_variable.resource_name from conversion_custom_variable");
            for (GoogleAdsRow googleAdsRow : searchPagedResponse.iterateAll()) {
                conversionCustomVariables.add(googleAdsRow.getConversionCustomVariable());
            }
        }
        return conversionCustomVariables;
    }

    public static List<String> getDirectlyAccessibleCustomers(GoogleAdsClient googleAdsClient) {
        List<String> accessibleCustomerIds = Lists.newArrayList();
        // Issues a request for listing all accessible customers by this authenticated Google account.

        try (CustomerServiceClient customerServiceClient =
                     googleAdsClient.getLatestVersion().createCustomerServiceClient()) {
            ListAccessibleCustomersResponse accessibleCustomers = customerServiceClient.listAccessibleCustomers(
                    ListAccessibleCustomersRequest.newBuilder().build());

            for (String customerResourceName : accessibleCustomers.getResourceNamesList()) {
                accessibleCustomerIds.add(CustomerName.parse(customerResourceName).getCustomerId());
            }
            return accessibleCustomerIds;
        }
    }

    public static Set<String> getAllAccessibleCustomers(GoogleAdsAppConfig googleAdsAppConfig) {
        GoogleAdsClient googleAdsClient = getGoogleAdsClient(googleAdsAppConfig, null);
        Set<String> allCustomers = Sets.newHashSet();
        List<String> accessibleCustomerIds = getDirectlyAccessibleCustomers(googleAdsClient);
        allCustomers.addAll(accessibleCustomerIds);
        for (String customerId : accessibleCustomerIds) {
            GoogleAdsClient gadsClient = getGoogleAdsClient(googleAdsAppConfig, customerId);
            allCustomers.addAll(getAllCustomers(gadsClient, customerId));
        }
        return allCustomers;

    }

    public static List<String> getAllCustomers(GoogleAdsClient googleAdsClient, String customerId) {
        List<String> allCustomers = Lists.newArrayList();
        List<String> directCustomers = getDirectlyAccessibleCustomers(googleAdsClient, customerId);
        allCustomers.addAll(directCustomers);
        for (String accessibleCustomer : directCustomers) {
            allCustomers.addAll(getAllCustomers(googleAdsClient, accessibleCustomer));

        }
        return allCustomers;
    }

    public static List<String> getDirectlyAccessibleCustomers(GoogleAdsClient googleAdsClient, String customerId) {
        List<String> accessibleCustomers = Lists.newArrayList();
        try (GoogleAdsServiceClient googleAdsServiceClient = googleAdsClient.getLatestVersion().createGoogleAdsServiceClient()) {
            GoogleAdsServiceClient.SearchPagedResponse searchPagedResponse = googleAdsServiceClient.search(
                    customerId, "select customer_client.id from customer_client");
            for (GoogleAdsRow googleAdsRow : searchPagedResponse.iterateAll()) {
                String accessibleCustomer = String.valueOf(googleAdsRow.getCustomerClient().getId());
                if (!accessibleCustomer.equals(customerId)) {
                    accessibleCustomers.add(String.valueOf(googleAdsRow.getCustomerClient().getId()));
                }
            }
        }
        return accessibleCustomers;
    }

    public static String getLoginCustomerId(GoogleAdsClient googleAdsClient, String customerId) {

        List<String> accessibleCustomerIds = getDirectlyAccessibleCustomers(googleAdsClient);
        if (accessibleCustomerIds.contains(customerId)) {
            return customerId;
        }
        for (String seedCustomerId : accessibleCustomerIds) {
            try (GoogleAdsServiceClient googleAdsServiceClient = googleAdsClient.getLatestVersion().createGoogleAdsServiceClient()) {
                GoogleAdsServiceClient.SearchPagedResponse searchPagedResponse = googleAdsServiceClient.search(
                        seedCustomerId, "select customer_client.id from customer_client");
                for (GoogleAdsRow googleAdsRow : searchPagedResponse.iterateAll()) {
                    if (googleAdsRow.getCustomerClient().getId() == convertCustomerIdToLong(customerId)) {
                        return seedCustomerId;
                    }
                }
            }
        }
        return null;

    }


}
