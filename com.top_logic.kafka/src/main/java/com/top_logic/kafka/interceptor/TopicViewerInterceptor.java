/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.interceptor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.kafka.layout.kafka.AbstractKafkaTopic;
import com.top_logic.kafka.layout.kafka.KafkaMessage;
import com.top_logic.kafka.layout.kafka.KafkaTopic;
import com.top_logic.kafka.services.consumer.ConsumerProcessor;
import com.top_logic.kafka.services.sensors.Sensor;

/**
 * Convert consumed records from kafka into {@link Sensor} entries.
 * 
 * @author <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
 */
public class TopicViewerInterceptor extends AbstractConfiguredInstance<TopicViewerInterceptor.Config>
		implements ConsumerProcessor<String, String> {

	/**
	 * Configuration of a {@link TopicViewerInterceptor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<TopicViewerInterceptor> {

		/** Size of the internal queue of {@link KafkaMessage}s. */
		@IntDefault(20)
		int getQueueSize();
		
	}

	/** Size of the internal queue of {@link KafkaMessage}s. */
	protected final int _size;

	private static TopicViewerInterceptor _singleton;

	private final Map<String, KafkaTopic<?>> _topicMap;

	/**
	 * Creates a new {@link TopicViewerInterceptor}.
	 */
	public TopicViewerInterceptor(InstantiationContext context, Config config) {
		super(context, config);

		_topicMap = new HashMap<>();
		_size = config.getQueueSize();

		// TODO #22251: FindBugs(ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD)
		_singleton = this;
	}

	@Override
	public void process(ConsumerRecords<String, String> records) {
		int count = records.count();
		logInfo("Begin: Processing " + count + " changesets.");
		int index = 0;
		for (ConsumerRecord<String, String> record : records) {
			try {
				logInfo("Processing record " + index + " of " + count + ".");
				getTopic(record).addMessage(record);
			} catch (Exception ex) {
				logError("Exception during record " + index + ": " + ex.getMessage(), ex);
			}
			index += 1;
		}
		logInfo("End: Processing.");
	}

	private Collection<? extends KafkaTopic<?>> _getTopics() {
		return _topicMap.values();
	}

	/**
	 * Create a new topic for the given name.
	 * 
	 * @param topic
	 *        Name of the topic to be created.
	 * @return New created topic.
	 */
	protected KafkaTopic<?> newKafkaTopic(String topic) {
		return new StringKafkaTopic(topic, _size);
	}

	private KafkaTopic<?> getTopic(ConsumerRecord<?, ?> record) {
		String topicName = record.topic();
		KafkaTopic<?> existingTopic = _topicMap.get(topicName);

		if (existingTopic == null) {
			existingTopic = newKafkaTopic(topicName);

			_topicMap.put(topicName, existingTopic);
		}

		return existingTopic;
	}

	private void logError(String message, Throwable throwable) {
		Logger.info(message, throwable, TopicViewerInterceptor.class);
	}

	private void logInfo(String message) {
		Logger.info(message, TopicViewerInterceptor.class);
	}

	/**
	 * Return the only instance of this class in the system.
	 * 
	 * @return The requested instance, may be <code>null</code>.
	 */
	public static Collection<? extends KafkaTopic<?>> getTopics() {
		return (_singleton != null) ? _singleton._getTopics() : Collections.<KafkaTopic<?>> emptyList();
	}

	/**
	 * String based kafka topic returning messages as string.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class StringKafkaTopic extends AbstractKafkaTopic<String> {

		/**
		 * Creates a new {@link StringKafkaTopic}.
		 */
		public StringKafkaTopic(String topic, int size) {
			super(topic, topic, size);
		}

		// Overridden methods from AbstractKafkaTopic

		@Override
		protected KafkaMessage createMessage(ConsumerRecord<?, ?> aRecord) {
			return new KafkaMessage(this, aRecord);
		}
	}
}
