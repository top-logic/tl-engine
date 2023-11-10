/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.util.List;

import com.top_logic.layout.form.FormField;

/**
 * Factory for {@link FormField}s representing a {@link StructuredText}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class StructuredTextFieldFactory {

	/**
	 * Creates a {@link FormField} displaying the given {@link StructuredText} with the
	 * {@link com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService.Config#getDefaultEditorConfig()
	 * default feature set}.
	 * 
	 * @param value
	 *        Null represents "no value".
	 * @return Never null.
	 */
	public static FormField create(String name, StructuredText value) {
		FormField structuredTextField = createInternal(name, value);
		structuredTextField.setControlProvider(StructuredTextControlProvider.INSTANCE);
		return structuredTextField;
	}

	/**
	 * Creates a {@link FormField} displaying the given {@link StructuredText} with the given
	 * {@link com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService.Config#getFeatureSets()
	 * feature set}.
	 * 
	 * @param value
	 *        Null represents "no value".
	 * @param featureSetName
	 *        If null, the
	 *        {@link com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService.Config#getDefaultEditorConfig()
	 *        default feature set} is used.
	 * @return Never null.
	 */
	public static FormField create(String name, StructuredText value, String featureSetName) {
		return create(name, value, featureSetName, null, null);
	}

	/**
	 * Creates a {@link FormField} displaying the given {@link StructuredText} with the given
	 * {@link com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService.Config#getFeatureSets()
	 * feature set} and the given templates.
	 * 
	 * @param value
	 *        Null represents "no value".
	 * @param featureSetName
	 *        If null, the
	 *        {@link com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService.Config#getDefaultEditorConfig()
	 *        default feature set} is used.
	 * @param templateFiles
	 *        {@link List} of paths to template files. If null only the default templates will be
	 *        offered.
	 * @param templates
	 *        Comma separated list of templates. If null only the default templates will be offered.
	 * 
	 * @return Never null.
	 */
	public static FormField create(String name, StructuredText value, String featureSetName, List<String> templateFiles, String templates) {
		FormField structuredTextField = createInternal(name, value);
		structuredTextField
			.setControlProvider(new StructuredTextControlProvider(featureSetName, templateFiles, templates));
		return structuredTextField;
	}

	/**
	 * Creates a {@link FormField} displaying the given {@link StructuredText} with the given
	 * {@link com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService.Config#getFeatures()
	 * features}.
	 * 
	 * @param value
	 *        Null represents "no value".
	 * @param featureNames
	 *        If null, the
	 *        {@link com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService.Config#getDefaultEditorConfig()
	 *        default feature set} is used.
	 * @param templateFiles
	 *        {@link List} of paths to template files. If null only the default templates will be
	 *        offered.
	 * @param templates
	 *        Comma separated list of templates. If null only the default templates will be offered.
	 * 
	 * @return Never null.
	 */
	public static FormField create(String name, StructuredText value, List<String> featureNames, List<String> templateFiles, String templates) {
		FormField structuredTextField = createInternal(name, value);
		structuredTextField
			.setControlProvider(new StructuredTextControlProvider(featureNames, templateFiles, templates));
		return structuredTextField;
	}

	private static FormField createInternal(String name, StructuredText value) {
		StructuredTextField field = new StructuredTextField(name);
		field.initializeField(value);
		return field;
	}

}
