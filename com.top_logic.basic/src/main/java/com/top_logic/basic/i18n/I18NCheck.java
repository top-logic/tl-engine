/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.i18n;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResourcesModule;

/**
 * Service checking the existence of all static resource keys in all languages.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({
	ResourcesModule.Module.class,
})
public class I18NCheck extends ConfiguredManagedClass<I18NCheck.Config> {

	/**
	 * Configuration options for {@link I18NCheck}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<I18NCheck> {

		/**
		 * Configurations of the {@link I18NChecker} to use.
		 */
		@DefaultContainer
		@Key(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME)
		Map<Class<?>, PolymorphicConfiguration<I18NChecker>> getCheckers();
	}

	/**
	 * Creates a {@link I18NCheck} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public I18NCheck(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();

		Map<Class<?>, PolymorphicConfiguration<I18NChecker>> checkerConfigs = getConfig().getCheckers();
		DefaultInstantiationContext ctx = new DefaultInstantiationContext(I18NCheck.class);
		Map<Class<?>, I18NChecker> checkers = TypedConfiguration.getInstanceMap(ctx, checkerConfigs);
		try {
			ctx.checkErrors();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		checkers.values().forEach(I18NChecker::checkI18N);
	}

	/**
	 * Singleton reference for {@link I18NCheck}.
	 */
	public static class Module extends TypedRuntimeModule<I18NCheck> {

		/**
		 * Singleton {@link Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<I18NCheck> getImplementation() {
			return I18NCheck.class;
		}

	}

}
