/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.common;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Collections.*;

import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.logging.LogUtil;
import com.top_logic.kafka.services.consumer.ConsumerDispatcher;
import com.top_logic.kafka.services.consumer.KafkaClientProperty;
import com.top_logic.kafka.services.producer.TLKafkaProducer;

/**
 * Common code for the {@link TLKafkaProducer} and the {@link ConsumerDispatcher}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface KafkaCommonClient<V, C extends CommonClientConfig<V, ?>> extends ConfiguredInstance<C> {

	/** The {@link LogUtil#withLogMark(String, String, Runnable) log mark} for Kafka. */
	String LOG_MARK_KAFKA = "in-kafka-context";

	/**
	 * The Kafka {@link Properties} to be used for {@link KafkaConsumer} instantiation. A
	 *         new, mutable and resizable {@link Map}.
	 */
	default Map<String, Object> getAllProperties() {
		Map<String, Object> properties = getTypedProperties();
		properties.putAll(getUntypedProperties());
		return properties;
	}

	/**
	 * The properties with explicit an {@link PropertyDescriptor} in the {@link ConfigurationItem}.
	 * <p>
	 * If a property is not set, the {@link Map} will contain its default value. If the (default)
	 * value is null, the {@link Map} will {@link Map#containsKey(Object) contain} the key and
	 * value.
	 * </p>
	 * 
	 * @return The Kafka {@link Properties} to be used for {@link KafkaConsumer} instantiation. A
	 *         new, mutable and resizable {@link Map}.
	 */
	default Map<String, Object> getTypedProperties() {
		Map<String, Object> properties = map();
		C config = getConfig();
		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			if (property.getAnnotation(KafkaClientProperty.class) == null) {
				continue;
			}
			if (!config.valueSet(property) && !property.hasExplicitDefault()) {
				// Do not add null values to Kafka properties that just represent unset
				// configuration values.
				continue;
			}
			Object value = config.value(property);
			properties.put(property.getPropertyName(), value);
		}
		return properties;
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
