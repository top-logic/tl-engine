/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Append-only conversation log that serves as the single source of truth
 * for an agent session.
 *
 * <p>Replaces {@code ToolCallTracker}, {@code Chat}, and the {@code ChatEntry}
 * list. All tool calls, approvals, user interactions, and agent output are
 * recorded here as {@link Part} entries within {@link Turn}s.</p>
 *
 * <p>Observers are notified on every structural change (turn start/complete,
 * part add/update), enabling push-based SSE streaming and persistence.</p>
 *
 * <p><b>Threading:</b> A {@link Turn} is started by the servlet thread (in
 * {@code sendMessage}), then parts are added by the agent background thread.
 * {@link CopyOnWriteArrayList} and {@code volatile} provide thread safety.</p>
 */
public class ConversationLog {

	/** Observer for conversation-level events. */
	public interface Observer {

		/** A new turn has started. */
		void onTurnStarted(Turn turn);

		/** The current turn has completed. */
		void onTurnCompleted(Turn turn);

		/** A part was added to the given turn. */
		void onPartAdded(Turn turn, Part part);

		/** A part was updated in the given turn. */
		void onPartUpdated(Turn turn, Part part);
	}

	private final CopyOnWriteArrayList<Turn> _turns = new CopyOnWriteArrayList<>();

	private final CopyOnWriteArrayList<Observer> _observers = new CopyOnWriteArrayList<>();

	private final AtomicInteger _turnCounter = new AtomicInteger(0);

	private volatile Turn _currentTurn;

	private final Turn.Observer _partForwarder = new Turn.Observer() {
		@Override
		public void onPartAdded(Turn turn, Part part) {
			for (Observer obs : _observers) {
				obs.onPartAdded(turn, part);
			}
		}

		@Override
		public void onPartUpdated(Turn turn, Part part) {
			for (Observer obs : _observers) {
				obs.onPartUpdated(turn, part);
			}
		}
	};

	/** Start a new turn. Auto-assigns an ID and increments the turn counter. */
	public Turn startTurn() {
		return startTurn(Instant.now());
	}

	/**
	 * Start a turn that began at the given moment, as {@link #startTurn()} does otherwise.
	 *
	 * <p>
	 * For a turn read back from a transcript: its start is what the "worked for" line and the
	 * message times are measured from, and the moment it was read back is neither (#568).
	 * </p>
	 */
	public Turn startTurn(Instant timestamp) {
		int number = _turnCounter.incrementAndGet();
		Turn turn = new Turn(UUID.randomUUID().toString(), number, timestamp);
		turn.addObserver(_partForwarder);
		_turns.add(turn);
		_currentTurn = turn;
		for (Observer obs : _observers) {
			obs.onTurnStarted(turn);
		}
		return turn;
	}

	/** Mark the current turn as complete. */
	public void completeTurn() {
		Turn turn = _currentTurn;
		_currentTurn = null;
		if (turn != null) {
			for (Observer obs : _observers) {
				obs.onTurnCompleted(turn);
			}
		}
	}

	/** The turn currently being processed, or {@code null} if idle. */
	public Turn currentTurn() {
		return _currentTurn;
	}

	/** Immutable snapshot of all turns in this conversation. */
	public List<Turn> turns() {
		return List.copyOf(_turns);
	}

	/** Number of turns recorded so far. */
	public int turnCount() {
		return _turns.size();
	}

	/** Registers an observer for turn lifecycle events. */
	/**
	 * Removes the turn at the given index and every turn after it.
	 *
	 * <p>
	 * Used when the user regenerates an answer or edits an earlier message. A conversation is a
	 * sequence, so what followed the removed turn was said in reply to it and goes with it.
	 * </p>
	 *
	 * @param fromIndex
	 *        zero-based index of the first turn to remove
	 * @return how many turns were removed
	 */
	public int removeFrom(int fromIndex) {
		if (fromIndex < 0 || fromIndex >= _turns.size()) {
			return 0;
		}
		int removed = _turns.size() - fromIndex;
		// Rebuilt rather than trimmed in place: a CopyOnWriteArrayList's sublist cannot be cleared.
		List<Turn> kept = new ArrayList<>(_turns.subList(0, fromIndex));
		_turns.clear();
		_turns.addAll(kept);
		_currentTurn = null;
		return removed;
	}

	/**
	 * Registers an {@link Observer} to be notified of what happens to this log.
	 *
	 * @see #removeObserver(Observer)
	 */
	public void addObserver(Observer observer) {
		_observers.add(observer);
	}

	/** Removes a previously registered observer. */
	public void removeObserver(Observer observer) {
		_observers.remove(observer);
	}
}
