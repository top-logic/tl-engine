/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * {@link TypeDemos} part for demonstrating how primitive value objects are displayed.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
@DisplayOrder({
	PrimitiveObjectTypes.BOOLEAN_OBJECT,
	PrimitiveObjectTypes.BYTE_OBJECT,
	PrimitiveObjectTypes.SHORT_OBJECT,
	PrimitiveObjectTypes.CHARACTER_OBJECT,
	PrimitiveObjectTypes.INTEGER_OBJECT,
	PrimitiveObjectTypes.LONG_OBJECT,
	PrimitiveObjectTypes.FLOAT_OBJECT,
	PrimitiveObjectTypes.DOUBLE_OBJECT,
	PrimitiveObjectTypes.TIME_OBJECT,
})
public interface PrimitiveObjectTypes extends ConfigurationItem {

	/** Configuration name for the value of {@link #getBooleanObject()}. */
	String BOOLEAN_OBJECT = "boolean-object";

	/** Configuration name for the value of {@link #getByteObject()}. */
	String BYTE_OBJECT = "byte-object";

	/** Configuration name for the value of {@link #getShortObject()}. */
	String SHORT_OBJECT = "short-object";

	/** Configuration name for the value of {@link #getIntegerObject()}. */
	String INTEGER_OBJECT = "integer-object";

	/** Configuration name for the value of {@link #getLongObject()}. */
	String LONG_OBJECT = "long-object";

	/** Configuration name for the value of {@link #getCharacterObject()}. */
	String CHARACTER_OBJECT = "character-object";

	/** Configuration name for the value of {@link #getFloatObject()}. */
	String FLOAT_OBJECT = "float-object";

	/** Configuration name for the value of {@link #getDoubleObject()}. */
	String DOUBLE_OBJECT = "double-object";

	/** Configuration name for the value of {@link #getTimeObject()}. */
	String TIME_OBJECT = "time-object";

	/** Demo property of the wrapper type {@link Boolean}. */
	@Name(BOOLEAN_OBJECT)
	Boolean getBooleanObject();

	/** Demo property of the wrapper type {@link Byte}. */
	@Name(BYTE_OBJECT)
	Byte getByteObject();

	/** Demo property of the wrapper type {@link Short}. */
	@Name(SHORT_OBJECT)
	Short getShortObject();

	/** Demo property of the wrapper type {@link Integer}. */
	@Name(INTEGER_OBJECT)
	Integer getIntegerObject();

	/** Demo property of the wrapper type {@link Long}. */
	@Name(LONG_OBJECT)
	Long getLongObject();

	/** Demo property of the wrapper type {@link Character}. */
	@Name(CHARACTER_OBJECT)
	Character getCharacterObject();

	/** Demo property of the wrapper type {@link Float}. */
	@Name(FLOAT_OBJECT)
	Float getFloatObject();

	/** Demo property of the wrapper type {@link Double}. */
	@Name(DOUBLE_OBJECT)
	Double getDoubleObject();

	/**
	 * Specialized long property that is entered in a time format "1h 15min".
	 */
	@Name(TIME_OBJECT)
	@Format(MillisFormat.class)
	Long getTimeObject();
}
