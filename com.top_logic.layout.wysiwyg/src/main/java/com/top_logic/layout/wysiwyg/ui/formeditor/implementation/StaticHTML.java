/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.formeditor.implementation;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;
import com.top_logic.layout.wysiwyg.ui.StructuredTextControlProvider;
import com.top_logic.model.form.definition.FormElement;

/**
 * {@link FormElement} to display static content in a form.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("static-html")
public interface StaticHTML extends FormElement<StaticHTMLTemplateProvider> {

	/** Configuration option for {@link #getContent()}. */
	String CONTENT = "content";

	/** Configuration option for {@link #getWholeLine()}. */
	String WHOLE_LINE = "wholeLine";

	/**
	 * String representation of the static HTML to render.
	 */
	@Name(CONTENT)
	@ControlProvider(StructuredTextControlProvider.class)
	@Mandatory
	@RenderWholeLine
	String getContent();

	/**
	 * Whether the content must be rendered using the whole line.
	 */
	@Name(WHOLE_LINE)
	boolean getWholeLine();

}

