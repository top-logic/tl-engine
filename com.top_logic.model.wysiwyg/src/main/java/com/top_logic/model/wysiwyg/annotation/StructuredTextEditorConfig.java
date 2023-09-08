/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.annotation;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link TLAttributeAnnotation} for setting an attribute-specialized editor configuration using a
 * feature set and a template set for a HTML attribute.
 * 
 * @see #getFeatures()
 * @see com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService.Config
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
@TargetType(value = TLTypeKind.STRING, name = { "tl.model.wysiwyg:Html", "tl.model.i18n:I18NHtml" })
@TagName(StructuredTextEditorConfig.EDITOR_CONFIGURATION)
@InApp
public interface StructuredTextEditorConfig extends TLAttributeAnnotation {

	/**
	 * Tag name for the editor config {@link TLAttributeAnnotation}.
	 */
	public static final String EDITOR_CONFIGURATION = "wysiwyg";

	/**
	 * List of
	 * {@link com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService.Config#getFeatures()
	 * features} to select.
	 */
	@Format(CommaSeparatedStrings.class)
	@Options(fun = StructuredEditorConfigOptions.class)
	List<String> getFeatures();

	/**
	 * List of template files to select.
	 */
	@ListBinding
	List<String> getTemplateFiles();

	/**
	 * Comma separated list of templates to select
	 */
	String getTemplates();

}
