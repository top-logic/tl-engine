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

	String BOOLEAN_OBJECT = "boolean-object";

	String BYTE_OBJECT = "byte-object";

	String SHORT_OBJECT = "short-object";

	String INTEGER_OBJECT = "integer-object";

	String LONG_OBJECT = "long-object";

	String CHARACTER_OBJECT = "character-object";

	String FLOAT_OBJECT = "float-object";

	String DOUBLE_OBJECT = "double-object";

	String TIME_OBJECT = "time-object";

	@Name(BOOLEAN_OBJECT)
	Boolean getBooleanObject();

	@Name(BYTE_OBJECT)
	Byte getByteObject();

	@Name(SHORT_OBJECT)
	Short getShortObject();

	@Name(INTEGER_OBJECT)
	Integer getIntegerObject();

	@Name(LONG_OBJECT)
	Long getLongObject();

	@Name(CHARACTER_OBJECT)
	Character getCharacterObject();

	@Name(FLOAT_OBJECT)
	Float getFloatObject();

	@Name(DOUBLE_OBJECT)
	Double getDoubleObject();

	/**
	 * Specialized long property that is entered in a time format "1h 15min".
	 */
	@Name(TIME_OBJECT)
	@Format(MillisFormat.class)
	Long getTimeObject();
}
