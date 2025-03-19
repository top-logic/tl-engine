/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.http.basic;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Not;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.authentication.http.HTTPAuthentication;
import com.top_logic.service.openapi.common.authentication.http.HTTPSecret;
import com.top_logic.service.openapi.common.authentication.http.LoginCredentials;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.common.util.OpenAPIConfigs;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;
import com.top_logic.service.openapi.server.authentication.impl.NeverAuthenticated;

/**
 * Authentication using HTTP <i>BasicAuth</i> mechanism.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("HTTP authentication (BasicAuth)")
public class BasicAuthentication extends AbstractConfiguredInstance<BasicAuthentication.Config<?>>
		implements ServerAuthentication<BasicAuthentication.Config<?>> {

	/**
	 * Configuration options for {@link BasicAuthentication}.
	 */
	@TagName("basic-authentication")
	public interface Config<I extends BasicAuthentication> extends ServerAuthentication.Config<I>, HTTPAuthentication {

		/**
		 * @see #getInUserContext()
		 */
		String IN_USER_CONTEXT = "in-user-context";

		/**
		 * Whether user's can use the API with their login credentials (user name and password).
		 * 
		 * <p>
		 * With this option checked, no additional secrets must be configured for this
		 * authentication method, since authentication is done against the local password database.
		 * </p>
		 * 
		 * <p>
		 * With this option checked, the API implementation has access to the authenticated user
		 * through the <code>currentUser()</code> function.
		 * </p>
		 */
		@Name(IN_USER_CONTEXT)
		boolean getInUserContext();

		/**
		 * @see #getInUserContext()
		 */
		void setInUserContext(boolean value);

		@Derived(fun = Not.class, args = { @Ref(IN_USER_CONTEXT) })
		@Override
		boolean isSeparateSecretNeeded();

	}

	/**
	 * Creates a {@link BasicAuthentication} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BasicAuthentication(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public SecuritySchemeObject createSchemaObject(String schemaName) {
		SecuritySchemeObject securityScheme = OpenAPIConfigs.newHTTPAuthentication(schemaName);
		securityScheme.setScheme("Basic");
		securityScheme.setUserContext(getConfig().getInUserContext(), null);
		return securityScheme;
	}

	@Override
	public Authenticator createAuthenticator(List<? extends SecretConfiguration> availableSecrets) {
		Config<?> config = getConfig();
		if (config.getInUserContext()) {
			return new BasicUserAuthenticator();
		} else {
			Set<LoginCredentials> authentications =
				OpenAPIConfigs.secretsOfType(config, availableSecrets, HTTPSecret.class)
					.map(HTTPSecret::getLogin)
					.collect(Collectors.toSet());
			if (authentications.isEmpty()) {
				return NeverAuthenticated.missingSecret();
			} else {
				return new BasicTechnicalAuthenticator(authentications);
			}
		}
	}

}

