/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ByteDefault;
import com.top_logic.basic.config.annotation.defaults.CharDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.ShortDefault;

/**
 * {@link TypeDemos} part for demonstrating how primitive value objects are displayed when they have
 * a non-null default value.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface PrimitiveObjectTypesNonNullDefault extends PrimitiveObjectTypes {

	@BooleanDefault(true)
	@Override
	Boolean getBooleanObject();

	@ByteDefault(1)
	@Override
	Byte getByteObject();

	@ShortDefault(1)
	@Override
	Short getShortObject();

	@IntDefault(1)
	@Override
	Integer getIntegerObject();

	@LongDefault(1)
	@Override
	Long getLongObject();

	@CharDefault('a')
	@Override
	Character getCharacterObject();

	@FloatDefault(1.23456789f)
	@Override
	Float getFloatObject();

	@DoubleDefault(1.23456789)
	@Override
	Double getDoubleObject();

}
