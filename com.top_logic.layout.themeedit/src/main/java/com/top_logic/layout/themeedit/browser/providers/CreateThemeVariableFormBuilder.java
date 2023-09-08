/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.gui.config.ThemeSetting.IconSetting;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;

/**
 * Editor to create a new theme variable.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CreateThemeVariableFormBuilder
		extends DeclarativeFormBuilder<ThemeSetting.Config<?>, CreateThemeVariableFormBuilder.EditModel> {

	/**
	 * Message to indicate that the component is in theme variable creation mode.
	 */
	private static final String THEME_VARIABLE_CREATE_MODE = "Is in theme variable create mode";

	/**
	 * Creates a {@link CreateThemeVariableFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public CreateThemeVariableFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Base edit properties.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface EditModel extends ConfigurationItem {
		
		/**
		 * Name of config property getThemeSettingConfig.
		 */
		public static final String THEME_SETTING_CONFIG = "themeSettingConfig";

		/**
		 * Selected ThemeSetting.Config to edit.
		 */
		@Name(THEME_SETTING_CONFIG)
		@ItemDefault(IconSetting.Config.class)
		ThemeSetting.Config<?> getThemeSettingConfig();

		/**
		 * @see #getThemeSettingConfig()
		 */
		void setThemeSettingConfig(ThemeSetting.Config<?> themeSettingConfig);
	}

	@Override
	protected Class<? extends ThemeSetting.Config<?>> getModelType() {
		throw new UnreachableAssertion(THEME_VARIABLE_CREATE_MODE);
	}

	@Override
	protected Class<? extends EditModel> getFormType(Object contextModel) {
		return EditModel.class;
	}

	@Override
	protected void fillFormModel(EditModel formModel, ThemeSetting.Config<?> businessModel) {
		// Nothing to do.
	}
}
