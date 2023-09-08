/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.kafka;

import java.util.Date;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Interface for a sensor displayed in the sensor cockpit.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface KafkaTopic<M extends Object> {

	/**
	 * The _name of the topic.
	 */
	String getName();

	/**
	 * Unique ID of the topic.
	 */
	String getID();

	/**
	 * The last messages send by the topic, may be <code>null</code> when topic hasn't send
	 *         data at all.
	 */
	List<KafkaMessage> getMessages();

	/**
	 * Date the data has been send by the topic, may be <code>null</code> when topic hasn't
	 *         send data at all.
	 */
	Date getDate();

	/**
	 * Add a record from the real kafka to this representation.
	 * 
	 * @param record
	 *        The record to be added to this representation.
	 * @return The message created.
	 */
	KafkaMessage addMessage(ConsumerRecord<?, ?> record);

}
