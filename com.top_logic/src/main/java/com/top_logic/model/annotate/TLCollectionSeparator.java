/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.IfTrue;
import com.top_logic.layout.form.values.edit.annotation.DynamicMandatory;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLTypeAnnotation} that configures the element separator, with which multiple primitive
 * values can be separated when entering a value of an attribute with primitive type and multiple
 * values.
 */
@InApp
@TagName("collection-separator")
@TargetType(value = { TLTypeKind.BINARY, TLTypeKind.BOOLEAN, TLTypeKind.CUSTOM, TLTypeKind.DATE, TLTypeKind.FLOAT,
	TLTypeKind.INT, TLTypeKind.STRING })
public interface TLCollectionSeparator extends TLAttributeAnnotation {

	/**
	 * @see #isRegularExpression()
	 */
	String REGULAR_EXPRESSION = "regular-expression";

	/**
	 * The separator, a user has to enter in a field to separate multiple values.
	 */
	@Mandatory
	@Name("parse-separator")
	String getParseSeparator();

	/**
	 * Whether the {@link #getParseSeparator()} is given as regular expression.
	 */
	@Name(REGULAR_EXPRESSION)
	boolean isRegularExpression();

	/**
	 * The separator that is printed between multiple values.
	 * 
	 * <p>
	 * Note: If given, the {@link #getParseSeparator()} must be a substring of this value.
	 * </p>
	 */
	@Nullable
	@Name("format-separator")
	@DynamicMandatory(fun = IfTrue.class, args = @Ref(REGULAR_EXPRESSION))
	String getFormatSeparator();

}
