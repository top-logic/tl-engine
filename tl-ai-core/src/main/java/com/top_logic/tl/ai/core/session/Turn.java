/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.tl.ai.core.message.TokenUsage;

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

	/**
	 * Creates a new turn.
	 *
	 * @param id         unique turn identifier
	 * @param turnNumber 1-based turn number within the session
	 */
	public Turn(String id, int turnNumber) {
		_id = id;
		_turnNumber = turnNumber;
		_timestamp = Instant.now();
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
	 * Add token usage from one {@link com.top_logic.tl.ai.core.agent.AgentLoop} iteration.
	 */
	public void addUsage(TokenUsage turnUsage) {
		_cumulativeUsage = _cumulativeUsage.add(turnUsage);
		if (turnUsage != null && turnUsage.promptTokens() > 0) {
			_lastIterationUsage = turnUsage;
		}
	}
}
