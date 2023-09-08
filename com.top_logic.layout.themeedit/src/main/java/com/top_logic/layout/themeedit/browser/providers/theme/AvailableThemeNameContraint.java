/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme;

import java.util.regex.Pattern;

import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.themeedit.browser.providers.I18NConstants;

/**
 * Checks if the theme name is free.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class AvailableThemeNameContraint extends ValueConstraint<String> {

	/**
	 * Creates a {@link AvailableThemeNameContraint}.
	 */
	public AvailableThemeNameContraint() {
		super(String.class);
	}

	@Override
	protected void checkValue(PropertyModel<String> propertyModel) {
		MultiThemeFactory themeFactory = (MultiThemeFactory) ThemeFactory.getInstance();

		checkValidThemeName(propertyModel);

		if (themeFactory.getThemeConfig(propertyModel.getValue()) != null) {
			propertyModel.setProblemDescription(I18NConstants.THEME_NAME_IS_NOT_AVAILABLE);
		}
	}

	private void checkValidThemeName(PropertyModel<String> propertyModel) {
		String validThemeNameCharacters = "a-zA-Z0-9_-";
		String validThemeNameRegex = "^[a-zA-Z][" + validThemeNameCharacters + "]*$";

		Pattern pattern = Pattern.compile(validThemeNameRegex);

		if (!pattern.matcher(propertyModel.getValue()).find()) {
			propertyModel.setProblemDescription(createInvalidCharErrorMessage(validThemeNameCharacters));
		}
	}

	private ResKey createInvalidCharErrorMessage(String validThemeNameCharacters) {
		return I18NConstants.THEME_NAME_CONTAINS_INVALID_CHARS.fill(validThemeNameCharacters);
	}

}
