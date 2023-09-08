/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import java.util.Date;

import com.top_logic.demo.layout.form.declarative.values.DemoConfigItem;
import com.top_logic.demo.layout.form.declarative.values.DemoConfiguredInstance;
import com.top_logic.demo.layout.form.declarative.values.DemoEnum;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;

/**
 * {@link TypeDemos} part for demonstrating how object values are displayed when rendered over whole
 * line.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ObjectTypesWholeLine extends NullableObjectTypesWithNonNullDefault {

	@RenderWholeLine
	@Override
	String getString();

	@RenderWholeLine
	@Override
	Date getDate();

	@RenderWholeLine
	@Override
	DemoEnum getEnum();

	@RenderWholeLine
	@Override
	Class<?> getJavaClass();

	@RenderWholeLine
	@Override
	DemoConfigItem getConfigItem();

	@RenderWholeLine
	@Override
	DemoConfiguredInstance getConfiguredInstance();

}
