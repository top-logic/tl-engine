/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.util;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Determination whether the current user is anonymous.
 * 
 * @see TLContext#isAnonymous()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IsAnonymousUser implements BooleanSupplier, Supplier<Boolean> {

	/** Singleton {@link IsAnonymousUser} instance. */
	public static final IsAnonymousUser INSTANCE = new IsAnonymousUser();

	/**
	 * Creates a new {@link IsAnonymousUser}.
	 */
	protected IsAnonymousUser() {
		// singleton instance
	}

	@Override
	public boolean getAsBoolean() {
		return TLContext.isAnonymous();
	}

	@Override
	public Boolean get() {
		return getAsBoolean();
	}

}

