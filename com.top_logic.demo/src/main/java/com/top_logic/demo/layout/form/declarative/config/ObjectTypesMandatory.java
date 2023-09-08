/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.demo.layout.form.declarative.values.DemoConfigItem;
import com.top_logic.demo.layout.form.declarative.values.DemoConfiguredInstance;
import com.top_logic.demo.layout.form.declarative.values.DemoEnum;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link TypeDemos} part for demonstrating how object values are displayed when they are
 * {@link Mandatory}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface ObjectTypesMandatory extends NullableObjectTypesWithNonNullDefault {

	@Mandatory
	@Override
	String getString();

	@Mandatory
	@Override
	Date getDate();

	@Mandatory
	@Override
	DemoEnum getEnum();

	@Mandatory
	@Override
	Class<?> getJavaClass();

	@Mandatory
	@Override
	DemoConfigItem getConfigItem();

	@Mandatory
	@Override
	DemoConfiguredInstance getConfiguredInstance();

	@Mandatory
	@Override
	Object getSingleton();

	@Mandatory
	@Override
	Expr getExpr();

	/**
	 * Property that is set {@link Mandatory} in the configuration.
	 */
	String getStringConfiguredMandatory();

	/**
	 * Demo of a mandatory list property where multiple subtypes can be entered as elements to the
	 * list.
	 */
	@Mandatory
	List<DemoConfigItem> getMandatoryPolymorphicList();

	/**
	 * Demo of a mandatory map property where multiple subtypes can be entered as entries to the
	 * map.
	 */
	@Key(DemoConfigItem.FIRST)
	@Mandatory
	Map<String, DemoConfigItem> getMandatoryPolymorphicMap();

}
