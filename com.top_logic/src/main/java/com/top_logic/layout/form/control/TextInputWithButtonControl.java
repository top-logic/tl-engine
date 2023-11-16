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
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		if (this.isMultiLine()) {
			Icons.TEXT_INPUT_WITH_BUTTONS_EDIT_MULTI_TEMPLATE.get().write(context, out, this);
		} else {
			Icons.TEXT_INPUT_WITH_BUTTONS_EDIT_TEMPLATE.get().write(context, out, this);
		}
	}

	@Override
	public void writeButtons(DisplayContext context, TagWriter out) throws IOException {
		getButton().write(context, out);
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
