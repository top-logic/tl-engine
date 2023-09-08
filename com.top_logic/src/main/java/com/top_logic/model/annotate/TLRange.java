/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} that defines a value constraint to {@link Number}-valued attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("value-range")
@InApp
@DisplayOrder({ TLRange.MIN_PROPERTY, TLRange.MAX_PROPERTY })
@TargetType({ TLTypeKind.FLOAT, TLTypeKind.INT })
public interface TLRange extends TLAttributeAnnotation, TLTypeAnnotation {

	/** @see #getMaximum() */
	String MAX_PROPERTY = "max";

	/**
	 * The upper bound on the value, <code>null</code> for no constraint.
	 */
	@Name(MAX_PROPERTY)
	Double getMaximum();

	/**
	 * @see #getMaximum()
	 */
	void setMaximum(Double value);

	/** @see #getMinimum() */
	String MIN_PROPERTY = "min";

	/**
	 * The lower bound of the value, <code>null</code> for no constraint.
	 */
	@Name(MIN_PROPERTY)
	Double getMinimum();

	/**
	 * @see #getMinimum()
	 */
	void setMinimum(Double value);

}
