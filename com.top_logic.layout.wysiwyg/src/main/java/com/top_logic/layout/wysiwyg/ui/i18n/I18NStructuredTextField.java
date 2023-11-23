/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.i18n;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.list;
import static java.util.Collections.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.i18n.I18NField;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;
import com.top_logic.layout.wysiwyg.ui.StructuredTextField;
import com.top_logic.layout.wysiwyg.ui.StructuredTextFieldFactory;
import com.top_logic.util.Resources;

/**
 * An {@link I18NField} that displays {@link I18NStructuredText}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class I18NStructuredTextField extends I18NField<FormField, I18NStructuredText, Map<Locale, StructuredText>> {

	private static final Constraint NOT_EMPTY_I18N_STRUCTURED_TEXT = new AbstractConstraint() {

		@Override
		public boolean check(Object value) throws CheckException {
			if (value == null) {
				throw createNotEmptyException();
			}
			I18NStructuredText i18nValue = (I18NStructuredText) value;
			if (i18nValue.getEntries().isEmpty()) {
				throw createNotEmptyException();
			}
			for (StructuredText html : i18nValue.getEntries().values()) {
				if (!StructuredTextField.NOT_EMPTY_STRUCTURED_TEXT.check(html)) {
					return false;
				}
			}
			return true;
		}

		private CheckException createNotEmptyException() {
			return new CheckException(Resources.getInstance().getString(
				com.top_logic.layout.form.I18NConstants.NOT_EMPTY));
		}

	};

	/**
	 * Creates a new {@link I18NStructuredTextField}.
	 * 
	 * @param fieldName
	 *        Is not allowed to be null.
	 * @param featureConfig
	 *        If null, a default feature set is used:
	 *        {@link StructuredTextConfigService#getI18nHTMLConfig(List, String)}
	 */
	public static I18NStructuredTextField new18NStructuredTextField(String fieldName, boolean mandatory,
			boolean immutable, List<String> featureConfig) {
		return new18NStructuredTextField(fieldName, mandatory, immutable, featureConfig, null, null);
	}

	/**
	 * Creates a new {@link I18NStructuredTextField}.
	 * 
	 * @param fieldName
	 *        Is not allowed to be null.
	 * @param featureConfig
	 *        If null, a default feature set is used:
	 *        {@link StructuredTextConfigService#getI18nHTMLConfig(List, String)}
	 * @param templateFiles
	 *        {@link List} of paths to template files. If null only the default templates will be
	 *        offered.
	 * @param templates
	 *        Comma separated list of templates. If null only the default templates will be offered.
	 */
	public static I18NStructuredTextField new18NStructuredTextField(String fieldName, boolean mandatory,
			boolean immutable, List<String> featureConfig, List<String> templateFiles, String templates) {
		I18NStructuredTextField field =
			new I18NStructuredTextField(fieldName, mandatory, immutable, featureConfig, templateFiles, templates);
		field.initLanguageFields();
		return field;
	}

	private final List<String> _featureConfig;

	private final List<String> _templateFiles;
	
	private final String _templates;

	/**
	 * Creates an {@link I18NStructuredTextField}.
	 * 
	 * @implNote It is important to {@link #initLanguageFields() initialise} the language fields
	 *           after creation, i.e. caller must either trigger {@link #initLanguageFields()} or
	 *           add this note.
	 * 
	 * @param fieldName
	 *        Is not allowed to be null.
	 * @param featureConfig
	 *        If null, a default feature set is used:
	 *        {@link StructuredTextConfigService#getI18nHTMLConfig(List, String)}
	 * @param templateFiles
	 *        {@link List} of paths to template files. If null only the default templates will be
	 *        offered.
	 * @param templates
	 *        Comma separated list of templates. If null only the default templates will be offered.
	 */
	protected I18NStructuredTextField(String fieldName, boolean mandatory, boolean immutable,
			List<String> featureConfig, List<String> templateFiles, String templates) {
		super(fieldName, mandatory, immutable, NOT_EMPTY_I18N_STRUCTURED_TEXT);
		_featureConfig = unmodifiableList(list(featureConfig));
		_templateFiles = unmodifiableList(list(templateFiles));
		_templates = templates;
	}

	@Override
	protected FormField createLanguageSpecificField(String fieldName, boolean isMandatory, boolean immutable,
			Locale language) {
		FormField field =
			StructuredTextFieldFactory.create(fieldName, null, getFeatureConfig(), getTemplateFiles(), getTemplates());
		field.setMandatory(isMandatory);
		field.setImmutable(immutable);
		return field;
	}

	@Override
	protected boolean hasNonEmptyValue(FormField languageField) {
		try {
			StructuredTextField.NOT_EMPTY_STRUCTURED_TEXT.check(languageField.getValue());
			return true;
		} catch (CheckException ex) {
			return false;
		}
	}

	/**
	 * Unmodifiable {@link List} of activated features.
	 * <p>
	 * See: {@link com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService.Config#getFeatures()}
	 * </p>
	 */
	public List<String> getFeatureConfig() {
		return _featureConfig;
	}

	/**
	 * Unmodifiable {@link List} of template files.
	 * 
	 * @see com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService.Config#getTemplateFiles()
	 */
	public List<String> getTemplateFiles() {
		return _templateFiles;
	}

	/**
	 * Comma separated list of templates.
	 * 
	 * @see com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService.Config#getTemplates()
	 */
	public String getTemplates() {
		return _templates;
	}

	@Override
	protected I18NStructuredText toI18NValue(Object value) {
		return value instanceof I18NStructuredText ? (I18NStructuredText) value : null;
	}

	@Override
	protected Object localize(Locale locale, I18NStructuredText i18nValue) {
		if (i18nValue == null) {
			return null;
		}
		return i18nValue.getEntries().get(locale);
	}

	@Override
	protected Map<Locale, StructuredText> createValueBuilder() {
		return map();
	}

	@Override
	protected void addValueToBuilder(Map<Locale, StructuredText> builder, FormField proxy, Locale locale, FormField field) {
		if (field.hasValue()) {
			addValue(builder, locale, (StructuredText) field.getValue());
		}
	}

	private void addValue(Map<Locale, StructuredText> builder, Locale locale, StructuredText text) {
		if (text != null && !isEmpty(text)) {
			builder.put(locale, text);
		}
	}

	private boolean isEmpty(StructuredText value) {
		return value.getImages().isEmpty() && value.getSourceCode().isEmpty();
	}

	@Override
	protected I18NStructuredText buildValue(Map<Locale, StructuredText> builder) {
		return new I18NStructuredText(builder);
	}

}
