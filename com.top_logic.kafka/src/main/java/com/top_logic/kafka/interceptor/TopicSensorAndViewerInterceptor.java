/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.interceptor;

import java.util.Date;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.kafka.layout.kafka.KafkaMessage;
import com.top_logic.kafka.layout.kafka.KafkaTopic;
import com.top_logic.kafka.services.sensors.Sensor;
import com.top_logic.kafka.services.sensors.SensorService;

/**
 * Combine message and sensor view as an interceptor.
 * 
 * @author <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TopicSensorAndViewerInterceptor extends TopicViewerInterceptor {

	/**
	 * Creates a new TopicSensorAndViewerInterceptor.
	 */
	public TopicSensorAndViewerInterceptor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected KafkaTopic<?> newKafkaTopic(String aTopic) {
		KafkaSensorTopic sensorTopic = new KafkaSensorTopic(aTopic, _size);

		if (SensorService.Module.INSTANCE.isActive()) {
			SensorService.Module.INSTANCE.getImplementationInstance().addSensor(sensorTopic);
		}

		return sensorTopic;
	}

	/**
	 * Combination of a kafka topic which acts as sensor too (for the "sensor.xml").
	 * 
	 * @author <a href="mailto:mga@top-logic.com">mga</a>
	 */
	public static class KafkaSensorTopic extends StringKafkaTopic implements Sensor<String, String> {

		private final String _type;

		private String _activity;

		/** The last signal provided by this sensor. */
		private Object _signal;

		/** The time stamp the last signal has been produced. */
		private Date _timestamp;

		private Object _key;

		/**
		 * Creates a new {@link KafkaSensorTopic}.
		 */
		public KafkaSensorTopic(String topic, int size) {
			this(topic, null, size);
		}

		/**
		 * Creates a new {@link KafkaSensorTopic}.
		 */
		public KafkaSensorTopic(String topic, String activity, int size) {
			super(topic, size);

			_activity = activity;
			_type = topic;
		}

		@Override
		public String getDataSource() {
			return getName();
		}

		@Override
		public String getOperation() {
			return _activity;
		}

		@Override
		public String getSensorState() {
			return (_key != null) ? _key.toString() : "---";
		}

		@Override
		public String getType() {
			return _type;
		}

		@Override
		public SensorActivityState getActivityState() {
			long signalAge = getSignalAge();

			if (signalAge < 2) {
				return SensorActivityState.RUNNING;
			} else if (signalAge < 10) {
				return SensorActivityState.ACTIVE;
			} else {
				return SensorActivityState.INACTIVE;
			}
		}

		@Override
		public Object getSignal() {
			return _signal;
		}

		@Override
		public Date getSignalDate() {
			return _timestamp;
		}

		@Override
		public KafkaMessage addMessage(ConsumerRecord<?, ?> record) {
			KafkaMessage message = super.addMessage(record);

			_key = message.getKey();
			_signal = message.getValue();
			_timestamp = message.getDate();

			return message;
		}

		/**
		 * Return the age of the last signal received in seconds.
		 * 
		 * @return The age of the last signal in seconds.
		 */
		protected long getSignalAge() {
			if (_timestamp == null) {
				return Long.MAX_VALUE;
			} else {
				long millis = new Date().getTime() - _timestamp.getTime();

				return millis / 1000l;
			}
		}
	}
}
