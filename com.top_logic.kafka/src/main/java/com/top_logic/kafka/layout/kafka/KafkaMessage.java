/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.kafka;

import java.util.Date;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.top_logic.basic.tools.NameBuilder;

/**
 * Message from kafka in a format to be used by the {@link KafkaTopicAccessor}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class KafkaMessage {

	private final Date _timestamp;

	private final KafkaTopic<?> _topic;

	private final Object _key;

	private final Object _value;

	private final long _offset;

	/**
	 * Creates a new {@link KafkaMessage}.
	 */
	public KafkaMessage(KafkaTopic<?> topic, ConsumerRecord<?, ?> record) {
		_topic = topic;
		_timestamp = new Date(record.timestamp());
		_key = record.key();
		_value = record.value();
		_offset = record.offset();
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("topic", _topic)
			.add("timestamp", _timestamp)
			.add("key", _key)
			.build();
	}

	/**
	 * Topic the message belongs to.
	 */
	public KafkaTopic<?> getTopic() {
		return _topic;
	}

	/**
	 * Time stamp the message has been send.
	 */
	public Date getDate() {
		return _timestamp;
	}

	/**
	 * Key identifying the message.
	 */
	public Object getKey() {
		return _key;
	}

	/**
	 * Value containing the real message part.
	 */
	public Object getValue() {
		return _value;
	}

	/**
	 * Offset of the message.
	 */
	public long getOffset() {
		return _offset;
	}
}
