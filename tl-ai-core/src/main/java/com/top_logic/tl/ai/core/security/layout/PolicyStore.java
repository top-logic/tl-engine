/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.io.File;
import java.io.StringWriter;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.util.autoconf.OverrideConfigurationWriter;
import com.top_logic.util.error.TopLogicException;

/**
 * Writes a tool access policy where the next application start reads it, and restarts the service.
 *
 * <p>
 * The same file, the same override attribute and the same reload the service editor of the engine
 * performs when a service configuration is saved there. Both surfaces therefore write one policy, and
 * an administrator can keep using either.
 * </p>
 */
/*
 * P12 of the concept, and the reason the confirmation says out loud what is written: the file this
 * produces carries config:override="true" for the whole service configuration, so from then on the
 * policy of the configuration files - an aspect, the application's own conf - is shadowed by this
 * snapshot. That is how the engine's service editor has always worked; what was missing was anybody
 * saying so.
 *
 * Deliberately assembled from the engine's own public parts (OverrideConfigurationWriter, the autoconf
 * folder of XMLProperties, TLServiceUtils.reloadConfigurations) rather than from a second mechanism of
 * our own. TLSaveServiceConfigHandler does exactly this, but as an apply handler bound to the service
 * editor's edit model, so it cannot be called from here.
 */
public class PolicyStore {

	/** The root element every application configuration file has. */
	private static final String APPLICATION = "application";

	private PolicyStore() {
		// Static utility.
	}

	/**
	 * Writes the given policy as this application's tool access configuration.
	 *
	 * <p>
	 * Does not change what the running service applies - see {@link #restartService()}. What it does
	 * change is what the next start reads, which is why nothing invalid may get here.
	 * </p>
	 *
	 * @param policy
	 *        The service configuration to store. Never <code>null</code>.
	 * @throws TopLogicException
	 *         If the file cannot be written or the reloaded configuration cannot be read. The previous
	 *         file is then still in place.
	 */
	public static void save(ServiceConfiguration<?> policy) {
		File target = configurationFile();
		try {
			ApplicationConfig.Config file = TypedConfiguration.newConfigItem(ApplicationConfig.Config.class);
			file.getServices().put(AiToolAccessService.class, moduleConfiguration(policy));

			FileUtilities.enforceDirectory(target.getParentFile());
			FileUtilities.writeStringToFile(serialize(file), target, StringServices.UTF8);
			Logger.info("Agent tool access policy written to " + target.getAbsolutePath() + ".",
				PolicyStore.class);
		} catch (Exception ex) {
			throw new TopLogicException(I18NConstants.POLICY_SAVE_FAILED, ex);
		}
		TLServiceUtils.reloadConfigurations();
	}

	/**
	 * Stops the tool access service and starts it again, so that the saved policy takes effect.
	 *
	 * <p>
	 * The labelled action behind the two unlabelled icons of the module list. Every service that
	 * depends on this one is restarted with it, which is what the engine's own restart does.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If the service does not come up. The message is what the administrator has to act on -
	 *         and it arrives here rather than only in the log, which is the point of doing the restart
	 *         from a labelled button.
	 */
	public static void restartService() {
		try {
			ModuleUtil.INSTANCE.restart(AiToolAccessService.Module.INSTANCE, null);
		} catch (RestartException | RuntimeException ex) {
			throw new TopLogicException(I18NConstants.POLICY_RESTART_FAILED, ex);
		}
		if (!AiToolAccessService.Module.INSTANCE.isActive()) {
			// Belt and braces: a start that reported nothing and did not produce a running service must
			// not be reported as success, because every other view would then answer from a service that
			// is not there.
			throw new TopLogicException(I18NConstants.POLICY_RESTART_FAILED);
		}
	}

	/**
	 * The file this writes, which is the one the engine's service editor writes for the same service.
	 */
	public static File configurationFile() {
		return new File(XMLProperties.Setting.resolveAutoconfFolder(),
			AiToolAccessService.class.getName() + ".config.xml");
	}

	private static ModuleConfiguration moduleConfiguration(ServiceConfiguration<?> policy) {
		ModuleConfiguration module = TypedConfiguration.newConfigItem(ModuleConfiguration.class);
		module.setServiceClass(AiToolAccessService.class);
		module.setInstance(policy);
		return module;
	}

	/**
	 * The configuration file's text, with the override attribute on the service entry.
	 *
	 * <p>
	 * The attribute is what makes this file replace the policy of every other configuration file
	 * instead of being merged with it - a merge would union the grant lists, and an administrator who
	 * removed a grant here would find it still there.
	 * </p>
	 */
	private static String serialize(ConfigurationItem file) throws Exception {
		String raw;
		try (StringWriter buffer = new StringWriter()) {
			try (OverrideConfigurationWriter writer =
				new OverrideConfigurationWriter(buffer, OVERRIDDEN)) {
				TypedConfiguration.serialize(APPLICATION, file, writer);
			}
			raw = buffer.toString();
		}
		return TypedConfiguration.prettyPrint(raw, ThemeUtil.THEME_PRINTER_CONFIG);
	}

	/** The configuration types the written file marks as replacing rather than extending. */
	private static final Set<Class<?>> OVERRIDDEN = Set.of(ModuleConfiguration.class);

}
