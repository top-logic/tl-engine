/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.authentication;

import com.top_logic.ai.mcp.server.MCPServerService;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeySecret;
import com.top_logic.service.openapi.common.authentication.oauth.CredentialSecret;
import com.top_logic.service.openapi.server.authentication.apikey.ServerAPIKeySecret;
import com.top_logic.service.openapi.server.authentication.apikey.ServerAPIKeySecret.AllAPIKeyDomains;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentications;
import com.top_logic.service.openapi.server.authentication.oauth.ServerCredentialSecret;
import com.top_logic.service.openapi.server.authentication.oauth.ServerCredentialSecret.AllServerCredentialsDomains;
import com.top_logic.service.openapi.server.layout.AllUsers;
import com.top_logic.service.openapi.server.layout.UserNameMapping;

/**
 * A secret a caller of {@code /mcp/*} authenticates with, configured on
 * {@link MCPServerService.Config#getSecrets()}.
 *
 * <p>
 * The engine's own secret types ({@link ServerAPIKeySecret}, {@link ServerCredentialSecret}) may
 * only stand in the configuration of the <i>OpenAPI</i> server: their container property is typed
 * with that configuration, and the configuration framework refuses them anywhere else (issue #672).
 * So the MCP service has these types, with the same tag names and properties, and
 * {@link McpAuthentication} turns them into the engine's types when it builds the authenticator. A
 * configuration written for the <i>OpenAPI</i> server can therefore be copied unchanged.
 * </p>
 *
 * <p>
 * There is no counterpart of the engine's {@code basic-auth-server-secret}: HTTP basic with a
 * technical login processes a request in system context, which {@code /mcp/*} refuses. HTTP basic
 * against the account's own password needs no secret.
 * </p>
 */
@Abstract
public interface McpSecret extends SecretConfiguration, ConfigPart {

	/**
	 * @see #getOwner()
	 */
	String OWNER = "owner";

	/**
	 * The configuration of the MCP service this secret belongs to.
	 */
	@Container
	@Name(OWNER)
	@Hidden
	MCPServerService.Config<?> getOwner();

	/**
	 * An API key, and the account a request with it acts as.
	 *
	 * <p>
	 * An {@code <api-key-authentication>} of the same domain checks the key.
	 * </p>
	 *
	 * @see ServerAPIKeySecret
	 */
	@DisplayOrder({
		APIKeySecret.DOMAIN,
		APIKeySecret.API_KEY,
		ApiKey.USER_ID,
		APIKeySecret.DESCRIPTION
	})
	@TagName("api-key-server-secret")
	@Label("API key")
	interface ApiKey extends McpSecret, APIKeySecret {

		/**
		 * @see #getUserId()
		 */
		String USER_ID = ServerAPIKeySecret.USER_ID;

		@Override
		@Options(fun = AllAPIKeyDomains.class,
			args = { @Ref({ McpSecret.OWNER, ServerAuthentications.AUTHENTICATIONS }) })
		String getDomain();

		/**
		 * The login name of the account a request with this key acts as.
		 *
		 * <p>
		 * Required in practice: for a key without an account, the framework processes the request
		 * in system context, and {@code /mcp/*} refuses such a request.
		 * </p>
		 */
		@Name(USER_ID)
		@Nullable
		@Options(fun = AllUsers.class, mapping = UserNameMapping.class)
		String getUserId();

	}

	/**
	 * The client credentials the MCP service introspects OAuth bearer tokens with.
	 *
	 * <p>
	 * A {@code <client-credentials-authentication>} of the same domain uses them.
	 * </p>
	 *
	 * @see ServerCredentialSecret
	 */
	@DisplayOrder({
		CredentialSecret.DOMAIN,
		CredentialSecret.CLIENT_SECRET
	})
	@TagName("client-credentials-server-secret")
	@Label("OpenID authentication")
	interface ClientCredentials extends McpSecret, CredentialSecret {

		@Override
		@Options(fun = AllServerCredentialsDomains.class,
			args = { @Ref({ McpSecret.OWNER, ServerAuthentications.AUTHENTICATIONS }) })
		String getDomain();

	}

}
