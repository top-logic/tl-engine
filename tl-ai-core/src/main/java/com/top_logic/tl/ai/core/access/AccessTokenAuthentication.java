/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.access;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.misc.AlwaysFalse;
import com.top_logic.element.accesstoken.AccessTokens;
import com.top_logic.layout.view.ViewServlet;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.common.util.OpenAPIConfigs;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.conf.EndpointCredential;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;

/**
 * Authenticates a caller by a personal access token in the header {@code Authorization: Bearer}, followed by
 * the token.
 *
 * <p>
 * The tokens are not configured: each person creates their own in the application, and the request
 * acts as that person. So this scheme needs no secret - configuring it is the whole decision to
 * accept personal access tokens. See {@link AccessTokens} for the format and the storage, and
 * the developer article {@code ai/security/mcp-endpoint} for how to switch it on.
 * </p>
 */
@Label("Personal access token")
public class AccessTokenAuthentication extends AbstractConfiguredInstance<AccessTokenAuthentication.Config<?>>
		implements ServerAuthentication<AccessTokenAuthentication.Config<?>>, EndpointCredential.Provider {

	/**
	 * The HTTP authentication scheme a personal access token is sent with in the header
	 * {@link EndpointCredential#AUTHORIZATION_HEADER}.
	 */
	public static final String BEARER_SCHEME = "Bearer";

	/**
	 * Configuration options for {@link AccessTokenAuthentication}.
	 */
	@TagName("personal-access-token-authentication")
	public interface Config<I extends AccessTokenAuthentication> extends ServerAuthentication.Config<I> {

		/**
		 * Never: the tokens are created by the persons they belong to, not configured.
		 */
		@Override
		@Derived(fun = AlwaysFalse.class, args = {})
		boolean isSeparateSecretNeeded();

	}

	/**
	 * Creates a {@link AccessTokenAuthentication} from configuration.
	 */
	@CalledByReflection
	public AccessTokenAuthentication(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public SecuritySchemeObject createSchemaObject(String schemaName) {
		SecuritySchemeObject securityScheme = OpenAPIConfigs.newHTTPAuthentication(schemaName);
		securityScheme.setScheme(BEARER_SCHEME);
		securityScheme.setUserContext(true, null);
		return securityScheme;
	}

	@Override
	public EndpointCredential endpointCredential() {
		return new EndpointCredential(I18NConstants.CREDENTIAL_PERSONAL_ACCESS_TOKEN,
			EndpointCredential.AUTHORIZATION_HEADER, I18NConstants.CREDENTIAL_PERSONAL_ACCESS_TOKEN_VALUE,
			context -> I18NConstants.CREDENTIAL_PERSONAL_ACCESS_TOKEN_OBTAIN__URL.fill(context.url(ViewServlet.ROOT_PATH)));
	}

	/**
	 * The value of the header {@link EndpointCredential#AUTHORIZATION_HEADER} presenting the given
	 * token.
	 */
	public static String headerValue(String token) {
		return BEARER_SCHEME + ' ' + token;
	}

	/**
	 * Whether the given authentications of an endpoint accept personal access tokens.
	 */
	public static boolean isAccepted(Collection<? extends ServerAuthentication.Config<?>> authentications) {
		for (ServerAuthentication.Config<?> authentication : authentications) {
			if (authentication instanceof AccessTokenAuthentication.Config<?>) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Authenticator createAuthenticator(List<? extends SecretConfiguration> availableSecrets) {
		return new AccessTokenAuthenticator();
	}

}
