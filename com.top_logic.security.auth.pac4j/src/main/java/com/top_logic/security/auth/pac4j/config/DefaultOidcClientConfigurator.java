/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config;

import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.ServletContext;

import org.pac4j.core.client.Client;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.util.generator.ValueGenerator;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * Base class for {@link ClientConfigurator}s allowing to configure common properties.
 * 
 * @see #createRawClient()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultOidcClientConfigurator<C extends DefaultOidcClientConfigurator.Config<?>>
		extends AbstractConfiguredInstance<C> implements ClientConfigurator {

	/**
	 * Configuration options for {@link DefaultOidcClientConfigurator}.
	 */
	public interface Config<I extends DefaultOidcClientConfigurator<?>>
			extends ClientConfigurator.Config<I>, HasCallbackUrl {

		/**
		 * @see OidcConfiguration#getClientAuthenticationMethod()
		 * @see ClientAuthenticationMethod#CLIENT_SECRET_BASIC The default method to use, if none is
		 *      explicitly configured.
		 */
		@Name("client-authentication-method")
		@Nullable
		public String getClientAuthenticationMethod();

		/**
		 * @see OidcConfiguration#getClientId()
		 */
		@Name("client-id")
		@Mandatory
		public String getClientId();

		/**
		 * @see OidcConfiguration#getConnectTimeout()
		 */
		@Name("connect-timeout")
		@IntDefault(HttpConstants.DEFAULT_CONNECT_TIMEOUT)
		public int getConnectTimeout();

		/**
		 * URL to retrieve {@link #getProviderMetadata()} from.
		 * 
		 * <p>
		 * If not set, {@link #getProviderMetadata()} must be configured explicitly.
		 * </p>
		 * 
		 * @see OidcConfiguration#getDiscoveryURI()
		 */
		@Name("discovery-uri")
		@Nullable
		public String getDiscoveryURI();

		/**
		 * @see OidcConfiguration#getLogoutUrl()
		 */
		@Name("logout-url")
		@Nullable
		public String getLogoutUrl();

		/**
		 * @see OidcConfiguration#getMaxAge()
		 */
		@Name("max-age")
		public Integer getMaxAge();

		/**
		 * @see OidcConfiguration#getMaxClockSkew()
		 */
		@Name("max-clock-skew")
		@IntDefault(OidcConfiguration.DEFAULT_MAX_CLOCK_SKEW)
		public int getMaxClockSkew();

		/**
		 * @see OidcConfiguration#getPreferredJwsAlgorithm()
		 * @see JWSAlgorithm#parse(String)
		 * @see JWSAlgorithm#RS256 The default algorithm to use, if none is configured.
		 */
		@Name("preferred-jws-algorithm")
		@Nullable
		@StringDefault("RS256")
		public String getPreferredJwsAlgorithm();

		/**
		 * @see OidcConfiguration#getResourceRetriever()
		 */
		@Name("resource-retriever")
		@InstanceFormat
		public ResourceRetriever getResourceRetriever();

		/**
		 * @see OidcConfiguration#getResponseMode()
		 */
		@Name("response-mode")
		@Nullable
		public String getResponseMode();

		/**
		 * @see OidcConfiguration#getResponseType()
		 */
		@Name("response-type")
		@StringDefault("code")
		public String getResponseType();

		/**
		 * @see OidcConfiguration#getScope()
		 */
		@Name("scope")
		@Nullable
		public String getScope();

		/**
		 * @see OidcConfiguration#getSecret()
		 */
		@Name("secret")
		@Mandatory
		@Encrypted
		public String getSecret();

		/**
		 * @see OidcConfiguration#getStateGenerator()
		 */
		@Name("state-generator")
		@Nullable
		@InstanceFormat
		public ValueGenerator getStateGenerator();

		/**
		 * @see OidcConfiguration#isUseNonce()
		 */
		@Name("use-nonce")
		public boolean getUseNonce();

		/**
		 * @see OidcConfiguration#isWithState()
		 */
		@Name("with-state")
		public boolean getWithState();

		/**
		 * @see OidcConfiguration#getReadTimeout()
		 */
		@Name("read-timeout")
		@IntDefault(HttpConstants.DEFAULT_READ_TIMEOUT)
		public int getReadTimeout();

		/**
		 * @see OidcConfiguration#getCustomParams()
		 */
		@Name("custom-params")
		@MapBinding
		public Map<String, String> getCustomParams();

		/**
		 * The provider detail configuration in JSON format.
		 * 
		 * <p>
		 * If not set, {@link #getDiscoveryURI()} must be set to dynamically retrieve the provider
		 * configuration.
		 * </p>
		 * 
		 * @see OidcConfiguration#getProviderMetadata()
		 */
		@Name("provider-metadata")
		@Format(ProviderFormat.class)
		public OIDCProviderMetadata getProviderMetadata();

		/**
		 * Parser for {@link OIDCProviderMetadata} JSON format.
		 */
		class ProviderFormat extends AbstractConfigurationValueProvider<OIDCProviderMetadata> {

			/**
			 * Creates a {@link ProviderFormat}.
			 */
			public ProviderFormat() {
				super(OIDCProviderMetadata.class);
			}

			@Override
			protected OIDCProviderMetadata getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				try {
					return OIDCProviderMetadata.parse(propertyValue.toString());
				} catch (ParseException ex) {
					throw new ConfigurationException(
						I18NConstants.INVALID_FORMAT__DETAILS.fill(ex.getMessage()),
						propertyName, propertyValue, ex);
				}
			}

			@Override
			protected String getSpecificationNonNull(OIDCProviderMetadata configValue) {
				return configValue.toJSONObject().toJSONString();
			}
		}

	}

	/**
	 * Creates a {@link DefaultOidcClientConfigurator} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultOidcClientConfigurator(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public final Client createClient(ServletContext context) {
		OidcClient result = createRawClient();

		result.setName(getConfig().getName());
		C config = getConfig();
		result.setCallbackUrl(Pac4jConfigFactory.resolveCallbackUrl(context, config));
		result.setUrlResolver(Pac4jConfigFactory.createUrlResolver(config));

		return result;
	}

	/**
	 * Implementation of {@link #createClient(ServletContext)}.
	 * 
	 * @see #buildConfig()
	 */
	protected OidcClient createRawClient() {
		return createClientImpl(buildConfig());
	}

	/**
	 * Creates the client implementation form the given configuration.
	 */
	protected OidcClient createClientImpl(OidcConfiguration clientConfig) {
		return new OidcClient(clientConfig);
	}

	/**
	 * Creates a filled {@link Client} configuration from the application configuration.
	 */
	protected final OidcConfiguration buildConfig() {
		OidcConfiguration odic = createEmptyConfig();
		fillConfig(odic);
		return odic;
	}

	/**
	 * Creates an empty {@link OidcClient} configuration implementation object.
	 */
	protected OidcConfiguration createEmptyConfig() {
		return new OidcConfiguration();
	}

	/**
	 * Fills the given {@link OidcClient} configuration object with values from the application
	 * configuration.
	 */
	protected void fillConfig(OidcConfiguration odic) {
		Config<?> config = getConfig();
		String clientAuthenticationMethod = config.getClientAuthenticationMethod();
		if (clientAuthenticationMethod != null) {
			odic.setClientAuthenticationMethodAsString(clientAuthenticationMethod);
		}
		odic.setClientId(config.getClientId());
		odic.setConnectTimeout(config.getConnectTimeout());
		for (Entry<String, String> entry : config.getCustomParams().entrySet()) {
			odic.addCustomParam(entry.getKey(), entry.getValue());
		}
		odic.setDiscoveryURI(config.getDiscoveryURI());
		odic.setLogoutUrl(config.getLogoutUrl());
		odic.setMaxAge(config.getMaxAge());
		odic.setMaxClockSkew(config.getMaxClockSkew());
		String preferredJwsAlgorithm = config.getPreferredJwsAlgorithm();
		if (preferredJwsAlgorithm != null) {
			odic.setPreferredJwsAlgorithm(JWSAlgorithm.parse(preferredJwsAlgorithm));
		}
		OIDCProviderMetadata providerMetadata = config.getProviderMetadata();
		if (providerMetadata != null) {
			odic.setProviderMetadata(providerMetadata);
		}
		odic.setReadTimeout(config.getReadTimeout());
		odic.setResourceRetriever(config.getResourceRetriever());
		odic.setResponseMode(config.getResponseMode());
		odic.setResponseType(config.getResponseType());
		odic.setScope(config.getScope());
		odic.setSecret(config.getSecret());
		ValueGenerator stateGenerator = config.getStateGenerator();
		if (stateGenerator != null) {
			odic.setStateGenerator(stateGenerator);
		}
		odic.setUseNonce(config.getUseNonce());
		odic.setWithState(config.getWithState());
	}

}
