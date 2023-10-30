/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.control.ValueDisplayControl.ValueDisplay;
import com.top_logic.layout.form.template.model.MemberStyle;
import com.top_logic.layout.form.values.edit.TemplateProvider;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;

/**
 * Form type of the {@link ThemeSetting} editor.
 */
@UseTemplate(SettingForm.Template.class)
public interface SettingForm extends ConfigurationItem {

	/**
	 * @see #getSetting()
	 */
	String SETTINGS = "settings";

	/**
	 * @see #getDocumentation()
	 */
	String DOCUMENTATION = "documentation";

	/**
	 * {@link TemplateProvider} for {@link SettingForm}.
	 */
	public class Template extends TemplateProvider {
		@Override
		public HTMLTemplateFragment get(ConfigurationItem model) {
			return div(
				div(member(DOCUMENTATION, MemberStyle.NONE, ValueDisplay.INSTANCE)),
				div(member(SETTINGS, MemberStyle.NONE, div(
					fieldBox(path("outerContainer.contentContainer.content", ThemeSetting.Config.VALUE)),
					fieldBox(path(
						"outerContainer.contentContainer.content", ThemeSetting.Config.EXPRESSION_ATTRIBUTE))))));
		}
	}

	/**
	 * Documentation for the edited variable.
	 */
	@Name(DOCUMENTATION)
	@InstanceFormat
	@ItemDisplay(ItemDisplayType.VALUE)
	HTMLFragment getDocumentation();

	/**
	 * @see #getDocumentation()
	 */
	void setDocumentation(HTMLFragment value);

	/**
	 * The value of the variable to edit.
	 */
	@Name(SETTINGS)
	ThemeSetting.Config<?> getSetting();

	/**
	 * @see #getSetting()
	 */
	void setSetting(ThemeSetting.Config<?> value);

}
