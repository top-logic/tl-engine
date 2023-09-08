/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.producer;

import org.apache.kafka.clients.producer.ProducerRecord;

import com.top_logic.basic.config.InstantiationContext;

/**
 * {@link TLKafkaProducer} for string values and keys.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StringTLKafkaProducer extends TLKafkaProducer<String, String> {

	/**
	 * Configuration of the {@link StringTLKafkaProducer}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends TLKafkaProducer.Config<String, String> {
		// currently empty
	}

	/**
	 * The name of the topic to write the data to.
	 */
	private final String _topic;

	/**
	 * Creates a new {@link StringTLKafkaProducer}.
	 */
	public StringTLKafkaProducer(InstantiationContext context, Config config) {
		super(context, config);
		_topic = config.getTopic();
	}

	/**
	 * @param key
	 *        the {@link String} to be used as key for the new record
	 * @param value
	 *        the {@link String} to be used as value of the new record
	 * @return the new {@link ProducerRecord} with the given key and value
	 */
	public ProducerRecord<String, String> newProducerRecord(final String key, final String value) {
		return new ProducerRecord<>(_topic, key, value);
	}

}

