/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.producer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.kafka.services.common.CommonClientConfig;

/**
 * {@link TLKafkaProducer} for string values and keys.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StringTLKafkaProducer extends TLKafkaProducer<String, String> {

	/**
	 * Configuration of the {@link StringTLKafkaProducer}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@DisplayOrder({
		NamedConfiguration.NAME_ATTRIBUTE,
		CommonClientConfigs.CLIENT_ID_CONFIG,
		CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
		Config.TOPIC,
		// As this class uses only strings, these config options are fix and therefore hidden.
		// ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
		// Config.KEY_SERIALIZER_TYPED_CONFIG,
		// ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
		// Config.VALUE_SERIALIZER_TYPED_CONFIG,
		CommonClientConfig.LOG_WRITER,
		PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME,
		ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
		ProducerConfig.TRANSACTIONAL_ID_CONFIG,
		ProducerConfig.TRANSACTION_TIMEOUT_CONFIG,
		ProducerConfig.ACKS_CONFIG,
		ProducerConfig.RETRIES_CONFIG,
		ProducerConfig.BATCH_SIZE_CONFIG,
		ProducerConfig.BUFFER_MEMORY_CONFIG,
		ProducerConfig.COMPRESSION_TYPE_CONFIG,
		ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG,
		ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG,
		ProducerConfig.LINGER_MS_CONFIG,
		ProducerConfig.MAX_BLOCK_MS_CONFIG,
		ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
		ProducerConfig.MAX_REQUEST_SIZE_CONFIG,
		ProducerConfig.PARTITIONER_CLASS_CONFIG,
		CommonClientConfigs.CLIENT_DNS_LOOKUP_CONFIG,
		CommonClientConfigs.CONNECTIONS_MAX_IDLE_MS_CONFIG,
		CommonClientConfigs.METADATA_MAX_AGE_CONFIG,
		CommonClientConfigs.METRIC_REPORTER_CLASSES_CONFIG,
		CommonClientConfigs.METRICS_NUM_SAMPLES_CONFIG,
		CommonClientConfigs.METRICS_RECORDING_LEVEL_CONFIG,
		CommonClientConfigs.METRICS_SAMPLE_WINDOW_MS_CONFIG,
		CommonClientConfigs.RECEIVE_BUFFER_CONFIG,
		CommonClientConfigs.RECONNECT_BACKOFF_MAX_MS_CONFIG,
		CommonClientConfigs.RECONNECT_BACKOFF_MS_CONFIG,
		CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG,
		CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG,
		CommonClientConfigs.SEND_BUFFER_CONFIG,
		CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
		CommonClientConfig.SECURITY_PROVIDERS,
		SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS,
		SaslConfigs.SASL_JAAS_CONFIG,
		SaslConfigs.SASL_KERBEROS_KINIT_CMD,
		SaslConfigs.SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN,
		SaslConfigs.SASL_KERBEROS_SERVICE_NAME,
		SaslConfigs.SASL_KERBEROS_TICKET_RENEW_JITTER,
		SaslConfigs.SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR,
		SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS,
		SaslConfigs.SASL_LOGIN_CLASS,
		SaslConfigs.SASL_LOGIN_REFRESH_BUFFER_SECONDS,
		SaslConfigs.SASL_LOGIN_REFRESH_MIN_PERIOD_SECONDS,
		SaslConfigs.SASL_LOGIN_REFRESH_WINDOW_FACTOR,
		SaslConfigs.SASL_LOGIN_REFRESH_WINDOW_JITTER,
		SaslConfigs.SASL_MECHANISM,
		SslConfigs.SSL_CIPHER_SUITES_CONFIG,
		SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG,
		SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,
		SslConfigs.SSL_KEY_PASSWORD_CONFIG,
		SslConfigs.SSL_KEYMANAGER_ALGORITHM_CONFIG,
		SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
		SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,
		SslConfigs.SSL_KEYSTORE_TYPE_CONFIG,
		SslConfigs.SSL_PROTOCOL_CONFIG,
		SslConfigs.SSL_PROVIDER_CONFIG,
		SslConfigs.SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG,
		SslConfigs.SSL_TRUSTMANAGER_ALGORITHM_CONFIG,
		SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
		SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
		SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG,
		CommonClientConfig.UNTYPED_PROPERTIES,
	})
	public static interface Config extends TLKafkaProducer.Config<String, String> {
		// currently empty
	}

	/**
	 * The name of the topic to write the data to.
	 */
	private final String _topic;

	/**
	 * Creates a new {@link StringTLKafkaProducer}.
	 */
	public StringTLKafkaProducer(InstantiationContext context, Config config) {
		super(context, config);
		_topic = config.getTopic();
	}

	/**
	 * @param key
	 *        the {@link String} to be used as key for the new record
	 * @param value
	 *        the {@link String} to be used as value of the new record
	 * @return the new {@link ProducerRecord} with the given key and value
	 */
	public ProducerRecord<String, String> newProducerRecord(final String key, final String value) {
		return new ProducerRecord<>(_topic, key, value);
	}

}

