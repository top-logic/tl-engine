/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.ConstantProvider;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.form.boxes.layout.BoxLayout;
import com.top_logic.layout.form.boxes.layout.VerticalLayout;
import com.top_logic.layout.form.boxes.model.DefaultCollectionBox;

/**
 * {@link BoxContainerTag} that accepts a generic layout algorithm name defined in the configration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BoxLayoutTag extends AbstractBoxStructureTag {

	/**
	 * Service creating configured {@link BoxLayout} factories from configuration.
	 * 
	 * @see #getLayout(String)
	 */
	public static final class ConfigService extends ManagedClass {
		
		private final Map<String, Provider<BoxLayout>> _layoutFactoryByName;

		/**
		 * Configuration of {@link BoxLayoutTag.ConfigService}.
		 */
		public interface Config extends ServiceConfiguration<ConfigService> {
			
			/**
			 * @see #getLayouts()
			 */
			String LAYOUTS = "layouts";

			/**
			 * Named {@link BoxLayout} configurations.
			 * 
			 * @see LayoutConfig
			 */
			@Key(LayoutConfig.NAME)
			@Name(LAYOUTS)
			Map<String, LayoutConfig> getLayouts();
			
			/**
			 * Named {@link BoxLayout} configuration.
			 */
			public interface LayoutConfig extends ConfigurationItem {

				/**
				 * @see #getName()
				 */
				String NAME = "name";
				
				/**
				 * @see #getImplementation()
				 */
				String IMPLEMENTATION = "implementation";
				
				/**
				 * The name of the layout algorithm to use.
				 * 
				 * @see BoxLayoutTag#setType(String)
				 */
				@Name(NAME)
				@Mandatory
				String getName();

				/**
				 * The {@link BoxLayout} configuration to instantiate whenever a layout with
				 * {@link #getName()} is referenced.
				 * 
				 * @see BoxLayoutTag#setType(String)
				 */
				@Name(IMPLEMENTATION)
				@Mandatory
				PolymorphicConfiguration<BoxLayout> getImplementation();
				
			}
		}

		/**
		 * Creates a {@link BoxLayoutTag.ConfigService} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public ConfigService(InstantiationContext context, Config config) {
			super(context, config);

			Map<String, PolymorphicConfiguration<BoxLayout>> configByName =
				mapValue(config.getLayouts(), Config.LayoutConfig.IMPLEMENTATION);

			_layoutFactoryByName = buildFactoryMap(context, configByName);
		}

		private static <K, C extends ConfigurationItem, V> Map<K, V> mapValue(Map<K, C> configs, String propertyName) {
			HashMap<K, V> result = MapUtil.newMap(configs.size());
			for (Entry<K, C> entry : configs.entrySet()) {
				C config = entry.getValue();
				@SuppressWarnings("unchecked")
				V value = (V) config.value(config.descriptor().getProperty(propertyName));
				result.put(entry.getKey(), value);
			}
			return result;
		}

		private <K, V> Map<K, Provider<V>> buildFactoryMap(InstantiationContext context,
				Map<K, ? extends PolymorphicConfiguration<V>> configurationByName) {
			Map<K, Provider<V>> factoryByName = new HashMap<>();
			for (Entry<K, ? extends PolymorphicConfiguration<V>> entry : configurationByName.entrySet()) {
				final PolymorphicConfiguration<V> implConfig = entry.getValue();
				Class<? extends V> implClass = implConfig.getImplementationClass();

				Factory configurationFactory;
				try {
					configurationFactory = DefaultConfigConstructorScheme.getFactory(implClass);
				} catch (ConfigurationException ex) {
					context.error("Invalid layout class '" + implClass + "'.", ex);
					continue;
				}

				Provider<V> provider;
				if (configurationFactory.isSharedInstance()) {
					V singletonInstance = context.getInstance(implConfig);
					provider = new ConstantProvider<>(singletonInstance);
				} else {
					provider = new Provider<>() {
						@Override
						public V get() {
							InstantiationContext simpleContext = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
							return simpleContext.getInstance(implConfig);
						}
					};
				}
				factoryByName.put(entry.getKey(), provider);
			}
			return factoryByName;
		}

		/**
		 * Create a layout with the given name defined in the configuration.
		 */
		public BoxLayout getLayout(String layoutName) {
			Provider<BoxLayout> factory = _layoutFactoryByName.get(layoutName);
			if (factory == null) {
				throw new IllegalArgumentException("No such layout algorithm '" + layoutName + "'.");
			}
			return factory.get();
		}

		/**
		 * Singleton {@link BoxLayoutTag.ConfigService}.
		 */
		public static final class Module extends TypedRuntimeModule<ConfigService> {

			/**
			 * Singleton {@link BoxLayoutTag.ConfigService.Module} instance.
			 */
			public static final Module INSTANCE = new Module();
			
			private Module() {
				// Singleton constructor.
			}

			@Override
			public Class<ConfigService> getImplementation() {
				return ConfigService.class;
			}
			
		}
	}
	
	/**
	 * XML name of this tag.
	 */
	public static final String LAYOUT_TAG = "form:layout";

	private BoxLayout _layout = VerticalLayout.INSTANCE;

	/**
	 * The name of the layout algorithm to be used.
	 * 
	 * <p>
	 * Available layout algorithm names are given in the configuration section
	 * {@link BoxLayoutTag.ConfigService.Config#getLayouts()}.
	 * </p>
	 * 
	 * @see BoxLayoutTag.ConfigService.Config.LayoutConfig#getName()
	 */
	public void setType(String layoutName) {
		_layout = ConfigService.Module.INSTANCE.getImplementationInstance().getLayout(layoutName);
	}

	@Override
	protected DefaultCollectionBox createCollectionBox() {
		return new DefaultCollectionBox(_layout);
	}

	@Override
	protected String getTagName() {
		return LAYOUT_TAG;
	}

}
