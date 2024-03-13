/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.form.implementation.additional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.knowledge.wrap.person.PersonalConfigurationWrapper;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.admin.component.ThemeLabelProvider;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.util.TLContext;

/**
 * {@link FormElementTemplateProvider} that allows to select the {@link Theme} for the edited
 * {@link Person}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
@TagName("theme-selector")
public class ThemeSelector extends PersonTemplateProvider {

	@Override
	protected FormField createField(FormEditorContext context, String fieldName, Person account) {
		ThemeFactory themeFactory = ThemeFactory.getInstance();
		String currentThemeId = fetchCurrentThemeID(account);
		Theme currentTheme = resolveTheme(themeFactory, currentThemeId);

		// Retrieve all available themes
		List<Theme> themes = new ArrayList<>(themeFactory.getChoosableThemes());
		Collections.sort(themes, LabelComparator.newCachingInstance(ThemeLabelProvider.INSTANCE));

		SelectField themeSelector = FormFactory.newSelectField(fieldName, themes, !SelectField.MULTIPLE,
			Collections.singletonList(currentTheme), FormFactory.MANDATORY, !FormFactory.IMMUTABLE, null);

		context.getFormContext().addStoreAlgorithm(ctx -> {
			if (!themeSelector.isChanged()) {
				return;
			}
			Theme theme = (Theme) themeSelector.getSingleSelection();
			if (theme != null) {
				setCurrentTheme(account, theme);
			}

		});
		return themeSelector;
	}

	@Override
	protected String fieldName() {
		return "themeSelector";
	}

	@Override
	protected ResKey fieldLabel() {
		return I18NConstants.LABEL_THEME_SELECTOR;
	}

	private Theme resolveTheme(ThemeFactory themeFactory, String currentThemeId) {
		Theme currentTheme;
		if (currentThemeId == null) {
			currentTheme = themeFactory.getDefaultTheme();
		} else {
			currentTheme = themeFactory.getTheme(currentThemeId);
			if (currentTheme == null) {
				// Theme no longer available.
				currentTheme = themeFactory.getDefaultTheme();
			}
		}
		return currentTheme;
	}

	private String fetchCurrentThemeID(Person account) {
		PersonalConfiguration pc = getPersonalConfiguration(account);
		return pc == null ? null : MultiThemeFactory.getPersonalThemeId(pc);
	}

	private void setCurrentTheme(Person account, Theme theme) {
		String themeID = theme.getThemeID();
		PersonalConfigurationWrapper pcw = PersonalConfigurationWrapper.createPersonalConfiguration(account);
		MultiThemeFactory.setPersonalThemeId(pcw, themeID);
		TLContext currentContext = TLContext.getContext();
		if (Utils.equals(account, currentContext.getCurrentPersonWrapper())) {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			MultiThemeFactory.setPersonalThemeId(pc, themeID);

			String themeString = MetaLabelProvider.INSTANCE.getLabel(theme);
			ResKey message = com.top_logic.knowledge.gui.layout.person.I18NConstants.CHANGED_THEME_RELOGIN_NECESSARY__THEME
				.fill(themeString);
			InfoService.showInfo(message);
		}
	}

}