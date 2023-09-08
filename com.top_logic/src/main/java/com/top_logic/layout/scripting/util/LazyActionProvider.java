/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.util;

import com.top_logic.basic.col.Provider;
import com.top_logic.layout.scripting.action.ApplicationAction;

/**
 * An {@link Provider} for {@link ApplicationAction}s.
 * <p>
 * Can be used to delays failures until test execution to prevent breaking all tests when suite
 * creation fails.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class LazyActionProvider implements Provider<ApplicationAction> {

	private ApplicationAction lazyAction;

	@Override
	public ApplicationAction get() {
		if (lazyAction == null) {
			lazyAction = createAction();
		}
		return lazyAction;
	}

	/** Is called when the action is requested the first time. */
	protected abstract ApplicationAction createAction();

}
