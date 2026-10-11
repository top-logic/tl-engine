/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.ai.a2a.A2AService.ExposedAgentConfig;
import com.top_logic.ai.a2a.protocol.ArtifactRef;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.ai.a2a.server.A2ARequestHandler;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.util.TLContextManager;

/**
 * The HTTP side of {@link A2AService}.
 *
 * <ul>
 * <li>{@code GET /.well-known/agent-card.json}, {@code GET /a2a/{agent}/agent-card.json} (also below
 * {@code .well-known/}) - agent cards, public; the first form names the first exposed agent.</li>
 * <li>{@code POST /a2a/{agent}} - JSON-RPC 2.0, a credential required.</li>
 * <li>{@code GET /a2a-artifacts?ref=...&section=...} - an artifact by reference, a credential
 * required.</li>
 * </ul>
 */
public class A2AServlet extends HttpServlet {

	private static final String CARD = "agent-card.json";

	private static final String WELL_KNOWN_CARD = "/.well-known/" + CARD;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		A2AService service = A2AService.getInstance();
		if (service == null) {
			error(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The A2A service is not running.");
			return;
		}
		String path = path(req);
		if (path.equals("/a2a-artifacts")) {
			artifact(service, req, resp);
			return;
		}
		ExposedAgentConfig agent;
		if (path.equals(WELL_KNOWN_CARD)) {
			agent = service.getConfig().getAgents().stream().findFirst().orElse(null);
		} else if (path.startsWith("/a2a/") && path.endsWith("/" + CARD)) {
			// An SDK resolves the card below the agent's URL: /a2a/{agent}/.well-known/agent-card.json.
			String rest = path.substring("/a2a/".length()).replace(WELL_KNOWN_CARD, "/" + CARD);
			agent = service.exposedAgent(rest.substring(0, rest.length() - CARD.length() - 1));
		} else {
			agent = null;
		}
		if (agent == null) {
			error(resp, HttpServletResponse.SC_NOT_FOUND, "No such A2A agent.");
			return;
		}
		String host = req.getHeader("Host");
		String base = req.getScheme() + "://" + (host == null ? req.getServerName() + ":" + req.getServerPort() : host)
			+ req.getContextPath();
		json(resp, HttpServletResponse.SC_OK, Json.write(service.agentCard(agent, base)));
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		A2AService service = A2AService.getInstance();
		if (service == null) {
			error(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The A2A service is not running.");
			return;
		}
		String path = path(req);
		String agent = path.startsWith("/a2a/") ? path.substring("/a2a/".length()) : "";
		if (agent.isEmpty() || agent.contains("/")) {
			error(resp, HttpServletResponse.SC_NOT_FOUND, "POST to /a2a/{agent}.");
			return;
		}
		Person caller = authenticate(service, req, resp);
		if (caller == null) {
			return;
		}
		String body = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		service.handler().handle(body, req.getHeader("A2A-Version"), caller.getName(), agent, new Transport(resp));
	}

	/**
	 * The account the request's credential names, or <code>null</code> after the refusal was sent.
	 *
	 * <p>
	 * Resolving a credential reads the {@code KnowledgeBase}, so it runs in a system interaction - and
	 * nothing else of the request does: the agent runs act as the resolved account on their own
	 * threads. A credential that names no account is refused, as on the MCP endpoint.
	 * </p>
	 */
	private static Person authenticate(A2AService service, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Person person;
		try {
			person = ThreadContextManager.<Person, AuthenticationFailure, RuntimeException> inSystemInteraction(
				A2AServlet.class, () -> service.authenticator().authenticate(req, resp));
		} catch (AuthenticationFailure ex) {
			Logger.info("Refused an A2A request to " + req.getRequestURI() + ": " + ex.getMessage(), A2AServlet.class);
			ex.enhanceResponse(resp, req.getRequestURI());
			return null;
		}
		if (person == null) {
			error(resp, HttpServletResponse.SC_FORBIDDEN, "The credential names no account.");
		}
		return person;
	}

	/**
	 * {@code GET /a2a-artifacts}: one artifact of this application, or one section of it, read in the
	 * caller's {@code TLContext}, so an {@link A2AArtifactSource} reads with the caller's rights.
	 */
	private static void artifact(A2AService service, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Person caller = authenticate(service, req, resp);
		if (caller == null) {
			return;
		}
		ArtifactRef ref = ArtifactRef.parse(req.getParameter("ref"));
		if (ref == null) {
			error(resp, HttpServletResponse.SC_BAD_REQUEST, "Parameter ref must be tl-artifact://<app>/<kind>/<id>.");
			return;
		}
		Map<String, Object> answer = TLContextManager.<Map<String, Object>, RuntimeException, RuntimeException> inPersonContext(
			caller, () -> A2AService.artifactResponse(service.resolveArtifact(ref, caller.getName()), ref,
				req.getParameter("section")));
		json(resp, answer.containsKey("error") ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_OK,
			Json.write(answer));
	}

	private static String path(HttpServletRequest req) {
		return (req.getServletPath() == null ? "" : req.getServletPath())
			+ (req.getPathInfo() == null ? "" : req.getPathInfo());
	}

	private static void json(HttpServletResponse resp, int status, String json) throws IOException {
		byte[] body = json.getBytes(StandardCharsets.UTF_8);
		resp.setStatus(status);
		resp.setContentType("application/json;charset=UTF-8");
		resp.setContentLength(body.length);
		resp.getOutputStream().write(body);
	}

	private static void error(HttpServletResponse resp, int status, String message) throws IOException {
		json(resp, status, Json.write(Json.map("error", message)));
	}

	/**
	 * Hands the JSON-RPC handler's answers to the HTTP response.
	 */
	private record Transport(HttpServletResponse resp) implements A2ARequestHandler.Transport {

		@Override
		public void sendJson(String json) throws IOException {
			json(resp, HttpServletResponse.SC_OK, json);
		}

		@Override
		public A2ARequestHandler.EventSink openStream() throws IOException {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("text/event-stream;charset=UTF-8");
			resp.setHeader("Cache-Control", "no-cache");
			resp.setHeader("X-Accel-Buffering", "no");
			OutputStream out = resp.getOutputStream();
			resp.flushBuffer();
			return new A2ARequestHandler.EventSink() {
				@Override
				public void send(String data) throws IOException {
					write("data: " + data + "\n\n");
				}

				@Override
				public void keepAlive() throws IOException {
					write(": keepalive\n\n");
				}

				private void write(String text) throws IOException {
					out.write(text.getBytes(StandardCharsets.UTF_8));
					out.flush();
				}
			};
		}
	}
}
