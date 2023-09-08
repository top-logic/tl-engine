/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.stylesheet;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeConfig.StyleSheetRef;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Handler to delete a stylesheet for a given ThemeConfig.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DeleteStylesheetHandler extends AbstractCommandHandler {

	/**
	 * Creates a {@link DeleteStylesheetHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DeleteStylesheetHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		ThemeConfig.StyleSheetRef stylesheetRef = getStylesheetReference(component);

		deleteStylesheetFile(stylesheetRef);
		updateThemeConfig((ThemeConfig) model, stylesheetRef);

		component.invalidate();

		return HandlerResult.DEFAULT_RESULT;
	}

	private void updateThemeConfig(ThemeConfig themeConfig, ThemeConfig.StyleSheetRef stylesheetRef) {
		updatePersistentThemeConfig(getThemeId(themeConfig), stylesheetRef);
		updateTransientThemeConfig(themeConfig, stylesheetRef);
	}

	private void updateTransientThemeConfig(ThemeConfig themeConfig, ThemeConfig.StyleSheetRef stylesheetRef) {
		themeConfig.getStyles().remove(stylesheetRef);
	}

	private String getThemeId(ThemeConfig themeConfig) {
		return themeConfig.getId();
	}

	private void updatePersistentThemeConfig(String themeId, StyleSheetRef stylesheetRef) {
		File themeConfigFile = FileManager.getInstance().getIDEFileOrNull(ThemeUtil.getThemeConfigPath(themeId));
		if (themeConfigFile != null) {
			ThemeConfig themeConfig = ThemeUtil.readThemeConfigFile(themeConfigFile);
			List<StyleSheetRef> styles = themeConfig.getStyles();
			Optional<StyleSheetRef> stylesheet = findStylesheet(styles, stylesheetRef.getName());
			if (stylesheet.isPresent()) {
				styles.remove(stylesheet.get());
				writeThemeConfig(themeConfigFile, themeConfig);
			}
		}
	}

	private void writeThemeConfig(File themeConfigFile, ThemeConfig themeConfig) throws IOError {
		try {
			ThemeUtil.writeThemeConfig(themeConfig, themeConfigFile);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private Optional<StyleSheetRef> findStylesheet(List<StyleSheetRef> stylessheets, String stylesheetName) {
		return stylessheets.stream()
			.filter(stylesheet -> stylesheet.getName().equals(stylesheetName))
			.findFirst();
	}

	private void deleteStylesheetFile(ThemeConfig.StyleSheetRef stylesheetRef) {
		FileManager.getInstance().getIDEFile(stylesheetRef.getName()).delete();
	}

	private ThemeConfig.StyleSheetRef getStylesheetReference(LayoutComponent component) {
		TableComponent tableComponent = (TableComponent) component;

		return (StyleSheetRef) tableComponent.getSelected();
	}

}
