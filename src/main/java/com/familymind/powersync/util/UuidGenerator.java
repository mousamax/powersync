package com.familymind.powersync.util;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

/**
 * Utility class for generating time-ordered UUID v7 identifiers.
 * UUID v7 provides better database indexing performance compared to random UUIDs.
 */
public class UuidGenerator {

    private UuidGenerator() {
        // Private constructor to prevent instantiation
    }

    /**
     * Generates a time-ordered UUID v7.
     *
     * @return A new UUID v7 identifier
     */
    public static UUID generateUuidV7() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}

