/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.ReadSource;

/**
 * One user-assistant cycle in a conversation.
 *
 * <p>A turn groups the user's input parts and the assistant's output parts
 * (text, tool calls, approvals, interactions, etc.).</p>
 *
 * <p><b>Important:</b> One Turn corresponds to one user request, NOT one
 * {@link com.top_logic.tl.ai.core.agent.AgentLoop AgentLoop} iteration. A single user message
 * may trigger multiple {@link com.top_logic.tl.ai.core.agent.AgentLoop AgentLoop} turns
 * (LLM call - tool execution - LLM call - ...). All of those iterations
 * accumulate parts and token usage into this single Turn.</p>
 */
public class Turn {

	/** Observer for part lifecycle events within a turn. */
	public interface Observer {

		/** Called when a new part is added to the turn. */
		void onPartAdded(Turn turn, Part part);

		/** Called when an existing part is updated (e.g., tool call completed). */
		void onPartUpdated(Turn turn, Part part);
	}

	private final String _id;

	private final int _turnNumber;

	private final Instant _timestamp;

	private volatile List<Part> _userParts = List.of();

	private final CopyOnWriteArrayList<Part> _assistantParts = new CopyOnWriteArrayList<>();

	private final CopyOnWriteArrayList<Observer> _observers = new CopyOnWriteArrayList<>();

	private volatile String _model;

	private volatile String _provider;

	private volatile TokenUsage _cumulativeUsage = TokenUsage.EMPTY;

	private volatile TokenUsage _lastIterationUsage = TokenUsage.EMPTY;

	private volatile AgentEvent.ContextComposition _contextComposition;

	private volatile String _turnContext;

	private volatile List<ReadSource> _sources = List.of();

	/**
	 * How many sources of a turn the transcript keeps.
	 *
	 * <p>
	 * A query that showed the model thousands of objects read each of them; the transcript is read
	 * back on every reopen and must not grow by that much. What was read first is kept.
	 * </p>
	 */
	public static final int MAX_RECORDED_SOURCES = 500;

	/**
	 * Creates a new turn.
	 *
	 * @param id         unique turn identifier
	 * @param turnNumber 1-based turn number within the session
	 */
	public Turn(String id, int turnNumber) {
		this(id, turnNumber, Instant.now());
	}

	/**
	 * Creates a turn that began at the given moment.
	 *
	 * @param id         unique turn identifier
	 * @param turnNumber 1-based turn number within the session
	 * @param timestamp  when the turn started; a turn read back from a transcript keeps its own
	 */
	public Turn(String id, int turnNumber, Instant timestamp) {
		_id = id;
		_turnNumber = turnNumber;
		_timestamp = timestamp;
	}

	/** Unique turn identifier. */
	public String id() {
		return _id;
	}

	/** 1-based loop iteration number. */
	public int turnNumber() {
		return _turnNumber;
	}

	/** When this turn was created. */
	public Instant timestamp() {
		return _timestamp;
	}

	/** The user-submitted parts for this turn. */
	public List<Part> userParts() {
		return _userParts;
	}

	/** Sets the user-submitted parts for this turn. */
	public void setUserParts(List<Part> parts) {
		_userParts = List.copyOf(parts);
	}

	/**
	 * What the host stated about this turn - the facts that change while the conversation runs -
	 * or {@code null} when it stated none.
	 *
	 * <p>
	 * Kept apart from {@link #userParts()} on purpose: the block travels to the model beside the
	 * user's text, and the user's text is what the bubble shows. A reader that wants the one the
	 * user wrote asks for the parts; a reader that wants the whole turn as it was sent - the
	 * transcript, the run detail of the activity monitor - asks for this as well.
	 * </p>
	 *
	 * @see com.top_logic.tl.ai.core.agent.environment.TurnContext
	 */
	public String turnContext() {
		return _turnContext;
	}

	/** Records what the host stated about this turn. */
	public void setTurnContext(String turnContext) {
		_turnContext = turnContext;
	}

	/**
	 * What the run of this turn read - its reading list, including what the sub-agents whose
	 * results it received read - whether the answer cited it or not.
	 *
	 * <p>
	 * Kept with the transcript so that what an answer rested on stays readable after the run, for a
	 * view that shows what was read as well as what was cited. Without the read text: the reading
	 * list holds that for the run only.
	 * </p>
	 */
	public List<ReadSource> sources() {
		return _sources;
	}

	/**
	 * Records the reading list of this turn's run, the first {@link #MAX_RECORDED_SOURCES} of it.
	 */
	public void setSources(List<ReadSource> sources) {
		List<ReadSource> stripped = new ArrayList<>();
		if (sources != null) {
			for (ReadSource source : sources) {
				if (stripped.size() >= MAX_RECORDED_SOURCES) {
					break;
				}
				stripped.add(source.withoutText());
			}
		}
		_sources = List.copyOf(stripped);
	}

	/** Immutable snapshot of the assistant's response parts. */
	public List<Part> assistantParts() {
		return List.copyOf(_assistantParts);
	}

	/**
	 * Find a part by ID, searching from the end (most recent first).
	 *
	 * @return the part, or {@code null} if not found
	 */
	public Part findPart(String partId) {
		for (int i = _assistantParts.size() - 1; i >= 0; i--) {
			Part p = _assistantParts.get(i);
			if (p.id().equals(partId)) {
				return p;
			}
		}
		return null;
	}

	/** Append a new part and notify observers. */
	public void addAssistantPart(Part part) {
		_assistantParts.add(part);
		for (Observer obs : _observers) {
			obs.onPartAdded(this, part);
		}
	}

	/**
	 * Replace an existing part by ID and notify observers.
	 *
	 * <p>Used for state transitions (e.g., tool RUNNING to COMPLETED).
	 * Searches from the end since recent parts are most likely to be updated.</p>
	 */
	public void updatePart(String partId, Part updated) {
		for (int i = _assistantParts.size() - 1; i >= 0; i--) {
			if (_assistantParts.get(i).id().equals(partId)) {
				_assistantParts.set(i, updated);
				break;
			}
		}
		for (Observer obs : _observers) {
			obs.onPartUpdated(this, updated);
		}
	}

	/** Registers an observer for part changes in this turn. */
	public void addObserver(Observer observer) {
		_observers.add(observer);
	}

	/** Removes a previously registered observer. */
	public void removeObserver(Observer observer) {
		_observers.remove(observer);
	}

	/** The LLM model used for this turn. */
	public String model() {
		return _model;
	}

	/** Sets the LLM model used for this turn. */
	public void setModel(String model) {
		_model = model;
	}

	/** The LLM provider that handled this turn. */
	public String provider() {
		return _provider;
	}

	/** Sets the LLM provider that handled this turn. */
	public void setProvider(String provider) {
		_provider = provider;
	}

	/**
	 * Cumulative token usage across all {@link com.top_logic.tl.ai.core.agent.AgentLoop} iterations
	 * in this turn.
	 */
	public TokenUsage cumulativeUsage() {
		return _cumulativeUsage;
	}

	/**
	 * Token usage of the <em>last</em> {@link com.top_logic.tl.ai.core.agent.AgentLoop} iteration of
	 * this turn.
	 *
	 * <p>
	 * Its {@link TokenUsage#promptTokens() prompt size} is the exact context the model saw last, and
	 * therefore the only number from which a UI may state how full the context window is.
	 * {@link #cumulativeUsage()} cannot be used for that: every iteration resends the whole
	 * conversation, so summing the iterations' prompt sizes counts the conversation once per
	 * iteration.
	 * </p>
	 */
	public TokenUsage lastIterationUsage() {
		return _lastIterationUsage;
	}

	/** Sets the usage of the last iteration; used when a persisted turn is read back. */
	public void setLastIterationUsage(TokenUsage usage) {
		_lastIterationUsage = usage == null ? TokenUsage.EMPTY : usage;
	}

	/**
	 * What the prompt of this turn's last request was made of, or {@code null} when unrecorded.
	 *
	 * <p>
	 * Kept so a re-opened chat can show the same breakdown a live one does; the figures are the
	 * agent loop's estimates, not provider counts.
	 * </p>
	 */
	public AgentEvent.ContextComposition contextComposition() {
		return _contextComposition;
	}

	/** Records what the prompt of this turn's latest request was made of. */
	public void setContextComposition(AgentEvent.ContextComposition composition) {
		_contextComposition = composition;
	}

	/**
	 * Add token usage from one {@link com.top_logic.tl.ai.core.agent.AgentLoop} iteration.
	 */
	public void addUsage(TokenUsage turnUsage) {
		_cumulativeUsage = _cumulativeUsage.add(turnUsage);
		if (turnUsage != null && turnUsage.promptTokens() > 0) {
			_lastIterationUsage = turnUsage;
		}
	}
}
