/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;

/**
 * {@link TextInputControl} that renders an additional button in edit mode next to the input field.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TextInputWithButtonControl extends TextInputControl {

	private HTMLFragment _button;

	/**
	 * Creates a {@link TextInputWithButtonControl}.
	 */
	public TextInputWithButtonControl(FormField model, Map<String, ControlCommand> commandsByName,
			HTMLFragment button) {
		super(model, commandsByName);
		setButton(button);
	}

	/**
	 * Creates a {@link TextInputWithButtonControl}.
	 */
	public TextInputWithButtonControl(FormField model, HTMLFragment button) {
		super(model);
		setButton(button);
	}
	
	@Override
	protected String getTypeCssClass() {
		return "cTextWithButton";
	}

	@Override
	protected void writeEditableContents(DisplayContext context, TagWriter out) throws IOException {
		super.writeEditableContents(context, out);

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
