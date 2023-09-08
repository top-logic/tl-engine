/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.consumer;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationItem;

/**
 * An {@link Annotation} to flag {@link ConfigurationItem} properties as Kafka
 * specific.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface KafkaClientProperty {
	// pure marker annotation
}
