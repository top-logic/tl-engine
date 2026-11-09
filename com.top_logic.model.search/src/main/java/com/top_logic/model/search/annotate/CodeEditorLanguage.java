/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.annotate;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Function0;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link TLAttributeAnnotation} to set for a <code>Code</code> attribute to select the code editor
 * language.
 */
@InApp
@TagName("code-editor-language")
@TargetType(value = TLTypeKind.CUSTOM, name = { "tl.model.search:Code", TypeSpec.JSON_TYPE })
public interface CodeEditorLanguage extends TLAttributeAnnotation {

	/**
	 * The code editor language to use for the annotated attribute.
	 */
	@Options(fun = LanguageOptions.class)
	@Mandatory
	String getValue();

	/**
	 * The values that can be chosen for {@link #getValue()}.
	 * 
	 * <p>
	 * Each value is the identifier of an Ace editor language mode. The code editor of the classic
	 * UI uses it as its language mode.
	 * </p>
	 */
	List<String> LANGUAGE_MODES = List.of(
		"ace/mode/text",
		"ace/mode/css",
		"ace/mode/html",
		"ace/mode/xml",
		"ace/mode/javascript",
		"ace/mode/json",
		"ace/mode/markdown");

	/**
	 * Option provider function for {@link CodeEditorLanguage#getValue()}.
	 */
	class LanguageOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			return LANGUAGE_MODES;
		}
	}

}
