/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Size of the input field of the annotated attribute.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("input-size")
@InApp
@TargetType(value = { TLTypeKind.STRING, TLTypeKind.DATE, TLTypeKind.FLOAT, TLTypeKind.INT })
public interface InputSize extends TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * @see #getValue()
	 */
	String VALUE = "value";

	/**
	 * Number of text columns to display when nothing is given.
	 * 
	 * @see #getValue()
	 */
	int DEFAULT_VALUE = 50;

	/**
	 * The number of text columns of the input field.
	 */
	@Name(VALUE)
	@IntDefault(DEFAULT_VALUE)
	int getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(int value);

	/**
	 * Creates an {@link InputSize} annotation with the size set to the given value.
	 */
	static TLAnnotation size(int size) {
		InputSize result = TypedConfiguration.newConfigItem(InputSize.class);
		result.setValue(size);
		return result;
	}

}
