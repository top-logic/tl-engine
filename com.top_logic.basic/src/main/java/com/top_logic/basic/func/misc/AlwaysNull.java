/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func.misc;

import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.IFunction0;

/**
 * {@link IFunction0} returning always <code>null</code>.
 * 
 * <p>
 * Note: This function cannot have a type parameter <code>T</code> because otherwise it can no
 * longer be used as class literal in annotations where a parameterized class is expected since the
 * raw type of the class literal is not compatible with a parameterized class type.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AlwaysNull extends Function0<Object> {

	/**
	 * Singleton {@link AlwaysNull} instance.
	 */
	public static final AlwaysNull INSTANCE = new AlwaysNull();

	private AlwaysNull() {
		// Singleton constructor.
	}

	@Override
	public Object apply() {
		return null;
	}
}