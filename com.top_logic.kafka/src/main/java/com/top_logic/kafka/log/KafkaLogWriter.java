/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.log;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.kafka.services.consumer.ConsumerDispatcher;

/**
 * Writes the data in a message sent or received by Kafka to a {@link TagWriter}.
 * <p>
 * The result should be XML, as many text editors can automatically format XML for better
 * readability. Other formats, such as JSON, don't have the same level of support, yet.
 * </p>
 * <p>
 * In a {@link Producer}, the "message" is {@link ProducerRecord#value()}. In a
 * {@link ConsumerDispatcher}, it is a {@link ConsumerRecord#value()}.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public interface KafkaLogWriter<M> {

	/**
	 * Writes only the meta data of the message.
	 * <p>
	 * "Meta data" means: The amount of data written should be fixed and not grow for large
	 * messages.
	 * </p>
	 */
	void writeMetaData(TagWriter output, M message);

	/**
	 * Writes all the data of the message.
	 * <p>
	 * "All data" means: The data should be detailed enough to fully reconstruct the message from
	 * the written data.
	 * </p>
	 */
	void writeAllData(TagWriter output, M message);

}
