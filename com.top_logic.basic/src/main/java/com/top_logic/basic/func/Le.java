/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;


/**
 * {@link GenericFunction} comparing two values and returns whether the first is lower or equal to
 * the second.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Le<T extends Comparable<T>> extends Function2<Boolean, T, T> {

	@Override
	public Boolean apply(T arg1, T arg2) {
		return Boolean.valueOf(Gt.compare(arg1, arg2) <= 0);
	}

}
