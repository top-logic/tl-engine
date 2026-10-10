/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.health;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.provider.ProviderException;

/**
 * What every model of this installation is currently known to be good for.
 *
 * <p>
 * Fed from the classification the providers already produce - a
 * {@link com.top_logic.tl.ai.core.provider.ProviderException.Kind} - and read by
 * {@link com.top_logic.tl.ai.core.agent.AgentLoop} before it calls a model and after a call failed.
 * Three strengths, from {@link HealthCause}:
 * </p>
 *
 * <ul>
 * <li>{@link HealthCause#QUOTA} - the model is not called at all until the long TTL runs out or an
 * administrator resets the mark. A billing lockout or a rejected key is not something to retry
 * into.</li>
 * <li>{@link HealthCause#CAPACITY} - the model is not called for the next few seconds. Rate
 * limiting and overload pass on their own.</li>
 * <li>{@link HealthCause#TRANSIENT} - the model keeps one attempt per turn. A fault nothing
 * recognised is not a reason to route around a model that has been working.</li>
 * </ul>
 *
 * <p>
 * A weaker cause never replaces a stronger mark that still holds: a single unclassified blip during
 * a quota lockout does not turn the lockout into "retry once", and a capacity mark does not shorten
 * it either. Equal causes replace, which refreshes the lifetime of a fault that is still happening.
 * </p>
 *
 * <h2>Why this is not a {@code ManagedClass}</h2>
 *
 * <p>
 * It is a component of {@link com.top_logic.tl.ai.core.provider.LLMProviderService}, reached through
 * {@link com.top_logic.tl.ai.core.provider.LLMProviderService#modelHealth()}, not a service of its own. The
 * state is keyed by exactly the model names that service routes and is configured in the same
 * {@code <failover>} section, so a second {@link com.top_logic.basic.module.ManagedClass} would add
 * a module to the startup graph and a second place that has to agree about model names, and buy
 * nothing. As a plain object it also takes its {@link Clock} from its caller, which is what lets
 * the TTL rules be tested without waiting for them.
 * </p>
 *
 * <p>
 * Thread-safe: every agent run of the installation reports into the same instance, concurrently.
 * </p>
 */
public final class ModelAvailability {

	/** The marks, keyed by model name. Absent means healthy. */
	private final ConcurrentHashMap<String, ModelHealthMark> _marks = new ConcurrentHashMap<>();

	private final Duration _quotaTtl;

	private final Duration _capacityTtl;

	private final Duration _transientTtl;

	private final Clock _clock;

	/**
	 * Creates a {@link ModelAvailability} with the lifetimes of the given section.
	 *
	 * @param config
	 *        The {@code <failover>} section, or <code>null</code> for the shipped defaults.
	 * @param clock
	 *        The clock the TTLs are measured against; must not be <code>null</code>.
	 */
	public ModelAvailability(FailoverConfig config, Clock clock) {
		_clock = clock;
		_quotaTtl = ttl(config == null ? 86_400 : config.getQuotaTtlSeconds());
		_capacityTtl = ttl(config == null ? 30 : config.getCapacityTtlSeconds());
		_transientTtl = ttl(config == null ? 300 : config.getTransientTtlSeconds());
	}

	/** A non-positive configured lifetime means "until an administrator resets it". */
	private static Duration ttl(int seconds) {
		return seconds > 0 ? Duration.ofSeconds(seconds) : null;
	}

	/**
	 * What the given provider failure says about the model's health, or <code>null</code> when it
	 * says nothing.
	 *
	 * <p>
	 * Two kinds say nothing on purpose. A malformed request is this application's fault and would
	 * be malformed for the next model too, and a context overflow is answered by compacting, not by
	 * changing models - marking either one would route healthy models away over a fault that
	 * travels with the request.
	 * </p>
	 */
	public static HealthCause causeOf(ProviderException.Kind kind) {
		if (kind == null) {
			return null;
		}
		return switch (kind) {
			case BILLING, AUTHENTICATION -> HealthCause.QUOTA;
			case RATE_LIMITED, OVERLOADED -> HealthCause.CAPACITY;
			case UNKNOWN -> HealthCause.TRANSIENT;
			case INVALID_REQUEST, CONTEXT_WINDOW_EXCEEDED -> null;
		};
	}

	/**
	 * Records that a call to the given model failed.
	 *
	 * <p>
	 * A failure the classification says nothing about (see {@link #causeOf}) is ignored, so a
	 * healthy model is never marked over a request this application built wrongly.
	 * </p>
	 *
	 * @param model
	 *        The model that was called; must not be <code>null</code>.
	 * @param kind
	 *        How the provider classified the failure.
	 * @param detail
	 *        The provider's own message, or <code>null</code>.
	 * @param turnToken
	 *        The turn the call belonged to, as
	 *        {@link #turnToken(String, int)} builds it. The turn is recorded as having used up the
	 *        model's one attempt, so a failure inside a turn is never retried within that same
	 *        turn.
	 * @return The mark now held against the model, or <code>null</code> when the failure said
	 *         nothing about it.
	 */
	public ModelHealthMark reportFailure(String model, ProviderException.Kind kind, String detail,
			String turnToken) {
		HealthCause cause = causeOf(kind);
		if (cause == null || model == null) {
			return null;
		}
		Instant now = _clock.instant();
		String kindName = kind.name();
		ModelHealthMark result = _marks.compute(model, (name, live) -> {
			if (live != null && live.live(now) && !cause.overrides(live.cause())) {
				// A weaker statement about a model that is already known to be worse off. The live
				// mark is terminal in this branch - TRANSIENT is the weakest cause there is - so
				// the model is blocked anyway and nothing needs adjusting.
				return live;
			}
			return new ModelHealthMark(name, cause != HealthCause.TRANSIENT, cause, kindName,
				detail, now, expiry(now, cause), turnToken);
		});
		Logger.info("Model '" + model + "' marked " + result.cause().name().toLowerCase()
			+ (result.terminal() ? " (not called again" : " (one attempt per turn")
			+ (result.expiresAt() == null ? ", until reset)" : ", until " + result.expiresAt() + ")")
			+ " after " + kindName + ".", ModelAvailability.class);
		return result;
	}

	private Instant expiry(Instant now, HealthCause cause) {
		Duration ttl = switch (cause) {
			case QUOTA -> _quotaTtl;
			case CAPACITY -> _capacityTtl;
			case TRANSIENT -> _transientTtl;
		};
		return ttl == null ? null : now.plus(ttl);
	}

	/**
	 * Whether the given model may be called for the given turn, consuming its one attempt if that
	 * is what makes the answer <code>true</code>.
	 *
	 * <p>
	 * This both asks and takes, in one atomic step, because two turns running concurrently must not
	 * both get the single attempt a {@link HealthCause#TRANSIENT} mark leaves. Calling it twice for
	 * the same turn is safe and gives the same answer: the turn that already took the attempt keeps
	 * it.
	 * </p>
	 *
	 * @param model
	 *        The model about to be called.
	 * @param turnToken
	 *        The turn asking; see {@link #turnToken(String, int)}. Must not be <code>null</code> -
	 *        a reader that only wants to look asks {@link #mark(String)}, which takes nothing.
	 */
	public boolean claim(String model, String turnToken) {
		if (model == null) {
			return true;
		}
		ModelHealthMark[] before = new ModelHealthMark[1];
		Instant now = _clock.instant();
		_marks.compute(model, (name, live) -> {
			before[0] = live != null && live.live(now) ? live : null;
			if (before[0] == null) {
				// Drop an expired mark while we are here; nothing else sweeps the map.
				return null;
			}
			if (before[0].terminal()) {
				return before[0];
			}
			if (turnToken.equals(before[0].consumedTurn())) {
				return before[0];
			}
			return before[0].consumedBy(turnToken);
		});
		ModelHealthMark live = before[0];
		if (live == null) {
			return true;
		}
		if (live.terminal()) {
			return false;
		}
		// The one attempt is free exactly when this turn had not taken it yet.
		return !turnToken.equals(live.consumedTurn());
	}

	/**
	 * The mark currently held against the given model, or <code>null</code> when it is healthy.
	 *
	 * <p>
	 * A pure read: it takes no attempt and expires nothing.
	 * </p>
	 */
	public ModelHealthMark mark(String model) {
		ModelHealthMark live = model == null ? null : _marks.get(model);
		return live != null && live.live(_clock.instant()) ? live : null;
	}

	/**
	 * Every mark that still holds, newest first.
	 */
	public List<ModelHealthMark> marks() {
		Instant now = _clock.instant();
		List<ModelHealthMark> result = new ArrayList<>();
		for (Map.Entry<String, ModelHealthMark> entry : _marks.entrySet()) {
			ModelHealthMark live = entry.getValue();
			if (live.live(now)) {
				result.add(live);
			}
		}
		result.sort(Comparator.comparing(ModelHealthMark::markedAt).reversed());
		return List.copyOf(result);
	}

	/**
	 * Drops the mark held against the given model.
	 *
	 * @return <code>true</code> when a mark that still held was dropped. A mark that had already
	 *         run out holds nothing, so dropping it is not reported as a change.
	 */
	public boolean reset(String model) {
		if (model == null) {
			return false;
		}
		ModelHealthMark removed = _marks.remove(model);
		boolean held = removed != null && removed.live(_clock.instant());
		if (held) {
			Logger.info("Model health mark for '" + model + "' reset.", ModelAvailability.class);
		}
		return held;
	}

	/**
	 * Drops every mark.
	 *
	 * @return How many of them still held; see {@link #reset(String)}.
	 */
	public int resetAll() {
		Instant now = _clock.instant();
		int held = 0;
		for (ModelHealthMark mark : _marks.values()) {
			if (mark.live(now)) {
				held++;
			}
		}
		_marks.clear();
		if (held > 0) {
			Logger.info("All " + held + " model health mark(s) reset.", ModelAvailability.class);
		}
		return held;
	}

	/**
	 * The token identifying one turn of one run, which is the scope a
	 * {@link HealthCause#TRANSIENT} mark's single attempt belongs to.
	 */
	public static String turnToken(String runId, int turn) {
		return runId + "#" + turn;
	}
}
