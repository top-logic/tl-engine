/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

import com.top_logic.ai.mcp.server.authentication.McpSessionPrincipals;
import com.top_logic.basic.Logger;

import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStreamableServerSession;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Pings the sessions of the MCP transport, and removes a session that does not answer and that no
 * client has used for a while.
 *
 * <p>
 * <b>Why this exists.</b> The MCP Java SDK (1.1.4) pings every session at the keep-alive interval
 * and, when a ping fails, only logs a warning. A session goes away only when its client deletes it
 * ({@code DELETE /mcp}) or when a write to its event stream fails. A client that sends its
 * requests by {@code POST} and never opens the event stream - a script, a command-line tool - can
 * never answer a ping, and most such clients never delete their session either. Their sessions
 * therefore stayed until the application stopped, and each one wrote a warning at every interval: in
 * an evaluation of Pietro (#917, Pietro #738) ten such sessions wrote 1,200 warnings an hour.
 * </p>
 *
 * <p>
 * <b>The rule.</b> At every interval each session is pinged. A session that answers stays. A session
 * that does not answer is removed when no request named it for {@link #idleTimeout()}; until then it
 * stays, without a warning. It is not removed at its first failed ping, because a client that only
 * posts cannot answer a ping even while it is working. A removed session is closed and leaves the
 * transport and {@link McpSessionPrincipals}; the next request of its client is answered
 * {@code 404}, which tells an MCP client to start a new session.
 * </p>
 *
 * <p>
 * <b>Why this reaches into the SDK.</b> The transport keeps its sessions in a private map and
 * offers no way to remove one but the client's {@code DELETE}. {@link #attach} reads that map once;
 * should an SDK upgrade remove it, the keeper is not started, a warning says so, and
 * {@code TestMcpSessionKeeper} fails. Delete this class when the SDK removes sessions that do not
 * answer by itself.
 * </p>
 */
final class McpSessionKeeper {

	private static final TypeRef<Object> ANY = new TypeRef<>() {
		// Whatever the client answers a ping with.
	};

	/** The transport's own sessions, by session id. */
	private final Map<String, McpStreamableServerSession> _sessions;

	/** When a request last named a session, by session id. */
	private final Map<String, Long> _lastRequest = new ConcurrentHashMap<>();

	private final Duration _idleTimeout;

	private final McpSessionPrincipals _principals;

	private final LongSupplier _clock;

	private Disposable _schedule;

	private McpSessionKeeper(Map<String, McpStreamableServerSession> sessions, Duration idleTimeout,
			McpSessionPrincipals principals, LongSupplier clock) {
		_sessions = sessions;
		_idleTimeout = idleTimeout;
		_principals = principals;
		_clock = clock;
	}

	/**
	 * A keeper of the sessions of the given transport, or <code>null</code> when the SDK does not
	 * hold its sessions where this class reads them.
	 *
	 * @param idleTimeout
	 *        How long a session that does not answer a ping stays after the last request that named it.
	 * @param principals
	 *        The owners of the sessions, from which a removed session is released.
	 * @param clock
	 *        The current time in milliseconds.
	 */
	static McpSessionKeeper attach(HttpServletStreamableServerTransportProvider transport, Duration idleTimeout,
			McpSessionPrincipals principals, LongSupplier clock) {
		try {
			Field field = HttpServletStreamableServerTransportProvider.class.getDeclaredField("sessions");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<String, McpStreamableServerSession> sessions =
				(Map<String, McpStreamableServerSession>) field.get(transport);
			return new McpSessionKeeper(sessions, idleTimeout, principals, clock);
		} catch (ReflectiveOperationException | RuntimeException ex) {
			Logger.warn("MCP sessions that no client uses any more are not removed: the MCP SDK holds its"
				+ " sessions in a way this version does not know (" + ex + "). Each such session stays until"
				+ " the application stops.", McpSessionKeeper.class);
			return null;
		}
	}

	/**
	 * How long a session that does not answer a ping stays after the last request that named it.
	 */
	Duration idleTimeout() {
		return _idleTimeout;
	}

	/**
	 * Notes that a request named the given session, or opened it.
	 *
	 * @param sessionId
	 *        The {@code mcp-session-id} of the request or of its answer; <code>null</code> is ignored.
	 */
	void noteRequest(String sessionId) {
		if (sessionId != null) {
			_lastRequest.put(sessionId, Long.valueOf(_clock.getAsLong()));
		}
	}

	/**
	 * Checks the sessions at every interval, on a thread of its own, until {@link #stop()}.
	 */
	void start(Duration interval) {
		_schedule = Flux.interval(interval, interval, Schedulers.boundedElastic())
			.subscribe(tick -> checkAll(), ex -> Logger.error("The MCP session keeper stopped.", ex,
				McpSessionKeeper.class));
	}

	/**
	 * Stops the checks {@link #start(Duration)} began.
	 */
	void stop() {
		Disposable schedule = _schedule;
		if (schedule != null) {
			schedule.dispose();
			_schedule = null;
		}
	}

	/**
	 * Pings every session once, and removes those that do not answer and are idle. Returns when all
	 * pings are answered or failed.
	 */
	void checkAll() {
		_lastRequest.keySet().retainAll(_sessions.keySet());
		Flux.fromIterable(Map.copyOf(_sessions).entrySet())
			.flatMap(entry -> ping(entry.getKey(), entry.getValue()))
			.then()
			.block();
	}

	private Mono<Void> ping(String sessionId, McpStreamableServerSession session) {
		// A session the keeper sees before any request named it counts from now.
		_lastRequest.putIfAbsent(sessionId, Long.valueOf(_clock.getAsLong()));
		return session.sendRequest(McpSchema.METHOD_PING, null, ANY)
			.then()
			.onErrorResume(ex -> {
				unanswered(sessionId, session, ex);
				return Mono.empty();
			});
	}

	private void unanswered(String sessionId, McpStreamableServerSession session, Throwable ex) {
		Long last = _lastRequest.get(sessionId);
		long idle = _clock.getAsLong() - (last == null ? 0 : last.longValue());
		if (idle < _idleTimeout.toMillis()) {
			Logger.debug("MCP session " + sessionId + " did not answer a ping: " + ex.getMessage(),
				McpSessionKeeper.class);
			return;
		}
		if (!_sessions.remove(sessionId, session)) {
			return;
		}
		_lastRequest.remove(sessionId);
		_principals.release(sessionId);
		session.closeGracefully().onErrorResume(error -> Mono.empty()).subscribe();
		Logger.info("Removed MCP session " + sessionId + ": it did not answer a ping, and no request named it"
			+ " for " + Duration.ofMillis(idle).toMinutes() + " minutes.", McpSessionKeeper.class);
	}

}
