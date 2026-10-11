/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.client;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.a2a.A2AService;
import com.top_logic.ai.a2a.A2AService.RemoteAgentConfig;
import com.top_logic.ai.a2a.protocol.A2AError;
import com.top_logic.ai.a2a.protocol.ArtifactRef;
import com.top_logic.ai.a2a.protocol.Json;

/**
 * The remote A2A agents this application may call, with their agent cards cached.
 */
public final class RemoteAgents {

	private static final long CARD_TTL_MILLIS = 60_000;

	private final Map<String, RemoteAgent> _agents = new LinkedHashMap<>();

	private final A2AClient _client = new A2AClient();

	/**
	 * A remote agent.
	 *
	 * @param tokenEnv
	 *        The environment variable or system property holding the bearer token, or
	 *        <code>null</code>.
	 */
	public record RemoteAgent(String name, String cardUrl, String tokenEnv, Card card) {

		/**
		 * Creates a {@link RemoteAgent} whose card is not fetched yet.
		 */
		public RemoteAgent(String name, String cardUrl, String tokenEnv) {
			this(name, cardUrl, tokenEnv, new Card());
		}

		/**
		 * The bearer token calls to this agent carry, or <code>null</code>.
		 */
		public String token() {
			if (tokenEnv == null || tokenEnv.isBlank()) {
				return null;
			}
			String value = System.getenv(tokenEnv.trim());
			return value != null && !value.isBlank() ? value.trim() : System.getProperty(tokenEnv.trim());
		}

		/**
		 * The display name from the card if known, else the configured name.
		 */
		public String displayName() {
			String name = Json.str(card.cached(), "name");
			return name == null ? this.name : name;
		}
	}

	/**
	 * The cached agent card of a remote agent.
	 */
	public static final class Card {

		private Map<String, Object> _card;

		private long _time;

		synchronized Map<String, Object> cached() {
			return _card;
		}

		synchronized Map<String, Object> get(A2AClient client, RemoteAgent agent) throws IOException, InterruptedException {
			if (_card == null || System.currentTimeMillis() - _time > CARD_TTL_MILLIS) {
				Map<String, Object> card = Json.asObject(client.getJson(agent.cardUrl(), null, false));
				if (card == null) {
					throw new A2AError(A2AError.INVALID_AGENT_RESPONSE, "The agent card of '" + agent.name() + "' is no object.");
				}
				_card = card;
				_time = System.currentTimeMillis();
			}
			return _card;
		}
	}

	/**
	 * Creates the registry from configuration.
	 */
	public RemoteAgents(List<? extends RemoteAgentConfig> configs) {
		for (RemoteAgentConfig config : configs) {
			add(new RemoteAgent(config.getName(), config.getCardUrl(), config.getTokenEnv()));
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
		return agent.card().get(_client, agent);
	}

	/**
	 * The JSON-RPC endpoint the card of a remote agent names.
	 */
	public String endpoint(RemoteAgent agent) throws IOException, InterruptedException {
		for (Object entry : Json.list(card(agent), "supportedInterfaces")) {
			if ("JSONRPC".equalsIgnoreCase(Json.str(entry, "protocolBinding")) && Json.str(entry, "url") != null) {
				return Json.str(entry, "url");
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
				String query = url + "?ref=" + URLEncoder.encode(ref.toString(), StandardCharsets.UTF_8)
					+ (section == null || section.isBlank() ? ""
						: "&section=" + URLEncoder.encode(section.trim(), StandardCharsets.UTF_8));
				Map<String, Object> answer = Json.asObject(_client.getJson(query, agent.token(), true));
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
