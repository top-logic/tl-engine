/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.util.List;

import com.nimbusds.oauth2.sdk.token.AccessTokenType;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.misc.AlwaysFalse;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.common.util.OpenAPIConfigs;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.conf.EndpointCredential;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;

/**
 * Authenticates a caller by an access token of the application's own
 * {@link OAuthAuthorizationServer}, sent in the header <code>Authorization: Bearer</code>.
 *
 * <p>
 * The request acts as the person who authorized the client. A token is accepted only at the
 * {@link Config#getResource() resource} it was issued for, so that a token a client obtained for
 * one endpoint cannot be replayed at another. See the developer article <code>oauth-server</code>.
 * </p>
 *
 * <p>
 * A client needs no configured credential: a refusal names the protected resource metadata of the
 * endpoint ({@link ProtectedResources}), from which the client learns the authorization server and
 * signs the person in.
 * </p>
 */
@Label("OAuth access token of this application")
public class OAuthAccessTokenAuthentication
		extends AbstractConfiguredInstance<OAuthAccessTokenAuthentication.Config<?>>
		implements ServerAuthentication<OAuthAccessTokenAuthentication.Config<?>>, EndpointCredential.Provider {

	/**
	 * Configuration options for {@link OAuthAccessTokenAuthentication}.
	 */
	@TagName("oauth-access-token-authentication")
	public interface Config<I extends OAuthAccessTokenAuthentication> extends ServerAuthentication.Config<I> {

		/**
		 * Configuration name of {@link #getResource()}.
		 */
		String RESOURCE = "resource";

		/**
		 * The resource (RFC 8707) the guarded endpoint is, which a token must have been issued
		 * for.
		 *
		 * <p>
		 * Either an absolute URI, or a path relative to the application's context path starting
		 * with a slash, e.g. <code>/mcp</code>, which is resolved against the application URL as
		 * the request reaches it.
		 * </p>
		 */
		@Name(RESOURCE)
		@Mandatory
		String getResource();

		/**
		 * Never: the tokens are issued by the application itself, not configured.
		 */
		@Override
		@Derived(fun = AlwaysFalse.class, args = {})
		boolean isSeparateSecretNeeded();

	}

	/**
	 * Creates a {@link OAuthAccessTokenAuthentication} from configuration.
	 */
	@CalledByReflection
	public OAuthAccessTokenAuthentication(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public SecuritySchemeObject createSchemaObject(String schemaName) {
		SecuritySchemeObject securityScheme = OpenAPIConfigs.newHTTPAuthentication(schemaName);
		securityScheme.setScheme(AccessTokenType.BEARER.getValue());
		securityScheme.setUserContext(true, null);
		return securityScheme;
	}

	/**
	 * A credential the client obtains by itself: it finds the authorization server in the
	 * protected resource metadata of the endpoint and signs the person in through the browser.
	 */
	@Override
	public EndpointCredential endpointCredential() {
		String metadataPath = ProtectedResources.metadataPath(getConfig().getResource());
		return EndpointCredential.bySignIn(I18NConstants.CREDENTIAL_SIGN_IN,
			context -> I18NConstants.CREDENTIAL_SIGN_IN_OBTAIN__URL.fill(context.url(metadataPath)));
	}

	/**
	 * Creates the authenticator and announces the {@link Config#getResource() resource} in
	 * {@link ProtectedResources}, so that its metadata is served from now on.
	 */
	@Override
	public Authenticator createAuthenticator(List<? extends SecretConfiguration> availableSecrets) {
		String resource = getConfig().getResource();
		ProtectedResources.register(resource);
		return new OAuthAccessTokenAuthenticator(resource);
	}

}
