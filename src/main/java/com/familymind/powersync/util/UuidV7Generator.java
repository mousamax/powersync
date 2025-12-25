package com.familymind.powersync.util;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;

import java.util.EnumSet;

/**
 * Custom Hibernate identifier generator for UUID v7.
 * This ensures that all entity IDs are generated as time-ordered UUIDs.
 * 
 * Updated to use the new BeforeExecutionGenerator interface instead of
 * the deprecated IdentifierGenerator.
 */
public class UuidV7Generator implements BeforeExecutionGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        return UuidGenerator.generateUuidV7();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }
}

