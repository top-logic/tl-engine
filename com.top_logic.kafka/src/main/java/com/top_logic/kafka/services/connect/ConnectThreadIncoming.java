/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.connect;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.serialization.Serializer;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.kafka.serialization.StructSerializer;

/**
 * A {@link ConnectThread} extension which handles incoming data and
 * writes it to kafka.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class ConnectThreadIncoming extends ConnectThread {

	/**
	 * Typed configuration interface definition for {@link ConnectThreadIncoming}.
	 * 
	 * @author <a href="mailto:wta@top-logic.com">wta</a>
	 */
	@TagName("incoming")
	public interface Config extends ConnectThread.Config {
		
		@ClassDefault(ConnectThreadIncoming.class)
		@Override
		Class<? extends ConnectThread> getImplementationClass();
		
		/**
		 * @see ProducerConfig#BOOTSTRAP_SERVERS_CONFIG
		 */
		@ConnectProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)
		@Mandatory
		String getBootstrapServers();
		
		/**
		 * @see ProducerConfig#BUFFER_MEMORY_CONFIG
		 */
		@ConnectProperty(ProducerConfig.BUFFER_MEMORY_CONFIG)
		@LongDefault(32 * 1024 * 1024L)
		long getBufferMemory();
		
		/**
		 * @see ProducerConfig#RETRIES_CONFIG
		 */
		@ConnectProperty(ProducerConfig.RETRIES_CONFIG)
		@IntDefault(0)
		int getRetries();
		
		/**
		 * @see ProducerConfig#ACKS_CONFIG
		 */
		@ConnectProperty(ProducerConfig.ACKS_CONFIG)
		@StringDefault("1")
		String getAcks();
		
		/**
		 * @see ProducerConfig#COMPRESSION_TYPE_CONFIG
		 */
		@ConnectProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG)
		@StringDefault("none")
		String getCompressionType();
		
		/**
		 * @see ProducerConfig#BATCH_SIZE_CONFIG
		 */
		@ConnectProperty(ProducerConfig.BATCH_SIZE_CONFIG)
		@IntDefault(16384)
		int getBatchSize();

		/**
		 * @see ProducerConfig#LINGER_MS_CONFIG
		 */
		@ConnectProperty(ProducerConfig.LINGER_MS_CONFIG)
		@LongDefault(0)
		long getLinger();

		/**
		 * @see ProducerConfig#CLIENT_ID_CONFIG
		 */
		@ConnectProperty(ProducerConfig.CLIENT_ID_CONFIG)
		@StringDefault(StringServices.EMPTY_STRING)
		String getClientId();
		
		/**
		 * @see ProducerConfig#SEND_BUFFER_CONFIG
		 */
		@ConnectProperty(ProducerConfig.SEND_BUFFER_CONFIG)
		@IntDefault(128 * 1024)
		int getSendBuffer();
		
		/**
		 * @see ProducerConfig#RECEIVE_BUFFER_CONFIG
		 */
		@ConnectProperty(ProducerConfig.RECEIVE_BUFFER_CONFIG)
		@IntDefault(32 * 1024)
		int getReceiveBuffer();
		
		/**
		 * @see ProducerConfig#MAX_REQUEST_SIZE_CONFIG
		 */
		@ConnectProperty(ProducerConfig.MAX_REQUEST_SIZE_CONFIG)
		@IntDefault(1 * 1024 * 1024)
		int getMaxRequestSize();
		
		/**
		 * @see ProducerConfig#RECONNECT_BACKOFF_MS_CONFIG
		 */
		@ConnectProperty(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG)
		@LongDefault(50L)
		long getReconnectBackoff();
		
		/**
		 * @see ProducerConfig#METRIC_REPORTER_CLASSES_CONFIG
		 */
		@ConnectProperty(ProducerConfig.METRIC_REPORTER_CLASSES_CONFIG)
		@StringDefault(StringServices.EMPTY_STRING)
		String getMetricReporterClasses();
		
		/**
		 * @see ProducerConfig#RETRY_BACKOFF_MS_CONFIG
		 */
		@ConnectProperty(ProducerConfig.RETRY_BACKOFF_MS_CONFIG)
		@LongDefault(100L)
		long getRetryBackoff();
		
		/**
		 * @see ProducerConfig#MAX_BLOCK_MS_CONFIG
		 */
		@ConnectProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG)
		@LongDefault(60 * 1000)
		long getMaxBlock();
		
		/**
		 * @see ProducerConfig#REQUEST_TIMEOUT_MS_CONFIG
		 */
		@ConnectProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG)
		@IntDefault(30 * 1000)
		int getRequestTimeout();
		
		/**
		 * @see ProducerConfig#METADATA_MAX_AGE_CONFIG
		 */
		@ConnectProperty(ProducerConfig.METADATA_MAX_AGE_CONFIG)
		@LongDefault(5 * 60 * 1000)
		long getMetadataMaxAge();
		
		/**
		 * @see ProducerConfig#METRICS_SAMPLE_WINDOW_MS_CONFIG
		 */
		@ConnectProperty(ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG)
		@LongDefault(30 * 1000)
		long getMetricsSampleWindow();
		
		/**
		 * @see ProducerConfig#METRICS_NUM_SAMPLES_CONFIG
		 */
		@ConnectProperty(ProducerConfig.METRICS_NUM_SAMPLES_CONFIG)
		@IntDefault(2)
		int getMetricsNumSamples();
		
		/**
		 * @see ProducerConfig#MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION
		 */
		@ConnectProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION)
		@IntDefault(5)
		int getMaxInFlightRequestsPerConnection();
		
		/**
		 * @see ProducerConfig#KEY_SERIALIZER_CLASS_CONFIG
		 */
		@ConnectProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)
		@ClassDefault(StructSerializer.class)
		Class<Serializer<?>> getKeySerializerClass();
		
		/**
		 * @see ProducerConfig#VALUE_SERIALIZER_CLASS_CONFIG
		 */
		@ConnectProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)
		@ClassDefault(StructSerializer.class)
		Class<Serializer<?>> getValueSerializerClass();
		
		/**
		 * @see ProducerConfig#CONNECTIONS_MAX_IDLE_MS_CONFIG
		 */
		@ConnectProperty(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG)
		@LongDefault(9 * 60 * 1000)
		long getConnectionsMaxIdle();
		
		/**
		 * @see ProducerConfig#PARTITIONER_CLASS_CONFIG
		 */
		@ConnectProperty(ProducerConfig.PARTITIONER_CLASS_CONFIG)
		@ClassDefault(DefaultPartitioner.class)
		Class<Partitioner> getPartitionerClass();
		
		/**
		 * @see ProducerConfig#INTERCEPTOR_CLASSES_CONFIG
		 */
		@ConnectProperty(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG)
		@NullDefault
		String getInterceptorClasses();
		
		/**
		 * @see CommonClientConfigs#SECURITY_PROTOCOL_CONFIG
		 */
		@ConnectProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG)
		@StringDefault(CommonClientConfigs.DEFAULT_SECURITY_PROTOCOL)
		String getSecurityProtocol();
		
		/**
		 * the configured {@link KafkaReceiver}
		 */
		@Mandatory
		PolymorphicConfiguration<KafkaReceiver> getReceiver();
		
		/**
		 * the amount of milliseconds between calls to
		 *         {@link KafkaReceiver}
		 */
		@LongDefault(1000)
		long getPollInterval();
	}

	/**
	 * @see #terminate()
	 */
	private final AtomicBoolean _terminate = new AtomicBoolean(false);
	
	/**
	 * The {@link KafkaReceiver} to be used for receiving data from external
	 * source.
	 */
	private KafkaReceiver _receiver;
	
	/**
	 * The {@link KafkaProducer} to be used for writing received data to.
	 */
	private KafkaProducer<Object, Object> _producer;
	
	/**
	 * Create a {@link ConnectThreadIncoming}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public ConnectThreadIncoming(final InstantiationContext context, final Config config) {
		super(context, config);
	}

	@Override
	public void terminate() {
		if(isAlive() && !_terminate.get()) {
			_terminate.set(true);
		}
	}
	
	@Override
	protected void startup() {
		super.startup();
		
		_receiver = new DefaultInstantiationContext(ConnectThreadIncoming.class).getInstance(getConfig().getReceiver());
		_producer = new KafkaProducer<>(KafkaConnectService.properties(getConfig()));
	}
	
	@Override
	protected void shutdown() {
		close(_receiver);
		close(_producer);
		
		// call super-implementation
		super.shutdown();
	}
	
	@Override
	protected void execute() {
		final long interval = getConfig().getPollInterval();
		
		while(!_terminate.get()) {
			try {
				_receiver.receive(_producer);
			} catch(Throwable e) {
				Logger.error(String.format("%s: failed to retrieve data", getName()), e, ConnectThreadIncoming.class);
			} finally {
				// delay execution unconditionally
				delay(interval);
			}
		}
	}
	
	@Override
	protected Config getConfig() {
		return (Config) super.getConfig();
	}
	
	/**
	 * Implementing classes are responsible for receiving data from an external
	 * source and writing it to kafka.
	 * 
	 * @author <a href=mailto:wta@top-logic.com>wta</a>
	 */
	public static interface KafkaReceiver extends AutoCloseable {
		
		/**
		 * Receive data from the external source and write it using the given
		 * producer.
		 * 
		 * @param sink
		 *            the {@link KafkaProducer} to be used for writing received
		 *            data
		 */
		void receive(KafkaProducer<Object, Object> sink);
	}
}
