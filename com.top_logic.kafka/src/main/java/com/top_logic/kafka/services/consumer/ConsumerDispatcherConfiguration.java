/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.consumer;

import java.util.List;
import java.util.Set;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.kafka.services.common.CommonClientConfig;

/**
 * Typed configuration interface for consumers retrieving data using a
 * {@link KafkaConsumer}.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
@DisplayOrder({
	NamedConfiguration.NAME_ATTRIBUTE,
	CommonClientConfigs.CLIENT_ID_CONFIG,
	ConsumerDispatcherConfiguration.CLIENT_RACK,
	ConsumerDispatcherConfiguration.PROCESSORS,
	CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
	ConsumerDispatcherConfiguration.TOPICS,
	ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
	ConsumerDispatcherConfiguration.KEY_DESERIALIZER_TYPED_CONFIG,
	ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
	ConsumerDispatcherConfiguration.VALUE_DESERIALIZER_TYPED_CONFIG,
	CommonClientConfig.LOG_WRITER,
	PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME,
	ConsumerDispatcherConfiguration.POLLING_TIMEOUT,
	ConsumerDispatcherConfiguration.ERROR_PAUSE_START,
	ConsumerDispatcherConfiguration.ERROR_PAUSE_FACTOR,
	ConsumerDispatcherConfiguration.ERROR_PAUSE_MAX,
	ConsumerDispatcherConfiguration.ALLOW_AUTO_CREATE_TOPICS,
	ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,
	ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
	ConsumerConfig.CHECK_CRCS_CONFIG,
	ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG,
	ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
	ConsumerConfig.EXCLUDE_INTERNAL_TOPICS_CONFIG,
	ConsumerConfig.FETCH_MAX_BYTES_CONFIG,
	ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG,
	ConsumerConfig.FETCH_MIN_BYTES_CONFIG,
	ConsumerConfig.GROUP_ID_CONFIG,
	ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG,
	ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG,
	ConsumerConfig.ISOLATION_LEVEL_CONFIG,
	ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
	ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,
	ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
	ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
	ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
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
	SaslConfigs.SASL_MECHANISM,
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
public interface ConsumerDispatcherConfiguration<K, V> extends CommonClientConfig<V, ConsumerDispatcher<K, V>> {

	/** Property name of {@link #getAllowAutoCreateTopics()}. */
	String ALLOW_AUTO_CREATE_TOPICS = "allow.auto.create.topics";

	/** Property name of {@link #getClientRack()}. */
	String CLIENT_RACK = "client.rack";

	/**
	 * @see #getKeyDeserializer()
	 */
	String KEY_DESERIALIZER_TYPED_CONFIG = "key.deserializer.typed.config";

	/**
	 * @see #getValueDeserializer()
	 */
	String VALUE_DESERIALIZER_TYPED_CONFIG = "value.deserializer.typed.config";

	/** Property name of {@link #getTopics()}. */
	String TOPICS = "topics";

	/** Property name of {@link #getPollingTimeout()}. */
	String POLLING_TIMEOUT = "polling-timeout";

	/** Property name of {@link #getErrorPauseStart()}. */
	String ERROR_PAUSE_START = "error-pause-start";

	/** Property name of {@link #getErrorPauseFactor()}. */
	String ERROR_PAUSE_FACTOR = "error-pause-factor";

	/** Property name of {@link #getErrorPauseMax()}. */
	String ERROR_PAUSE_MAX = "error-pause-max";

	/** Property name of {@link #getProcessors()}. */
	String PROCESSORS = "processors";

	@ClassDefault(ConsumerDispatcher.class)
	@Override
	Class<? extends ConsumerDispatcher<K, V>> getImplementationClass();

	/**
	 * a {@link Set} of topics to subscribe the consumer to
	 */
	@Format(CommaSeparatedStringSet.class)
	@Name(TOPICS)
	Set<String> getTopics();

	/**
	 * the number of milliseconds to wait for the arrival of new
	 *         messages
	 */
	@Name(POLLING_TIMEOUT)
	@LongDefault(1000)
	long getPollingTimeout();

	/**
	 * The pause in milliseconds after an error happened.
	 * <p>
	 * This is necessary to prevent the {@link ConsumerDispatcher} from flooding the logs when a
	 * Kafka message cannot be processed.
	 * </p>
	 * <p>
	 * See {@link #getErrorPauseFactor()} for the formula for the error pauses in case of repeated
	 * failures.
	 * </p>
	 */
	@Name(ERROR_PAUSE_START)
	@FormattedDefault("10s")
	@Format(MillisFormat.class)
	long getErrorPauseStart();

	/**
	 * When receiving messages keeps failing, the time between the retries increases by this factor
	 * every time.
	 * <p>
	 * When it fails for the first time, the first retry happens after {@link #getErrorPauseStart()}
	 * milliseconds. The second retry happens after {@link #getErrorPauseStart()} ms multiplied by
	 * this factor. The third retry happens after <code>start * factor * factor</code> ms and so on.
	 * But the time between retries never increases above {@link #getErrorPauseMax()}. It is capped
	 * there. When it has reached this value, it stays there, until the problem is resolved and
	 * receiving works again. The formula for the n'th retry is therefore:
	 * <code>min(error-pause-max, error-pause-start * (error-pause-factor ** (n-1)))</code> When it
	 * works again, but later starts failing again, the time between retries starts at
	 * {@link #getErrorPauseStart()} again.
	 * </p>
	 */
	@Name(ERROR_PAUSE_FACTOR)
	@FloatDefault(2)
	float getErrorPauseFactor();

	/**
	 * The maximum pause in milliseconds when receiving messages keeps failing.
	 * <p>
	 * See {@link #getErrorPauseFactor()} for the formula for the error pauses in case of repeated
	 * failures.
	 * </p>
	 */
	@Name(ERROR_PAUSE_MAX)
	@FormattedDefault("10min")
	@Format(MillisFormat.class)
	long getErrorPauseMax();

	/**
	 * a (possibly empty) {@link List} of configured
	 *         {@link ConsumerProcessor}s
	 */
	@Name(PROCESSORS)
	List<PolymorphicConfiguration<ConsumerProcessor<K,V>>> getProcessors();

	/**
	 * The Kafka consumer property: "allow.auto.create.topics"
	 */
	@Name(ALLOW_AUTO_CREATE_TOPICS)
	@BooleanDefault(true)
	@KafkaClientProperty
	boolean getAllowAutoCreateTopics();

	/**
	 * @see ConsumerConfig#AUTO_COMMIT_INTERVAL_MS_CONFIG
	 */
	@Name(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG)
	@IntDefault(5 * 1000)
	@KafkaClientProperty
	int getAutoCommitIntervalMS();

	/**
	 * @see ConsumerConfig#AUTO_OFFSET_RESET_DOC
	 */
	@Name(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)
	@StringDefault("latest")
	@KafkaClientProperty
	String getAutoOffsetReset();
	
	/**
	 * @see ConsumerConfig#CHECK_CRCS_CONFIG
	 */
	@Name(ConsumerConfig.CHECK_CRCS_CONFIG)
	@BooleanDefault(true)
	@KafkaClientProperty
	boolean getCheckCrcs();

	/**
	 * The Kafka consumer property: "client.rack"
	 */
	@Name(CLIENT_RACK)
	@KafkaClientProperty
	String getClientRack();

	/**
	 * @see ConsumerConfig#DEFAULT_API_TIMEOUT_MS_CONFIG
	 */
	@Name(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG)
	@IntDefault(60 * 1000)
	@KafkaClientProperty
	int getDefaultApiTimeoutMS();

	/**
	 * @see ConsumerConfig#ENABLE_AUTO_COMMIT_CONFIG
	 */
	@Name(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)
	@BooleanDefault(true)
	@KafkaClientProperty
	boolean getEnableAutoCommit();
	
	/**
	 * @see ConsumerConfig#EXCLUDE_INTERNAL_TOPICS_CONFIG
	 */
	@Name(ConsumerConfig.EXCLUDE_INTERNAL_TOPICS_CONFIG)
	@BooleanDefault(true)
	@KafkaClientProperty
	boolean getExcludeInternalTopics();
	
	/**
	 * @see ConsumerConfig#FETCH_MAX_BYTES_CONFIG
	 */
	@Name(ConsumerConfig.FETCH_MAX_BYTES_CONFIG)
	@IntDefault(50 * 1024 * 1024)
	@KafkaClientProperty
	int getFetchMaxBytes();
	
	/**
	 * @see ConsumerConfig#FETCH_MAX_WAIT_MS_CONFIG
	 */
	@Name(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG)
	@IntDefault(500)
	@KafkaClientProperty
	int getFetchMaxWaitMS();
	
	/**
	 * @see ConsumerConfig#FETCH_MIN_BYTES_CONFIG
	 */
	@Name(ConsumerConfig.FETCH_MIN_BYTES_CONFIG)
	@IntDefault(1)
	@KafkaClientProperty
	int getFetchMinBytes();
	
	/**
	 * @see ConsumerConfig#GROUP_ID_CONFIG
	 */
	@Name(ConsumerConfig.GROUP_ID_CONFIG)
	@Mandatory
	@KafkaClientProperty
	String getGroupId();

	/**
	 * @see ConsumerConfig#GROUP_INSTANCE_ID_DOC
	 */
	@Name(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG)
	@KafkaClientProperty
	String getGroupInstanceId();

	/**
	 * @see ConsumerConfig#HEARTBEAT_INTERVAL_MS_CONFIG
	 */
	@Name(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG)
	@IntDefault(3 * 1000)
	@KafkaClientProperty
	int getHeartbeatIntervalMS();
	
	/**
	 * @see ConsumerConfig#INTERCEPTOR_CLASSES_DOC
	 * 
	 * @implNote Kafka specifies this in the {@link ConsumerConfig} <em>and</em> the
	 *           {@link ProducerConfig}, but <em>not</em> in the {@link CommonClientConfigs}. It is
	 *           therefore declared here that way, too.
	 */
	@Name(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG)
	@KafkaClientProperty
	String getInterceptorClasses();

	/**
	 * @see ConsumerConfig#ISOLATION_LEVEL_DOC
	 */
	@Name(ConsumerConfig.ISOLATION_LEVEL_CONFIG)
	@StringDefault("read_uncommitted")
	@KafkaClientProperty
	String getIsolationLevel();

	/**
	 * @see ConsumerConfig#MAX_PARTITION_FETCH_BYTES_CONFIG
	 */
	@Name(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG)
	@IntDefault(1 * 1024 * 1024)
	@KafkaClientProperty
	int getMaxPartitionFetchBytes();

	/**
	 * When more than this much time passes between two requests, the client will be removed from
	 * the server.
	 * 
	 * @see ConsumerConfig#MAX_POLL_INTERVAL_MS_CONFIG
	 */
	@Name(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG)
	@IntDefault(5 * 60 * 1000)
	@KafkaClientProperty
	int getMaxPollIntervalMS();

	/**
	 * @see ConsumerConfig#MAX_POLL_RECORDS_CONFIG
	 */
	@Name(ConsumerConfig.MAX_POLL_RECORDS_CONFIG)
	@IntDefault(500)
	@KafkaClientProperty
	int getMaxPollRecords();

	/**
	 * @see ConsumerConfig#PARTITION_ASSIGNMENT_STRATEGY_CONFIG
	 */
	@Name(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG)
	@StringDefault("org.apache.kafka.clients.consumer.RangeAssignor,org.apache.kafka.clients.consumer.CooperativeStickyAssignor")
	@KafkaClientProperty
	String getPartitionAssignmentStrategy();

	@Override
	@IntDefault(64 * 1024)
	int getReceiveBufferBytes();
	
	@Override
	@IntDefault(30 * 1000)
	int getRequestTimeoutMS();
	
	/**
	 * @see ConsumerConfig#SESSION_TIMEOUT_MS_CONFIG
	 */
	@Name(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG)
	@IntDefault(45 * 1000)
	@KafkaClientProperty
	int getSessionTimeoutMS();
	
	/**
	 * This option is used only, when {@link #getKeyDeserializerClass()} is empty.
	 * 
	 * @see ConsumerConfig#KEY_DESERIALIZER_CLASS_DOC
	 */
	@Name(KEY_DESERIALIZER_TYPED_CONFIG)
	@ItemDefault(StringDeserializer.class)
	PolymorphicConfiguration<? extends Deserializer<K>> getKeyDeserializer();

	/**
	 * When this option is not empty, {@link #getKeyDeserializer()} will be ignored.
	 * 
	 * @see ConsumerConfig#KEY_DESERIALIZER_CLASS_DOC
	 */
	@Name(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)
	@KafkaClientProperty
	Class<? extends Deserializer<K>> getKeyDeserializerClass();

	/**
	 * This option is used only, when {@link #getValueDeserializerClass()} is empty.
	 * 
	 * @see ConsumerConfig#VALUE_DESERIALIZER_CLASS_DOC
	 */
	@Name(VALUE_DESERIALIZER_TYPED_CONFIG)
	@ItemDefault(StringDeserializer.class)
	PolymorphicConfiguration<? extends Deserializer<V>> getValueDeserializer();
	
	/**
	 * When this option is not empty, {@link #getValueDeserializer()} will be ignored.
	 * 
	 * @see ConsumerConfig#VALUE_DESERIALIZER_CLASS_DOC
	 */
	@Name(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG)
	@KafkaClientProperty
	Class<? extends Deserializer<K>> getValueDeserializerClass();

}
