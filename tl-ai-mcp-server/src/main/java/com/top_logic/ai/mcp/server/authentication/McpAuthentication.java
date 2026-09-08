/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.authentication;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;
import com.top_logic.service.openapi.server.authentication.impl.NeverAuthenticated;

/**
 * Builds the {@link Authenticator} that guards {@code /mcp/*} from the service configuration.
 *
 * <p>
 * The mechanism is not decided here. TopLogic already ships a pluggable, {@code Person}-resolving
 * HTTP authentication framework - HTTP basic against the account's authentication device, an API key
 * mapped to an account, and an OAuth bearer token validated by RFC 7662 introspection - so an
 * application configures {@code <authentications>} and {@code <secrets>} on the MCP service exactly
 * the way it already configures them on its <i>OpenAPI</i> server, and the choice becomes a
 * per-deployment one.
 * </p>
 *
 * <h2>Nothing configured means nothing served</h2>
 *
 * <p>
 * <b>An empty {@code <authentications>} yields {@link NeverAuthenticated}.</b> The <i>OpenAPI</i>
 * server defaults an operation that declares no authentication to
 * {@code AlwaysAuthenticated} - a REST path an administrator wrote out by hand is one they meant to
 * publish. {@code /mcp/*} is the opposite case: the path exists because the module is on the
 * classpath, nobody wrote it out, and its resource handlers read the {@code KnowledgeBase}. So the
 * default here is the other one, and it is the single decision this class exists to hold. Getting it
 * backwards reopens issue #261.
 * </p>
 *
 * <p>
 * The several configured authentications are combined with {@link Authenticator#or(Authenticator)},
 * so a caller may present any one of them - the same composition the <i>OpenAPI</i> server uses for
 * an operation that accepts several schemes. An authentication that cannot be built (a missing
 * secret, say) contributes a {@link NeverAuthenticated} rather than being skipped, so a
 * misconfiguration cannot widen access.
 * </p>
 *
 * @see McpAuthenticationFilter
 */
public final class McpAuthentication {

	private McpAuthentication() {
		// Static utility.
	}

	/**
	 * The {@link Authenticator} for {@code /mcp/*}.
	 *
	 * @param authentications
	 *        The configured authentication schemes by name, from
	 *        {@code MCPServerService.Config.getAuthentications()}. May be empty.
	 * @param secrets
	 *        The configured secrets, from {@code MCPServerService.Config.getSecrets()}.
	 * @return An {@link Authenticator} that refuses everything when {@code authentications} is
	 *         empty. Never <code>null</code>.
	 */
	public static Authenticator build(Map<String, ServerAuthentication.Config<?>> authentications,
			List<? extends SecretConfiguration> secrets) {
		if (authentications.isEmpty()) {
			return refuseEverything();
		}

		Authenticator result = null;
		for (Map.Entry<String, ServerAuthentication.Config<?>> entry : authentications.entrySet()) {
			Authenticator next = create(entry.getKey(), entry.getValue(), secrets);
			result = result == null ? next : result.or(next);
		}
		return result;
	}

	/**
	 * The {@link Authenticator} of a deployment that configured none.
	 *
	 * <p>
	 * Answers {@code 503} rather than {@code 401}: there is no scheme to challenge the caller with,
	 * and no credential the caller could send would change the answer. The message names what an
	 * administrator has to configure.
	 * </p>
	 */
	public static Authenticator refuseEverything() {
		return new NeverAuthenticated(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
			I18NConstants.ERROR_NO_AUTHENTICATION_CONFIGURED);
	}

	private static Authenticator create(String name, ServerAuthentication.Config<?> config,
			List<? extends SecretConfiguration> secrets) {
		try {
			return TypedConfigUtil.createInstance(config).createAuthenticator(secrets);
		} catch (RuntimeException ex) {
			// Not rethrown: one broken authentication must not take the whole application down, and
			// not skipped either - a skipped entry would leave the endpoint open to whatever the
			// remaining entries accept, which is not what the administrator configured.
			Logger.error("Cannot create the MCP authentication '" + name + "'; it will authenticate"
				+ " nobody.", ex, McpAuthentication.class);
			return new NeverAuthenticated(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				I18NConstants.ERROR_AUTHENTICATION_UNAVAILABLE__DOMAIN.fill(name));
		}
	}
}
