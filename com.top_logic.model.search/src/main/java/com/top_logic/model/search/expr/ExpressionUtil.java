/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Set;

import com.top_logic.basic.col.SetBuilder;

/**
 * Utilities for implementing expressions.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ExpressionUtil {

	/**
	 * These might not be _all_ integer types, but they are the _supported_ integer types.
	 */
	public static final Set<Class<?>> INTEGER_TYPES = new SetBuilder<Class<?>>()
		.add(Byte.class)
		.add(Short.class)
		.add(Integer.class)
		.add(Long.class)
		.add(Character.class)
		.toUnmodifiableSet();

	/**
	 * Converts {@link #INTEGER_TYPES} to {@link Long}s and everything else to {@link Double}s.
	 * 
	 * @return null, if and only if the given number is null.
	 */
	public static Number normalize(Number number) {
		if (number == null) {
			return null;
		}
		if (ExpressionUtil.INTEGER_TYPES.contains(number.getClass())) {
			return number.longValue();
		} else {
			return number.doubleValue();
		}
	}

}
