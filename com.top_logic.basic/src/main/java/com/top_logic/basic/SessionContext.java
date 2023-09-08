/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingListener;

import com.top_logic.basic.col.TypedAnnotatable;

/**
 * <i>TopLogic</i> representation of an {@link HttpSession}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SessionContext extends TypedAnnotatable {

	/**
	 * {@link SubSessionContext#getContextId()} prefix for context that are created on behalf of a
	 * user.
	 */
	String PERSON_ID_PREFIX = "person:";

	/**
	 * {@link SubSessionContext#getContextId()} prefix for context that are created by the system.
	 */
	String SYSTEM_ID_PREFIX = "system:";

	/**
	 * Identifier for this {@link SessionContext}.
	 */
	Object getId();

	/**
	 * The context ID of the session creator.
	 */
	String getOriginalContextId();

	/**
	 * @see #getOriginalContextId()
	 */
	void setOriginalContextId(String name);

	/**
	 * Associates the given listener with this {@link SessionContext}.
	 * 
	 * <p>
	 * If the listener is associated more than once with this {@link SessionContext} only the first
	 * call is successful.
	 * </p>
	 * 
	 * @param listener
	 *        the listener to add to this {@link SessionContext}
	 */
	void addHttpSessionBindingListener(HttpSessionBindingListener listener);

	/**
	 * Removes the given listener with this {@link SessionContext}.
	 * 
	 * <p>
	 * If the listener is associated more than once with this {@link SessionContext} only the first
	 * call is successful.
	 * </p>
	 * 
	 * @param listener
	 *        the listener to add to this {@link SessionContext}
	 */
	void removeHttpSessionBindingListener(HttpSessionBindingListener listener);

}

