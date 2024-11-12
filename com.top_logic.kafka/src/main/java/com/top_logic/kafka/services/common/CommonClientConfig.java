/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.common;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.auth.Login;
import org.apache.kafka.common.security.auth.SslEngineFactory;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedPolymorphicConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.ShortDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.format.MillisFormatInt;
import com.top_logic.kafka.log.KafkaLogWriter;
import com.top_logic.kafka.log.KafkaNoContentsLogWriter;
import com.top_logic.kafka.services.consumer.ConsumerDispatcher;
import com.top_logic.kafka.services.consumer.KafkaClientProperty;

/**
 * Common configurations for Kafka consumer and producer.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CommonClientConfig<V, T> extends NamedPolymorphicConfiguration<T> {

	/** @see #getAutoIncludeJmxReporter() */
	@SuppressWarnings("deprecation")
	@Deprecated
	String AUTO_INCLUDE_JMX_REPORTER_CONFIG = CommonClientConfigs.AUTO_INCLUDE_JMX_REPORTER_CONFIG;

	/** @see #getSslTruststoreLocation() */
	String SSL_TRUSTSTORE_LOCATION_CONFIG = SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG;

	/** @see #getSslTruststorePassword() */
	String SSL_TRUSTSTORE_PASSWORD_CONFIG = SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG;

	/** @see #getSslTruststoreType() */
	String SSL_TRUSTSTORE_TYPE_CONFIG = SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG;

	/** @see #getSslTruststoreCertificates() */
	String SSL_TRUSTMANAGER_ALGORITHM_CONFIG = SslConfigs.SSL_TRUSTMANAGER_ALGORITHM_CONFIG;

	/** @see #getSslTruststoreCertificates() */
	String SSL_TRUSTSTORE_CERTIFICATES_CONFIG = SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG;

	/** @see #getSslSecureRandomImplementation() */
	String SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG = SslConfigs.SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG;

	/** @see #getSslProvider() */
	String SSL_PROVIDER_CONFIG = SslConfigs.SSL_PROVIDER_CONFIG;

	/** @see #getSslProtocol() */
	String SSL_PROTOCOL_CONFIG = SslConfigs.SSL_PROTOCOL_CONFIG;

	/** @see #getSslEnabledProtocols() */
	String SSL_ENABLED_PROTOCOLS_CONFIG = SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG;

	/** @see #getSslKeystoreType() */
	String SSL_KEYSTORE_TYPE_CONFIG = SslConfigs.SSL_KEYSTORE_TYPE_CONFIG;

	/** @see #getSslKeystoreLocation() */
	String SSL_KEYSTORE_LOCATION_CONFIG = SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG;

	/** @see #getSslKeystorePassword() */
	String SSL_KEYSTORE_PASSWORD_CONFIG = SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG;

	/** @see #getSslKeystoreKey() */
	String SSL_KEYSTORE_KEY_CONFIG = SslConfigs.SSL_KEYSTORE_KEY_CONFIG;

	/** @see #getSslKeystoreCertificateChain() */
	String SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG = SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG;

	/** @see #getSslKeymanagerAlgorithm() */
	String SSL_KEYMANAGER_ALGORITHM_CONFIG = SslConfigs.SSL_KEYMANAGER_ALGORITHM_CONFIG;

	/** @see #getSslKeyPassword() */
	String SSL_KEY_PASSWORD_CONFIG = SslConfigs.SSL_KEY_PASSWORD_CONFIG;

	/** @see #getSslEngineFactoryClass() */
	String SSL_ENGINE_FACTORY_CLASS_CONFIG = SslConfigs.SSL_ENGINE_FACTORY_CLASS_CONFIG;

	/** @see #getSslEndpointIdentificationAlgorithm() */
	String SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG = SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;

	/** @see #getSslCipherSuites() */
	String SSL_CIPHER_SUITES_CONFIG = SslConfigs.SSL_CIPHER_SUITES_CONFIG;

	/** @see #getSendBufferBytes() */
	String SEND_BUFFER_CONFIG = CommonClientConfigs.SEND_BUFFER_CONFIG;

	/** @see #getSecurityProtocol() */
	String SECURITY_PROTOCOL_CONFIG = CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;

	/** @see #getSaslOauthbearerTokenEndpointUrl() */
	String SASL_OAUTHBEARER_TOKEN_ENDPOINT_URL = SaslConfigs.SASL_OAUTHBEARER_TOKEN_ENDPOINT_URL;

	/** @see #getSaslOauthbearerSubClaimName() */
	String SASL_OAUTHBEARER_SUB_CLAIM_NAME = SaslConfigs.SASL_OAUTHBEARER_SUB_CLAIM_NAME;

	/** @see #getSaslOauthbearerScopeClaimName() */
	String SASL_OAUTHBEARER_SCOPE_CLAIM_NAME = SaslConfigs.SASL_OAUTHBEARER_SCOPE_CLAIM_NAME;

	/** @see #getSaslOauthbearerJwksEndpointRetryBackoffMS() */
	String SASL_OAUTHBEARER_JWKS_ENDPOINT_RETRY_BACKOFF_MS =
		SaslConfigs.SASL_OAUTHBEARER_JWKS_ENDPOINT_RETRY_BACKOFF_MS;

	/** @see #getSaslOauthbearerJwksEndpointRetryBackoffMaxMS() */
	String SASL_OAUTHBEARER_JWKS_ENDPOINT_RETRY_BACKOFF_MAX_MS =
		SaslConfigs.SASL_OAUTHBEARER_JWKS_ENDPOINT_RETRY_BACKOFF_MAX_MS;

	/** @see #getSaslOauthbearerJwksEndpointRefreshMS() */
	String SASL_OAUTHBEARER_JWKS_ENDPOINT_REFRESH_MS = SaslConfigs.SASL_OAUTHBEARER_JWKS_ENDPOINT_REFRESH_MS;

	/** @see #getSaslOauthbearerJwksEndpointUrl() */
	String SASL_OAUTHBEARER_JWKS_ENDPOINT_URL = SaslConfigs.SASL_OAUTHBEARER_JWKS_ENDPOINT_URL;

	/** @see #getSaslOauthbearerExpectedIssuer() */
	String SASL_OAUTHBEARER_EXPECTED_ISSUER = SaslConfigs.SASL_OAUTHBEARER_EXPECTED_ISSUER;

	/** @see #getSaslOauthbearerExpectedAudience() */
	String SASL_OAUTHBEARER_EXPECTED_AUDIENCE = SaslConfigs.SASL_OAUTHBEARER_EXPECTED_AUDIENCE;

	/** @see #getSaslOauthbearerClockSkewSeconds() */
	String SASL_OAUTHBEARER_CLOCK_SKEW_SECONDS = SaslConfigs.SASL_OAUTHBEARER_CLOCK_SKEW_SECONDS;

	/** @see #getSaslMechanism() */
	String SASL_MECHANISM = SaslConfigs.SASL_MECHANISM;

	/** @see #getSaslLoginRetryBackoffMS() */
	String SASL_LOGIN_RETRY_BACKOFF_MS = SaslConfigs.SASL_LOGIN_RETRY_BACKOFF_MS;

	/** @see #getSaslLoginRetryBackoffMaxMS() */
	String SASL_LOGIN_RETRY_BACKOFF_MAX_MS = SaslConfigs.SASL_LOGIN_RETRY_BACKOFF_MAX_MS;

	/** @see #getSaslLoginRefreshWindowJitter() */
	String SASL_LOGIN_REFRESH_WINDOW_JITTER = SaslConfigs.SASL_LOGIN_REFRESH_WINDOW_JITTER;

	/** @see #getSaslLoginRefreshWindowFactor() */
	String SASL_LOGIN_REFRESH_WINDOW_FACTOR = SaslConfigs.SASL_LOGIN_REFRESH_WINDOW_FACTOR;

	/** @see #getSaslLoginRefreshMinPeriodSeconds() */
	String SASL_LOGIN_REFRESH_MIN_PERIOD_SECONDS = SaslConfigs.SASL_LOGIN_REFRESH_MIN_PERIOD_SECONDS;

	/** @see #getSaslLoginRefreshBufferSeconds() */
	String SASL_LOGIN_REFRESH_BUFFER_SECONDS = SaslConfigs.SASL_LOGIN_REFRESH_BUFFER_SECONDS;

	/** @see #getSaslLoginReadTimeoutMS() */
	String SASL_LOGIN_READ_TIMEOUT_MS = SaslConfigs.SASL_LOGIN_READ_TIMEOUT_MS;

	/** @see #getSaslLoginConnectTimeoutMS() */
	String SASL_LOGIN_CONNECT_TIMEOUT_MS = SaslConfigs.SASL_LOGIN_CONNECT_TIMEOUT_MS;

	/** @see #getSaslLoginClass() */
	String SASL_LOGIN_CLASS = SaslConfigs.SASL_LOGIN_CLASS;

	/** @see #getSaslLoginCallbackHandlerClass() */
	String SASL_LOGIN_CALLBACK_HANDLER_CLASS = SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;

	/** @see #getSaslKerberosTicketRenewWindowFactor() */
	String SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR = SaslConfigs.SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR;

	/** @see #getSaslKerberosTicketRenewJitter() */
	String SASL_KERBEROS_TICKET_RENEW_JITTER = SaslConfigs.SASL_KERBEROS_TICKET_RENEW_JITTER;

	/** @see #getSaslKerberosServiceName() */
	String SASL_KERBEROS_SERVICE_NAME = SaslConfigs.SASL_KERBEROS_SERVICE_NAME;

	/** @see #getSaslKerberosMinTimeBeforeRelogin() */
	String SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN = SaslConfigs.SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN;

	/** @see #getSaslKerberosKinitCmd() */
	String SASL_KERBEROS_KINIT_CMD = SaslConfigs.SASL_KERBEROS_KINIT_CMD;

	/** @see #getSaslJaasConfig() */
	String SASL_JAAS_CONFIG = SaslConfigs.SASL_JAAS_CONFIG;

	/** @see #getSaslClientCallbackHandlerClass() */
	String SASL_CLIENT_CALLBACK_HANDLER_CLASS = SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS;

	/** @see #getClientId() */
	String CLIENT_ID_CONFIG = CommonClientConfigs.CLIENT_ID_CONFIG;

	/** @see #getSocketConnectionSetupTimeoutMS() */
	String SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG = CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG;

	/** @see #getSocketConnectionSetupTimeoutMaxMS() */
	String SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG =
		CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG;

	/** @see #getRetryBackoffMS() */
	String RETRY_BACKOFF_MS_CONFIG = CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG;

	/** @see #getRequestTimeoutMS() */
	String REQUEST_TIMEOUT_MS_CONFIG = CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG;

	/** @see #getReconnectBackoffMS() */
	String RECONNECT_BACKOFF_MS_CONFIG = CommonClientConfigs.RECONNECT_BACKOFF_MS_CONFIG;

	/** @see #getReconnectBackoffMaxMS() */
	String RECONNECT_BACKOFF_MAX_MS_CONFIG = CommonClientConfigs.RECONNECT_BACKOFF_MAX_MS_CONFIG;

	/** @see #getMetricsSampleWindowMS() */
	String METRICS_SAMPLE_WINDOW_MS_CONFIG = CommonClientConfigs.METRICS_SAMPLE_WINDOW_MS_CONFIG;

	/** @see #getConnectionsMaxIdleMS() */
	String CONNECTIONS_MAX_IDLE_MS_CONFIG = CommonClientConfigs.CONNECTIONS_MAX_IDLE_MS_CONFIG;

	/** @see #getReceiveBufferBytes() */
	String RECEIVE_BUFFER_CONFIG = CommonClientConfigs.RECEIVE_BUFFER_CONFIG;

	/** @see #getMetricsRecordingLevel() */
	String METRICS_RECORDING_LEVEL_CONFIG = CommonClientConfigs.METRICS_RECORDING_LEVEL_CONFIG;

	/** @see #getMetricsNumSamples() */
	String METRICS_NUM_SAMPLES_CONFIG = CommonClientConfigs.METRICS_NUM_SAMPLES_CONFIG;

	/** @see #getMetricReporters() */
	String METRIC_REPORTER_CLASSES_CONFIG = CommonClientConfigs.METRIC_REPORTER_CLASSES_CONFIG;

	/** @see #getMetadataMaxAgeMS() */
	String METADATA_MAX_AGE_CONFIG = CommonClientConfigs.METADATA_MAX_AGE_CONFIG;

	/** @see #getClientDnsLookup() */
	String CLIENT_DNS_LOOKUP_CONFIG = CommonClientConfigs.CLIENT_DNS_LOOKUP_CONFIG;

	/** @see #getBootstrapServers() */
	String BOOTSTRAP_SERVERS_CONFIG = CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;

	/** Property name of {@link #getSecurityProviders()}. */
	String SECURITY_PROVIDERS = "security.providers";

	/** Property name of {@link #getLogWriter()}. */
	String LOG_WRITER = "log-writer";

	/** Property name of {@link #getUntypedProperties()}. */
	String UNTYPED_PROPERTIES = "untyped-properties";

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/**
	 * @see ConsumerConfig#AUTO_INCLUDE_JMX_REPORTER_CONFIG
	 */
	@Hidden
	@Deprecated
	@Name(AUTO_INCLUDE_JMX_REPORTER_CONFIG)
	@BooleanDefault(true)
	@KafkaClientProperty
	boolean getAutoIncludeJmxReporter();

	/**
	 * @see CommonClientConfigs#BOOTSTRAP_SERVERS_DOC
	 */
	@Name(BOOTSTRAP_SERVERS_CONFIG)
	@KafkaClientProperty
	@Mandatory
	String getBootstrapServers();

	/**
	 * @see CommonClientConfigs#BOOTSTRAP_SERVERS_DOC
	 */
	@Name(CLIENT_DNS_LOOKUP_CONFIG)
	@StringDefault("use_all_dns_ips")
	@KafkaClientProperty
	String getClientDnsLookup();

	/**
	 * @see CommonClientConfigs#CONNECTIONS_MAX_IDLE_MS_DOC
	 */
	@Name(CONNECTIONS_MAX_IDLE_MS_CONFIG)
	@LongDefault(9 * 60 * 1000)
	@Format(MillisFormat.class)
	@KafkaClientProperty
	long getConnectionsMaxIdleMS();

	/**
	 * @see CommonClientConfigs#METADATA_MAX_AGE_DOC
	 */
	@Name(METADATA_MAX_AGE_CONFIG)
	@LongDefault(5 * 60 * 1000)
	@Format(MillisFormat.class)
	@KafkaClientProperty
	long getMetadataMaxAgeMS();

	/**
	 * @see CommonClientConfigs#METRIC_REPORTER_CLASSES_DOC
	 */
	@Hidden
	@Name(METRIC_REPORTER_CLASSES_CONFIG)
	@KafkaClientProperty
	String getMetricReporters();

	/**
	 * @see CommonClientConfigs#METRICS_NUM_SAMPLES_DOC
	 */
	@Name(METRICS_NUM_SAMPLES_CONFIG)
	@IntDefault(2)
	@KafkaClientProperty
	int getMetricsNumSamples();

	/**
	 * @see CommonClientConfigs#METRICS_RECORDING_LEVEL_DOC
	 */
	@Name(METRICS_RECORDING_LEVEL_CONFIG)
	@StringDefault("INFO")
	@KafkaClientProperty
	String getMetricsRecordingLevel();

	/**
	 * @see CommonClientConfigs#METRICS_SAMPLE_WINDOW_MS_DOC
	 */
	@Name(METRICS_SAMPLE_WINDOW_MS_CONFIG)
	@LongDefault(30 * 1000)
	@Format(MillisFormat.class)
	@KafkaClientProperty
	long getMetricsSampleWindowMS();

	/**
	 * @see CommonClientConfigs#RECEIVE_BUFFER_DOC
	 */
	@Name(RECEIVE_BUFFER_CONFIG)
	@KafkaClientProperty
	int getReceiveBufferBytes();

	/**
	 * @see CommonClientConfigs#RECONNECT_BACKOFF_MAX_MS_DOC
	 */
	@Name(RECONNECT_BACKOFF_MAX_MS_CONFIG)
	@LongDefault(1000)
	@Format(MillisFormat.class)
	@KafkaClientProperty
	long getReconnectBackoffMaxMS();

	/**
	 * NOTE: not all characters are admissible, e.g. the character ':' must be avoided.
	 * 
	 * @see CommonClientConfigs#CLIENT_ID_DOC
	 */
	@Name(CLIENT_ID_CONFIG)
	@KafkaClientProperty
	String getClientId();

	/**
	 * @see CommonClientConfigs#RECONNECT_BACKOFF_MS_DOC
	 */
	@Name(RECONNECT_BACKOFF_MS_CONFIG)
	@LongDefault(50)
	@Format(MillisFormat.class)
	@KafkaClientProperty
	long getReconnectBackoffMS();

	/**
	 * @see CommonClientConfigs#REQUEST_TIMEOUT_MS_DOC
	 */
	@Name(REQUEST_TIMEOUT_MS_CONFIG)
	@Format(MillisFormatInt.class)
	@IntDefault(0)
	@KafkaClientProperty
	int getRequestTimeoutMS();

	/**
	 * @see CommonClientConfigs#RETRY_BACKOFF_MS_DOC
	 */
	@Name(RETRY_BACKOFF_MS_CONFIG)
	@LongDefault(100)
	@Format(MillisFormat.class)
	@KafkaClientProperty
	long getRetryBackoffMS();

	/**
	 * @see SaslConfigs#SASL_CLIENT_CALLBACK_HANDLER_CLASS_DOC
	 */
	@Hidden
	@Name(SASL_CLIENT_CALLBACK_HANDLER_CLASS)
	@KafkaClientProperty
	Class<? extends AuthenticateCallbackHandler> getSaslClientCallbackHandlerClass();

	/**
	 * @see SaslConfigs#SASL_JAAS_CONFIG_DOC
	 */
	@Name(SASL_JAAS_CONFIG)
	@KafkaClientProperty
	@Encrypted
	String getSaslJaasConfig();

	/**
	 * @see SaslConfigs#SASL_KERBEROS_KINIT_CMD_DOC
	 */
	@Name(SASL_KERBEROS_KINIT_CMD)
	@StringDefault("/usr/bin/kinit")
	@KafkaClientProperty
	String getSaslKerberosKinitCmd();

	/**
	 * @see SaslConfigs#SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN_DOC
	 */
	@Name(SASL_KERBEROS_MIN_TIME_BEFORE_RELOGIN)
	@LongDefault(60 * 1000)
	@KafkaClientProperty
	long getSaslKerberosMinTimeBeforeRelogin();

	/**
	 * @see SaslConfigs#SASL_KERBEROS_SERVICE_NAME_DOC
	 */
	@Name(SASL_KERBEROS_SERVICE_NAME)
	@KafkaClientProperty
	String getSaslKerberosServiceName();

	/**
	 * @see SaslConfigs#SASL_KERBEROS_TICKET_RENEW_JITTER_DOC
	 */
	@Name(SASL_KERBEROS_TICKET_RENEW_JITTER)
	@DoubleDefault(0.05)
	@KafkaClientProperty
	double getSaslKerberosTicketRenewJitter();

	/**
	 * @see SaslConfigs#SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR_DOC
	 */
	@Name(SASL_KERBEROS_TICKET_RENEW_WINDOW_FACTOR)
	@DoubleDefault(0.8)
	@KafkaClientProperty
	double getSaslKerberosTicketRenewWindowFactor();

	/**
	 * @see SaslConfigs#SASL_LOGIN_CALLBACK_HANDLER_CLASS_DOC
	 */
	@Hidden
	@Name(SASL_LOGIN_CALLBACK_HANDLER_CLASS)
	@KafkaClientProperty
	Class<? extends AuthenticateCallbackHandler> getSaslLoginCallbackHandlerClass();

	/**
	 * @see SaslConfigs#SASL_LOGIN_CLASS_DOC
	 */
	@Hidden
	@Name(SASL_LOGIN_CLASS)
	@KafkaClientProperty
	Class<? extends Login> getSaslLoginClass();

	/**
	 * @see SaslConfigs#SASL_LOGIN_CONNECT_TIMEOUT_MS_DOC
	 */
	@Name(SASL_LOGIN_CONNECT_TIMEOUT_MS)
	@Format(MillisFormatInt.class)
	@IntDefault(0)
	@KafkaClientProperty
	int getSaslLoginConnectTimeoutMS();

	/**
	 * @see SaslConfigs#SASL_LOGIN_READ_TIMEOUT_MS_DOC
	 */
	@Name(SASL_LOGIN_READ_TIMEOUT_MS)
	@Format(MillisFormatInt.class)
	@IntDefault(0)
	@KafkaClientProperty
	int getSaslLoginReadTimeoutMS();

	/**
	 * @see SaslConfigs#SASL_LOGIN_REFRESH_BUFFER_SECONDS_DOC
	 */
	@Name(SASL_LOGIN_REFRESH_BUFFER_SECONDS)
	@ShortDefault(5 * 60)
	@KafkaClientProperty
	short getSaslLoginRefreshBufferSeconds();

	/**
	 * @see SaslConfigs#SASL_LOGIN_REFRESH_MIN_PERIOD_SECONDS_DOC
	 */
	@Name(SASL_LOGIN_REFRESH_MIN_PERIOD_SECONDS)
	@ShortDefault(60)
	@KafkaClientProperty
	short getSaslLoginRefreshMinPeriodSeconds();

	/**
	 * @see SaslConfigs#SASL_LOGIN_REFRESH_WINDOW_FACTOR_DOC
	 */
	@Name(SASL_LOGIN_REFRESH_WINDOW_FACTOR)
	@DoubleDefault(0.8)
	@KafkaClientProperty
	double getSaslLoginRefreshWindowFactor();

	/**
	 * @see SaslConfigs#SASL_LOGIN_REFRESH_WINDOW_JITTER_DOC
	 */
	@Name(SASL_LOGIN_REFRESH_WINDOW_JITTER)
	@DoubleDefault(0.05)
	@KafkaClientProperty
	double getSaslLoginRefreshWindowJitter();

	/**
	 * @see SaslConfigs#SASL_LOGIN_RETRY_BACKOFF_MAX_MS_DOC
	 */
	@Name(SASL_LOGIN_RETRY_BACKOFF_MAX_MS)
	@LongDefault(10 * 1000)
	@Format(MillisFormat.class)
	@KafkaClientProperty
	long getSaslLoginRetryBackoffMaxMS();

	/**
	 * @see SaslConfigs#SASL_LOGIN_RETRY_BACKOFF_MS_DOC
	 */
	@Name(SASL_LOGIN_RETRY_BACKOFF_MS)
	@LongDefault(100)
	@Format(MillisFormat.class)
	@KafkaClientProperty
	long getSaslLoginRetryBackoffMS();

	/**
	 * @see SaslConfigs#SASL_MECHANISM_DOC
	 */
	@Name(SASL_MECHANISM)
	@StringDefault("GSSAPI")
	@KafkaClientProperty
	String getSaslMechanism();

	/**
	 * @see SaslConfigs#SASL_OAUTHBEARER_CLOCK_SKEW_SECONDS_DOC
	 */
	@Name(SASL_OAUTHBEARER_CLOCK_SKEW_SECONDS)
	@IntDefault(SaslConfigs.DEFAULT_SASL_OAUTHBEARER_CLOCK_SKEW_SECONDS)
	@KafkaClientProperty
	int getSaslOauthbearerClockSkewSeconds();

	/**
	 * @see SaslConfigs#SASL_OAUTHBEARER_EXPECTED_AUDIENCE_DOC
	 */
	@Name(SASL_OAUTHBEARER_EXPECTED_AUDIENCE)
	@KafkaClientProperty
	String getSaslOauthbearerExpectedAudience();

	/**
	 * @see SaslConfigs#SASL_OAUTHBEARER_EXPECTED_ISSUER_DOC
	 */
	@Name(SASL_OAUTHBEARER_EXPECTED_ISSUER)
	@KafkaClientProperty
	String getSaslOauthbearerExpectedIssuer();

	/**
	 * @see SaslConfigs#SASL_OAUTHBEARER_JWKS_ENDPOINT_URL_DOC
	 */
	@Name(SASL_OAUTHBEARER_JWKS_ENDPOINT_URL)
	@KafkaClientProperty
	String getSaslOauthbearerJwksEndpointUrl();

	/**
	 * @see SaslConfigs#SASL_OAUTHBEARER_JWKS_ENDPOINT_REFRESH_MS_DOC
	 */
	@Name(SASL_OAUTHBEARER_JWKS_ENDPOINT_REFRESH_MS)
	@LongDefault(SaslConfigs.DEFAULT_SASL_OAUTHBEARER_JWKS_ENDPOINT_REFRESH_MS)
	@Format(MillisFormat.class)
	@KafkaClientProperty
	Long getSaslOauthbearerJwksEndpointRefreshMS();

	/**
	 * @see SaslConfigs#SASL_OAUTHBEARER_JWKS_ENDPOINT_RETRY_BACKOFF_MAX_MS_DOC
	 */
	@Name(SASL_OAUTHBEARER_JWKS_ENDPOINT_RETRY_BACKOFF_MAX_MS)
	@LongDefault(SaslConfigs.DEFAULT_SASL_OAUTHBEARER_JWKS_ENDPOINT_RETRY_BACKOFF_MAX_MS)
	@Format(MillisFormat.class)
	@KafkaClientProperty
	Long getSaslOauthbearerJwksEndpointRetryBackoffMaxMS();

	/**
	 * @see SaslConfigs#SASL_OAUTHBEARER_JWKS_ENDPOINT_RETRY_BACKOFF_MS_DOC
	 */
	@Name(SASL_OAUTHBEARER_JWKS_ENDPOINT_RETRY_BACKOFF_MS)
	@LongDefault(SaslConfigs.DEFAULT_SASL_OAUTHBEARER_JWKS_ENDPOINT_RETRY_BACKOFF_MS)
	@Format(MillisFormat.class)
	@KafkaClientProperty
	Long getSaslOauthbearerJwksEndpointRetryBackoffMS();

	/**
	 * @see SaslConfigs#SASL_OAUTHBEARER_SCOPE_CLAIM_NAME_DOC
	 */
	@Name(SASL_OAUTHBEARER_SCOPE_CLAIM_NAME)
	@StringDefault(SaslConfigs.DEFAULT_SASL_OAUTHBEARER_SCOPE_CLAIM_NAME)
	@KafkaClientProperty
	String getSaslOauthbearerScopeClaimName();

	/**
	 * @see SaslConfigs#SASL_OAUTHBEARER_SUB_CLAIM_NAME_DOC
	 */
	@Name(SASL_OAUTHBEARER_SUB_CLAIM_NAME)
	@StringDefault(SaslConfigs.DEFAULT_SASL_OAUTHBEARER_SUB_CLAIM_NAME)
	@KafkaClientProperty
	String getSaslOauthbearerSubClaimName();

	/**
	 * @see SaslConfigs#SASL_OAUTHBEARER_TOKEN_ENDPOINT_URL_DOC
	 */
	@Name(SASL_OAUTHBEARER_TOKEN_ENDPOINT_URL)
	@KafkaClientProperty
	String getSaslOauthbearerTokenEndpointUrl();

	/**
	 * @see CommonClientConfigs#SECURITY_PROTOCOL_DOC
	 */
	@Name(SECURITY_PROTOCOL_CONFIG)
	@StringDefault("PLAINTEXT")
	@KafkaClientProperty
	String getSecurityProtocol();

	/**
	 * The Kafka configuration property: "security.providers"
	 */
	@Name(SECURITY_PROVIDERS)
	@KafkaClientProperty
	String getSecurityProviders();

	/**
	 * @see CommonClientConfigs#SEND_BUFFER_DOC
	 */
	@Name(SEND_BUFFER_CONFIG)
	@IntDefault(128 * 1024)
	@KafkaClientProperty
	int getSendBufferBytes();

	/**
	 * @see CommonClientConfigs#SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_DOC
	 */
	@Name(SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG)
	@LongDefault(30 * 1000)
	@Format(MillisFormat.class)
	@KafkaClientProperty
	long getSocketConnectionSetupTimeoutMaxMS();

	/**
	 * @see CommonClientConfigs#SOCKET_CONNECTION_SETUP_TIMEOUT_MS_DOC
	 */
	@Name(SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG)
	@LongDefault(10 * 1000)
	@Format(MillisFormat.class)
	@KafkaClientProperty
	long getSocketConnectionSetupTimeoutMS();

	/**
	 * @see SslConfigs#SSL_CIPHER_SUITES_DOC
	 */
	@Name(SSL_CIPHER_SUITES_CONFIG)
	@KafkaClientProperty
	String getSslCipherSuites();

	/**
	 * @see SslConfigs#SSL_ENABLED_PROTOCOLS_DOC
	 */
	@Name(SSL_ENABLED_PROTOCOLS_CONFIG)
	@StringDefault("TLSv1.2,TLSv1.3")
	@KafkaClientProperty
	String getSslEnabledProtocols();

	/**
	 * @see SslConfigs#SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_DOC
	 */
	@Name(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG)
	@StringDefault("https")
	@KafkaClientProperty
	String getSslEndpointIdentificationAlgorithm();

	/**
	 * @see SslConfigs#SSL_ENGINE_FACTORY_CLASS_DOC
	 */
	@Hidden
	@Name(SSL_ENGINE_FACTORY_CLASS_CONFIG)
	@KafkaClientProperty
	Class<? extends SslEngineFactory> getSslEngineFactoryClass();

	/**
	 * @see SslConfigs#SSL_KEY_PASSWORD_DOC
	 */
	@Name(SSL_KEY_PASSWORD_CONFIG)
	@KafkaClientProperty
	@Encrypted
	String getSslKeyPassword();

	/**
	 * @see SslConfigs#SSL_KEYMANAGER_ALGORITHM_DOC
	 */
	@Name(SSL_KEYMANAGER_ALGORITHM_CONFIG)
	@StringDefault("SunX509")
	@KafkaClientProperty
	String getSslKeymanagerAlgorithm();

	/**
	 * @see SslConfigs#SSL_KEYSTORE_CERTIFICATE_CHAIN_DOC
	 */
	@Name(SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG)
	@KafkaClientProperty
	@Encrypted
	String getSslKeystoreCertificateChain();

	/**
	 * @see SslConfigs#SSL_KEYSTORE_KEY_DOC
	 */
	@Name(SSL_KEYSTORE_KEY_CONFIG)
	@KafkaClientProperty
	@Encrypted
	String getSslKeystoreKey();

	/**
	 * @see SslConfigs#SSL_KEYSTORE_LOCATION_DOC
	 */
	@Name(SSL_KEYSTORE_LOCATION_CONFIG)
	@KafkaClientProperty
	String getSslKeystoreLocation();

	/**
	 * @see SslConfigs#SSL_KEYSTORE_PASSWORD_DOC
	 */
	@Name(SSL_KEYSTORE_PASSWORD_CONFIG)
	@KafkaClientProperty
	@Encrypted
	String getSslKeystorePassword();

	/**
	 * @see SslConfigs#SSL_KEYSTORE_TYPE_DOC
	 */
	@Name(SSL_KEYSTORE_TYPE_CONFIG)
	@StringDefault(SslConfigs.DEFAULT_SSL_KEYSTORE_TYPE)
	@KafkaClientProperty
	String getSslKeystoreType();

	/**
	 * @see SslConfigs#SSL_PROTOCOL_DOC
	 */
	@Name(SSL_PROTOCOL_CONFIG)
	@StringDefault("TLSv1.3")
	@KafkaClientProperty
	String getSslProtocol();

	/**
	 * @see SslConfigs#SSL_PROVIDER_DOC
	 */
	@Name(SSL_PROVIDER_CONFIG)
	@KafkaClientProperty
	String getSslProvider();

	/**
	 * @see SslConfigs#SSL_SECURE_RANDOM_IMPLEMENTATION_DOC
	 */
	@Name(SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG)
	@KafkaClientProperty
	String getSslSecureRandomImplementation();

	/**
	 * @see SslConfigs#SSL_TRUSTMANAGER_ALGORITHM_DOC
	 */
	@Name(SSL_TRUSTMANAGER_ALGORITHM_CONFIG)
	@StringDefault("PKIX")
	@KafkaClientProperty
	String getSslTrustmanagerAlgorithm();

	/**
	 * @see SslConfigs#SSL_TRUSTSTORE_CERTIFICATES_DOC
	 */
	@Name(SSL_TRUSTSTORE_CERTIFICATES_CONFIG)
	@KafkaClientProperty
	@Encrypted
	String getSslTruststoreCertificates();

	/**
	 * @see SslConfigs#SSL_TRUSTSTORE_LOCATION_DOC
	 */
	@Name(SSL_TRUSTSTORE_LOCATION_CONFIG)
	@KafkaClientProperty
	String getSslTruststoreLocation();

	/**
	 * @see SslConfigs#SSL_TRUSTSTORE_PASSWORD_DOC
	 */
	@Name(SSL_TRUSTSTORE_PASSWORD_CONFIG)
	@KafkaClientProperty
	@Encrypted
	String getSslTruststorePassword();

	/**
	 * @see SslConfigs#SSL_TRUSTSTORE_TYPE_DOC
	 */
	@Name(SSL_TRUSTSTORE_TYPE_CONFIG)
	@StringDefault(SslConfigs.DEFAULT_SSL_TRUSTSTORE_TYPE)
	@KafkaClientProperty
	String getSslTruststoreType();

	/**
	 * The map of Kafka configuration properties.
	 * <p>
	 * Properties which have dedicated TypedConfiguration properties must not be used here.
	 * </p>
	 */
	@Hidden
	@Name(UNTYPED_PROPERTIES)
	@MapBinding(tag = "property", key = "name")
	@Label("Additional properties")
	Map<String, String> getUntypedProperties();

	/**
	 * The {@link KafkaLogWriter} to use for logging the messages.
	 * 
	 * @implNote In a {@link Producer}, the "message" is {@link ProducerRecord#value()}. In a
	 *           {@link ConsumerDispatcher}, it is {@link ConsumerRecord#value()}.
	 */
	@Name(LOG_WRITER)
	@ImplementationClassDefault(KafkaNoContentsLogWriter.class)
	@ItemDefault
	@NonNullable
	PolymorphicConfiguration<KafkaLogWriter<? super V>> getLogWriter();

	/**
	 * The Kafka {@link Properties} to be used for {@link KafkaCommonClient} instantiation. A new,
	 * mutable and resizable {@link Map}.
	 */
	@Hidden
	default Map<String, Object> getAllProperties() {
		Map<String, Object> properties = getTypedProperties();
		properties.putAll(getUntypedProperties());
		return properties;
	}

	/**
	 * The properties with an explicit {@link PropertyDescriptor} in the {@link ConfigurationItem}.
	 * <p>
	 * If a property is not set, the {@link Map} will contain its default value. If the (default)
	 * value is null, the {@link Map} will {@link Map#containsKey(Object) contain} the key and
	 * value.
	 * </p>
	 * 
	 * @return The Kafka {@link Properties} to be used for {@link KafkaCommonClient} instantiation. A
	 *         new, mutable and resizable {@link Map}.
	 */
	@Hidden
	default Map<String, Object> getTypedProperties() {
		Map<String, Object> properties = map();
		for (PropertyDescriptor property : descriptor().getProperties()) {
			if (property.getAnnotation(KafkaClientProperty.class) == null) {
				continue;
			}
			if (!valueSet(property) && !property.hasExplicitDefault()) {
				// Do not add null values to Kafka properties that just represent unset
				// configuration values.
				continue;
			}
			Object value = value(property);
			properties.put(property.getPropertyName(), value);
		}
		return properties;
	}

}
