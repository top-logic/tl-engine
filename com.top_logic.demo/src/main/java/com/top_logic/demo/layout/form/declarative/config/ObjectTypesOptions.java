/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import java.util.Date;
import java.util.List;

import com.top_logic.basic.config.CommaSeparatedEnum;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.demo.layout.form.declarative.config.options.DemoClassOptions;
import com.top_logic.demo.layout.form.declarative.config.options.DemoConfigItemOptions;
import com.top_logic.demo.layout.form.declarative.config.options.DemoConfiguredInstanceOptions;
import com.top_logic.demo.layout.form.declarative.config.options.DemoDateOptions;
import com.top_logic.demo.layout.form.declarative.config.options.DemoEnumOptions;
import com.top_logic.demo.layout.form.declarative.config.options.DemoStringOptions;
import com.top_logic.demo.layout.form.declarative.values.DemoConfigItem;
import com.top_logic.demo.layout.form.declarative.values.DemoConfiguredInstance;
import com.top_logic.demo.layout.form.declarative.values.DemoEnum;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * {@link TypeDemos} part for demonstrating how object values are displayed when they have
 * {@link Options}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface ObjectTypesOptions extends NullableObjectTypesWithNonNullDefault {

	@Options(fun = DemoStringOptions.class)
	@Override
	String getString();

	@Options(fun = DemoDateOptions.class)
	@Override
	Date getDate();

	@Options(fun = DemoEnumOptions.class)
	@Override
	DemoEnum getEnum();

	/** @see #getEnums() */
	String ENUMS = "enums";

	/**
	 * A property for a {@link List} of {@link DemoEnum}s.
	 */
	@Name(ENUMS)
	@Nullable
	@Options(fun = DemoEnumOptions.class)
	@Format(CommaSeparatedDemoEnums.class)
	List<DemoEnum> getEnums();

	/**
	 * {@link CommaSeparatedEnum} for {@link DemoEnum}.
	 */
	public static class CommaSeparatedDemoEnums extends CommaSeparatedEnum<DemoEnum> {

		/** Singleton instance. */
		public static final CommaSeparatedDemoEnums INSTANCE = new CommaSeparatedDemoEnums();

		private CommaSeparatedDemoEnums() {
			super(DemoEnum.class);
		}
	}

	@Options(fun = DemoClassOptions.class)
	@Override
	Class<?> getJavaClass();

	@Options(fun = DemoConfigItemOptions.class)
	@ItemDisplay(ItemDisplayType.VALUE)
	@Override
	DemoConfigItem getConfigItem();

	@Options(fun = DemoConfiguredInstanceOptions.class)
	@ItemDisplay(ItemDisplayType.VALUE)
	@Override
	DemoConfiguredInstance getConfiguredInstance();

	/**
	 * Value with a built-in option provider.
	 */
	@InstanceFormat
	DemoValue getDemoValue();

}
