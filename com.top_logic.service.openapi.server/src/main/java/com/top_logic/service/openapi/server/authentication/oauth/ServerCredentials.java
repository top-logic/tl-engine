/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.oauth;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.layout.meta.HideActiveIfNot;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.authentication.oauth.DefaultURIProvider;
import com.top_logic.service.openapi.common.authentication.oauth.OpenIDURIProvider;
import com.top_logic.service.openapi.common.authentication.oauth.TokenBasedAuthentication;
import com.top_logic.service.openapi.common.authentication.oauth.TokenURIProvider;
import com.top_logic.service.openapi.common.document.OAuthFlow;
import com.top_logic.service.openapi.common.document.OAuthFlowObject;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.common.document.SecuritySchemeType;
import com.top_logic.service.openapi.common.util.OpenAPIConfigs;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;
import com.top_logic.service.openapi.server.authentication.impl.NeverAuthenticated;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link TokenBasedAuthentication} to authenticate by using client credentials or users access
 * token.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("OpenID authentication")
public class ServerCredentials extends AbstractConfiguredInstance<ServerCredentials.Config<?>>
		implements ServerAuthentication<ServerCredentials.Config<?>> {

	/**
	 * Configuration options for {@link ServerCredentials}.
	 */
	@TagName("client-credentials-authentication")
	@DisplayOrder({
		Config.IN_USER_CONTEXT,
		Config.USERNAME_FIELD,
	})
	public interface Config<I extends ServerCredentials>
			extends ServerAuthentication.Config<I>, TokenBasedAuthentication {
		/**
		 * Configuration name for {@link #isInUserContext()}.
		 */
		String IN_USER_CONTEXT = "in-user-context";

		/**
		 * Configuration name for {@link #getUsernameField()}.
		 */
		String USERNAME_FIELD = "username-field";

		/**
		 * Whether the protected operation must be executed in user context.
		 * 
		 * <p>
		 * If this property is set, then it is expected that the sent access token is the personal
		 * token of a person which has an account in the application. The operation is then
		 * processed as if the user has executed it after a login.
		 * </p>
		 */
		@Name(IN_USER_CONTEXT)
		boolean isInUserContext();

		/**
		 * Setter for {@link #isInUserContext()}.
		 */
		void setInUserContext(boolean value);

		/**
		 * Name of the field in the token introspection response which contains the user.
		 * 
		 * <p>
		 * When no field is configured the default field "username" is used to find the user name.
		 * </p>
		 */
		@Name(USERNAME_FIELD)
		@DynamicMode(fun = HideActiveIfNot.class, args = @Ref(IN_USER_CONTEXT))
		@Nullable
		String getUsernameField();

		/**
		 * Setter for {@link #getUsernameField()}.
		 */
		void setUsernameField(String value);
	}

	/**
	 * Creates a {@link ServerCredentials} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ServerCredentials(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public SecuritySchemeObject createSchemaObject(String schemaName) {
		Config<?> config = getConfig();
		SecuritySchemeObject securityScheme = OpenAPIConfigs.newSecuritySchema(schemaName);
		PolymorphicConfiguration<? extends TokenURIProvider> uriProvider = config.getURIProvider();
		if (uriProvider instanceof OpenIDURIProvider.Config) {
			securityScheme.setType(SecuritySchemeType.OPEN_ID_CONNECT);
			securityScheme.setOpenIdConnectUrl(((OpenIDURIProvider.Config) uriProvider).getOpenIDIssuer());
		} else if (uriProvider instanceof DefaultURIProvider.Config) {
			securityScheme.setType(SecuritySchemeType.OAUTH2);
			OAuthFlowObject flowObject = TypedConfiguration.newConfigItem(OAuthFlowObject.class);
			flowObject.setFlow(OAuthFlow.CLIENT_CREDENTIALS);
			flowObject.setTokenUrl(((DefaultURIProvider.Config) uriProvider).getTokenURL().toExternalForm());
			flowObject.setScopes(Collections.emptyMap());
			securityScheme.getFlows().put(flowObject.getFlow(), flowObject);
		} else {
			throw new UnsupportedOperationException();
		}
		if (config.isInUserContext()) {
			securityScheme.setUserContext(true, config.getUsernameField());
		}
		return securityScheme;
	}

	@Override
	public Authenticator createAuthenticator(List<? extends SecretConfiguration> availableSecrets) {
		Config<?> config = getConfig();
		Set<ServerCredentialSecret> secrets =
			OpenAPIConfigs.secretsOfType(config, availableSecrets, ServerCredentialSecret.class)
				.collect(Collectors.toSet());
		switch (secrets.size()) {
			case 0:
				return NeverAuthenticated.missingSecret();
			case 1:
				return new ClientCredentialsAuthenticator(config, secrets.iterator().next());
			default:
				throw new TopLogicException(
					I18NConstants.ERROR_MULTIPLE_CLIENT_CREDENTIAL_SECRETS__DOMAIN.fill(config.getDomain()));
		}
	}
}

