/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.net.URI;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.state.TextInputState;

/**
 * A {@link ReactFormFieldControl} for text input fields.
 *
 * <p>
 * The field edits a text; its {@link #getInputType() input type} says what the text means. A
 * {@link InputType#TEXT} field is a plain text box, while a field with a web address, an e-mail
 * address or a phone number asks the browser for the matching input type and offers the value as a
 * link: beside the input while it is edited, and in place of the text while it is only displayed.
 * </p>
 *
 * <p>
 * A {@link InputType#URL} typed without a scheme is completed to an {@code https} address when the
 * field is left, and a text that is no absolute address at all is rejected with
 * {@link I18NConstants#ERROR_INVALID_URL__VALUE}.
 * </p>
 */
public class ReactTextInputControl extends ReactFormFieldControl {

	private InputType _inputType = InputType.TEXT;

	/**
	 * Creates a new {@link ReactTextInputControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 */
	public ReactTextInputControl(ReactContext context, FieldModel model) {
		super(context, model, "TLTextInput");
	}

	/**
	 * The kind of value the field edits.
	 *
	 * @see #setInputType(InputType)
	 */
	public InputType getInputType() {
		return _inputType;
	}

	/**
	 * Sets the {@link #getInputType() kind of value} the field edits.
	 *
	 * <p>
	 * A {@link InputType#URL} field {@link #setSendValueOnBlur(boolean) holds a typed value back}
	 * until it is left: an address is only an address once it is written out, so every prefix of
	 * one on the way there is a text this field rejects. What reaches the server is the address the
	 * client completed when the field was left, judged once.
	 * </p>
	 *
	 * @param inputType
	 *        The kind of value, never {@code null}.
	 */
	public void setInputType(InputType inputType) {
		_inputType = inputType;
		putState(TextInputState.INPUT_TYPE__PROP, inputType.htmlType());
		if (inputType == InputType.URL) {
			setSendValueOnBlur(true);
		}
	}

	/**
	 * A {@link InputType#URL} value must be an absolute address; any other kind of value is taken
	 * as typed.
	 */
	@Override
	protected void applyRawClientValue(Object rawValue) {
		if (_inputType != InputType.URL) {
			super.applyRawClientValue(rawValue);
			return;
		}

		String text = rawValue == null ? "" : rawValue.toString().trim();
		if (text.isEmpty()) {
			setError(null);
			applyClientValue(null);
			return;
		}
		if (!isAbsoluteUrl(text)) {
			setError(I18NConstants.ERROR_INVALID_URL__VALUE.fill(text));
			return;
		}
		setError(null);
		applyClientValue(text);
	}

	/**
	 * Whether the given text is an address the browser can follow on its own, i.e. one naming its
	 * scheme.
	 */
	private static boolean isAbsoluteUrl(String text) {
		try {
			return URI.create(text).isAbsolute();
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	/**
	 * Reports the given problem with the typed text on the field model, so that the field chrome
	 * and the input itself show it.
	 */
	private void setError(ResKey error) {
		FieldModel model = getFieldModel();
		if (model instanceof AbstractFieldModel) {
			((AbstractFieldModel) model).setError(error);
		}
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		return rawValue != null ? rawValue.toString() : null;
	}

	/**
	 * A single-line text is complete when the user presses Enter.
	 *
	 * <p>
	 * In a text area Enter is part of the text, so a multi-line field has no submit gesture.
	 * </p>
	 */
	@Override
	public boolean hasSubmitGesture() {
		return !isMultiline();
	}

}
