/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Constraint annotation that limits the size of string attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("size-constraint")
@DisplayOrder({ TLSize.LOWER_BOUND_PROPERTY, TLSize.UPPER_BOUND_PROPERTY })
@TargetType(TLTypeKind.STRING)
@InApp
public interface TLSize extends TLAttributeAnnotation, TLTypeAnnotation {
	
	/**
	 * Constant for no upper limit.
	 * 
	 * @see #getUpperBound()
	 */
	public static final long NO_UPPER_BOUND = Long.MAX_VALUE;
	
	/**
	 * Constant for no lower limit.
	 * 
	 * @see #getLowerBound()
	 */
	public static final long NO_LOWER_BOUND = 0;

	/**
	 * @see #getLowerBound()
	 */
	String LOWER_BOUND_PROPERTY = "lower-bound";

	/**
	 * @see #getUpperBound()
	 */
	String UPPER_BOUND_PROPERTY = "upper-bound";

	/**
	 * A lower limit for the size of the annotated string property.
	 * 
	 * @see #NO_LOWER_BOUND
	 */
	@LongDefault(NO_LOWER_BOUND)
	@Name(LOWER_BOUND_PROPERTY)
	long getLowerBound();
	
	/**
	 * @see #getLowerBound()
	 */
	void setLowerBound(long value);

	/**
	 * An upper limit for the size of the annotated string property.
	 * 
	 * @see #NO_UPPER_BOUND
	 */
	@LongDefault(NO_UPPER_BOUND)
	@Name(UPPER_BOUND_PROPERTY)
	long getUpperBound();

	/**
	 * @see #getUpperBound()
	 */
	void setUpperBound(long value);

}
