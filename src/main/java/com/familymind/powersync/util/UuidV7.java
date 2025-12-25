package com.familymind.powersync.util;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom annotation for UUID v7 identifier generation.
 * This annotation replaces the deprecated @GenericGenerator approach.
 * 
 * Usage:
 * <pre>
 * @Id
 * @UuidV7
 * private UUID id;
 * </pre>
 */
@IdGeneratorType(UuidV7Generator.class)
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface UuidV7 {
}
