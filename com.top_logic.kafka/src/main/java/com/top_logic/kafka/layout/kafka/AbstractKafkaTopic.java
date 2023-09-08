/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.kafka;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.top_logic.basic.col.ArrayQueue;
import com.top_logic.basic.tools.NameBuilder;

/**
 * Abstract base implementation of a kafka topic.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractKafkaTopic<M extends Object> implements KafkaTopic<M> {

	private final String _name;

	private final String _id;

	private final ArrayQueue<KafkaMessage> _messages;

	private int _size;

	/**
	 * Creates a new {@link AbstractKafkaTopic}.
	 */
	public AbstractKafkaTopic(String name, String id, int size) {
		_name = name;
		_id = id;
		_size = size;
		_messages = new ArrayQueue<>(_size);
	}

	/**
	 * Create a new {@link KafkaMessage} out of the given record.
	 * 
	 * @param record
	 *        The record to be converted into a message.
	 * 
	 * @return The requested message.
	 */
	protected abstract KafkaMessage createMessage(ConsumerRecord<?, ?> record);

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getID() {
		return _id;
	}

	@Override
	public List<KafkaMessage> getMessages() {
		synchronized (_messages) {
			return new ArrayList<>(_messages);
		}
	}

	@Override
	public Date getDate() {
		KafkaMessage message;
		synchronized (_messages) {
			message = _messages.peek();
		}

		if (message != null) {
			return message.getDate();
		} else {
			return null;
		}
	}

	@Override
	public KafkaMessage addMessage(ConsumerRecord<?, ?> record) {
		KafkaMessage message = createMessage(record);

		if (message != null) {
			synchronized (_messages) {
				if (_messages.size() >= _size) {
					_messages.remove();
				}
				_messages.add(message);
			}
		}

		return message;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("_name", _name)
			.add("_id", _id)
			.build();
	}

}
