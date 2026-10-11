/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;

/**
 * What a running subagent has done so far, kept from its own events.
 *
 * <p>
 * The one source the lead's {@code subagent_status} and the chat read a running child's state from:
 * turns, tokens, the last {@value #MAX_STEPS} tool calls with a short argument summary and their
 * outcome, and an excerpt of the text the child wrote last. Filled by the framework from the events of
 * the child's loop, never by the child model, so it says what the child did rather than what it
 * claims to have done.
 * </p>
 *
 * <p>
 * Bounded on purpose: a worker can make hundreds of calls, and the lead reads this in its own
 * context. Thread-safe: the child's thread writes, any other thread reads a snapshot.
 * </p>
 */
public final class SubagentProgress {

	/** How many tool calls are kept; older ones are dropped. */
	public static final int MAX_STEPS = 20;

	/** How many characters of the child's last text are kept - its end, which is the newest. */
	public static final int MAX_TEXT_CHARS = 300;

	/** How many characters of a call's arguments are kept. */
	public static final int MAX_ARGUMENT_CHARS = 80;

	/** How a tool call of the child ended, so far. */
	public enum Outcome {
		/** The call has started and not ended yet. */
		RUNNING,
		/** The call returned a result. */
		OK,
		/** The call failed. */
		FAILED,
		/** The call was refused before it ran. */
		DENIED;
	}

	/**
	 * One tool call of the child.
	 *
	 * @param turn
	 *        The child's turn the call belongs to.
	 * @param toolName
	 *        The tool called.
	 * @param arguments
	 *        The start of the call's arguments, at most {@link SubagentProgress#MAX_ARGUMENT_CHARS}.
	 * @param outcome
	 *        How the call ended, so far.
	 */
	public record Step(int turn, String toolName, String arguments, Outcome outcome) {
		// Plain data.
	}

	private final Deque<MutableStep> _steps = new ArrayDeque<>();

	private int _turns;

	private long _tokens;

	private final StringBuilder _currentText = new StringBuilder();

	private String _lastText = "";

	/** Takes one event of the child's loop into account. */
	public synchronized void onEvent(AgentEvent event) {
		if (event instanceof AgentEvent.TurnStarted started) {
			_turns = Math.max(_turns, started.turnNumber());
			keepCurrentText();
		} else if (event instanceof AgentEvent.TextDelta delta) {
			_currentText.append(delta.text());
			if (_currentText.length() > 4 * MAX_TEXT_CHARS) {
				_currentText.delete(0, _currentText.length() - MAX_TEXT_CHARS);
			}
		} else if (event instanceof AgentEvent.TurnCompleted completed) {
			if (completed.turnUsage() != null) {
				_tokens += completed.turnUsage().totalTokens();
			}
		} else if (event instanceof AgentEvent.ToolCallStarted call) {
			_steps.addLast(new MutableStep(call.toolCallId(), call.turnNumber(), call.toolName(),
				shorten(call.argsJson(), MAX_ARGUMENT_CHARS)));
			while (_steps.size() > MAX_STEPS) {
				_steps.removeFirst();
			}
		} else if (event instanceof AgentEvent.ToolCallCompleted done) {
			end(done.toolCallId(), Outcome.OK);
		} else if (event instanceof AgentEvent.ToolCallFailed failed) {
			end(failed.toolCallId(), Outcome.FAILED);
		} else if (event instanceof AgentEvent.ToolCallDenied denied) {
			end(denied.toolCallId(), Outcome.DENIED);
		}
	}

	/** The turns the child has started. */
	public synchronized int turns() {
		return _turns;
	}

	/** The tokens the child's finished turns used. */
	public synchronized long tokens() {
		return _tokens;
	}

	/** The child's recent tool calls, oldest first, at most {@link #MAX_STEPS}. */
	public synchronized List<Step> steps() {
		List<Step> steps = new ArrayList<>(_steps.size());
		for (MutableStep step : _steps) {
			steps.add(step.snapshot());
		}
		return steps;
	}

	/** The child's newest tool call, or <code>null</code> before its first. */
	public synchronized Step currentStep() {
		MutableStep last = _steps.peekLast();
		return last == null ? null : last.snapshot();
	}

	/**
	 * The end of the text the child wrote last - in the turn now running if it has written any there,
	 * else in the last turn that had text - at most {@link #MAX_TEXT_CHARS}; empty before any.
	 */
	public synchronized String lastText() {
		String current = _currentText.toString();
		return tail(current.isBlank() ? _lastText : current, MAX_TEXT_CHARS);
	}

	private void keepCurrentText() {
		if (!_currentText.toString().isBlank()) {
			_lastText = tail(_currentText.toString(), MAX_TEXT_CHARS);
		}
		_currentText.setLength(0);
	}

	private void end(String toolCallId, Outcome outcome) {
		for (MutableStep step : _steps) {
			if (step._toolCallId.equals(toolCallId)) {
				step._outcome = outcome;
				return;
			}
		}
	}

	private static String shorten(String text, int max) {
		if (text == null) {
			return "";
		}
		String flat = text.replace('\n', ' ').strip();
		return flat.length() <= max ? flat : flat.substring(0, max) + "...";
	}

	private static String tail(String text, int max) {
		String stripped = text.strip();
		return stripped.length() <= max ? stripped : "..." + stripped.substring(stripped.length() - max);
	}

	private static final class MutableStep {
		final String _toolCallId;

		final int _turn;

		final String _toolName;

		final String _arguments;

		Outcome _outcome = Outcome.RUNNING;

		MutableStep(String toolCallId, int turn, String toolName, String arguments) {
			_toolCallId = toolCallId == null ? "" : toolCallId;
			_turn = turn;
			_toolName = toolName;
			_arguments = arguments;
		}

		Step snapshot() {
			return new Step(_turn, _toolName, _arguments, _outcome);
		}
	}
}
