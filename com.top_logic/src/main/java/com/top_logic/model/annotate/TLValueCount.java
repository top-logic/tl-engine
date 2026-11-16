/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.Positive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ValueCountConstraintCheck;

/**
 * Constraint annotation that limits the number of values of a
 * {@link TLStructuredTypePart#isMultiple() multi-valued} attribute.
 *
 * <p>
 * An object whose attribute holds more values than {@link #getMax()} is rejected when it is saved.
 * A form field of such an attribute is invalid with more values, and an upload field refuses files
 * beyond the maximum.
 * </p>
 *
 * @see ValueCountConstraintCheck
 */
@TagName(TLValueCount.TAG_NAME)
@InApp
public interface TLValueCount extends TLAttributeAnnotation {

	/**
	 * The tag name of this annotation in a model definition.
	 */
	String TAG_NAME = "value-count";

	/**
	 * @see #getMax()
	 */
	String MAX_PROPERTY = "max";

	/**
	 * The maximum number of values the annotated attribute may hold.
	 *
	 * <p>
	 * Without a value, the number of values is not limited.
	 * </p>
	 */
	@Name(MAX_PROPERTY)
	@Constraint(Positive.class)
	Integer getMax();

	/**
	 * @see #getMax()
	 */
	void setMax(Integer value);

}
