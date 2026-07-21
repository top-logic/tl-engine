/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.tool.boundsec.securityObjectProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLObject;
import com.top_logic.model.config.TLModelPartMapping;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.util.model.ModelService;

/**
 * {@link SecurityObjectProvider} that uses the singleton of a {@link TLModule} as security object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 * 
 * @see SecurityRootObjectProvider
 */
@InApp
@Label("Singleton of a module")
public class ModuleSingletonSecurityProvider extends AbstractConfiguredInstance<ModuleSingletonSecurityProvider.Config>
		implements SecurityObjectProvider {

	/**
	 * Typed configuration interface definition for {@link ModuleSingletonSecurityProvider}.
	 */
	@DisplayOrder({
		Config.MODULE,
		Config.SINGLETON_NAME,
	})
	public interface Config extends PolymorphicConfiguration<ModuleSingletonSecurityProvider> {

		/** Configuration name of {@link #getModule()}. */
		String MODULE = "module";

		/** Configuration name of {@link #getSingletonName()}. */
		String SINGLETON_NAME = "singleton-name";

		/**
		 * The module which contains the singleton that should serve as security object.
		 */
		@Mandatory
		@Options(fun = ModulesWithSingleton.class, mapping = TLModelPartMapping.class)
		@Name(MODULE)
		String getModule();
		
		/**
		 * The name of the singleton in {@link #getModule()}.
		 */
		@StringDefault(TLModule.DEFAULT_SINGLETON_NAME)
		@Options(fun = SingletonNames.class, args = { @Ref(MODULE) })
		@Name(SINGLETON_NAME)
		String getSingletonName();
		
		/**
		 * {@link Function0} delivering all {@link TLModule} which have singletons.
		 */
		class ModulesWithSingleton extends Function0<Collection<TLModule>>{

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

	/**
	 * Create a {@link ModuleSingletonSecurityProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ModuleSingletonSecurityProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		return (BoundObject) getSingleton();
	}

	@Override
	public Set<TLClass> getPossibleSecurityObjectTypes() {
		return Collections.singleton((TLClass) getSingleton().tType());
	}

	private TLObject getSingleton() {
		Config config = getConfig();
		String moduleName = config.getModule();
		String singletonName = config.getSingletonName();

		TLModule module = ModelService.getApplicationModel().getModule(moduleName);
		if (module == null) {
			throw new ConfigurationError(I18NConstants.INVALID_SINGLETON__SINGLETON.fill(moduleName + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + singletonName));
		}
		TLObject singleton = module.getSingleton(singletonName);
		if (singleton == null) {
			throw new ConfigurationError(I18NConstants.INVALID_SINGLETON__SINGLETON
				.fill(moduleName + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + singletonName));
		}
		return singleton;
	}

}

