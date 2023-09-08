/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func.misc;

import com.top_logic.basic.func.Function1;

/**
 * {@link Function1} deciding about its argument being not <code>null</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NonNull extends Function1<Boolean, Object> {

	/**
	 * Singleton {@link NonNull} instance.
	 */
	public static final NonNull INSTANCE = new NonNull();

	private NonNull() {
		// Singleton constructor.
	}

	@Override
	public Boolean apply(Object arg) {
		return arg != null;
	}

}
