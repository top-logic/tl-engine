/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;

/**
 * {@link StructuredTextControl} that renders an additional button in edit mode next to the input
 * field.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StructuredTextWithButtonControl extends StructuredTextControl {

	private HTMLFragment _button = Fragments.empty();

	/**
	 * Creates a {@link StructuredTextWithButtonControl}.
	 * 
	 * @see StructuredTextControl#StructuredTextControl(FormField, List, String)
	 */
	public StructuredTextWithButtonControl(FormField model, List<String> templateFiles, String templates) {
		super(model, templateFiles, templates);
	}

	/**
	 * Creates a {@link StructuredTextWithButtonControl}.
	 * 
	 * @see StructuredTextControl#StructuredTextControl(FormField, String, boolean)
	 */
	public StructuredTextWithButtonControl(FormField model, String editorConfig, boolean containsTextOnly) {
		super(model, editorConfig, containsTextOnly);
	}

	/**
	 * Creates a {@link StructuredTextWithButtonControl}.
	 *
	 * @see StructuredTextControl#StructuredTextControl(FormField, String)
	 */
	public StructuredTextWithButtonControl(FormField model, String editorConfig) {
		super(model, editorConfig);
	}

	/**
	 * Creates a {@link StructuredTextWithButtonControl}.
	 *
	 * @see StructuredTextControl#StructuredTextControl(FormField)
	 */
	public StructuredTextWithButtonControl(FormField model) {
		super(model);
	}

	@Override
	protected String getTypeCssClass() {
		return "cStructuredTextWithButton";
	}

	@Override
	protected void writeEditableContent(DisplayContext context, TagWriter out) throws IOException {
		super.writeEditableContent(context, out);

		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, FormConstants.FIXED_RIGHT_CSS_CLASS);
		out.endBeginTag();
		getButton().write(context, out);
		out.endTag(SPAN);
	}

	/**
	 * The button that is written in edit mode.
	 */
	public HTMLFragment getButton() {
		return _button;
	}

	/**
	 * Setter for {@link #getButton()}.
	 */
	public void setButton(HTMLFragment button) {
		requestRepaint();
		_button = Objects.requireNonNull(button);
	}

}
