/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.authoring;

import java.io.File;
import java.io.IOError;
import java.io.StringWriter;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.tl.ai.core.tools.interaction.ToolRegistryReloadFlag;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService.ToolFactoryConfig;

/**
 * Persists an additive overlay containing only {@link ScriptToolFactory} entries, then reloads
 * {@link ApplicationConfig} and restarts the {@link ToolRegistryService} so that the changed tool
 * definitions become immediately active.
 *
 * <p>
 * Only agent-authored factories (those backed by {@link ScriptToolFactory}) are written to the
 * overlay. All other factories flow through unchanged from the base configuration. A seed overlay
 * file must exist at boot so the path is registered in the webapp resource resolver.
 * </p>
 */
public class ScriptToolConfigPersistence {

	/**
	 * Extracts only the {@link ScriptToolFactory} entries from the given full configuration,
	 * writes them as an additive overlay, and restarts the service.
	 *
	 * @param fullConfig
	 *        The complete {@link ToolRegistryService} configuration (from
	 *        {@code ApplicationConfig}); only {@link ScriptToolFactory} entries are extracted for the
	 *        overlay.
	 */
	public static void persist(ToolRegistryService.Config<?> fullConfig) {
		try {
			ToolRegistryService.Config<?> overlay = extractScriptToolFactories(fullConfig);
			persistTo(overlayFile(), overlay);
			TLServiceUtils.reloadConfigurations();
			restartToolRegistry();
			ToolRegistryReloadFlag.request();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to persist TL-Script tool configuration: " + ex.getMessage(), ex);
		} catch (IOError err) {
			throw new RuntimeException(
				"Failed to reload application configuration after persisting tool config: " + err.getMessage(), err);
		}
	}

	/**
	 * Extracts only the ScriptToolFactory entries from a full service config into a fresh overlay.
	 */
	private static ToolRegistryService.Config<?> extractScriptToolFactories(
			ToolRegistryService.Config<?> fullConfig) {
		ToolRegistryService.Config<?> overlay =
			TypedConfiguration.newConfigItem(ToolRegistryService.Config.class);
		for (Map.Entry<String, ToolFactoryConfig> entry : fullConfig.getToolFactories().entrySet()) {
			if (entry.getValue().getImplementation() instanceof ScriptToolFactory.Config) {
				overlay.getToolFactories().put(entry.getKey(), entry.getValue());
			}
		}
		return overlay;
	}

	private static void restartToolRegistry() {
		try {
			ModuleUtil.INSTANCE.restart(ToolRegistryService.Module.INSTANCE, null);
		} catch (RestartException ex) {
			Logger.error("Failed to restart ToolRegistryService: " + ex.getMessage(), ex,
				ScriptToolConfigPersistence.class);
		}
	}

	/**
	 * Serializes the overlay as a pretty-printed XML file without reloading or restarting.
	 *
	 * <p>
	 * Prefer {@link #persist(ToolRegistryService.Config)} for normal use; call this method
	 * directly only when reload must be suppressed (e.g. in tests).
	 * </p>
	 */
	public static void persistTo(File file, ToolRegistryService.Config<?> overlay) throws Exception {
		FileUtilities.enforceDirectory(file.getParentFile());

		ApplicationConfig.Config app = TypedConfiguration.newConfigItem(ApplicationConfig.Config.class);
		ModuleConfiguration module = TypedConfiguration.newConfigItem(ModuleConfiguration.class);
		module.setServiceClass(ToolRegistryService.class);
		module.setInstance((ServiceConfiguration<?>) overlay);
		app.getServices().put(ToolRegistryService.class, module);

		String xml;
		try (StringWriter out = new StringWriter()) {
			try (ConfigurationWriter writer = new ConfigurationWriter(out)) {
				TypedConfiguration.serialize("application", app, writer);
			}
			xml = out.toString();
		}

		xml = XMLPrettyPrinter.prettyPrint(xml);
		FileUtilities.writeStringToFile(xml, file, StringServices.UTF8);
	}

	private static File overlayFile() {
		return new File(XMLProperties.Setting.resolveAutoconfFolder(),
			ToolRegistryService.class.getName() + ".config.xml");
	}
}
