/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.producer;

import static java.util.Objects.*;

import java.util.Map;
import java.util.Objects;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.format.MillisFormatInt;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.kafka.serialization.StringSerializer;
import com.top_logic.kafka.services.common.CommonClientConfig;
import com.top_logic.kafka.services.common.KafkaCommonClient;
import com.top_logic.kafka.services.consumer.KafkaClientProperty;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Handler for sending data to a Kafka topic.
 * 
 * @author <a href=mailto:tgi@top-logic.com>tgi</a>
 */
public class TLKafkaProducer<K, V> extends ProducerProxy<K, V>
		implements KafkaCommonClient<V, TLKafkaProducer.Config<K, V>> {

	/**
	 * Typed configuration interface for producers.
	 * 
	 * @author <a href=mailto:tgi@top-logic.com>tgi</a>
	 */
	@DisplayInherited(DisplayStrategy.APPEND)
	@DisplayOrder({
		Config.NAME_ATTRIBUTE,
		Config.TOPIC,
		Config.CLIENT_ID_CONFIG,
		Config.BOOTSTRAP_SERVERS_CONFIG,
		Config.KEY_SERIALIZER_TYPED_CONFIG,
		Config.VALUE_SERIALIZER_TYPED_CONFIG,
		Config.LOG_WRITER,
		Config.ENABLE_IDEMPOTENCE_CONFIG,
		Config.TRANSACTIONAL_ID_CONFIG,
		Config.TRANSACTION_TIMEOUT_CONFIG,
		Config.ACKS_CONFIG,
		Config.RETRIES_CONFIG,
		Config.BATCH_SIZE_CONFIG,
		Config.BUFFER_MEMORY_CONFIG,
		Config.COMPRESSION_TYPE_CONFIG,
		Config.DELIVERY_TIMEOUT_MS_CONFIG,
		Config.LINGER_MS_CONFIG,
		Config.MAX_BLOCK_MS_CONFIG,
		Config.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
		Config.MAX_REQUEST_SIZE_CONFIG,
		Config.CLIENT_DNS_LOOKUP_CONFIG,
		Config.CONNECTIONS_MAX_IDLE_MS_CONFIG,
		Config.METADATA_MAX_AGE_CONFIG,
		Config.METRICS_NUM_SAMPLES_CONFIG,
		Config.METRICS_RECORDING_LEVEL_CONFIG,
		Config.METRICS_SAMPLE_WINDOW_MS_CONFIG,
		Config.RECEIVE_BUFFER_CONFIG,
		Config.RECONNECT_BACKOFF_MAX_MS_CONFIG,
		Config.RECONNECT_BACKOFF_MS_CONFIG,
		Config.REQUEST_TIMEOUT_MS_CONFIG,
		Config.RETRY_BACKOFF_MS_CONFIG,
		Config.SEND_BUFFER_CONFIG,
		Config.SECURITY_PROTOCOL_CONFIG,
		Config.SECURITY_PROVIDERS,

		Config.PARTITIONER_AVAILABILITY_TIMEOUT_MS_CONFIG,
		Config.PARTITIONER_ADPATIVE_PARTITIONING_ENABLE_CONFIG,
		Config.PARTITIONER_IGNORE_KEYS_CONFIG,
		Config.METADATA_MAX_IDLE_CONFIG,

		Config.SASL_MECHANISM,
		Config.SASL_JAAS_CONFIG,
		Config.SASL_OAUTHBEARER_TOKEN_ENDPOINT_URL,
		Config.SASL_OAUTHBEARER_SUB_CLAIM_NAME,
		Config.SASL_OAUTHBEARER_SCOPE_CLAIM_NAME,
		Config.SASL_OAUTHBEARER_JWKS_ENDPOINT_URL,
		Config.SASL_OAUTHBEARER_JWKS_ENDPOINT_REFRESH_MS,
		Config.SASL_OAUTHBEARER_JWKS_ENDPOINT_RETRY_BACKOFF_MS,
		Config.SASL_OAUTHBEARER_JWKS_ENDPOINT_RETRY_BACKOFF_MAX_MS,
		Config.SASL_OAUTHBEARER_EXPECTED_ISSUER,
		Config.SASL_OAUTHBEARER_EXPECTED_AUDIENCE,
		Config.SASL_OAUTHBEARER_CLOCK_SKEW_SECONDS,

		Config.SASL_KERBEROS_KINIT_CMD,
		Config.SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN,
		Config.SASL_KERBEROS_SERVICE_NAME,
		Config.SASL_KERBEROS_TICKET_RENEW_JITTER,
		Config.SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR,

		Config.SASL_LOGIN_REFRESH_BUFFER_SECONDS,
		Config.SASL_LOGIN_REFRESH_MIN_PERIOD_SECONDS,
		Config.SASL_LOGIN_REFRESH_WINDOW_FACTOR,
		Config.SASL_LOGIN_REFRESH_WINDOW_JITTER,
		Config.SASL_LOGIN_RETRY_BACKOFF_MS,
		Config.SASL_LOGIN_RETRY_BACKOFF_MAX_MS,
		Config.SASL_LOGIN_READ_TIMEOUT_MS,
		Config.SASL_LOGIN_CONNECT_TIMEOUT_MS,

		Config.SSL_PROTOCOL_CONFIG,
		Config.SSL_PROVIDER_CONFIG,
		Config.SSL_ENABLED_PROTOCOLS_CONFIG,
		Config.SSL_CIPHER_SUITES_CONFIG,
		Config.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,
		Config.SSL_KEY_PASSWORD_CONFIG,
		Config.SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG,

		Config.SSL_KEYSTORE_LOCATION_CONFIG,
		Config.SSL_KEYSTORE_PASSWORD_CONFIG,
		Config.SSL_KEYSTORE_TYPE_CONFIG,
		Config.SSL_KEYSTORE_KEY_CONFIG,
		Config.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG,
		Config.SSL_KEYMANAGER_ALGORITHM_CONFIG,

		Config.SSL_TRUSTSTORE_LOCATION_CONFIG,
		Config.SSL_TRUSTSTORE_PASSWORD_CONFIG,
		Config.SSL_TRUSTSTORE_TYPE_CONFIG,
		Config.SSL_TRUSTMANAGER_ALGORITHM_CONFIG,
		Config.SSL_TRUSTSTORE_CERTIFICATES_CONFIG,

		Config.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG,
		Config.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG,
		Config.METRICS_SAMPLE_WINDOW_MS_CONFIG,

		Config.UNTYPED_PROPERTIES,
	})
	public interface Config<K, V> extends CommonClientConfig<V, TLKafkaProducer<K, V>> {

		/** @see #getTopic() */
		String TOPIC = "topic";

		/** @see #getKeySerializer() */
		String KEY_SERIALIZER_TYPED_CONFIG = "key.serializer.typed.config";

		/** @see #getKeySerializerClass() */
		String KEY_SERIALIZER_CLASS_CONFIG = ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;

		/** @see #getValueSerializer() */
		String VALUE_SERIALIZER_TYPED_CONFIG = "value.serializer.typed.config";

		/** @see #getValueSerializerClass() */
		String VALUE_SERIALIZER_CLASS_CONFIG = ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

		/** @see #getTransactionalId() */
		String TRANSACTIONAL_ID_CONFIG = ProducerConfig.TRANSACTIONAL_ID_CONFIG;

		/** @see #getTransactionTimeoutMS() */
		String TRANSACTION_TIMEOUT_CONFIG = ProducerConfig.TRANSACTION_TIMEOUT_CONFIG;

		/** @see #getMaxRequestSize() */
		String MAX_REQUEST_SIZE_CONFIG = ProducerConfig.MAX_REQUEST_SIZE_CONFIG;

		/** @see #getRetries() */
		String RETRIES_CONFIG = ProducerConfig.RETRIES_CONFIG;

		/** @see #getPartitionerAvailabilityTimeoutMS() */
		String PARTITIONER_AVAILABILITY_TIMEOUT_MS_CONFIG = ProducerConfig.PARTITIONER_AVAILABILITY_TIMEOUT_MS_CONFIG;

		/** @see #getPartitionerAdaptivePartitioningEnabled() */
		String PARTITIONER_ADPATIVE_PARTITIONING_ENABLE_CONFIG =
			ProducerConfig.PARTITIONER_ADPATIVE_PARTITIONING_ENABLE_CONFIG;

		/** @see #getPartitionerIgnoreKeys() */
		String PARTITIONER_IGNORE_KEYS_CONFIG = ProducerConfig.PARTITIONER_IGNORE_KEYS_CONFIG;

		/** @see #getPartitionerClass() */
		String PARTITIONER_CLASS_CONFIG = ProducerConfig.PARTITIONER_CLASS_CONFIG;

		/** @see #getMetadataMaxIdleMS() */
		String METADATA_MAX_IDLE_CONFIG = ProducerConfig.METADATA_MAX_IDLE_CONFIG;

		/** @see #getMaxInFlightRequestsPerConnection() */
		String MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION = ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION;

		/** @see #getMaxBlockMS() */
		String MAX_BLOCK_MS_CONFIG = ProducerConfig.MAX_BLOCK_MS_CONFIG;

		/** @see #getLingerMS() */
		String LINGER_MS_CONFIG = ProducerConfig.LINGER_MS_CONFIG;

		/** @see #getInterceptorClasses() */
		String INTERCEPTOR_CLASSES_CONFIG = ProducerConfig.INTERCEPTOR_CLASSES_CONFIG;

		/** @see #getEnableIdempotence() */
		String ENABLE_IDEMPOTENCE_CONFIG = ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG;

		/** @see #getDeliveryTimeoutMS() */
		String DELIVERY_TIMEOUT_MS_CONFIG = ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG;

		/** @see #getCompressionType() */
		String COMPRESSION_TYPE_CONFIG = ProducerConfig.COMPRESSION_TYPE_CONFIG;

		/** @see #getBufferMemory() */
		String BUFFER_MEMORY_CONFIG = ProducerConfig.BUFFER_MEMORY_CONFIG;

		/** @see #getBatchSize() */
		String BATCH_SIZE_CONFIG = ProducerConfig.BATCH_SIZE_CONFIG;

		/** @see #getAcks() */
		String ACKS_CONFIG = ProducerConfig.ACKS_CONFIG;

		/**
		 * The name of the KAFKA topic to write records to.
		 */
		@Name(TOPIC)
		String getTopic();

		/**
		 * The number of acknowledgments the producer requires the leader to have received before
		 * considering a request complete. This controls the durability of records that are sent.
		 * The following settings are allowed:
		 * 
		 * <ul>
		 * <li><code>acks=0</code> If set to zero then the producer will not wait for any
		 * acknowledgment from the server at all. The record will be immediately added to the socket
		 * buffer and considered sent. No guarantee can be made that the server has received the
		 * record in this case, and the <code>retries</code> configuration will not take effect (as
		 * the client won't generally know of any failures). The offset given back for each record
		 * will always be set to <code>-1</code>.</li>
		 * <li><code>acks=1</code> This will mean the leader will write the record to its local log
		 * but will respond without awaiting full acknowledgement from all followers. In this case
		 * should the leader fail immediately after acknowledging the record but before the
		 * followers have replicated it then the record will be lost.</li>
		 * <li><code>acks=all</code> This means the leader will wait for the full set of in-sync
		 * replicas to acknowledge the record. This guarantees that the record will not be lost as
		 * long as at least one in-sync replica remains alive. This is the strongest available
		 * guarantee. This is equivalent to the acks=-1 setting.</li>
		 * </ul>
		 * <p>
		 * Note that enabling idempotence requires this config value to be 'all'. If conflicting
		 * configurations are set and idempotence is not explicitly enabled, idempotence is
		 * disabled.
		 * </p>
		 * 
		 * @see ProducerConfig#ACKS_DOC
		 */
		@Name(ACKS_CONFIG)
		@StringDefault("all")
		@KafkaClientProperty
		String getAcks();

		/**
		 * The producer will attempt to batch records together into fewer requests whenever multiple
		 * records are being sent to the same partition. This helps performance on both the client
		 * and the server. This configuration controls the default batch size in bytes.
		 * <p>
		 * No attempt will be made to batch records larger than this size.
		 * </p>
		 * <p>
		 * Requests sent to brokers will contain multiple batches, one for each partition with data
		 * available to be sent.
		 * </p>
		 * <p>
		 * A small batch size will make batching less common and may reduce throughput (a batch size
		 * of zero will disable batching entirely). A very large batch size may use memory a bit
		 * more wastefully as we will always allocate a buffer of the specified batch size in
		 * anticipation of additional records.
		 * </p>
		 * <p>
		 * Note: This setting gives the upper bound of the batch size to be sent. If we have fewer
		 * than this many bytes accumulated for this partition, we will 'linger' for the
		 * <code>linger.ms</code> time waiting for more records to show up. This
		 * <code>linger.ms</code> setting defaults to 0, which means we'll immediately send out a
		 * record even the accumulated batch size is under this <code>batch.size</code> setting.
		 * </p>
		 * 
		 * @see ProducerConfig#BATCH_SIZE_DOC
		 */
		@Name(BATCH_SIZE_CONFIG)
		@IntDefault(16 * 1024)
		@KafkaClientProperty
		int getBatchSize();

		/**
		 * The total bytes of memory the producer can use to buffer records waiting to be sent to
		 * the server. If records are sent faster than they can be delivered to the server the
		 * producer will block for {@link #getMaxBlockMS()} after which it will throw an exception.
		 * <p>
		 * This setting should correspond roughly to the total memory the producer will use, but is
		 * not a hard bound since not all memory the producer uses is used for buffering. Some
		 * additional memory will be used for compression (if compression is enabled) as well as for
		 * maintaining in-flight requests.
		 * </p>
		 * 
		 * @see ProducerConfig#BUFFER_MEMORY_DOC
		 */
		@Name(BUFFER_MEMORY_CONFIG)
		@LongDefault(32 * 1024 * 1024)
		@KafkaClientProperty
		long getBufferMemory();

		/**
		 * @see ProducerConfig#COMPRESSION_TYPE_DOC
		 */
		@Name(COMPRESSION_TYPE_CONFIG)
		@StringDefault("none")
		@KafkaClientProperty
		String getCompressionType();

		/**
		 * @see ProducerConfig#DELIVERY_TIMEOUT_MS_DOC
		 */
		@Name(DELIVERY_TIMEOUT_MS_CONFIG)
		@IntDefault(2 * 60 * 1000)
		@Format(MillisFormatInt.class)
		@KafkaClientProperty
		int getDeliveryTimeoutMS();

		/**
		 * @see ProducerConfig#ENABLE_IDEMPOTENCE_DOC
		 */
		@Name(ENABLE_IDEMPOTENCE_CONFIG)
		@BooleanDefault(true)
		@KafkaClientProperty
		boolean getEnableIdempotence();

		/**
		 * @see ProducerConfig#INTERCEPTOR_CLASSES_CONFIG
		 * 
		 * @implNote Kafka specifies this in the {@link ConsumerConfig} <em>and</em> the
		 *           {@link ProducerConfig}, but <em>not</em> in the {@link CommonClientConfigs}. It
		 *           is therefore declared here that way, too.
		 */
		@Hidden
		@Name(INTERCEPTOR_CLASSES_CONFIG)
		@KafkaClientProperty
		String getInterceptorClasses();

		/**
		 * This option is used only, when {@link #getKeySerializerClass()} is empty.
		 * 
		 * @see ProducerConfig#KEY_SERIALIZER_CLASS_DOC
		 */
		@ItemDefault(StringSerializer.class)
		@Name(KEY_SERIALIZER_TYPED_CONFIG)
		@Options(fun = AllInAppImplementations.class)
		PolymorphicConfiguration<? extends Serializer<K>> getKeySerializer();

		/**
		 * When this option is not empty, {@link #getKeySerializer()} will be ignored.
		 * 
		 * @see ProducerConfig#KEY_SERIALIZER_CLASS_DOC
		 * @deprecated {@link #getKeySerializer()}
		 */
		@Deprecated
		@Hidden
		@Name(KEY_SERIALIZER_CLASS_CONFIG)
		@KafkaClientProperty
		Class<? extends Serializer<K>> getKeySerializerClass();

		/**
		 * @see ProducerConfig#LINGER_MS_DOC
		 */
		@Name(LINGER_MS_CONFIG)
		@IntDefault(0)
		@Format(MillisFormatInt.class)
		@KafkaClientProperty
		int getLingerMS();

		/**
		 * @see ProducerConfig#MAX_BLOCK_MS_DOC
		 */
		@Name(MAX_BLOCK_MS_CONFIG)
		@LongDefault(60 * 1000)
		@Format(MillisFormat.class)
		@KafkaClientProperty
		long getMaxBlockMS();

		/**
		 * @see ProducerConfig#MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION_DOC
		 */
		@Name(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION)
		@IntDefault(5)
		@KafkaClientProperty
		int getMaxInFlightRequestsPerConnection();

		/**
		 * @see ProducerConfig#METADATA_MAX_IDLE_CONFIG
		 */
		@Name(METADATA_MAX_IDLE_CONFIG)
		@LongDefault(5 * 60 * 1000)
		@Format(MillisFormat.class)
		@KafkaClientProperty
		long getMetadataMaxIdleMS();

		/**
		 * @see ProducerConfig#MAX_REQUEST_SIZE_DOC
		 */
		@Name(MAX_REQUEST_SIZE_CONFIG)
		@IntDefault(1000 * 1000 * 1000)
		@KafkaClientProperty
		int getMaxRequestSize();

		/**
		 * @see ProducerConfig#PARTITIONER_CLASS_CONFIG
		 */
		@Hidden
		@Name(PARTITIONER_CLASS_CONFIG)
		@KafkaClientProperty
		Class<? extends Partitioner> getPartitionerClass();

		/**
		 * @see ProducerConfig#PARTITIONER_IGNORE_KEYS_CONFIG
		 */
		@Name(PARTITIONER_IGNORE_KEYS_CONFIG)
		@KafkaClientProperty
		boolean getPartitionerIgnoreKeys();

		/**
		 * @see ProducerConfig#PARTITIONER_ADPATIVE_PARTITIONING_ENABLE_CONFIG
		 */
		@Name(PARTITIONER_ADPATIVE_PARTITIONING_ENABLE_CONFIG)
		@BooleanDefault(true)
		@KafkaClientProperty
		boolean getPartitionerAdaptivePartitioningEnabled();

		/**
		 * @see ProducerConfig#PARTITIONER_AVAILABILITY_TIMEOUT_MS_CONFIG
		 */
		@Name(PARTITIONER_AVAILABILITY_TIMEOUT_MS_CONFIG)
		@Format(MillisFormat.class)
		@LongDefault(0)
		@KafkaClientProperty
		long getPartitionerAvailabilityTimeoutMS();

		@Override
		@IntDefault(32 * 1024)
		int getReceiveBufferBytes();

		@Override
		@IntDefault(30 * 1000)
		@Format(MillisFormatInt.class)
		int getRequestTimeoutMS();

		/**
		 * @see ProducerConfig#RETRIES_CONFIG
		 */
		@Name(RETRIES_CONFIG)
		@IntDefault(Integer.MAX_VALUE)
		@KafkaClientProperty
		int getRetries();

		/**
		 * @see ProducerConfig#TRANSACTION_TIMEOUT_DOC
		 */
		@Name(TRANSACTION_TIMEOUT_CONFIG)
		@IntDefault(60 * 1000)
		@Format(MillisFormatInt.class)
		@KafkaClientProperty
		int getTransactionTimeoutMS();

		/**
		 * @see ProducerConfig#TRANSACTIONAL_ID_DOC
		 */
		@Name(TRANSACTIONAL_ID_CONFIG)
		@KafkaClientProperty
		String getTransactionalId();

		/**
		 * This option is used only, when {@link #getValueSerializerClass()} is empty.
		 * 
		 * @see ProducerConfig#VALUE_SERIALIZER_CLASS_DOC
		 */
		@ItemDefault(StringSerializer.class)
		@Name(VALUE_SERIALIZER_TYPED_CONFIG)
		@Options(fun = AllInAppImplementations.class)
		PolymorphicConfiguration<? extends Serializer<V>> getValueSerializer();

		/**
		 * When this option is not empty, {@link #getValueSerializer()} will be ignored.
		 * 
		 * @see ProducerConfig#VALUE_SERIALIZER_CLASS_DOC
		 * @deprecated Use {@link #getValueSerializer()}
		 */
		@Hidden
		@Name(VALUE_SERIALIZER_CLASS_CONFIG)
		@KafkaClientProperty
		@Deprecated
		Class<? extends Serializer<K>> getValueSerializerClass();

    }

	private final KafkaProducer<K, V> _producer;

	/**
	 * @see #getConfig()
	 */
	private final Config<K, V> _config;

	private final String _topic;

	/**
	 * Create a {@link TLKafkaProducer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the instance in
	 * @param config
	 *        the {@link Config} to create the instance with
	 */
	public TLKafkaProducer(InstantiationContext context, Config<K, V> config) {
		super(context.getInstance(config.getLogWriter()));
        _config = config;
		_topic = config.getTopic();
		_producer = requireNonNull(newKafkaProducer(context, config));
	}

	/**
	 * The default topic to send to.
	 */
	public String getTopic() {
		return _topic;
	}

	/**
	 * This method is not intended to be overridden. It is private to enable overrides in cases
	 * where nothing else helps.
	 */
	protected KafkaProducer<K, V> newKafkaProducer(InstantiationContext context, Config<K, V> config) {
		try {
			return newKafkaProducerInternal(context, config);
		} catch (Exception ex) {
			StringBuilder msg = new StringBuilder();
			msg.append("Unable to create ");
			msg.append(KafkaProducer.class.getName());
			msg.append(" with name ");
			msg.append(config.getName());
			context.error(msg.toString(), ex);
			return null;
		}
	}

	/**
	 * This method is not intended to be overridden. It is private to enable overrides in cases
	 * where nothing else helps.
	 */
	protected KafkaProducer<K, V> newKafkaProducerInternal(InstantiationContext context, Config<K, V> config) {
		Map<String, Object> kafkaConf = getAllProperties();
		Serializer<K> keySerializer = getKeySerializer(context, config, kafkaConf);
		Serializer<V> valueSerializer = getValueSerializer(context, config, kafkaConf);
		return new KafkaProducer<>(kafkaConf, keySerializer, valueSerializer);
	}

	/**
	 * The {@link Serializer} that is passed in the constructor of the {@link KafkaProducer}.
	 * <p>
	 * This method is called in the constructor. Fields from subclasses are therefore not
	 * initialized, yet.
	 * </p>
	 * 
	 * @param context
	 *        Never null.
	 * @param config
	 *        Never null.
	 * 
	 * @return If null, the value in the Kafka properties is used.
	 */
	protected Serializer<K> getKeySerializer(InstantiationContext context, Config<K, V> config,
			Map<String, Object> kafkaConf) {
		if (config.getKeySerializerClass() != null) {
			kafkaConf.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getKeySerializerClass());
			return null;
		}
		return context.getInstance(config.getKeySerializer());
	}

	/**
	 * The {@link Serializer} that is passed in the constructor of the {@link KafkaProducer}.
	 * <p>
	 * This method is called in the constructor. Fields from subclasses are therefore not
	 * initialized, yet.
	 * </p>
	 * 
	 * @param context
	 *        Never null.
	 * @param config
	 *        Never null.
	 * 
	 * @return If null, the value in the Kafka properties is used.
	 */
	protected Serializer<V> getValueSerializer(InstantiationContext context, Config<K, V> config,
			Map<String, Object> kafkaConf) {
		if (config.getValueSerializerClass() != null) {
			kafkaConf.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getValueSerializerClass());
			return null;
		}
		return context.getInstance(config.getValueSerializer());
	}

	@Override
	public Map<String, Object> getTypedProperties() {
		Map<String, Object> properties = KafkaCommonClient.super.getTypedProperties();
		workaroundTransactionalIdConfigBug(properties);
		return properties;
	}

	/**
	 * Workaround for a Kafka bug:
	 * <p>
	 * Kafka reacts buggy if some values are set, even if they are set to null and that is the
	 * default: In "KafkaProducer.configureTransactionState" an exception is thrown when
	 * "enable.idempotence" is explicitly set to false and "transactional.id" is explicitly set,
	 * even if it is set to null, which is the default.
	 * </p>
	 * 
	 * @param properties
	 *        Never null. Always mutable and resizable. Is changed by this method.
	 */
	protected void workaroundTransactionalIdConfigBug(Map<String, Object> properties) {
		if (isIdempotenceExplicitlyDisabled(properties) && isTransactionalIdExplicitlySetToEmptyDefault(properties)) {
			properties.remove(ProducerConfig.TRANSACTIONAL_ID_CONFIG);
		}
	}

	/**
	 * Whether {@link ProducerConfig#ENABLE_IDEMPOTENCE_CONFIG} is explicitly set to false.
	 * 
	 * @param properties
	 *        Never null. Always mutable and resizable.
	 */
	private boolean isIdempotenceExplicitlyDisabled(Map<String, Object> properties) {
		/* Neither a check with containsKey nor a check whether it is not true check, whether it is
		 * EXPLICITLY set to FALSE. */
		return Objects.equals(properties.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG), false);
	}

	/**
	 * Whether {@link ProducerConfig#TRANSACTIONAL_ID_CONFIG} is explicitly set to its null or the
	 * empty {@link String}, and whether that is the <i>TopLogic</i> default.
	 * 
	 * @param properties
	 *        Never null. Always mutable and resizable.
	 */
	private boolean isTransactionalIdExplicitlySetToEmptyDefault(Map<String, Object> properties) {
		if (!properties.containsKey(ProducerConfig.TRANSACTIONAL_ID_CONFIG)) {
			return false;
		}
		Object transactionalId = properties.get(ProducerConfig.TRANSACTIONAL_ID_CONFIG);
		if (!StringServices.isEmpty(transactionalId)) {
			return false;
		}
		PropertyDescriptor propertyDescriptor =
			getConfig().descriptor().getProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG);
		return Objects.equals(transactionalId, propertyDescriptor.getDefaultValue());
	}

	@Override
	protected KafkaProducer<K, V> getImpl() {
		return _producer;
	}

    @Override
	public Config<K, V> getConfig() {
        return _config;
    }

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("name", getName())
			.add("topic", getConfig().getTopic())
			.build();
	}

	@Override
	public String getName() {
		return getConfig().getName();
	}

}
