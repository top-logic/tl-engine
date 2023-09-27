/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.common;

import static java.util.Collections.*;

import java.util.Map;
import java.util.Properties;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.logging.LogUtil;
import com.top_logic.kafka.services.consumer.ConsumerDispatcher;
import com.top_logic.kafka.services.producer.TLKafkaProducer;

/**
 * Common code for the {@link TLKafkaProducer} and the {@link ConsumerDispatcher}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface KafkaCommonClient<V, C extends CommonClientConfig<V, ?>> extends ConfiguredInstance<C> {

	/** The {@link LogUtil#withLogMark(String, String, Runnable) log mark} for Kafka. */
	String LOG_MARK_KAFKA = "in-kafka-context";

	/** @see CommonClientConfig#getAllProperties() */
	default Map<String, Object> getAllProperties() {
		return getConfig().getAllProperties();
	}

	/** @see CommonClientConfig#getTypedProperties() */
	default Map<String, Object> getTypedProperties() {
		return getConfig().getTypedProperties();
	}

	/**
	 * An unmodifiable view of the configured untyped properties.
	 * 
	 * @return Is not allowed to be null. A {@link Map} of {@link String} to {@link String}, not a
	 *         {@link Properties} object, as the latter would accept any value type, not just
	 *         {@link String}s, which is wrong.
	 */
	default Map<String, String> getUntypedProperties() {
		return unmodifiableMap(getConfig().getUntypedProperties());
	}

}
