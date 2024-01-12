/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.form.implementation.additional;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.model.form.implementation.FormMode;

/**
 * {@link FormElementTemplateProvider} that allows to set
 * {@link PersonalConfiguration#getAutoTranslate() auto-translate resources} for the edited
 * {@link Person}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
@TagName("auto-translate")
public class AutoTranslation extends PersonTemplateProvider {

	@Override
	protected FormField createField(FormEditorContext context, String fieldName, Person account) {
		PersonalConfiguration personalConfiguration = getPersonalConfiguration(account);
		boolean initialValue;
		if (personalConfiguration != null) {
			initialValue = personalConfiguration.getAutoTranslate();
		} else {
			initialValue = false;
		}
		BooleanField field = FormFactory.newBooleanField(fieldName, initialValue, !FormFactory.IMMUTABLE);
		context.getFormContext().addStoreAlgorithm(ctx -> {
			if (!field.isChanged()) {
				return;
			}
			storeToPersonalConfiguration(account, pc -> pc.setAutoTranslate(field.getAsBoolean()));
		});
		return field;
	}

	@Override
	protected String fieldName() {
		return "autoTranslate";
	}

	@Override
	protected ResKey fieldLabel() {
		return I18NConstants.AUTO_TRANSLATE;
	}

	@Override
	public boolean isVisible(TLStructuredType type, FormMode formMode) {
		return TranslationService.isActive();
	}

}

