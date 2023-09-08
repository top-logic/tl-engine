/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperValueAnalyzer;
import com.top_logic.knowledge.wrap.WrapperValueFactory;

/**
 * @author     <a href="mailto:cca@top-logic.com">cca</a>
 */
public class ObjectProviderDataSource extends AbstractConfiguredInstance<ObjectProviderDataSource.Config>
		implements ChartDataSource<DataContext> {

	/**
	 * Implementation of {@link ConfigurationValueProvider} that supports all types that can be
	 * serialized / deserialized as JSON-String including {@link Wrapper}.
	 */
	public static class JSONValueProvider extends AbstractConfigurationValueProvider<Object> {

		/**
		 * Singleton <code>INSTANCE</code> of {@link JSONValueProvider}
		 */
		public static JSONValueProvider INSTANCE = new JSONValueProvider();

		/**
		 * Creates a new {@link JSONValueProvider} - should not be necessary, use
		 * {@link JSONValueProvider#INSTANCE}
		 * 
		 */
		public JSONValueProvider() {
			super(Object.class);
		}

		@Override
		protected Object getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			try {
				return JSON.fromString(propertyValue, WrapperValueFactory.WRAPPER_INSTANCE);
			} catch (ParseException ex) {
				return null;
			}
		}

		@Override
		protected String getSpecificationNonNull(Object configValue) {
			return JSON.toString(WrapperValueAnalyzer.WRAPPER_INSTANCE, configValue);
		}

	}

	/**
	 * Config-interface for {@link ObjectProviderDataSource}.
	 */
	public interface Config extends PolymorphicConfiguration<ObjectProviderDataSource> {

		/**
		 * The objects to provide.
		 * 
		 * @see ChartDataSource#getRawData(DataContext)
		 */
		@Format(JSONValueProvider.class)
		public List<? extends Object> getObjects();

		/**
		 * see {@link #getObjects()}
		 */
		public void setObjects(List<? extends Object> objects);
	}

	/**
	 * Config-Constructor for {@link ObjectProviderDataSource}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public ObjectProviderDataSource(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Collection<? extends Object> getRawData(DataContext context) {
		return getConfig().getObjects();
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item(List<? extends Object> objects) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setObjects(objects);
		return item;
	}

	/**
	 * Factory method to create an initialized {@link ObjectProviderDataSource}.
	 * 
	 * @return a new ObjectProviderDataSource.
	 */
	public static ObjectProviderDataSource instance(List<? extends Object> objects) {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(item(objects));
	}

}
