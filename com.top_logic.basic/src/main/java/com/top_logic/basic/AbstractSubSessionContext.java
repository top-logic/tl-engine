/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.basic.col.TypedAnnotationContainer;
import com.top_logic.basic.time.TimeZones;

/**
 * Abstract implementation of {@link SubSessionContext}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractSubSessionContext extends TypedAnnotationContainer implements SubSessionContext {

	private volatile SessionContext _context;

	private volatile String _contextId;

	private volatile Locale _locale = Locale.getDefault();

	private volatile TimeZone _timeZone = TimeZones.defaultUserTimeZone();

	private final AtomicInteger _lock = new AtomicInteger();

	/**
	 * Creates a new {@link AbstractSubSessionContext}.
	 */
	public AbstractSubSessionContext() {
		super();
	}

	@Override
	public SessionContext getSessionContext() {
		return _context;
	}

	@Override
	public void setSessionContext(SessionContext context) {
		_context = context;
	}

	@Override
	public String getContextId() {
		return _contextId;
	}

	@Override
	public void setContextId(String contextId) {
		_contextId = contextId;
	}

	@Override
	public TimeZone getCurrentTimeZone() {
		return _timeZone;
	}

	@Override
	public void setCurrentTimeZone(TimeZone timeZone) {
		_timeZone = timeZone;
	}

	@Override
	public Locale getCurrentLocale() {
		return _locale;
	}

	@Override
	public void setCurrentLocale(Locale locale) {
		_locale = locale;
	}

	@Override
	public void lock() {
		_lock.incrementAndGet();
	}

	@Override
	public boolean unlock() {
		int numberLocks = _lock.decrementAndGet();
		if (numberLocks < 0) {
			_lock.incrementAndGet();
			throw new IllegalStateException();
		}
		return numberLocks == 0;
	}

	@Override
	public boolean isLocked() {
		return _lock.intValue() != 0;
	}

}
