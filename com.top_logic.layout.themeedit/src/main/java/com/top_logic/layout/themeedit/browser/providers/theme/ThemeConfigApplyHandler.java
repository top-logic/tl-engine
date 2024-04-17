/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeInitializationFailure;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.themeedit.browser.providers.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * Handler to store a {@link ThemeConfig}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ThemeConfigApplyHandler extends AbstractCommandHandler {

	/**
	 * Creates a {@link ThemeConfigApplyHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ThemeConfigApplyHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> args) {
		FormContext formContext = getFormContext(component);
		ThemeConfig themeConfig = (ThemeConfig) EditorFactory.getModel(formContext);

		try {
			if (!existThemeCycle(themeConfig)) {
				try {
					return applyThemeConfig(component, themeConfig);
				} catch (ThemeInitializationFailure ex) {
					throw new TopLogicException(ex.getErrorKey());
				}
			}

			return createDependencyCycleErrorResult(formContext);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private HandlerResult applyThemeConfig(LayoutComponent component, ThemeConfig themeConfig)
			throws IOException, ThemeInitializationFailure {
		updateTransientThemeConfig(themeConfig);

		ThemeUtil.writeThemeConfig(themeConfig, getThemeConfigurationFile(themeConfig.getId()));
		updateTableRow(component, themeConfig);

		return HandlerResult.DEFAULT_RESULT;
	}

	private FormContext getFormContext(LayoutComponent component) {
		FormComponent form = (FormComponent) component;

		return form.getFormContext();
	}

	private HandlerResult createDependencyCycleErrorResult(FormContext formContext) {
		HandlerResult handlerResult = new HandlerResult();

		fillErrorResult(formContext, handlerResult);

		return handlerResult;
	}

	private void fillErrorResult(FormContext formContext, HandlerResult handlerResult) {
		ResKey cycleErrorKey = I18NConstants.THEME_CYCLE_ERROR;

		AbstractApplyCommandHandler.fillHandlerResultWithErrors(cycleErrorKey, formContext, handlerResult);
	}

	private boolean existThemeCycle(ThemeConfig themeConfig) {
		MultiThemeFactory themeFactory = (MultiThemeFactory) ThemeFactory.getInstance();

		String currentThemeId = themeConfig.getId();
		List<String> extendedThemeIds = themeConfig.getExtends();

		return existThemeCycle(themeFactory, currentThemeId, extendedThemeIds);
	}

	private boolean existThemeCycle(MultiThemeFactory themeFactory, String currentThemeId,
			List<String> extendedThemeIds) {
		if (extendedThemeIds != null) {
			if (extendedThemeIds.contains(currentThemeId)) {
				return true;
			}

			for (String extendedThemeId : extendedThemeIds) {
				List<String> list = themeFactory.getThemeConfig(extendedThemeId).getExtends();
				if (existThemeCycle(themeFactory, currentThemeId, list)) {
					return true;
				}
			}
		}
		
		return false;
	}

	private void updateTableRow(LayoutComponent component, ThemeConfig themeConfig) {
		TableComponent themesTable = (TableComponent) component.getMaster();

		themesTable.invalidate();
		themesTable.setSelected(themeConfig);
	}

	private void updateTransientThemeConfig(ThemeConfig themeConfig) throws ThemeInitializationFailure {
		MultiThemeFactory themeFactory = (MultiThemeFactory) ThemeFactory.getInstance();

		themeFactory.replaceThemeConfig(themeConfig.getId(), themeConfig);
	}

	private File getThemeConfigurationFile(String themeId) {
		return FileManager.getInstance().getIDEFile(ThemeUtil.getThemeConfigPath(themeId));
	}

}
