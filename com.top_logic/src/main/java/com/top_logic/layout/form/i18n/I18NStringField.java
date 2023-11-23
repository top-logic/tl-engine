/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.i18n;

import static com.top_logic.basic.shared.string.StringServicesShared.*;
import static com.top_logic.layout.form.model.FormFactory.*;

import java.util.Locale;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.model.StringField;

/**
 * An {@link I18NField} where the inner {@link FormField}s are {@link StringField}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class I18NStringField extends I18NField<StringField, ResKey, ResKey.Builder> {

	/**
	 * Creates a {@link I18NStringField}.
	 */
	public static I18NStringField newI18NStringField(String fieldName, boolean isMandatory, boolean immutable) {
		I18NStringField field = new I18NStringField(fieldName, isMandatory, immutable);
		field.initLanguageFields();
		return field;
	}

	/**
	 * Creates a {@link I18NStringField}.
	 * 
	 * @implNote It is important to {@link #initLanguageFields() initialise} the language fields
	 *           after creation, i.e. caller must either trigger {@link #initLanguageFields()} or
	 *           add this note.
	 */
	protected I18NStringField(String fieldName, boolean isMandatory, boolean immutable) {
		super(fieldName, isMandatory, immutable, GenericMandatoryConstraint.SINGLETON);
	}

	@Override
	protected StringField createLanguageSpecificField(String fieldName, boolean isMandatory, boolean immutable,
			Locale language) {
		return newStringField(fieldName, StringField.EMPTY_STRING_VALUE, isMandatory, immutable, NO_CONSTRAINT);
	}

	@Override
	protected boolean hasNonEmptyValue(StringField languageField) {
		return !languageField.getAsString().isEmpty();
	}

	@Override
	protected ResKey toI18NValue(Object value) {
		return value instanceof ResKey ? (ResKey) value : null;
	}

	@Override
	protected Object localize(Locale locale, ResKey i18nValue) {
		if (i18nValue == null) {
			return null;
		}
		return ResKeyUtil.translateWithoutFallback(locale, i18nValue);
	}

	@Override
	protected ResKey.Builder createValueBuilder() {
		return ResKey.builder();
	}

	@Override
	protected void addValueToBuilder(ResKey.Builder builder, FormField proxy, Locale locale, StringField field) {
		if (field.hasValue()) {
			addValue(builder, locale, field.getAsString());
		}
	}

	private void addValue(ResKey.Builder builder, Locale locale, String translation) {
		if (!isEmpty(translation)) {
			builder.add(locale, translation);
		}
	}

	@Override
	protected ResKey buildValue(ResKey.Builder builder) {
		return builder.build();
	}

}
