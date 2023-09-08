/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

/**
 * Thing being observed by {@link Listener}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Observable {

	/**
	 * Adds the given observer to this {@link Observable}.
	 */
	ListenerBinding addListener(Listener listener);

}
