/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;

/**
 * The position of this {@link TLStructuredTypePart} relative to the other parts of its
 * {@link TLClass}.
 * <p>
 * It does not matter whether the {@link TLClass} itself or a superclass of it defines the part. It
 * does not define a sort order for parts of different {@link TLClass}es that are not superclasses
 * of one another.
 * </p>
 * <p>
 * The {@link TLStructuredTypePart} with the lowest value is displayed first.
 * </p>
 * 
 * @see DisplayAnnotations#getSortOrder(TLStructuredTypePart)
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@TagName("sort-order")
@InApp
public interface TLSortOrder extends TLAttributeAnnotation {

	/**
	 * The default value if no sort order is defined.
	 */
	double DEFAULT_SORT_ORDER = Double.POSITIVE_INFINITY;

	/** Property name of {@link #getValue()}. */
	String VALUE = "value";

	/**
	 * {@link TLStructuredTypePart}s of a {@link TLClass} are ordered according to this sort order.
	 */
	@DoubleDefault(DEFAULT_SORT_ORDER)
	@Name(VALUE)
	double getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(double value);

}
