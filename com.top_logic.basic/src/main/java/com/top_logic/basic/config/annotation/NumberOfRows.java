/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;

/**
 * Annotation class for annotating the number of displayed rows of type {@link Integer#TYPE}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NumberOfRows {

	/**
	 * Property for a field containing the number of rows that should be displayed.
	 * 
	 * @see #min()
	 * @see #max()
	 */
	@SuppressWarnings("unchecked")
	Property<Pair<Integer, Integer>> NUMBER_OF_ROWS = TypedAnnotatable.propertyRaw(Pair.class, "numberOfRows");

	/**
	 * Minimum number of rows that are be displayed.
	 */
	int min() default 1;

	/**
	 * Maximum number of rows that are be displayed.
	 */
	int max();

}
