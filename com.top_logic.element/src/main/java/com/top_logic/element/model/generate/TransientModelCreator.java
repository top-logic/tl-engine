/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.generate;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.DynamicModelService.Config;
import com.top_logic.element.model.ModelConfigLoader;
import com.top_logic.element.model.ModelResolver;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * @since 5.8.0
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TransientModelCreator {

	private Protocol _log;

	private TLModel _model;

	private InstantiationContext _context;

	private ModelResolver _modelResolver;

	private ModelConfig _modelConfig;

	public TransientModelCreator(Protocol log, TLModel model, TLFactory factory) {
		_log = log;
		_model = model;
		_context = new DefaultInstantiationContext(log);
		_modelResolver = new ModelResolver(log, _model, factory);
		Config<?> serviceConfig;
		try {
			serviceConfig = serviceConfig();
		} catch (ConfigurationException ex) {
			_log.error("Unable to get model service configuration.");
			serviceConfig = TypedConfiguration.newConfigItem(DynamicModelService.Config.class);
		}
		_modelConfig = new ModelConfigLoader().load(_context, serviceConfig);
	}

	private Config<?> serviceConfig() throws ConfigurationException {
		ServiceConfiguration<ModelService> modelServiceConfig =
			ApplicationConfig.getInstance().getServiceConfiguration(ModelService.class);
		if (modelServiceConfig instanceof DynamicModelService.Config<?>) {
			return (Config<?>) modelServiceConfig;
		}
		throw new ConfigurationException("Configuration of ModelService is not a " + Config.class.getName());
	}

	public void fillModel() {
		// Build model.
		setupModules();
		_modelResolver.complete();

	}

	private void setupModules() {
		for (ModuleConfig moduleConf : _modelConfig.getModules()) {
			_log.info("Setting up module '" + moduleConf.getName() + "'.");
			setupModule(moduleConf);
		}
	}

	private TLModule setupModule(ModuleConfig moduleConf) {
		String moduleName = moduleConf.getName();
		TLModule module = TLModelUtil.makeModule(_model, moduleName);

		// Global MetaElements
		_modelResolver.setupScope(module, module, moduleConf);
		return module;
	}


}

