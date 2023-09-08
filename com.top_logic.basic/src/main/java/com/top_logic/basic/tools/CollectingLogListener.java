/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.listener.Listener;
import com.top_logic.basic.logging.Level;

/**
 * Stores {@link LogEntry}s with the given {@link Level}.
 * Once it is activated ({@link #activate()}, it stores them until {@link #deactivate()} is called.
 * It can also be activated directly in the constructor.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan.Stolzenburg</a>
 */
public class CollectingLogListener implements Listener<LogEntry> {

	private final Set<Level> prioritiesToCollect;

	private final List<LogEntry> logEntries = new ArrayList<>();

	private boolean _isActive = false;

	/**
	 * @param prioritiesToCollect
	 *        Only {@link LogEntry}s with one of these {@link Level}s are logged.
	 * @param activateNow
	 *        If it is not activated, it ignores all logged errors until it gets {@link #activate() activated}.
	 */
	public CollectingLogListener(Set<Level> prioritiesToCollect, boolean activateNow) {
		this.prioritiesToCollect = new HashSet<>(prioritiesToCollect);
		if (activateNow) {
			activate();
		}
	}

	/**
	 * Activates it.
	 * <p>
	 * If it is not activated, it does not collect any {@link LogEntry}. Calling this method twice
	 * makes no difference to calling it just once.
	 * </p>
	 */
	public synchronized void activate() {
		if (_isActive) {
			// Don't register twice, as the semantics of that are unspecified.
			return;
		}
		_isActive = true;
		Logger.getListenerRegistry().register(this);
	}

	/**
	 * Deactivates it.
	 * <p>
	 * If it is deactivated, it does not collect any {@link LogEntry}. Calling this method twice
	 * makes no difference to calling it just once.
	 * </p>
	 */
	public synchronized void deactivate() {
		if (!_isActive) {
			return;
		}
		_isActive = false;
		Logger.getListenerRegistry().unregister(this);
	}

	@Override
	public synchronized void notify(LogEntry logEntry) {
		if (!prioritiesToCollect.contains(logEntry.getPriority())) {
			return;
		}
		logEntries.add(logEntry);
	}

	/**
	 * Returns a copy of the collected {@link LogEntry}s.
	 * <p>
	 * Returns only {@link LogEntry LogEntries} that match the constructor-specified set of
	 * priorities to collect. <br/>
	 * The list is sorted: First logged - First in list
	 * </p>
	 */
	public synchronized List<LogEntry> getLogEntries() {
		return new ArrayList<>(logEntries);
	}

	/**
	 * Clears the collected {@link LogEntry}s.
	 */
	public synchronized void clear() {
		logEntries.clear();
	}

	/**
	 * Returns a copy of the collected {@link LogEntry}s and clears them.
	 * <p>
	 * Returns only {@link LogEntry LogEntries} that match the constructor-specified set of
	 * priorities to collect. <br/>
	 * The list is sorted: First logged - First in list
	 * </p>
	 */
	public synchronized List<LogEntry> getAndClearLogEntries() {
		List<LogEntry> logEntriesCopy = getLogEntries();
		clear();
		return logEntriesCopy;
	}

}