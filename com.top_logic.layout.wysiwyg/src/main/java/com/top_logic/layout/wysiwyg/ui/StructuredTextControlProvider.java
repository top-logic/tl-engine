/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.util.List;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} creating {@link StructuredTextControl} instances.
 */
public class StructuredTextControlProvider implements ControlProvider {

	/**
	 * Singleton {@link StructuredTextControlProvider} instance.
	 */
	public static final StructuredTextControlProvider INSTANCE = new StructuredTextControlProvider();

	/** Name of the features to get CKEditor instance for. May be <code>null</code>. */
	protected final List<String> _featureNames;

	/** Name of the feature set to get CKEditor instance for. May be <code>null</code>. */
	protected final String _featureSet;

	/** Paths of template files to use for the CKEditor instance. May be <code>null</code>. */
	protected final List<String> _templateFiles;

	/**
	 * Comma separated list of templates to use for the CKEditor instance. May be <code>null</code>.
	 */
	protected final String _templates;

	/**
	 * Constructor to create a {@link ControlProvider} for HTML texts.
	 */
	public StructuredTextControlProvider() {
		_featureNames = null;
		_featureSet = null;
		_templateFiles = null;
		_templates = null;
	}

	/**
	 * Constructor to create a {@link ControlProvider} for HTML texts.
	 * 
	 * @param templateFiles
	 *        {@link List} of template file paths that should be offered in the CKEditor instance.
	 * @param templates
	 *        Comma separated list of templates that should be offered in the CKEditor instance.
	 */
	public StructuredTextControlProvider(List<String> templateFiles, String templates) {
		_featureNames = null;
		_featureSet = null;
		_templateFiles = templateFiles;
		_templates = templates;
	}

	/**
	 * Constructor to create a {@link ControlProvider} for HTML texts.
	 * 
	 * @param featureNames
	 *        Name of the features to get CKEditor instance for.
	 */
	public StructuredTextControlProvider(List<String> featureNames) {
		this(featureNames, null, null);
	}

	/**
	 * Constructor to create a {@link ControlProvider} for HTML texts.
	 * 
	 * @param featureSet
	 *        Name of the feature set to get CKEditor instance for.
	 */
	public StructuredTextControlProvider(String featureSet) {
		this(featureSet, null, null);
	}

	/**
	 * Constructor to create a {@link ControlProvider} for HTML texts.
	 * 
	 * @param featureNames
	 *        Name of the features to get CKEditor instance for.
	 * @param templateFiles
	 *        {@link List} of template file paths that should be offered in the CKEditor instance.
	 * @param templates
	 *        Comma separated list of templates that should be offered in the CKEditor instance.
	 */
	public StructuredTextControlProvider(List<String> featureNames, List<String> templateFiles, String templates) {
		_featureNames = featureNames;
		_featureSet = null;
		_templateFiles = templateFiles;
		_templates = templates;
	}

	/**
	 * Constructor to create a {@link ControlProvider} for HTML texts.
	 * 
	 * @param featureSet
	 *        Name of the feature set to get CKEditor instance for.
	 * @param templateFiles
	 *        {@link List} of template file paths that should be offered in the CKEditor instance.
	 * @param templates
	 *        Comma separated list of templates that should be offered in the CKEditor instance.
	 */
	public StructuredTextControlProvider(String featureSet, List<String> templateFiles, String templates) {
		_featureNames = null;
		_featureSet = featureSet;
		_templateFiles = templateFiles;
		_templates = templates;
	}

	@Override
	public Control createControl(Object model, String style) {
		FormField field = (FormField) model;

		StructuredTextConfigService service = StructuredTextConfigService.getInstance();
		if (_featureNames != null) {
			return new StructuredTextControl(field,
				service.getEditorConfig(_featureNames, _templateFiles, _templates));
		} else if (_featureSet != null) {
			return new StructuredTextControl(field,
				service.getEditorConfig(_featureSet, _templateFiles, _templates));
		}

		return new StructuredTextControl(field, _templateFiles, _templates);
	}
}