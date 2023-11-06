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

import com.top_logic.basic.CollectionUtil;
import com.top_logic.element.i18n.I18NField;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;
import com.top_logic.layout.wysiwyg.ui.StructuredTextControlProvider;

/**
 * An {@link I18NField} that displays {@link I18NStructuredText}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class I18NStructuredTextField extends I18NField<FormField, I18NStructuredText, Map<Locale, StructuredText>> {

	/**
	 * Creates a new {@link I18NStructuredTextField}.
	 * 
	 * @param fieldName
	 *        Is not allowed to be null.
	 * @param constraint
	 *        Null means there is no constraint.
	 * @param featureConfig
	 *        If null, a default feature set is used:
	 *        {@link StructuredTextConfigService#getI18nHTMLConfig(List, String)}
	 */
	public static I18NStructuredTextField new18NStructuredTextField(String fieldName, boolean mandatory,
			boolean disabled, Constraint constraint, List<String> featureConfig) {
		return new18NStructuredTextField(fieldName, mandatory, disabled, constraint, featureConfig, null, null);
	}

	/**
	 * Creates a new {@link I18NStructuredTextField}.
	 * 
	 * @param fieldName
	 *        Is not allowed to be null.
	 * @param constraint
	 *        Null means there is no constraint.
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
			boolean disabled, Constraint constraint, List<String> featureConfig, List<String> templateFiles, String templates) {
		I18NStructuredTextField field =
			new I18NStructuredTextField(fieldName, mandatory, disabled, constraint, featureConfig, templateFiles,
				templates);
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
	 * @param constraint
	 *        Null means there is no constraint.
	 * @param featureConfig
	 *        If null, a default feature set is used:
	 *        {@link StructuredTextConfigService#getI18nHTMLConfig(List, String)}
	 * @param templateFiles
	 *        {@link List} of paths to template files. If null only the default templates will be
	 *        offered.
	 * @param templates
	 *        Comma separated list of templates. If null only the default templates will be offered.
	 */
	protected I18NStructuredTextField(String fieldName, boolean mandatory, boolean disabled,
			Constraint constraint, List<String> featureConfig, List<String> templateFiles, String templates) {
		super(fieldName, mandatory, disabled, constraint);
		_featureConfig = unmodifiableList(list(featureConfig));
		_templateFiles = unmodifiableList(list(templateFiles));
		_templates = templates;
	}

	@Override
	protected FormField createLanguageSpecificField(String fieldName, boolean isMandatory, boolean isDisabled,
			Constraint constraint, Locale language) {
		HiddenField field = FormFactory.newHiddenField(fieldName);
		field.setMandatory(isMandatory);
		field.setDisabled(isDisabled);
		if (constraint != null) {
			field.addConstraint(constraint);
		}
		field.setControlProvider(getStructuredTextControlProvider());
		return field;
	}

	private ControlProvider getStructuredTextControlProvider() {
		if (CollectionUtil.isEmptyOrNull(getFeatureConfig())) {
			return new StructuredTextControlProvider(getTemplateFiles(), getTemplates());
		}
		return new StructuredTextControlProvider(getFeatureConfig(), getTemplateFiles(), getTemplates());
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
		return i18nValue.localize(locale);
	}

	@Override
	protected Map<Locale, StructuredText> createValueBuilder() {
		return map();
	}

	@Override
	protected void addValueToBuilder(Map<Locale, StructuredText> builder, FormField proxy, Locale locale, FormField field) {
		if (field.isChanged()) {
			addValue(builder, locale, (StructuredText) field.getValue());
		} else if (proxy.hasValue()) {
			I18NStructuredText originalValue = (I18NStructuredText) proxy.getValue();
			if (originalValue != null) {
				StructuredText structuredText = originalValue.getEntries().get(locale);
				addValue(builder, locale, structuredText);
			}
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
