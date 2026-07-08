/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.config;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.util.model.ModelService;

/**
 * Configuration fragment referencing a {@link TLModule#getSingleton(String) module singleton} by
 * the module name and the singleton name.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	ModuleSingletonConfig.MODULE,
	ModuleSingletonConfig.SINGLETON_NAME,
})
public interface ModuleSingletonConfig extends ConfigurationItem {

	/** Configuration name of {@link #getModule()}. */
	String MODULE = "module";

	/** Configuration name of {@link #getSingletonName()}. */
	String SINGLETON_NAME = "singleton-name";

	/**
	 * The name of the module that contains the singleton.
	 */
	@Mandatory
	@Options(fun = ModulesWithSingleton.class, mapping = TLModelPartMapping.class)
	@Name(MODULE)
	String getModule();

	/**
	 * The name of the singleton within {@link #getModule()}.
	 *
	 * <p>
	 * Defaults to the {@link TLModule#DEFAULT_SINGLETON_NAME default singleton} of the module.
	 * </p>
	 */
	@StringDefault(TLModule.DEFAULT_SINGLETON_NAME)
	@Options(fun = SingletonNames.class, args = { @Ref(MODULE) })
	@Name(SINGLETON_NAME)
	String getSingletonName();

	/**
	 * {@link Function0} delivering all {@link TLModule}s that have singletons.
	 */
	class ModulesWithSingleton extends Function0<Collection<TLModule>> {

		@Override
		public Collection<TLModule> apply() {
			return ModelService.getApplicationModel().getModules()
				.stream()
				.filter(module -> !module.getSingletons().isEmpty())
				.toList();
		}
	}

	/**
	 * {@link Function1} delivering all singleton names for a given {@link TLModule}.
	 */
	class SingletonNames extends Function1<Collection<String>, String> {

		@Override
		public Collection<String> apply(String moduleName) {
			if (StringServices.isEmpty(moduleName)) {
				return Collections.emptyList();
			}
			TLModel model = ModelService.getApplicationModel();
			TLModule module = model.getModule(moduleName);
			if (module == null) {
				return Collections.emptyList();
			}
			return module.getSingletons()
				.stream()
				.map(TLModuleSingleton::getName)
				.toList();
		}
	}

}
