/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

import java.util.Arrays;
import java.util.List;

/**
 * {@link GenericFunction} accessing {@link List#subList(int, int)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Sublist extends GenericFunction<List<?>> {

	@Override
	public List<?> invoke(Object... args) {
		if (args.length > 3) {
			throw new IllegalArgumentException("Invalid number of arguments, expecting at most 3: "
				+ Arrays.asList(args));
		}
		List<?> list = (List<?>) args[0];
		int from = ((Number) args[1]).intValue();
		int to = args.length > 2 ? ((Number) args[2]).intValue() : list.size();
		return list.subList(from, to);
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
