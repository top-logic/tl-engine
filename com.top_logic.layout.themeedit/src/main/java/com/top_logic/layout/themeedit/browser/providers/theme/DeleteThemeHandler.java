/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme;

import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Handler to delete the given theme.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DeleteThemeHandler extends AbstractCommandHandler {

	/**
	 * Creates a {@link DeleteThemeHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DeleteThemeHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		FileManager fileManager = FileManager.getInstance();
		String themeName = ((ThemeConfig) model).getId();

		removePersistentThemeFiles(fileManager, themeName);
		updateTransientThemeConfigs(themeName);
		component.invalidate();

		return HandlerResult.DEFAULT_RESULT;
	}

	private void removePersistentThemeFiles(FileManager fileManager, String theme) {
		FileUtilities.deleteR(fileManager.getIDEFile(ThemeUtil.getThemeStylePath(theme)));
		FileUtilities.deleteR(fileManager.getIDEFile(ThemeUtil.getThemePath(theme)));
		FileUtilities.deleteR(fileManager.getIDEFile(ThemeUtil.getThemeResourcesPath(theme)));
	}

	private void updateTransientThemeConfigs(String themeName) {
		((MultiThemeFactory) ThemeFactory.getInstance()).removeTheme(themeName);
	}

}
