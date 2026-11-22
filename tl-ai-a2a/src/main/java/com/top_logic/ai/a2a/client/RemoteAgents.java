/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.top_logic.ai.a2a.A2AService;
import com.top_logic.ai.a2a.A2AService.RemoteAgentConfig;
import com.top_logic.ai.a2a.protocol.A2AError;
import com.top_logic.ai.a2a.protocol.ArtifactRef;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.ai.core.tools.web.UrlSafety;

/**
 * The remote A2A agents this application may call, with their agent cards cached.
 *
 * <p>
 * The client reads the card of a remote agent without credentials, so who can change the card can
 * name any address in it. The calls carry the credentials of the agent and the messages of the
 * caller, so they go only to an allowed origin of the agent: the origin of its card URL and the
 * origins of its {@link RemoteAgentConfig#getEndpointOrigins() endpoint origins}. An origin is
 * scheme, host and port, so this also stops a card read over https from moving the calls to http.
 * </p>
 */
public final class RemoteAgents {

	private static final long CARD_TTL_MILLIS = 60_000;

	private final Map<String, RemoteAgent> _agents = new LinkedHashMap<>();

	private final A2AClient _client = new A2AClient();

	/**
	 * A remote agent and its cached card.
	 */
	public static final class RemoteAgent {

		private final String _name;

		private final String _cardUrl;

		/** The {@link RemoteAgents#origin(String) origins} that may get the calls to this agent. */
		private final Set<String> _allowedOrigins = new HashSet<>();

		private final CredentialProvider _credentials;

		private Map<String, Object> _card;

		private long _cardTime;

		/**
		 * Creates a {@link RemoteAgent}.
		 *
		 * @param endpointOrigins
		 *        The origins that the card may name besides the origin of the card URL, see
		 *        {@link RemoteAgentConfig#getEndpointOrigins()}.
		 * @param credentials
		 *        Authenticates the calls to the agent.
		 */
		public RemoteAgent(String name, String cardUrl, Collection<String> endpointOrigins,
				CredentialProvider credentials) {
			_name = name;
			_cardUrl = cardUrl;
			_credentials = credentials;
			_allowedOrigins.add(origin(cardUrl));
			for (String endpointOrigin : endpointOrigins) {
				_allowedOrigins.add(origin(endpointOrigin));
			}
			// What has no origin allows no address.
			_allowedOrigins.remove(null);
		}

		/** The configured name. */
		public String name() {
			return _name;
		}

		/**
		 * Authenticates the calls to this agent.
		 */
		public CredentialProvider credentials() {
			return _credentials;
		}

		/**
		 * The display name from the card if known, else the configured name.
		 */
		public synchronized String displayName() {
			String name = Json.str(_card, "name");
			return name == null ? _name : name;
		}

		synchronized Map<String, Object> card(A2AClient client) throws IOException, InterruptedException {
			if (_card == null || System.currentTimeMillis() - _cardTime > CARD_TTL_MILLIS) {
				Map<String, Object> card = Json.asObject(client.getJson(_cardUrl, A2AClient.NO_CREDENTIALS, false));
				if (card == null) {
					throw new A2AError(A2AError.INVALID_AGENT_RESPONSE, "The agent card of '" + _name + "' is no object.");
				}
				_card = card;
				_cardTime = System.currentTimeMillis();
			}
			return _card;
		}

		/**
		 * Checks that an address of the card of this agent is at an allowed origin.
		 *
		 * @throws A2AError
		 *         If it is not. Then no request was sent.
		 */
		void checkOrigin(String address) {
			if (!_allowedOrigins.contains(origin(address))) {
				throw new A2AError(A2AError.INVALID_AGENT_RESPONSE, "The agent card of '" + _name
					+ "' names the address " + address + ", which is neither at the origin of its card URL " + _cardUrl
					+ " nor at one of its endpoint-origins. The calls to a remote agent go only to these origins"
					+ " (scheme, host and port), so no request was sent.");
			}
		}
	}

	/**
	 * Creates the registry from configuration.
	 *
	 * @param context
	 *        The context for instantiating the credentials of the agents. It gets an error for an
	 *        entry of the endpoint origins that is no origin.
	 */
	public RemoteAgents(InstantiationContext context, List<? extends RemoteAgentConfig> configs) {
		for (RemoteAgentConfig config : configs) {
			warnIfUnencrypted(config.getName(), config.getCardUrl());
			List<String> endpointOrigins = new ArrayList<>();
			for (String entry : config.getEndpointOrigins()) {
				if (isOrigin(entry)) {
					warnIfUnencrypted(config.getName(), entry);
					endpointOrigins.add(entry);
				} else {
					context.error("The entry '" + entry + "' of the endpoint-origins of the remote agent '"
						+ config.getName() + "' is no origin. Write scheme://host or scheme://host:port, with no"
						+ " path, query, fragment or user info, e.g. https://agents.example.com.");
				}
			}
			CredentialProvider credentials = config.getCredentials() == null ? A2AClient.NO_CREDENTIALS
				: context.getInstance(config.getCredentials());
			add(new RemoteAgent(config.getName(), config.getCardUrl(), endpointOrigins, credentials));
		}
	}

	private static void warnIfUnencrypted(String agent, String address) {
		if (isUnencrypted(address)) {
			Logger.warn("The calls to the remote agent '" + agent + "' can go over http to a host that is not a"
				+ " loopback host: " + address + ". Then its credentials and the messages go out without"
				+ " encryption. Use https.", RemoteAgents.class);
		}
	}

	/**
	 * The origin of an address: scheme and host in lower case, the host without a trailing dot, and
	 * the port, also where the address leaves out the default port of http or https.
	 *
	 * @return The origin as {@code scheme://host:port}, or <code>null</code> if the address is no
	 *         absolute URL with a host.
	 */
	static String origin(String address) {
		URI uri = parse(address);
		String host = uri == null ? null : UrlSafety.normalizeHost(uri.getHost());
		if (host == null || host.isEmpty() || uri.getScheme() == null) {
			return null;
		}
		String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
		int port = uri.getPort() >= 0 ? uri.getPort() : "https".equals(scheme) ? 443 : "http".equals(scheme) ? 80 : -1;
		return scheme + "://" + host + ":" + port;
	}

	/**
	 * Whether an entry of the endpoint origins is an origin only: a scheme and a host, perhaps a
	 * port, nothing more.
	 */
	static boolean isOrigin(String entry) {
		URI uri = parse(entry);
		return origin(entry) != null && uri.getRawUserInfo() == null && uri.getRawPath().isEmpty()
			&& uri.getRawQuery() == null && uri.getRawFragment() == null;
	}

	/**
	 * Whether the calls to an address go over http to a host that is not a loopback host, so that they
	 * cross a network without encryption.
	 */
	static boolean isUnencrypted(String address) {
		URI uri = parse(address);
		String host = uri == null ? null : UrlSafety.normalizeHost(uri.getHost());
		return host != null && "http".equalsIgnoreCase(uri.getScheme()) && !isLoopbackHost(host);
	}

	/**
	 * Whether a host is {@code localhost} or a loopback address. Another name is not looked up: a
	 * name lookup at the start can block, and its answer can change later.
	 */
	private static boolean isLoopbackHost(String host) {
		if (host.equals("localhost") || host.matches("127(\\.[0-9]{1,3}){3}")) {
			return true;
		}
		if (host.startsWith("[")) {
			try {
				// An IPv6 literal in brackets is parsed without a name lookup.
				return InetAddress.getByName(host).isLoopbackAddress();
			} catch (UnknownHostException ex) {
				return false;
			}
		}
		return false;
	}

	/**
	 * The address as the HTTP client reads it, or <code>null</code> if it is no URI.
	 */
	private static URI parse(String address) {
		try {
			return URI.create(address);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	/**
	 * Adds a remote agent.
	 */
	public void add(RemoteAgent agent) {
		_agents.put(agent.name(), agent);
	}

	/** The client. */
	public A2AClient client() {
		return _client;
	}

	/** All remote agents. */
	public List<RemoteAgent> all() {
		return new ArrayList<>(_agents.values());
	}

	/** The configured names. */
	public List<String> names() {
		return new ArrayList<>(_agents.keySet());
	}

	/**
	 * The remote agent by name or display name, ignoring case.
	 *
	 * @throws A2AError
	 *         If there is none.
	 */
	public RemoteAgent get(String name) {
		for (RemoteAgent agent : _agents.values()) {
			if (agent.name().equalsIgnoreCase(name.trim()) || agent.displayName().equalsIgnoreCase(name.trim())) {
				return agent;
			}
		}
		throw new A2AError(A2AError.INVALID_PARAMS,
			"Unknown remote agent '" + name + "'. Known: " + String.join(", ", names()) + ".");
	}

	/**
	 * The agent card of a remote agent, fetched at most once a minute.
	 */
	public Map<String, Object> card(RemoteAgent agent) throws IOException, InterruptedException {
		return agent.card(_client);
	}

	/**
	 * The JSON-RPC endpoint the card of a remote agent names.
	 *
	 * @throws A2AError
	 *         If the card names no JSON-RPC endpoint, or one that is not at an allowed origin of the
	 *         agent.
	 */
	public String endpoint(RemoteAgent agent) throws IOException, InterruptedException {
		for (Object entry : Json.list(card(agent), "supportedInterfaces")) {
			String url = Json.str(entry, "url");
			if ("JSONRPC".equalsIgnoreCase(Json.str(entry, "protocolBinding")) && url != null) {
				agent.checkOrigin(url);
				return url;
			}
		}
		throw new A2AError(A2AError.INVALID_AGENT_RESPONSE, "The agent card of '" + agent.name()
			+ "' names no JSON-RPC interface.");
	}

	/**
	 * Fetches an artifact of another application by reference, from the artifact endpoint the card of
	 * a remote agent of that application names.
	 *
	 * @return The JSON answer of the artifact endpoint.
	 * @throws A2AError
	 *         If the card of that application names an artifact endpoint that is not at an allowed
	 *         origin of its agent.
	 */
	public Map<String, Object> fetchArtifact(ArtifactRef ref, String section) throws IOException, InterruptedException {
		for (RemoteAgent agent : _agents.values()) {
			String url;
			try {
				url = artifactUrl(card(agent), ref.app());
			} catch (IOException | RuntimeException ex) {
				// An unreachable peer cannot be the one asked for.
				continue;
			}
			if (url != null) {
				// Outside the try above: a foreign address ends the fetch, it is no unreachable peer.
				agent.checkOrigin(url);
				String query = url + "?ref=" + URLEncoder.encode(ref.toString(), StandardCharsets.UTF_8)
					+ (section == null || section.isBlank() ? ""
						: "&section=" + URLEncoder.encode(section.trim(), StandardCharsets.UTF_8));
				Map<String, Object> answer = Json.asObject(_client.getJson(query, agent.credentials(), true));
				if (answer == null) {
					throw new A2AError(A2AError.INVALID_AGENT_RESPONSE, "The artifact endpoint of '" + agent.name()
						+ "' answered no JSON object.");
				}
				return answer;
			}
		}
		throw new A2AError(A2AError.INVALID_PARAMS, "No remote agent here belongs to the application '" + ref.app()
			+ "', so " + ref + " cannot be fetched.");
	}

	private static String artifactUrl(Map<String, Object> card, String app) {
		for (Object extension : Json.list(Json.obj(card, "capabilities"), "extensions")) {
			Map<String, Object> params = Json.obj(extension, "params");
			if (A2AService.ARTIFACTS_EXTENSION.equals(Json.str(extension, "uri"))
				&& app.equalsIgnoreCase(Json.str(params, "app"))) {
				return Json.str(params, "url");
			}
		}
		return null;
	}
}
