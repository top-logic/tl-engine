/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import java.util.Date;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.demo.layout.form.declarative.values.DemoConfigItem;
import com.top_logic.demo.layout.form.declarative.values.DemoConfiguredInstance;
import com.top_logic.demo.layout.form.declarative.values.DemoDefaultConfiguredInstance;
import com.top_logic.demo.layout.form.declarative.values.DemoEnum;
import com.top_logic.demo.layout.form.declarative.values.DemoPolymorphicConfig;
import com.top_logic.demo.layout.form.declarative.values.DemoSingletonFirst;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link TypeDemos} part for demonstrating how object values are displayed when they are
 * {@link Nullable} but have a non-null default value.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface NullableObjectTypesWithNonNullDefault extends NullableObjectTypes {

	@StringDefault("Demo")
	@Override
	String getString();

	@FormattedDefault("2000-01-01")
	@Override
	Date getDate();

	@Override
	DemoEnum getEnum();

	@ClassDefault(Object.class)
	@Override
	Class<?> getJavaClass();

	@ItemDefault
	@Override
	DemoConfigItem getConfigItem();

	@ComplexDefault(DemoConfiguredInstanceDefault.class)
	@Override
	DemoConfiguredInstance getConfiguredInstance();

	@InstanceDefault(DemoSingletonFirst.class)
	@Override
	Object getSingleton();

	@Override
	@FormattedDefault("x -> $x")
	Expr getExpr();

	/**
	 * {@link DefaultValueProvider} creating a {@link DemoConfiguredInstance}.
	 */
	class DemoConfiguredInstanceDefault extends DefaultValueProvider {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			DemoPolymorphicConfig config = TypedConfiguration.newConfigItem(DemoPolymorphicConfig.class);
			config.setImplementationClass(DemoDefaultConfiguredInstance.class);
			config.setFirst("A");
			config.setSecond("B");
			return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		}
	}
}
