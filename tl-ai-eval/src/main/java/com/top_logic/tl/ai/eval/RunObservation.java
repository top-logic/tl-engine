/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * The {@link AgentEventListener} the eval hangs on a run, and everything it learned from it.
 *
 * <p>
 * Everything here is read off the event stream rather than out of the persisted execution row, for
 * one reason: the store commits on its own writer thread, so a run's row is not yet visible to the
 * thread that started it. The events are, immediately and in order.
 * </p>
 *
 * <p>
 * A tool counts as called when the model asked for it, which is two events rather than one: the loop
 * emits {@code ToolCallStarted} only after the security and approval gates have passed, and
 * {@code ToolCallDenied} instead when one of them refused. Both are recorded here, because that is
 * what a {@link ToolSequence#getForbidden() forbidden} name has to mean - the finding is that the
 * model reached for the tool, and a gate that happened to stop it does not undo the attempt. A call
 * that started and then failed appears once, since {@code ToolCallFailed} follows a
 * {@code ToolCallStarted} that was already counted.
 * </p>
 */
public class RunObservation implements AgentEventListener {

	/**
	 * Provider errors that say something about <em>our</em> access, not about the model.
	 *
	 * <p>
	 * A wrong key, an unpaid bill or a provider that is down did not put the scenario to the model
	 * at all. Reporting such a run as a failure would attribute a number to a question nobody was
	 * asked, and - worse - would put a plausible-looking 0.00 into a table next to an arm whose key
	 * happened to work.
	 * </p>
	 */
	private static final Set<String> INFRASTRUCTURE_KINDS =
		Set.of("AUTHENTICATION", "BILLING", "OVERLOADED");

	/**
	 * One tool call the loop dispatched: the name the model used, and its arguments as it wrote them.
	 *
	 * @param name
	 *        The tool name on the wire.
	 * @param argsJson
	 *        The arguments, a JSON object.
	 */
	public record Call(String name, String argsJson) {
		// Sole record.
	}

	private final List<String> _toolCalls = Collections.synchronizedList(new ArrayList<>());

	private final List<Call> _calls = Collections.synchronizedList(new ArrayList<>());

	private volatile int _turns;

	private volatile TokenUsage _usage = TokenUsage.EMPTY;

	private volatile StopReason _stopReason;

	private volatile String _infrastructureFailure;

	@Override
	public void onEvent(AgentEvent event) {
		// if/instanceof rather than a pattern switch: this module compiles at source 17, where
		// pattern matching for switch is still a preview feature.
		if (event instanceof AgentEvent.TurnStarted started) {
			_turns = Math.max(_turns, started.turnNumber());
		} else if (event instanceof AgentEvent.ToolCallStarted call) {
			_toolCalls.add(call.toolName());
			_calls.add(new Call(call.toolName(), call.argsJson()));
		} else if (event instanceof AgentEvent.ToolCallDenied denied) {
			_toolCalls.add(denied.toolName());
		} else if (event instanceof AgentEvent.ProviderError error) {
			if (INFRASTRUCTURE_KINDS.contains(error.kind())) {
				_infrastructureFailure = error.kind() + ": " + error.message();
			}
		} else if (event instanceof AgentEvent.RunCompleted completed) {
			_stopReason = completed.reason();
			if (completed.totalUsage() != null) {
				_usage = completed.totalUsage();
			}
		}
		// Everything else is not a signal this eval reads. Deliberately silent: the loop emits far
		// more than the minimal version grades, and a listener that logged the rest would drown the
		// matrix output.
	}

	/** Every tool the model asked for, in call order, by the name it used on the wire. */
	public List<String> toolCalls() {
		synchronized (_toolCalls) {
			return List.copyOf(_toolCalls);
		}
	}

	/**
	 * The calls that ran, with their arguments, in call order: what a grader of what a call did
	 * reads, e.g. {@link Presented}. A denied call is in {@link #toolCalls()} only - it did nothing.
	 */
	public List<Call> calls() {
		synchronized (_calls) {
			return List.copyOf(_calls);
		}
	}

	/** Agent-loop iterations the run used. */
	public int turns() {
		return _turns;
	}

	/** Token usage of the whole run, as the provider reported it. */
	public TokenUsage usage() {
		return _usage;
	}

	/**
	 * Why the run never reached the model at all, or {@code null} if it did.
	 *
	 * @see #INFRASTRUCTURE_KINDS
	 */
	public String infrastructureFailure() {
		return _infrastructureFailure;
	}

	/** Why the loop stopped, or {@code null} if it never announced a completion. */
	public StopReason stopReason() {
		return _stopReason;
	}
}
