/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.debug;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.configuration.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command that reloads the theme.
 */
public class ReloadThemeCommand extends AbstractCommandHandler {

	/**
	 * Creates a {@link ReloadThemeCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ReloadThemeCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		ReloadableManager reloadManager = ReloadableManager.getInstance();
		String[] reload = {
			ApplicationConfig.getInstance().getName(),
			AliasManager.getInstance().getName(),
		};
		List<String> failedKeys = Arrays.asList(reloadManager.reloadKnown(reload));
		if (!failedKeys.isEmpty()) {
			return HandlerResult.error(I18NConstants.RELOAD_FAILED__NAMES.fill(failedKeys));
		}

		try {
			ModuleUtil.INSTANCE.restart(ThemeFactory.Module.INSTANCE, null);
		} catch (RestartException ex) {
			Logger.warn("Restart of theme factory failed.", ex, ReloadThemeCommand.class);
			return HandlerResult.error(
				I18NConstants.RESTART_OF_THEME_FACTORY_FAILED__PROBLEM.fill(ex.getMessage()));
		}

		// Force reload.
		MainLayout mainLayout = context.getLayoutContext().getMainLayout();
		mainLayout.invalidate();

		return HandlerResult.DEFAULT_RESULT;
	}

}