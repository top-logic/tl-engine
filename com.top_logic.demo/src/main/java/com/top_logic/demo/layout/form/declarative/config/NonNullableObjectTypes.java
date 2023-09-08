/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.demo.layout.form.declarative.config.options.DemoConfigItemOptions;
import com.top_logic.demo.layout.form.declarative.config.options.DemoConfiguredInstanceOptions;
import com.top_logic.demo.layout.form.declarative.values.DemoConfigItem;
import com.top_logic.demo.layout.form.declarative.values.DemoConfiguredInstance;
import com.top_logic.demo.layout.form.declarative.values.DemoDefaultConfiguredInstance;
import com.top_logic.demo.layout.form.declarative.values.DemoEnum;
import com.top_logic.demo.layout.form.declarative.values.DemoPolymorphicConfig;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link TypeDemos} part for demonstrating how object values are displayed when they are
 * {@link NonNullable}. This requires them to have a non-null default value.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface NonNullableObjectTypes extends NullableObjectTypesWithNonNullDefault {

	/**
	 * @see #getValueItems()
	 */
	String VALUE_ITEMS = "value-items";

	/**
	 * @see #getValueInstances()
	 */
	String VALUE_INSTANCES = "value-instances";

	@NonNullable
	@Override
	String getString();

	@NonNullable
	@Override
	Date getDate();

	@NonNullable
	@Override
	DemoEnum getEnum();

	@NonNullable
	@Override
	Class<?> getJavaClass();

	@NonNullable
	@Override
	DemoConfigItem getConfigItem();

	/**
	 * A monomorphic list.
	 */
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	List<DemoConfigItem> getMonomorphicList();

	/**
	 * Demo of a list property where multiple subtypes can be entered as elements to the list.
	 */
	List<DemoConfigItem> getPolymorphicList();

	/**
	 * A monomorphic map.
	 */
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	@Key(DemoConfigItem.FIRST)
	Map<String, DemoConfigItem> getMonomorphicMap();

	/**
	 * Demo of a map property where multiple subtypes can be entered as entries to the map.
	 */
	@Key(DemoConfigItem.FIRST)
	Map<String, DemoConfigItem> getPolymorphicMap();

	/**
	 * Demo of a map property with subtype options indexed by type.
	 */
	@Key(DemoConfigItem.CONFIGURATION_INTERFACE_NAME)
	Map<String, DemoConfigItem> getPolymorphicTypeMap();

	/**
	 * Demo for a list of {@link ConfiguredInstance}s.
	 */
	List<DemoConfiguredInstance> getInstanceList();

	/**
	 * Demo for a map of {@link ConfiguredInstance}s indexed by one of their common properties.
	 */
	@Key(DemoPolymorphicConfig.FIRST)
	Map<String, DemoDefaultConfiguredInstance> getInstanceMap();

	/**
	 * Demo for displaying a plain select field for entering a list of pre-configured items.
	 */
	@Name(VALUE_ITEMS)
	@ItemDisplay(ItemDisplayType.VALUE)
	@Options(fun = DemoConfigItemOptions.class)
	List<DemoConfigItem> getValueItems();

	/**
	 * Demo for displaying a plain select field for entering a list of pre-configured instances.
	 */
	@Name(VALUE_INSTANCES)
	@ItemDisplay(ItemDisplayType.VALUE)
	@Options(fun = DemoConfiguredInstanceOptions.class)
	List<DemoConfiguredInstance> getValueInstances();

	@NonNullable
	@Override
	DemoConfiguredInstance getConfiguredInstance();

	@NonNullable
	@Override
	Object getSingleton();

	@NonNullable
	@Override
	Expr getExpr();

}
