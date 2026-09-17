/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.control.form.InputType;
import com.top_logic.layout.react.control.form.ReactTextInputControl;

/**
 * {@link ReactFieldControlProvider} for string, tristate, binary, and other text-representable
 * attributes.
 *
 * <p>
 * The {@link Config#getInputType() input type} says what the entered text means: a plain text, a
 * web address, an e-mail address, a phone number, or a search term. The browser edits the value
 * with the input of that kind - offering the matching on-screen keyboard and completion - and the
 * field displays an address as a link opening it. Configured for a model type in the
 * {@link FieldControlService} type map, the input type reaches every attribute of that type.
 * </p>
 *
 * <p>
 * Also serves as the ultimate fallback when no other provider matches, where the text is a plain
 * one.
 * </p>
 */
public class TextInputControlProvider implements ReactFieldControlProvider {

	/**
	 * Configuration options for {@link TextInputControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<TextInputControlProvider> {

		/** Configuration name for {@link #getInputType()}. */
		String INPUT_TYPE = "input-type";

		/** The {@link #getInputType()} of a field that states none. */
		String DEFAULT_INPUT_TYPE = "TEXT";

		@Override
		@ClassDefault(TextInputControlProvider.class)
		Class<? extends TextInputControlProvider> getImplementationClass();

		/**
		 * The kind of value the field edits.
		 *
		 * <p>
		 * A plain text is edited in a text box, while a web address, an e-mail address and a phone
		 * number are also offered as a link that opens the value.
		 * </p>
		 */
		@Name(INPUT_TYPE)
		@FormattedDefault(DEFAULT_INPUT_TYPE)
		InputType getInputType();
	}

	private final InputType _inputType;

	/**
	 * Creates a configured {@link TextInputControlProvider}.
	 */
	@CalledByReflection
	public TextInputControlProvider(InstantiationContext context, Config config) {
		_inputType = config.getInputType();
	}

	/**
	 * The kind of value the fields built here edit.
	 */
	public InputType getInputType() {
		return _inputType;
	}

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		ReactTextInputControl control = new ReactTextInputControl(context, model);
		if (_inputType != InputType.TEXT) {
			// A field that states no kind edits a plain text, so a plain text field is left as it
			// is built.
			control.setInputType(_inputType);
		}
		int rows = field.getMultilineRows();
		if (rows > 0) {
			control.setMultiline(rows);
		}
		return control;
	}

}
