/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Admission of non-browser callers to the {@link AgentServlet}.
 *
 * <p>
 * A browser reaches the endpoint with its session cookie. An agent outside the browser presents a
 * credential in the {@code Authorization} header instead, which the registered
 * {@link AgentAuthenticator}s resolve to the account it acts as.
 * </p>
 *
 * <p>
 * A credential also names the session the agent works in: the user issues it from a running session
 * and thereby lets the agent drive that session's windows. The session is registered here under a
 * key the credential carries, and the registration ends with the session — an agent cannot outlive
 * the session it was admitted to.
 * </p>
 *
 * <p>
 * With no authenticator registered, only browsers reach the endpoint.
 * </p>
 */
@Label("Agent access")
public class AgentAccess extends ConfiguredManagedClass<AgentAccess.Config> {

	/**
	 * The HTTP header a caller presents its credential in.
	 */
	public static final String AUTHORIZATION_HEADER = "Authorization";

	/**
	 * The scheme prefix of the {@link #AUTHORIZATION_HEADER} value.
	 */
	public static final String BEARER_PREFIX = "Bearer ";

	/**
	 * Configuration of {@link AgentAccess}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<AgentAccess> {

		/**
		 * The authenticators recognizing the credentials of non-browser callers, asked in order.
		 */
		@Name("authenticators")
		List<AgentAuthenticator.Config> getAuthenticators();

	}

	private final List<AgentAuthenticator> _authenticators;

	private final Map<String, HttpSession> _sessions = new ConcurrentHashMap<>();

	/**
	 * Creates an {@link AgentAccess} service from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The service configuration.
	 */
	@CalledByReflection
	public AgentAccess(InstantiationContext context, Config config) {
		super(context, config);
		_authenticators = TypedConfiguration.getInstanceList(context, config.getAuthenticators());
	}

	/**
	 * Resolves the credential the given request presents, if any.
	 *
	 * @param request
	 *        The request of a potential agent caller.
	 * @return The admitted caller, or <code>null</code> if the request presents no credential or
	 *         none of the {@link Config#getAuthenticators() authenticators} recognizes it.
	 */
	public AgentCredential authenticate(HttpServletRequest request) {
		String header = request.getHeader(AUTHORIZATION_HEADER);
		if (header == null || !header.startsWith(BEARER_PREFIX)) {
			return null;
		}
		String credential = header.substring(BEARER_PREFIX.length()).trim();
		if (StringServices.isEmpty(credential)) {
			return null;
		}
		for (AgentAuthenticator authenticator : _authenticators) {
			AgentCredential result = authenticator.authenticate(credential);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Registers the given session as reachable by an agent under the given key.
	 *
	 * <p>
	 * Called when a credential for that session is issued. The registration is dropped when the
	 * session ends.
	 * </p>
	 *
	 * @param key
	 *        The key a credential refers to the session by.
	 * @param session
	 *        The session the agent acts in.
	 */
	public void bindSession(String key, HttpSession session) {
		_sessions.put(key, session);
		session.setAttribute(SessionCleanup.ATTRIBUTE + key, new SessionCleanup(this, key));
	}

	/**
	 * The session registered under the given key.
	 *
	 * @param key
	 *        The key from an {@link AgentCredential#sessionKey()}.
	 * @return The session, or <code>null</code> if it has ended or was never registered.
	 */
	public HttpSession session(String key) {
		HttpSession session = _sessions.get(key);
		if (session == null) {
			return null;
		}
		try {
			// An invalidated session may still be reachable through the map when its unbinding has
			// not run yet; touching it reveals that.
			session.getCreationTime();
		} catch (IllegalStateException ex) {
			_sessions.remove(key);
			return null;
		}
		return session;
	}

	/**
	 * Drops the registration of the session under the given key.
	 *
	 * @param key
	 *        The key the session was {@link #bindSession(String, HttpSession) registered} under.
	 */
	public void unbindSession(String key) {
		_sessions.remove(key);
	}

	/**
	 * The {@link AgentAccess} service.
	 */
	public static AgentAccess getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Ends an agent's reach into a session when that session ends.
	 */
	private static final class SessionCleanup implements HttpSessionBindingListener {

		/** Prefix of the session attribute holding a cleanup listener. */
		static final String ATTRIBUTE = SessionCleanup.class.getName() + ".";

		private final AgentAccess _access;

		private final String _key;

		SessionCleanup(AgentAccess access, String key) {
			_access = access;
			_key = key;
		}

		@Override
		public void valueUnbound(HttpSessionBindingEvent event) {
			_access.unbindSession(_key);
		}
	}

	/**
	 * Module for {@link AgentAccess}.
	 */
	public static final class Module extends TypedRuntimeModule<AgentAccess> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<AgentAccess> getImplementation() {
			return AgentAccess.class;
		}
	}

}
