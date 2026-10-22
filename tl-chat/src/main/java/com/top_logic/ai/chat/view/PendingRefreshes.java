/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * Which accounts changed since the last refresh, and whether a refresh has to be armed for them.
 *
 * <p>
 * The whole point of this class is that <b>no change is ever forgotten</b>. A refresh takes time -
 * it enters every registered window's sub-session and waits for that window's interaction - and an
 * agent writes throughout that time. A change that arrives while a refresh is running therefore
 * needs a refresh of its own, and the earlier shape of this bookkeeping had no way to say so: it
 * cleared the pending set before doing the work and refused to arm a second pass while the first
 * one was "not done", so a write landing in that gap was dropped and its row stood still until some
 * unrelated event happened to synthesize the window's model events (#497).
 * </p>
 *
 * <p>
 * Three states, and the caller only has to do what the answers say:
 * </p>
 *
 * <ul>
 * <li>{@link #changed(Person)} records the account and answers whether the caller must arm a
 * pass.</li>
 * <li>{@link #start()} begins one and hands over what it has to cover.</li>
 * <li>{@link #finish()} ends it and answers whether another pass must be armed, which it does
 * exactly when a change arrived while this one was running.</li>
 * </ul>
 *
 * <p>
 * Every method is synchronized on this object; the states are too small and too contended for
 * anything cleverer to pay off.
 * </p>
 *
 * @see AgentViewRefreshService
 */
final class PendingRefreshes {

	/** Accounts whose data changed and whose windows have not been refreshed since. */
	private final Set<Person> _owners = new HashSet<>();

	/** Whether a change of no particular account is pending, which refreshes every window. */
	private boolean _all;

	/** Whether a pass is armed and has not started yet. */
	private boolean _due;

	/** Whether a pass is running right now. */
	private boolean _running;

	/** Whether a change arrived while the running pass was working, i.e. after it took its batch. */
	private boolean _missed;

	/**
	 * What one refresh pass has to cover.
	 *
	 * @param all
	 *        Whether every registered window is meant, whatever account it shows.
	 * @param owners
	 *        The accounts whose windows are meant; empty with {@code all} set to <code>false</code>
	 *        means there is nothing to do.
	 */
	record Batch(boolean all, Set<Person> owners) {

		/** Whether this batch names any window at all. */
		boolean isEmpty() {
			return !all && owners.isEmpty();
		}
	}

	/**
	 * Records that the data of {@code owner} changed.
	 *
	 * @param owner
	 *        The account; <code>null</code> means "some account", which refreshes every window.
	 * @return Whether the caller has to arm a pass now. A pass that is already armed picks the
	 *         change up by itself, and a pass that is <em>running</em> has already taken its batch,
	 *         so the change is carried by the follow-up {@link #finish()} asks for.
	 */
	synchronized boolean changed(Person owner) {
		if (owner == null) {
			_all = true;
		} else {
			_owners.add(owner);
		}
		if (_running) {
			_missed = true;
			return false;
		}
		if (_due) {
			return false;
		}
		_due = true;
		return true;
	}

	/**
	 * Takes back the arming {@link #changed(Person)} asked for, for a caller that could not arm one
	 * after all - there is no scheduler to arm it with.
	 *
	 * <p>
	 * Without this the bookkeeping would believe a pass is coming and never arm another one.
	 * </p>
	 */
	synchronized void notArmed() {
		_due = false;
	}

	/**
	 * Begins a pass and hands over what it has to cover.
	 *
	 * <p>
	 * The pending accounts are taken, not copied: from here on a change is recorded for the
	 * <em>next</em> pass, which is what {@link #finish()} then asks for.
	 * </p>
	 */
	synchronized Batch start() {
		_due = false;
		_running = true;
		_missed = false;
		Batch batch = new Batch(_all, Set.copyOf(_owners));
		_all = false;
		_owners.clear();
		return batch;
	}

	/**
	 * Ends the running pass.
	 *
	 * @return Whether another pass has to be armed, which is the case exactly when a change arrived
	 *         while this one was working.
	 */
	synchronized boolean finish() {
		_running = false;
		if (_missed) {
			_missed = false;
			_due = true;
			return true;
		}
		return false;
	}

	/** Forgets everything, for a service that is shutting down. */
	synchronized void clear() {
		_owners.clear();
		_all = false;
		_due = false;
		_running = false;
		_missed = false;
	}
}
