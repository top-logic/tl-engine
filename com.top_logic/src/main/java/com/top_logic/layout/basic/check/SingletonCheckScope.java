/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

import java.util.Collection;
import java.util.Collections;

/**
 * {@link CheckScope} providing a single {@link ChangeHandler} for check.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SingletonCheckScope implements CheckScope {

	private final ChangeHandler _handler;

	/**
	 * Creates a {@link SingletonCheckScope}.
	 * 
	 * @param handler
	 *        The single {@link ChangeHandler} to check for changes.
	 */
	public SingletonCheckScope(ChangeHandler handler) {
		this._handler = handler;
	}

	@Override
	public Collection<? extends ChangeHandler> getAffectedFormHandlers() {
		return Collections.singletonList(_handler);
	}

}
