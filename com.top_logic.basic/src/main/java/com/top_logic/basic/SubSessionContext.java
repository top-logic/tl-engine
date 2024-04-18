/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Supplier;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.util.ResourcesModule;

/**
 * Representation of a session in a browser window.
 * 
 * <p>
 * A sub session represents a session in a browser. In contrast to a {@link SessionContext} there
 * can be more than one {@link SubSessionContext} for a session. This can for example happen, when
 * in the same browser in multiple tabs a <i>TopLogic</i> application is opened in such case for each tab
 * a {@link SubSessionContext} is constructed.
 * </p>
 * 
 * <p>
 * A {@link SubSessionContext} enables access to the {@link SessionContext} and allows to store
 * objects within the whole sub session.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SubSessionContext extends TypedAnnotatable {

	/**
	 * Sets the value of {@link #getSessionContext()}
	 * 
	 * @param session
	 *        value of {@link #getSessionContext()}
	 */
	void setSessionContext(SessionContext session);

	/**
	 * The {@link SessionContext} associated to this sub session.
	 */
	SessionContext getSessionContext();

	/**
	 * Returns the identifier of this {@link SessionContext}
	 */
	String getContextId();

	/**
	 * Sets value of {@link #getContextId()}.
	 * 
	 * @param contextId
	 *        New value of {@link #getContextId()}
	 */
	void setContextId(String contextId);

	/**
	 * Returns the current locale for this session.
	 */
	Locale getCurrentLocale();

	/**
	 * Sets the value of {@link #getCurrentLocale()}
	 * 
	 * @param locale
	 *        The new value of {@link #getCurrentLocale()}.
	 */
	void setCurrentLocale(Locale locale);

	/**
	 * Calls the {@link Supplier} with the {@link #getCurrentLocale() locale} switched temporarily
	 * to the given one.
	 * <p>
	 * The parameters are not allowed to be <code>null</code>.
	 * </p>
	 */
	default <T> T withLocale(Locale locale, Supplier<T> supplier) {
		Locale previousLocale = getCurrentLocale();
		if (locale.equals(previousLocale)) {
			return supplier.get();
		}
		setCurrentLocale(locale);
		try {
			if (ResourcesModule.Module.INSTANCE.isActive()) {
				return ResourcesModule.getInstance().withLocale(this, supplier);
			} else {
				return supplier.get();
			}
		} finally {
			setCurrentLocale(previousLocale);
		}
	}

	/**
	 * Returns the current {@link TimeZone} for this session.
	 */
	TimeZone getCurrentTimeZone();

	/**
	 * Sets the value of {@link #getCurrentTimeZone()}
	 * 
	 * @param timeZone
	 *        The new value of {@link #getCurrentTimeZone()}.
	 */
	void setCurrentTimeZone(TimeZone timeZone);

	/**
	 * Whether this {@link SubSessionContext} is locked.
	 * 
	 * <p>
	 * If this {@link SubSessionContext} is locked, the session revision is not updated.
	 * </p>
	 * 
	 * @return Whether this {@link SubSessionContext} is currently locked.
	 * 
	 * @see #lock()
	 * @see #unlock()
	 */
	boolean isLocked();

	/**
	 * Locks this {@link SubSessionContext}.
	 * 
	 * <p>
	 * This method must be used when some actions are executed in a different thread, before the
	 * thread is started. It must be ensured that the context is {@link #unlock() unlocked} when the
	 * work is done.
	 * </p>
	 * 
	 * @see #unlock()
	 * @see #isLocked()
	 */
	void lock();

	/**
	 * Unlocks a formerly locked session.
	 * 
	 * @return <code>true</code> if this sub session is not longer locked after the method call
	 *         returns.
	 * 
	 * @see #lock()
	 * @see #isLocked()
	 */
	boolean unlock();

}

