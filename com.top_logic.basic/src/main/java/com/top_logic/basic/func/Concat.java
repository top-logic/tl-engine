/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

/**
 * {@link GenericFunction} concatenating several values to a single {@link String}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Concat extends GenericFunction<String> {

	@Override
	public String invoke(Object... args) {
		StringBuilder buffer = new StringBuilder();
		for (Object arg : args) {
			if (arg == null) {
				continue;
			}

			buffer.append(arg);
		}
		return buffer.toString();
	}

	@Override
	public int getArgumentCount() {
		return 0;
	}

	@Override
	public boolean hasVarArgs() {
		return true;
	}

}
