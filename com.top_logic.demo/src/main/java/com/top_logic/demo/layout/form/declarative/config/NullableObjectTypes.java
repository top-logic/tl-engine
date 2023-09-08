/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import java.util.Date;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.demo.layout.form.declarative.values.DemoConfigItem;
import com.top_logic.demo.layout.form.declarative.values.DemoConfiguredInstance;
import com.top_logic.demo.layout.form.declarative.values.DemoEnum;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link TypeDemos} part for demonstrating how primitive values are displayed when they are
 * {@link Nullable}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@DisplayOrder({
	NullableObjectTypes.STRING,
	NullableObjectTypes.DATE,
	NullableObjectTypes.ENUM,
	NullableObjectTypes.JAVA_CLASS,
	NullableObjectTypes.SINGLETON,
	NullableObjectTypes.EXPR,
	NullableObjectTypes.CONFIG_ITEM,
	NullableObjectTypes.CONFIGURED_INSTANCE,
})
public interface NullableObjectTypes extends ConfigurationItem {

	/** Configuration name for {@link #getString()}. */
	String STRING = "string";

	/** Configuration name for {@link #getDate()}. */
	String DATE = "date";

	/** Configuration name for {@link #getEnum()}. */
	String ENUM = "enum";

	/** Configuration name for {@link #getJavaClass()}. */
	String JAVA_CLASS = "java-class";

	/** Configuration name for {@link #getConfigItem()}. */
	String CONFIG_ITEM = "config-item";

	/** Configuration name for {@link #getConfiguredInstance()}. */
	String CONFIGURED_INSTANCE = "configured-instance";

	/** Configuration name for {@link #getSingleton()}. */
	String SINGLETON = "singleton";

	/** Configuration name for {@link #getExpr()}. */
	String EXPR = "expr";

	/**
	 * An example for a {@link String} value.
	 */
	@Nullable
	@Name(STRING)
	String getString();

	/**
	 * An example for a {@link Date} value.
	 */
	@Nullable
	@Name(DATE)
	Date getDate();

	/**
	 * An example for a {@link DemoEnum} value.
	 */
	@Nullable
	@Name(ENUM)
	DemoEnum getEnum();

	/**
	 * An example for a JAVA class object value.
	 */
	@Nullable
	@Name(JAVA_CLASS)
	Class<?> getJavaClass();

	/**
	 * An example for a {@link DemoConfigItem} displayed without polymorphism.
	 */
	@Nullable
	@Name(CONFIG_ITEM)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	DemoConfigItem getConfigItem();

	/**
	 * An example for a {@link DemoConfiguredInstance}.
	 */
	@Nullable
	@Name(CONFIGURED_INSTANCE)
	DemoConfiguredInstance getConfiguredInstance();

	/**
	 * A singleton object which is displayed as "value".
	 */
	@Nullable
	@InstanceFormat
	@Name(SINGLETON)
	@ItemDisplay(ItemDisplayType.VALUE)
	@Subtypes({})
	Object getSingleton();

	/**
	 * An example for a {@link Expr}.
	 */
	@Nullable
	@Name(EXPR)
	Expr getExpr();

}
