/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

/**
 * Observer interface for {@link ObjectScope}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ScopeListener {

	/**
	 * Informs about a change in an {@link ObjectScope}.
	 * 
	 * @param event
	 *        Description of the change.
	 * 
	 * @see ScopeEvent#getScope()
	 */
	void handleObjectScopeEvent(ScopeEvent event);

}
