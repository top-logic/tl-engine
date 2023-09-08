/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.connect;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.Deserializer;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.kafka.serialization.StructDeserializer;

/**
 * A {@link ConnectThread} extension which listens to kafka topics and
 * writes data to external systems.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class ConnectThreadOutgoing extends ConnectThread {

	/**
	 * Typed configuration interface definition for {@link ConnectThreadOutgoing}.
	 * 
	 * @author <a href="mailto:wta@top-logic.com">wta</a>
	 */
	@TagName("outgoing")
	public interface Config extends ConnectThread.Config {

		@ClassDefault(ConnectThreadOutgoing.class)
		@Override
		Class<? extends ConnectThread> getImplementationClass();
		
		/**
		 * @see ConsumerConfig#BOOTSTRAP_SERVERS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)
		@Mandatory
		String getBootstrapServers();
		
		/**
		 * @see ConsumerConfig#GROUP_ID_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.GROUP_ID_CONFIG)
		@DerivedRef(NAME_ATTRIBUTE)
		String getGroupId();
		
		/**
		 * @see ConsumerConfig#SESSION_TIMEOUT_MS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG)
		@IntDefault(10 * 1000)
		int getSessionTimeout();
		
		/**
		 * @see ConsumerConfig#HEARTBEAT_INTERVAL_MS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG)
		@IntDefault(3 * 1000)
		int getHeartbeatInterval();
		
		/**
		 * @see ConsumerConfig#METADATA_MAX_AGE_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.METADATA_MAX_AGE_CONFIG)
		@LongDefault(5 * 60 * 1000)
		long getMetadataMaxAge();
		
		/**
		 * @see ConsumerConfig#ENABLE_AUTO_COMMIT_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)
		@BooleanDefault(true)
		boolean getEnableAutoCommit();
		
		/**
		 * @see ConsumerConfig#AUTO_COMMIT_INTERVAL_MS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG)
		@IntDefault(5 * 1000)
		int getAutoCommitInterval();
		
		/**
		 * @see ConsumerConfig#CLIENT_ID_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.CLIENT_ID_CONFIG)
		@StringDefault(StringServices.EMPTY_STRING)
		String getClientId();
		
		/**
		 * @see ConsumerConfig#MAX_PARTITION_FETCH_BYTES_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG)
		@IntDefault(1 * 1024 * 1024)
		int getMaxPartitionFetchBytes();
		
		/**
		 * @see ConsumerConfig#SEND_BUFFER_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.SEND_BUFFER_CONFIG)
		@IntDefault(128 * 1024)
		int getSendBuffer();
		
		/**
		 * @see ConsumerConfig#RECEIVE_BUFFER_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.RECEIVE_BUFFER_CONFIG)
		@IntDefault(64 * 1024)
		int getReceiveBuffer();
		
		/**
		 * @see ConsumerConfig#FETCH_MIN_BYTES_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG)
		@IntDefault(1)
		int getFetchMinBytes();
		
		/**
		 * @see ConsumerConfig#FETCH_MAX_BYTES_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.FETCH_MAX_BYTES_CONFIG)
		@IntDefault(50 * 1024 * 1024)
		int getFetchMaxBytes();
		
		/**
		 * @see ConsumerConfig#FETCH_MAX_WAIT_MS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG)
		@IntDefault(500)
		int getFetchMaxWait();
		
		/**
		 * @see ConsumerConfig#RECONNECT_BACKOFF_MS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG)
		@LongDefault(50L)
		long getReconnectBackoff();
		
		/**
		 * @see ConsumerConfig#RETRY_BACKOFF_MS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG)
		@LongDefault(100L)
		long getRetryBackoff();
		
		/**
		 * @see ConsumerConfig#AUTO_OFFSET_RESET_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)
		@StringDefault("latest")
		String getAutoOffsetReset();
		
		/**
		 * @see ConsumerConfig#CHECK_CRCS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.CHECK_CRCS_CONFIG)
		@BooleanDefault(true)
		boolean getCheckCrcs();
		
		/**
		 * @see ConsumerConfig#METRICS_SAMPLE_WINDOW_MS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG)
		@LongDefault(30 * 1000)
		long getMetricsSampleWindow();
		
		/**
		 * @see ConsumerConfig#METRICS_NUM_SAMPLES_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.METRICS_NUM_SAMPLES_CONFIG)
		@IntDefault(2)
		int getMetricsNumSamples();
		
		/**
		 * @see ConsumerConfig#METRICS_RECORDING_LEVEL_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.METRICS_RECORDING_LEVEL_CONFIG)
		@StringDefault("INFO")
		String getMetricsRecordingLevel();
		
		/**
		 * @see ConsumerConfig#METRIC_REPORTER_CLASSES_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.METRIC_REPORTER_CLASSES_CONFIG)
		@StringDefault(StringServices.EMPTY_STRING)
		String getMetricReporterClasses();
		
		/**
		 * @see ConsumerConfig#KEY_DESERIALIZER_CLASS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)
		@ClassDefault(StructDeserializer.class)
		Class<Deserializer<?>> getKeyDeserializerClass();
		
		/**
		 * @see ConsumerConfig#VALUE_DESERIALIZER_CLASS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG)
		@ClassDefault(StructDeserializer.class)
		Class<Deserializer<?>> getValueDeserializerClass();
		
		/**
		 * @see ConsumerConfig#REQUEST_TIMEOUT_MS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG)
		@IntDefault(305 * 1000)
		int getRequestTimeout();
		
		/**
		 * @see ConsumerConfig#CONNECTIONS_MAX_IDLE_MS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG)
		@LongDefault(9 * 60 * 1000)
		long getConnectionsMaxIdle();
		
		/**
		 * @see ConsumerConfig#INTERCEPTOR_CLASSES_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG)
		@NullDefault()
		String getInterceptorClasses();
		
		/**
		 * @see ConsumerConfig#MAX_POLL_RECORDS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG)
		@IntDefault(500)
		int getMaxPollRecords();
		
		/**
		 * @see ConsumerConfig#MAX_POLL_INTERVAL_MS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG)
		@IntDefault(5 * 60 * 1000)
		int getMaxPollInterval();
		
		/**
		 * @see ConsumerConfig#EXCLUDE_INTERNAL_TOPICS_CONFIG
		 */
		@ConnectProperty(ConsumerConfig.EXCLUDE_INTERNAL_TOPICS_CONFIG)
		@BooleanDefault(true)
		boolean getExcludeInternalTopics();
		
		/**
		 * @see CommonClientConfigs#SECURITY_PROTOCOL_CONFIG
		 */
		@ConnectProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG)
		@StringDefault(CommonClientConfigs.DEFAULT_SECURITY_PROTOCOL)
		String getSecurityProtocol();
		
		/**
		 * the polling timeout in milliseconds
		 */
		@LongDefault(1000)
		long getPollTimeout();
		
		/**
		 * a {@link Set} of topics to retrieve the
		 *         data from
		 */
		@Mandatory
		@Format(CommaSeparatedStringSet.class)
		Set<String> getTopics();
		
		/**
		 * the configured {@link KafkaSender}
		 */
		@Mandatory
		PolymorphicConfiguration<KafkaSender> getSender();
	}

	/**
	 * @see #terminate()
	 */
	private final AtomicBoolean _terminate = new AtomicBoolean(false);
	
	/**
	 * The {@link KafkaSender} to be used for sending data to the external sink.
	 */
	private KafkaSender _sender;
	
	/**
	 * The {@link KafkaConsumer} to be used for reading data from kafka.
	 */
	private KafkaConsumer<Object, Object> _consumer;
	
	/**
	 * Create a {@link ConnectThreadOutgoing}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public ConnectThreadOutgoing(final InstantiationContext context, final Config config) {
		super(context, config);
	}
	
	@Override
	public void terminate() {
		if(isAlive() && !_terminate.get()) {
			_terminate.set(true);
			_consumer.wakeup();
		}
	}

	@Override
	protected void startup() {
		// call super-implementation first
		super.startup();
		
		_sender = new DefaultInstantiationContext(ConnectThreadOutgoing.class).getInstance(getConfig().getSender());
		_consumer = new KafkaConsumer<>(KafkaConnectService.properties(getConfig()));
		_consumer.subscribe(getConfig().getTopics());
	}
	
	@Override
	protected void shutdown() {
		close(_sender);
		close(_consumer);
		
		// call super-implementation last
		super.shutdown();
	}
	
	@Override
	protected void execute() {
		final long timeout = getConfig().getPollTimeout();
		
		while(!_terminate.get()) {
			try {
				_sender.send(_consumer.poll(timeout));
			} catch(WakeupException e) {
				Logger.info(String.format("%s: woke up", getConfig().getName()), e, ConnectThreadOutgoing.class);
			} catch(Throwable e) {
				Logger.error(String.format("%s: failed to send data", getConfig().getName()), e, ConnectThreadOutgoing.class);

				// delay execution upon error occurrence
				delay(timeout);
			}
		}
	}
	
	@Override
	protected Config getConfig() {
		return (Config) super.getConfig();
	}
	
	/**
	 * Implementing class are responsible for sending data from kafka to
	 * external systems.
	 * 
	 * @author <a href=mailto:wta@top-logic.com>wta</a>
	 */
	public static interface KafkaSender extends AutoCloseable {
		
		/**
		 * Send the given data to the external sink.
		 * 
		 * @param data
		 *            the {@link ConsumerRecords} to be sent
		 */
		void send(ConsumerRecords<Object, Object> data);
	}
}
