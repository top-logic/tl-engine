/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.DefaultLabelProvider;

/**
 * Demo for a custom value with options in a declarative form.
 * 
 * @see ObjectTypesOptions#getDemoValue()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Options(fun = DemoValue.OptionsProvider.class)
@OptionLabels(DefaultLabelProvider.class)
@Format(DemoValue.ConfigFormat.class)
public class DemoValue {

	private final String _name;

	/**
	 * Creates a {@link DemoValue}.
	 */
	public DemoValue(String name) {
		_name = name;
	}

	@Override
	public String toString() {
		return "Demo-" + getName();
	}

	/**
	 * The name of the value.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Function producing options for {@link DemoValue}-typed properties.
	 */
	public static class OptionsProvider extends Function0<List<DemoValue>> {

		private List<DemoValue> _list = Arrays.asList(new DemoValue("A"), new DemoValue("B"), new DemoValue("C"));

		@Override
		public List<DemoValue> apply() {
			return _list;
		}

	}

	/**
	 * {@link ConfigurationValueProvider} for {@link DemoValue}s.
	 */
	public static class ConfigFormat extends AbstractConfigurationValueProvider<DemoValue> {
	
		/**
		 * Singleton {@link DemoValue.ConfigFormat} instance.
		 */
		public static final DemoValue.ConfigFormat INSTANCE = new DemoValue.ConfigFormat();
	
		private ConfigFormat() {
			super(DemoValue.class);
		}
	
		@Override
		protected DemoValue getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			return new DemoValue(propertyValue.toString());
		}
	
		@Override
		protected String getSpecificationNonNull(DemoValue configValue) {
			return configValue.getName();
		}
	}

}
