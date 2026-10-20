/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.access;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.misc.AlwaysFalse;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.common.util.OpenAPIConfigs;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;

/**
 * Authenticates a caller by a personal access token in the header {@code Authorization: Bearer}, followed by
 * the token.
 *
 * <p>
 * The tokens are not configured: each person creates their own in the application, and the request
 * acts as that person. So this scheme needs no secret - configuring it is the whole decision to
 * accept personal access tokens. See {@link AccessTokens} for the format and the storage, and
 * {@code doc/security/mcp-endpoint.md} for how to switch it on.
 * </p>
 */
@Label("Personal access token")
public class AccessTokenAuthentication extends AbstractConfiguredInstance<AccessTokenAuthentication.Config<?>>
		implements ServerAuthentication<AccessTokenAuthentication.Config<?>> {

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
		securityScheme.setScheme("Bearer");
		securityScheme.setUserContext(true, null);
		return securityScheme;
	}

	@Override
	public Authenticator createAuthenticator(List<? extends SecretConfiguration> availableSecrets) {
		return new AccessTokenAuthenticator();
	}

}
