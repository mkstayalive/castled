package io.castled.apps.connectors.mailchimp.client.dtos;

public enum BatchOperationStatus {
    pending,
    preprocessing,
    started,
    finalizing,
    finished
}
