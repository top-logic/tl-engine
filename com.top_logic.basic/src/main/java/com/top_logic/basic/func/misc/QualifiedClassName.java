/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func.misc;

import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.GenericFunction;

/**
 * {@link GenericFunction} that tests its argument for being not empty in the widest sense.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class QualifiedClassName extends Function1<String, Class<?>> {

	/**
	 * Singleton {@link QualifiedClassName} instance.
	 */
	public static final QualifiedClassName INSTANCE = new QualifiedClassName();

	private QualifiedClassName() {
		// Singleton constructor.
	}

	@Override
	public String apply(Class<?> arg) {
		if (arg == null) {
			return null;
		}
		return arg.getName();
	}

}
