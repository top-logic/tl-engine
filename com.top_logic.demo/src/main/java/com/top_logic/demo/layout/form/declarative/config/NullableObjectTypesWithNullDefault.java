/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import java.util.Date;

import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.demo.layout.form.declarative.values.DemoConfigItem;
import com.top_logic.demo.layout.form.declarative.values.DemoConfiguredInstance;
import com.top_logic.demo.layout.form.declarative.values.DemoEnum;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link TypeDemos} part for demonstrating how object values are displayed when they are
 * {@link Nullable} and have null as default value.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface NullableObjectTypesWithNullDefault extends NullableObjectTypes {

	@NullDefault
	@Override
	String getString();

	@NullDefault
	@Override
	Date getDate();

	@NullDefault
	@Override
	DemoEnum getEnum();

	@NullDefault
	@Override
	Class<?> getJavaClass();

	@NullDefault
	@Override
	DemoConfigItem getConfigItem();

	@NullDefault
	@Override
	DemoConfiguredInstance getConfiguredInstance();

	@NullDefault
	@Override
	Object getSingleton();

	@NullDefault
	@Override
	Expr getExpr();

}
