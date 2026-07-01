package com.battery.integration.dpp.registry;

/**
 * Exception thrown when communicating with the EU DPP Registry API.
 */
public class DppRegistryException extends RuntimeException {

    public DppRegistryException(String message) {
        super(message);
    }

    public DppRegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
