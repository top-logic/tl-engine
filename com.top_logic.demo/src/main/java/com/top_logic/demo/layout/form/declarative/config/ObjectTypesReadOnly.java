/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import java.util.Date;

import com.top_logic.basic.config.ConfigurationItemDefaultProvider;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.basic.config.annotation.ReadOnly.ReadOnlyMode;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.demo.layout.form.declarative.values.DemoConfigItem;
import com.top_logic.demo.layout.form.declarative.values.DemoConfiguredInstance;
import com.top_logic.demo.layout.form.declarative.values.DemoEnum;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.ItemEditor;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link TypeDemos} part for demonstrating how object values are displayed when they are
 * {@link ReadOnly}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface ObjectTypesReadOnly extends NullableObjectTypesWithNonNullDefault {

	@ReadOnly
	@Override
	String getString();

	@ReadOnly
	@Override
	Date getDate();

	@ReadOnly
	@Override
	DemoEnum getEnum();

	@ReadOnly
	@Override
	Class<?> getJavaClass();

	@ReadOnly
	@Override
	@ComplexDefault(DemoConfigItemDefault.class)
	DemoConfigItem getConfigItem();

	@ReadOnly
	@Override
	Expr getExpr();

	/**
	 * Allows editing the contents of a referenced other item.
	 */
	@Name("config-item-view")
	@DerivedRef(CONFIG_ITEM)
	@ReadOnly(ReadOnlyMode.LOCAL)
	// Explicit editor annotation required, since derived properties are displayed as value only by
	// default.
	@PropertyEditor(ItemEditor.class)
	// Explicit item display annotation required to prevent the value rendering with label and
	// content.
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	DemoConfigItem getConfigItemView();

	@ReadOnly
	@Override
	DemoConfiguredInstance getConfiguredInstance();

	@ReadOnly
	@Override
	Object getSingleton();

	/**
	 * {@link DefaultValueProvider} creating a {@link DemoConfigItem}.
	 */
	class DemoConfigItemDefault extends ConfigurationItemDefaultProvider<DemoConfigItem> {
		public DemoConfigItemDefault() {
			super(DemoConfigItem.class);
		}

		@Override
		protected void intitializeConfiguration(DemoConfigItem config) {
			config.setFirst("X");
			config.setSecond("Y");
		}
	}
}
