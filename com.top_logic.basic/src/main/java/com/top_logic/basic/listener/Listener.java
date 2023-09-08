/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

/**
 * A listener can will be {@link Object#notify() notified} by the {@link ListenerRegistry} if the
 * event it is listening for {@link ListenerRegistry#notify(Object) is happening}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public interface Listener<T> {

	/**
	 * Notification of the {@link Listener} that the event occurred it is listening for.
	 */
	void notify(T notification);

}
