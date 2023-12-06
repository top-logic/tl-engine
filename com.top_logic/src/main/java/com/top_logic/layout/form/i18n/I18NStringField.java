/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.i18n;

import static com.top_logic.basic.shared.string.StringServicesShared.*;
import static com.top_logic.layout.form.model.FormFactory.*;
import static com.top_logic.layout.form.values.Fields.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.util.DefaultBundle;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.util.Resources;

/**
 * An {@link I18NField} where the inner {@link FormField}s are {@link StringField}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class I18NStringField extends I18NField<StringField, ResKey, ResKey.Builder> {

	private Map<String, Map<String, StringField>> _derivedFields = Collections.emptyMap();

	private Map<String, ResKey> _derivedKeyDefinitions = Collections.emptyMap();

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

	/**
	 * The names of the derived resources that can also be edited.
	 *
	 * @return A mapping from the name of the derived resource to the label for the field to edit
	 *         the derived resource.
	 */
	public Map<String, ResKey> derivedResources() {
		return _derivedKeyDefinitions;
	}

	/**
	 * Allows configuration of derived resources for the {@link ResKey} value.
	 * 
	 * @param derivedKeyDefinitions
	 *        New value of {@link #derivedResources()}.
	 */
	public void enableDerivedResources(Map<String, ResKey> derivedKeyDefinitions) {
		_derivedKeyDefinitions = derivedKeyDefinitions;
		initDerivedFields();
	}

	private void initDerivedFields() {
		_derivedFields.values().forEach(entry -> entry.values().forEach(this::removeMember));
		if (derivedResources().isEmpty()) {
			_derivedFields = Collections.emptyMap();
			return;
		}
		if (_derivedFields.isEmpty()) {
			_derivedFields = new HashMap<>();
		} else {
			_derivedFields.clear();
		}
		
		DefaultBundle resources = Resources.getInstance();
		for (StringField lf : getLanguageFields()) {
			Locale lang = language(lf);
			ValueListener transportValue = new TransportLanguageValueListener(lf);
			Map<String, StringField> fieldMap = Collections.emptyMap();
			for (Entry<String, ResKey> derivedKeyDef : derivedResources().entrySet()) {
				String keySuffix = derivedKeyDef.getKey();
				StringField suffixField = FormFactory.newStringField(suffixFieldName(lang, keySuffix));

				ResKey i18N = derivedKeyDef.getValue();
				suffixField.setLabel(resources.getString(i18N));
				suffixField.setTooltip(resources.getString(i18N.tooltipOptional()));
				suffixField.set(LANGUAGE, lang);
				// Trigger change value of language field to update value
				suffixField.addValueListener(transportValue);
				addMember(suffixField);
				fieldMap = add(fieldMap, keySuffix, suffixField);
			}
			_derivedFields.put(lang.getLanguage(), fieldMap);
		}
	}

	private static Map<String, StringField> add(Map<String, StringField> map, String key, StringField value) {
		switch (map.size()) {
			case 0: {
				return Collections.singletonMap(key, value);
			}
			case 1: {
				Map<String, StringField> result = new HashMap<>(map);
				result.put(key, value);
				return result;
			}
			default: {
				map.put(key, value);
				return map;
			}
		}
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
	protected void localizeValue(StringField langField, ResKey i18nValue) {
		super.localizeValue(langField, i18nValue);

		Locale locale = language(langField);
		Resources resources = Resources.getInstance(locale);
		for (Entry<String, StringField> derived : derivedForLanguage(locale).entrySet()) {
			Object derivedValue;
			if (i18nValue == null) {
				derivedValue = null;
			} else {
				derivedValue = resources.getString(i18nValue.suffix(derived.getKey()), null);
			}
			derived.getValue().setValue(derivedValue);
		}

	}

	@Override
	protected void localizeDefaultValue(StringField langField, ResKey i18nValue) {
		super.localizeDefaultValue(langField, i18nValue);

		Locale locale = language(langField);
		Resources resources = Resources.getInstance(locale);
		for (Entry<String, StringField> derived : derivedForLanguage(locale).entrySet()) {
			Object derivedValue;
			if (i18nValue == null) {
				derivedValue = null;
			} else {
				derivedValue = resources.getString(i18nValue.suffix(derived.getKey()), null);
			}
			derived.getValue().setDefaultValue(derivedValue);
		}
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
		for (Entry<String, StringField> derived : derivedForLanguage(locale).entrySet()) {
			String suffix = derived.getKey();
			StringField suffixField = derived.getValue();
			if (suffixField.isChanged()) {
				addSuffixValue(builder, locale, suffix, derived.getValue().getAsString());
			} else if (proxy.hasValue()) {
				ResKey oldKey = (ResKey) proxy.getValue();
				if (oldKey != null) {
					addSuffixValue(builder, locale, suffix, ResKeyUtil.getTranslation(oldKey.suffix(suffix), locale));
				}
			}
		}
	}

	private Map<String, StringField> derivedForLanguage(Locale locale) {
		return _derivedFields.getOrDefault(locale.getLanguage(), Collections.emptyMap());
	}

	private void addValue(ResKey.Builder builder, Locale locale, String translation) {
		if (!isEmpty(translation)) {
			builder.add(locale, translation);
		}
	}

	private void addSuffixValue(ResKey.Builder builder, Locale locale, String suffix, String translation) {
		if (!isEmpty(translation)) {
			builder.suffix(suffix).add(locale, translation);
		}
	}

	@Override
	protected ResKey buildValue(ResKey.Builder builder) {
		return builder.build();
	}

	/**
	 * Name of the {@link FormField} that contains the value for the derived {@link ResKey} with
	 * suffix <code>keySuffix</code> for the given <code>language</code>.
	 */
	public static String suffixFieldName(Locale language, String keySuffix) {
		return normalizeFieldName(language + "_" + keySuffix);
	}

	/**
	 * {@link ValueListener} for the fields for the derived resources that triggers the update of
	 * {@link I18NStringField#getValue()}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private class TransportLanguageValueListener implements ValueListener {

		private StringField _sender;

		public TransportLanguageValueListener(StringField sender) {
			_sender = sender;
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			if (!Utils.getbooleanValue(_sender.get(LISTENER_DISABLED))) {
				transportValues(_sender, _sender.getValue());
			}
		}

	}

}
