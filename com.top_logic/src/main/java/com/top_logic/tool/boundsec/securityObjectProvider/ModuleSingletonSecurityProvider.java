/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.tool.boundsec.securityObjectProvider;

import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.config.ModuleSingletonConfig;
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
	public interface Config extends PolymorphicConfiguration<ModuleSingletonSecurityProvider>, ModuleSingletonConfig {

		// The module and singleton-name properties (including their option providers) are inherited
		// from ModuleSingletonConfig.

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

