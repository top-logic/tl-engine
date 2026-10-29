/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.access;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeySecret;
import com.top_logic.service.openapi.common.authentication.oauth.CredentialSecret;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.apikey.ServerAPIKeySecret;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;
import com.top_logic.service.openapi.server.authentication.impl.NeverAuthenticated;
import com.top_logic.service.openapi.server.authentication.oauth.ServerCredentialSecret;

/**
 * Builds the {@link Authenticator} that guards an agent endpoint ({@code /mcp/*}, {@code /a2a/*})
 * from its service's configuration.
 *
 * <p>
 * The mechanism is not decided here. TopLogic already ships a pluggable, {@code Person}-resolving
 * HTTP authentication framework - HTTP basic against the account's authentication device, an API key
 * mapped to an account, and an OAuth bearer token validated by RFC 7662 introspection - and this
 * module adds personal access tokens ({@link AccessTokenAuthentication}). So an application configures
 * {@code <authentications>} and {@code <secrets>} on the endpoint's service exactly the way it already
 * configures them on its <i>OpenAPI</i> server, and the choice becomes a per-deployment one.
 * </p>
 *
 * <h2>Nothing configured means nothing served</h2>
 *
 * <p>
 * <b>An empty {@code <authentications>} yields {@link NeverAuthenticated}.</b> The <i>OpenAPI</i>
 * server defaults an operation that declares no authentication to
 * {@code AlwaysAuthenticated} - a REST path an administrator wrote out by hand is one they meant to
 * publish. An agent endpoint is the opposite case: the path exists because the module is on the
 * classpath, nobody wrote it out, and what answers there reads the {@code KnowledgeBase}. So the
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
 */
public final class EndpointAuthentication {

	private EndpointAuthentication() {
		// Static utility.
	}

	/**
	 * The {@link Authenticator} for an endpoint.
	 *
	 * @param endpoint
	 *        The path the authenticator guards, e.g. {@code /mcp/*}, for the messages.
	 * @param authentications
	 *        The configured authentication schemes by name. May be empty.
	 * @param secrets
	 *        The configured secrets; converted to the engine's secret types here, see
	 *        {@link EndpointSecret}.
	 * @return An {@link Authenticator} that refuses everything when {@code authentications} is
	 *         empty. Never <code>null</code>.
	 */
	public static Authenticator build(String endpoint, Map<String, ServerAuthentication.Config<?>> authentications,
			List<? extends EndpointSecret> secrets) {
		if (authentications.isEmpty()) {
			// 503 rather than 401: there is no scheme to challenge the caller with, and no credential
			// the caller could send would change the answer.
			return new NeverAuthenticated(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
				I18NConstants.ERROR_NO_AUTHENTICATION_CONFIGURED__ENDPOINT.fill(endpoint));
		}

		List<SecretConfiguration> serverSecrets = serverSecrets(secrets);
		Authenticator result = null;
		for (Map.Entry<String, ServerAuthentication.Config<?>> entry : authentications.entrySet()) {
			Authenticator next = create(endpoint, entry.getKey(), entry.getValue(), serverSecrets);
			result = result == null ? next : result.or(next);
		}
		return result;
	}

	/**
	 * The engine's secrets for the given endpoint secrets.
	 *
	 * <p>
	 * The engine's authentications look for their own secret types ({@link ServerAPIKeySecret},
	 * {@link ServerCredentialSecret}), which cannot be configured on an endpoint's service - see
	 * {@link EndpointSecret}. The copies stand in no configuration, so nothing restricts their container.
	 * </p>
	 */
	private static List<SecretConfiguration> serverSecrets(List<? extends EndpointSecret> secrets) {
		List<SecretConfiguration> result = new ArrayList<>(secrets.size());
		for (EndpointSecret secret : secrets) {
			result.add(serverSecret(secret));
		}
		return result;
	}

	private static SecretConfiguration serverSecret(EndpointSecret secret) {
		if (secret instanceof EndpointSecret.ApiKey apiKey) {
			ServerAPIKeySecret result = TypedConfiguration.newConfigItem(ServerAPIKeySecret.class);
			TypedConfigUtil.setProperty(result, APIKeySecret.DOMAIN, apiKey.getDomain());
			TypedConfigUtil.setProperty(result, APIKeySecret.API_KEY, TypedConfiguration.copy(apiKey.getAPIKey()));
			TypedConfigUtil.setProperty(result, ServerAPIKeySecret.USER_ID, apiKey.getUserId());
			TypedConfigUtil.setProperty(result, APIKeySecret.DESCRIPTION, apiKey.getDescription());
			return result;
		}
		if (secret instanceof EndpointSecret.ClientCredentials credentials) {
			ServerCredentialSecret result = TypedConfiguration.newConfigItem(ServerCredentialSecret.class);
			TypedConfigUtil.setProperty(result, CredentialSecret.DOMAIN, credentials.getDomain());
			TypedConfigUtil.setProperty(result, CredentialSecret.CLIENT_SECRET,
				TypedConfiguration.copy(credentials.getClientSecret()));
			return result;
		}
		throw new IllegalArgumentException("Unknown kind of endpoint secret: " + secret);
	}

	/**
	 * The account the request's credential names, in a context that can look one up.
	 *
	 * <p>
	 * <b>Resolving a credential is itself a {@code KnowledgeBase} read</b> - every authenticator ends
	 * in an account lookup by login name - and a servlet request thread carries no TopLogic
	 * interaction, so one has to be opened around it. It is a <em>system</em> interaction because at
	 * this point there is no account to open it as; that is the one thing a system interaction is
	 * legitimately for here, and it is closed again before anything of the request is served. What
	 * runs inside it is the authenticator and nothing else. The login name is read here for the same
	 * reason.
	 * </p>
	 *
	 * @param requester
	 *        The class that resolves, named on the interaction.
	 * @return The caller, or <code>null</code> if the credential authenticated but names no account -
	 *         which an endpoint refuses, because it never acts in system context.
	 * @throws AuthenticationFailure
	 *         With the scheme's own challenge, see {@link AuthenticationFailure#enhanceResponse}.
	 */
	public static Caller resolve(Authenticator authenticator, HttpServletRequest req, HttpServletResponse resp,
			Class<?> requester) throws AuthenticationFailure {
		return ThreadContext.<Caller, AuthenticationFailure, RuntimeException> inSystemContext(requester,
			() -> {
				Person account = authenticator.authenticate(req, resp);
				return account == null ? null : new Caller(account, account.getName());
			});
	}

	/**
	 * An authenticated account and its login name, carried alongside because reading the name is a
	 * {@code KnowledgeBase} operation and is needed where no interaction is bound.
	 *
	 * @param account
	 *        The authenticated account.
	 * @param name
	 *        The login name of {@code account}.
	 */
	public record Caller(Person account, String name) {
		// Pure value.
	}

	private static Authenticator create(String endpoint, String name, ServerAuthentication.Config<?> config,
			List<? extends SecretConfiguration> secrets) {
		try {
			return TypedConfigUtil.createInstance(config).createAuthenticator(secrets);
		} catch (RuntimeException ex) {
			// Not rethrown: one broken authentication must not take the whole application down, and
			// not skipped either - a skipped entry would leave the endpoint open to whatever the
			// remaining entries accept, which is not what the administrator configured.
			Logger.error("Cannot create the authentication '" + name + "' of " + endpoint
				+ "; it will authenticate nobody.", ex, EndpointAuthentication.class);
			return new NeverAuthenticated(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				I18NConstants.ERROR_AUTHENTICATION_UNAVAILABLE__DOMAIN_ENDPOINT.fill(name, endpoint));
		}
	}
}
