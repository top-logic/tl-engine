/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.element.ElementException;
import com.top_logic.element.config.DefinitionReader;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.ObjectTypeConfig;
import com.top_logic.element.config.RoleAssignment;
import com.top_logic.element.config.SingletonConfig;
import com.top_logic.element.model.DynamicModelService.Config;
import com.top_logic.model.config.TLModuleAnnotation;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.v5.transform.ModelLayout;
import com.top_logic.util.list.ListInitializationUtil;

/**
 * Algorithm for loading a unified {@link ModelConfig} from the application configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModelConfigLoader {

	private DynamicModelService.Config<?> _serviceConfig;

	private ModelConfig _modelConfig;

	/**
	 * Creates a {@link ModelConfigLoader}.
	 */
	public ModelConfigLoader() {
		super();
	}

	/**
	 * Load and combine a {@link ModelConfig} from the given application configuration.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} to create configuration items in.
	 * @param serviceConfig
	 *        The application configuration.
	 * @return The loaded {@link ModelConfig}.
	 */
	public ModelConfig load(InstantiationContext context, DynamicModelService.Config<?> serviceConfig) {
		_serviceConfig = serviceConfig;
		_modelConfig = parseModel(context);
		if (_modelConfig == null) {
			return null;
		}

		try {
			createLegacyEnumModule();
		} catch (IOException ex) {
			throw new IOError(ex);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Invalid legacy enum configuration.", ex);
		}

		// Check that settings are only given for defined modules.
		for (Config.ModuleSetting setting : _serviceConfig.getSettings().values()) {
			String moduleName = setting.getName();
			if (_modelConfig.getModule(moduleName) == null) {
				context.error("No module '" + moduleName + "' defined. Invalid module setting in '"
					+ setting.location() + "'.");
			}
		}

		// Apply (external) settings to modules.
		for (Iterator<ModuleConfig> it = _modelConfig.getModules().iterator(); it.hasNext();) {
			ModuleConfig module = it.next();
			String moduleName = module.getName();

			Config.ModuleSetting setting = settingOptional(moduleName);
			if (setting != null) {
				if (!setting.isEnabled()) {
					context.info("Skipped disabled module '" + moduleName + "'.");
					it.remove();
				} else {
					for (TLModuleAnnotation annotation : setting.getAnnotations()) {
						module.getAnnotations().removeIf(
							a -> a.descriptor().getConfigurationInterface() == annotation.getConfigurationInterface());
						module.getAnnotations().add(TypedConfiguration.copy(annotation));
					}
				}
			}

			DynamicModelService.addTLObjectExtension(module);
		}

		resolveUnqualifiedTypes(context, _modelConfig);

		try {
			context.checkErrors();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Unable to start dynamic model service.", ex);
		}
		return _modelConfig;
	}

	private void createLegacyEnumModule() throws IOException, ConfigurationException {
		ModuleConfig legacyEnumModuleConfig = TypedConfiguration.newConfigItem(ModuleConfig.class);
		legacyEnumModuleConfig.setName(ModelLayout.TL5_ENUM_MODULE);

		ListInitializationUtil.loadLegacyEnums(legacyEnumModuleConfig, _serviceConfig.getClassifications().values());

		_modelConfig.getModules().add(legacyEnumModuleConfig);
	}

	/**
	 * Fix "me:[TypeName]" references that are not qualified by a module name.
	 * 
	 * @param modelConfig
	 *        The {@link ModelConfig} to operate on.
	 */
	public static void resolveUnqualifiedTypes(InstantiationContext context, ModelConfig modelConfig) {
		Map<String, String> moduleOfGlobalType = new HashMap<>();
		fillGlobalTypes(modelConfig, moduleOfGlobalType);
	}

	/**
	 * Compute qualifying prefixes for all global types.
	 * 
	 * @param result
	 *        Mapping of local type name to the name of its defining module (if unambiguous).
	 */
	private static void fillGlobalTypes(ModelConfig modelConfig, Map<String, String> result) {
		List<String> ambiguous = new ArrayList<>();

		for (ModuleConfig module : modelConfig.getModules()) {
			for (TypeConfig type : module.getTypes()) {
				if (!(type instanceof ObjectTypeConfig)) {
					continue;
				}

				String clash = result.put(type.getName(), module.getName());
				if (clash != null) {
					if (clash.equals(module.getName())) {
						ambiguous.add(type.getName());
					}
				}
			}
		}

		result.keySet().removeAll(ambiguous);
	}

	private void applyRoleAssignments(SingletonConfig setting, SingletonConfig definition) {
		if (!setting.getRoleAssignments().isEmpty()) {
			PropertyDescriptor definitionProperty =
				definition.descriptor().getProperty(SingletonConfig.ROLE_ASSIGNMENTS);
			definition.update(definitionProperty, combineRoleAssignments(setting, definition));
		}
	}

	private List<RoleAssignment> combineRoleAssignments(SingletonConfig setting, SingletonConfig definition) {
		Collection<RoleAssignment> settingRoles = setting.getRoleAssignments();
		Collection<RoleAssignment> definitionRoles = definition.getRoleAssignments();

		int combinedSize = settingRoles.size() + definitionRoles.size();
		List<RoleAssignment> combinedRoles = new ArrayList<>(combinedSize);
		combinedRoles.addAll(definitionRoles);
		combinedRoles.addAll(settingRoles);
		return combinedRoles;
	}

	private ModelConfig parseModel(final InstantiationContext context) {
		ModelConfig modelPart = null;
		for (Config.DeclarationConfig declaration : _serviceConfig.getDeclarations()) {
			modelPart = parseModelPart(context, modelPart, declaration.getFile());
		}
		return modelPart;
	}

	private ModelConfig parseModelPart(InstantiationContext context, ModelConfig base, String modelDefinition) {
		FileManager fileManager = FileManager.getInstance();
		final String resource = modelDefinition;
		BinaryContent content = fileManager.getData(resource);
		ModelConfig result;
		try {
			result =
				DefinitionReader.readElementConfig(context, content, base, true);
		} catch (IOException e) {
			throw new ElementException("Problem reading configuration '" + content + "'.", e);
		}

		return result;
	}

	private Config.ModuleSetting settingOptional(String moduleName) {
		return _serviceConfig.getSettings().get(moduleName);
	}

}
