/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider.generic;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.model.TLModel;
import com.top_logic.util.model.CompatibilityService;
import com.top_logic.util.model.ModelService;

/**
 * Cache {@link ManagedClass service} for {@link TLModel} related information for the
 * {@link GenericTableConfigurationProvider}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@ServiceDependencies({
	CompatibilityService.Module.class,
	ModelService.Module.class,
	PersistencyLayer.Module.class
})
public class TableConfigModelService extends ConfiguredManagedClass<TableConfigModelService.Config> {

	/**
	 * {@link com.top_logic.basic.module.ManagedClass.ServiceConfiguration} for the
	 * {@link TableConfigModelService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<TableConfigModelService> {

		/** The name of the {@link #getModelInfoProvider()} property. */
		String MODEL_INFO_PROVIDER = "model-info-provider";

		/**
		 * The {@link TableConfigModelInfoProvider} to use.
		 */
		@Name(MODEL_INFO_PROVIDER)
		@NonNullable
		@ItemDefault
		@ImplementationClassDefault(TableConfigModelCache.class)
		PolymorphicConfiguration<TableConfigModelInfoProvider> getModelInfoProvider();

	}

	private final TableConfigModelInfoProvider _modelInfoProvider;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link TableConfigModelService}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public TableConfigModelService(InstantiationContext context, Config config) {
		super(context, config);
		_modelInfoProvider = context.getInstance(config.getModelInfoProvider());
	}

	/**
	 * The configured {@link TableConfigModelInfoProvider}.
	 * 
	 * @return Never null.
	 */
	public TableConfigModelInfoProvider getModelInfoProvider() {
		return _modelInfoProvider;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("config", getConfig())
			.build();
	}

	/**
	 * The singleton {@link TableConfigModelService} instance.
	 */
	public static TableConfigModelService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * {@link TypedRuntimeModule} of the {@link TableConfigModelService}.
	 */
	public static final class Module extends TypedRuntimeModule<TableConfigModelService> {

		/**
		 * Singleton module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<TableConfigModelService> getImplementation() {
			return TableConfigModelService.class;
		}

	}

}
