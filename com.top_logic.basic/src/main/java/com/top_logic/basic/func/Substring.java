/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

import java.util.Arrays;

/**
 * {@link GenericFunction} accessing {@link CharSequence#subSequence(int, int)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Substring extends GenericFunction<String> {

	@Override
	public String invoke(Object... args) {
		if (args.length > 3) {
			throw new IllegalArgumentException("Invalid number of arguments, expecting at most 3: "
				+ Arrays.asList(args));
		}
		Object arg0 = args[0];
		if (!(arg0 instanceof CharSequence)) {
			throw new IllegalArgumentException("First argument must be a string: " + arg0);
		}
		CharSequence sequence = (CharSequence) arg0;
		int from = ((Number) args[1]).intValue();
		int to = args.length > 2 ? ((Number) args[2]).intValue() : sequence.length();
		return sequence.subSequence(from, to).toString();
	}

	@Override
	public int getArgumentCount() {
		return 2;
	}

	@Override
	public boolean hasVarArgs() {
		return true;
	}

}
