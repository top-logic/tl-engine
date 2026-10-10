/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;

/**
 * Starts a turn of the lead when the subagents of a chat have news and no turn of the lead runs to
 * read it.
 *
 * <p>
 * A background subagent usually finishes after the turn that started it has ended. Its result waits
 * in the registry until the lead's next turn, which used to be the user's next message: the user had
 * to type "and?" to get the result read. A subagent that asks the lead something waits the same way,
 * and its {@code ask_lead} times out unanswered. This listener starts that turn instead.
 * </p>
 *
 * <ul>
 * <li>News is collected for {@link #DEBOUNCE_MS}, so that subagents finishing close together are read
 * in one turn rather than in one each.</li>
 * <li>While a turn of the lead runs, nothing is started: that turn reads the news at its next step,
 * and when it ends, {@link #leadRunEnded()} looks again for what arrived too late for it.</li>
 * <li>At most {@link #MAX_WAKES} turns are started between two messages of the user, so that a lead
 * which spawns again on every result cannot go on spending on its own. When the limit is reached the
 * user is told once, and the news waits for their next message.</li>
 * <li>After the user stopped a run, nothing is started until their next message.</li>
 * </ul>
 *
 * <p>
 * A subagent that was cancelled brings no news (see {@link SubagentJobRegistry.News}); its result is
 * delivered with whatever turn comes next.
 * </p>
 */
final class SubagentWaker implements SubagentJobRegistry.Listener {

	/** How long news is collected before a turn is started for it. */
	static final long DEBOUNCE_MS = 2_000;

	/** How long to wait before trying again when a turn could not be started. */
	static final long RETRY_MS = 30_000;

	/** How often to try again before the news waits for the user's next message. */
	static final int MAX_RETRIES = 20;

	/** How many turns are started between two messages of the user. */
	static final int MAX_WAKES = 5;

	/** What the waker needs from the chat it wakes. */
	interface Host {

		/** Whether a turn of the lead runs in this chat now. */
		boolean leadRuns();

		/**
		 * Starts a turn of the lead that opens with the news.
		 *
		 * @return Whether it was started; <code>false</code> when the chat cannot run one now - no
		 *         window shows it, the window shows another chat, or the installation has no room -
		 *         which is asked again later.
		 */
		boolean wake(SubagentJobRegistry.News news);

		/** Tells the user that no more turns are started until their next message. */
		void wakesExhausted(int wakes);
	}

	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(task -> {
		Thread thread = new Thread(task, "subagent-waker");
		thread.setDaemon(true);
		return thread;
	});

	private final SubagentJobRegistry _registry;

	private final Host _host;

	private final long _debounceMs;

	private final long _retryMs;

	private int _wakes;

	private int _retries;

	private boolean _held;

	private boolean _exhaustedSaid;

	private boolean _closed;

	private ScheduledFuture<?> _pending;

	/**
	 * Creates a waker for the given registry.
	 */
	SubagentWaker(SubagentJobRegistry registry, Host host) {
		this(registry, host, DEBOUNCE_MS, RETRY_MS);
	}

	/**
	 * Creates a waker with the given delays.
	 */
	SubagentWaker(SubagentJobRegistry registry, Host host, long debounceMs, long retryMs) {
		_registry = registry;
		_host = host;
		_debounceMs = debounceMs;
		_retryMs = retryMs;
	}

	@Override
	public void finished(SubagentJob job) {
		schedule(_debounceMs);
	}

	@Override
	public void questionAsked(SubagentJob job) {
		schedule(_debounceMs);
	}

	/** A turn of the lead in this chat has ended; news it did not read may be waiting. */
	void leadRunEnded() {
		schedule(_debounceMs);
	}

	/** The user wrote a message: the turns started for them are counted from zero again. */
	synchronized void userSent() {
		_wakes = 0;
		_retries = 0;
		_held = false;
		_exhaustedSaid = false;
	}

	/** The user stopped a run: no turn is started until their next message. */
	synchronized void userStopped() {
		_held = true;
		cancelPending();
	}

	/** The chat is gone; nothing is started any more. */
	synchronized void close() {
		_closed = true;
		cancelPending();
	}

	private synchronized void schedule(long delayMs) {
		if (_closed || _held) {
			return;
		}
		if (_pending != null && !_pending.isDone()) {
			return;
		}
		_pending = SCHEDULER.schedule(this::check, delayMs, TimeUnit.MILLISECONDS);
	}

	private void cancelPending() {
		if (_pending != null) {
			_pending.cancel(false);
			_pending = null;
		}
	}

	private void check() {
		SubagentJobRegistry.News news = _registry.news();
		boolean sayExhausted = false;
		synchronized (this) {
			_pending = null;
			if (_closed || _held || news.isEmpty()) {
				return;
			}
			if (_wakes >= MAX_WAKES) {
				if (_exhaustedSaid) {
					return;
				}
				_exhaustedSaid = true;
				sayExhausted = true;
			}
		}
		if (sayExhausted) {
			_host.wakesExhausted(MAX_WAKES);
			return;
		}
		if (_host.leadRuns()) {
			// That turn reads the news, and leadRunEnded looks again when it is over.
			return;
		}
		boolean started;
		try {
			started = _host.wake(news);
		} catch (RuntimeException ex) {
			Logger.error("Could not start a lead turn for subagent news: " + ex.getMessage(), ex,
				SubagentWaker.class);
			started = false;
		}
		synchronized (this) {
			if (started) {
				_wakes++;
				_retries = 0;
			} else if (_retries < MAX_RETRIES) {
				_retries++;
				schedule(_retryMs);
			}
		}
	}

}
