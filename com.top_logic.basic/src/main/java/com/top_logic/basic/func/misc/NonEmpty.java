/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func.misc;

import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.GenericFunction;

/**
 * {@link GenericFunction} that tests its argument for being not empty in the widest sense.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NonEmpty extends Function1<Boolean, Object> {

	/**
	 * Singleton {@link NonEmpty} instance.
	 */
	public static final NonEmpty INSTANCE = new NonEmpty();

	private NonEmpty() {
		// Singleton constructor.
	}

	@Override
	public Boolean apply(Object arg) {
		return Boolean.valueOf(!IsEmpty.isEmpty(arg));
	}

}
