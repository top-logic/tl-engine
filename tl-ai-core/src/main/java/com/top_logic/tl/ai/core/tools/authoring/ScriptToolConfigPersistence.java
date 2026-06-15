/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.authoring;

import java.io.File;
import java.io.IOError;
import java.io.StringWriter;
import java.util.Collections;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.layout.admin.component.OverrideConfigurationWriter;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * Persists a modified {@link ToolRegistryService} configuration as an autoconf overlay file,
 * exactly as the Service Editor's save handler does, then reloads {@link ApplicationConfig}.
 *
 * <p>
 * The overlay activates the change only after the next application restart, because
 * {@link ToolRegistryService} builds its tool map once at startup and is not restarted by the
 * reload.
 * </p>
 */
public class ScriptToolConfigPersistence {

	private static final String APPLICATION_ROOT_TAG = "application";

	/**
	 * Writes the given registry configuration to
	 * {@code WEB-INF/autoconf/com.top_logic.tl.ai.tool.api.registry.ToolRegistryService.config.xml}
	 * and reloads the application configuration.
	 *
	 * @param serviceConfig
	 *        The complete {@link ToolRegistryService} configuration; it fully replaces any
	 *        existing overlay entry for this service on the next restart.
	 */
	public static void persist(ToolRegistryService.Config<?> serviceConfig) {
		try {
			File file = overlayFile();
			FileUtilities.enforceDirectory(file.getParentFile());

			ApplicationConfig.Config app = TypedConfiguration.newConfigItem(ApplicationConfig.Config.class);
			ModuleConfiguration module = TypedConfiguration.newConfigItem(ModuleConfiguration.class);
			module.setServiceClass(ToolRegistryService.class);
			module.setInstance((ServiceConfiguration<?>) serviceConfig);
			app.getServices().put(ToolRegistryService.class, module);

			String xml;
			try (StringWriter out = new StringWriter()) {
				try (ConfigurationWriter writer =
					new OverrideConfigurationWriter(out, Collections.singleton(TypedRuntimeModule.ModuleConfiguration.class))) {
					TypedConfiguration.serialize(APPLICATION_ROOT_TAG, app, writer);
				}
				xml = out.toString();
			}

			FileUtilities.writeStringToFile(xml, file, StringServices.UTF8);
			TLServiceUtils.reloadConfigurations();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to persist TL-Script tool configuration: " + ex.getMessage(), ex);
		} catch (IOError err) {
			throw new RuntimeException(
				"Failed to reload application configuration after persisting tool config: " + err.getMessage(), err);
		}
	}

	private static File overlayFile() {
		return new File(XMLProperties.Setting.resolveAutoconfFolder(),
			ToolRegistryService.class.getName() + ".config.xml");
	}
}
