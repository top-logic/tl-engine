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
import org.apache.kafka.common.serialization.StringSerializer;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.kafka.services.common.CommonClientConfig;
import com.top_logic.kafka.services.common.KafkaCommonClient;
import com.top_logic.kafka.services.consumer.KafkaClientProperty;

/**
 * Configurable wrapper class for {@link KafkaProducer}s.
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
	public interface Config<K, V> extends CommonClientConfig<V, TLKafkaProducer<K, V>> {

		/**
		 * @see #getTopic()
		 */
		String TOPIC = "topic";

		/**
		 * @see #getKeySerializer()
		 */
		String KEY_SERIALIZER_TYPED_CONFIG = "key.serializer.typed.config";

		/**
		 * @see #getValueSerializer()
		 */
		String VALUE_SERIALIZER_TYPED_CONFIG = "value.serializer.typed.config";

		/**
		 * @see #getMaxRequestSize()
		 */
		String MAX_REQUEST_SIZE = "maxRequestSize";

		/**
		 * the name of the kafka topic to write records to
		 */
		@Name(TOPIC)
		String getTopic();

		/**
		 * @see ProducerConfig#ACKS_DOC
		 */
		@Name(ProducerConfig.ACKS_CONFIG)
		@StringDefault("all")
		@KafkaClientProperty
		String getAcks();

		/**
		 * @see ProducerConfig#BATCH_SIZE_DOC
		 */
		@Name(ProducerConfig.BATCH_SIZE_CONFIG)
		@IntDefault(16 * 1024)
		@KafkaClientProperty
		int getBatchSize();

		/**
		 * @see ProducerConfig#BUFFER_MEMORY_DOC
		 */
		@Name(ProducerConfig.BUFFER_MEMORY_CONFIG)
		@LongDefault(32 * 1024 * 1024)
		@KafkaClientProperty
		long getBufferMemory();

		/**
		 * @see ProducerConfig#COMPRESSION_TYPE_DOC
		 */
		@Name(ProducerConfig.COMPRESSION_TYPE_CONFIG)
		@StringDefault("none")
		@KafkaClientProperty
		String getCompressionType();

		/**
		 * @see ProducerConfig#DELIVERY_TIMEOUT_MS_DOC
		 */
		@Name(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG)
		@IntDefault(2 * 60 * 1000)
		@KafkaClientProperty
		int getDeliveryTimeoutMS();

		/**
		 * @see ProducerConfig#ENABLE_IDEMPOTENCE_DOC
		 */
		@Name(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)
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
		@Name(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG)
		@KafkaClientProperty
		String getInterceptorClasses();

		/**
		 * This option is used only, when {@link #getKeySerializerClass()} is empty.
		 * 
		 * @see ProducerConfig#KEY_SERIALIZER_CLASS_DOC
		 */
		@ItemDefault(StringSerializer.class)
		@Name(KEY_SERIALIZER_TYPED_CONFIG)
		PolymorphicConfiguration<? extends Serializer<K>> getKeySerializer();

		/**
		 * When this option is not empty, {@link #getKeySerializer()} will be ignored.
		 * 
		 * @see ProducerConfig#KEY_SERIALIZER_CLASS_DOC
		 */
		@Name(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)
		@KafkaClientProperty
		Class<? extends Serializer<K>> getKeySerializerClass();

		/**
		 * @see ProducerConfig#LINGER_MS_DOC
		 */
		@Name(ProducerConfig.LINGER_MS_CONFIG)
		@IntDefault(0)
		@KafkaClientProperty
		int getLingerMS();

		/**
		 * @see ProducerConfig#MAX_BLOCK_MS_DOC
		 */
		@Name(ProducerConfig.MAX_BLOCK_MS_CONFIG)
		@LongDefault(60 * 1000)
		@KafkaClientProperty
		long getMaxBlockMS();

		/**
		 * @see ProducerConfig#MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION_DOC
		 */
		@Name(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION)
		@IntDefault(5)
		@KafkaClientProperty
		int getMaxInFlightRequestsPerConnection();

		/**
		 * @see ProducerConfig#METADATA_MAX_IDLE_CONFIG
		 */
		@Name(ProducerConfig.METADATA_MAX_IDLE_CONFIG)
		@LongDefault(5 * 60 * 1000)
		@KafkaClientProperty
		long getMetadataMaxIdleMS();

		/**
		 * @see ProducerConfig#MAX_REQUEST_SIZE_DOC
		 */
		@Name(ProducerConfig.MAX_REQUEST_SIZE_CONFIG)
		@IntDefault(1000 * 1000 * 1000)
		@KafkaClientProperty
		int getMaxRequestSize();

		/**
		 * @see ProducerConfig#PARTITIONER_CLASS_CONFIG
		 */
		@Name(ProducerConfig.PARTITIONER_CLASS_CONFIG)
		@KafkaClientProperty
		Class<? extends Partitioner> getPartitionerClass();

		/**
		 * @see ProducerConfig#PARTITIONER_IGNORE_KEYS_CONFIG
		 */
		@Name(ProducerConfig.PARTITIONER_IGNORE_KEYS_CONFIG)
		@KafkaClientProperty
		boolean getPartitionerIgnoreKeys();

		/**
		 * @see ProducerConfig#PARTITIONER_ADPATIVE_PARTITIONING_ENABLE_CONFIG
		 */
		@Name(ProducerConfig.PARTITIONER_ADPATIVE_PARTITIONING_ENABLE_CONFIG)
		@BooleanDefault(true)
		@KafkaClientProperty
		boolean getPartitionerAdaptivePartitioningEnabled();

		/**
		 * @see ProducerConfig#PARTITIONER_AVAILABILITY_TIMEOUT_MS_CONFIG
		 */
		@Name(ProducerConfig.PARTITIONER_AVAILABILITY_TIMEOUT_MS_CONFIG)
		@KafkaClientProperty
		long getPartitionerAvailabilityTimeoutMS();

		@Override
		@IntDefault(32 * 1024)
		int getReceiveBufferBytes();

		@Override
		@IntDefault(30 * 1000)
		int getRequestTimeoutMS();

		/**
		 * @see ProducerConfig#RETRIES_CONFIG
		 */
		@Name(ProducerConfig.RETRIES_CONFIG)
		@IntDefault(Integer.MAX_VALUE)
		@KafkaClientProperty
		int getRetries();

		/**
		 * @see ProducerConfig#TRANSACTION_TIMEOUT_DOC
		 */
		@Name(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG)
		@IntDefault(60 * 1000)
		@KafkaClientProperty
		int getTransactionTimeoutMS();

		/**
		 * @see ProducerConfig#TRANSACTIONAL_ID_DOC
		 */
		@Name(ProducerConfig.TRANSACTIONAL_ID_CONFIG)
		@KafkaClientProperty
		String getTransactionalId();

		/**
		 * This option is used only, when {@link #getValueSerializerClass()} is empty.
		 * 
		 * @see ProducerConfig#VALUE_SERIALIZER_CLASS_DOC
		 */
		@ItemDefault(StringSerializer.class)
		@Name(VALUE_SERIALIZER_TYPED_CONFIG)
		PolymorphicConfiguration<? extends Serializer<V>> getValueSerializer();

		/**
		 * When this option is not empty, {@link #getValueSerializer()} will be ignored.
		 * 
		 * @see ProducerConfig#VALUE_SERIALIZER_CLASS_DOC
		 */
		@Name(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)
		@KafkaClientProperty
		Class<? extends Serializer<K>> getValueSerializerClass();

    }

	private final KafkaProducer<K, V> _producer;

	/**
	 * @see #getConfig()
	 */
	private final Config<K, V> _config;

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
		_producer = requireNonNull(newKafkaProducer(context, config));
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
