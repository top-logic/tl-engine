/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.form.implementation.additional;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;

/**
 * {@link FormElementTemplateProvider} that allows to set
 * {@link PersonalConfiguration#getStartPageAutomatism() start page automatism} for the edited
 * {@link Person}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
@TagName("start-page-automatism")
public class StartPageAutomatism extends PersonTemplateProvider {

	@Override
	protected FormField createField(FormEditorContext context, String fieldName, Person account) {
		PersonalConfiguration personalConfiguration = getPersonalConfiguration(account);
		boolean initialValue;
		if (personalConfiguration != null) {
			initialValue = personalConfiguration.getStartPageAutomatism();
		} else {
			initialValue = false;
		}
		BooleanField field = FormFactory.newBooleanField(fieldName, initialValue, !FormFactory.IMMUTABLE);
		context.getFormContext().addStoreAlgorithm(ctx -> {
			if (!field.isChanged()) {
				return;
			}
			storeToPersonalConfiguration(account, pc -> pc.setStartPageAutomatism(field.getAsBoolean()));
		});
		return field;
	}

	@Override
	protected String fieldName() {
		return "startPageAutomatism";
	}

	@Override
	protected ResKey fieldLabel() {
		return I18NConstants.SET_START_PAGE_ON_LOGOUT;
	}

	@Override
	public boolean isVisible(FormEditorContext context) {
		return !PersonalConfiguration.getConfig().getHideOptionStartPageAutomatism();
	}

}

