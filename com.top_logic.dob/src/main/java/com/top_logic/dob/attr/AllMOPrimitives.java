/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.IFunction0;

/**
 * {@link IFunction0} looking up a sorted list of all {@link MOPrimitive} types known.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AllMOPrimitives extends Function0<List<MOPrimitive>> {

	@Override
	public List<MOPrimitive> apply() {
		ArrayList<MOPrimitive> result = new ArrayList<>(MOPrimitive.getAllPrimitives());
		Collections.sort(result, (p1, p2) -> p1.getName().compareTo(p2.getName()));
		return result;
	}

}
