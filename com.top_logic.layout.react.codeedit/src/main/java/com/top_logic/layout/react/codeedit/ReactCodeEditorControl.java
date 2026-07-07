/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.codeedit;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;

/**
 * A {@link ReactFormFieldControl} rendering a {@code CodeMirror} 6 source-code editor for a string or text
 * field.
 *
 * <p>
 * The {@link CodeEditorLanguage language} fixes syntax highlighting and (for JSON) client-side
 * validation. Value, editability and validation state are inherited from
 * {@link ReactFormFieldControl}; the {@code TLCodeEditor} React component renders them.
 * </p>
 */
public class ReactCodeEditorControl extends ReactFormFieldControl {

	/** State key for the {@link CodeEditorLanguage#clientId() client language id}. */
	private static final String LANGUAGE = "language";

	/**
	 * Creates a new {@link ReactCodeEditorControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model providing value, editability and change notifications.
	 * @param language
	 *        The source language for syntax highlighting.
	 */
	public ReactCodeEditorControl(ReactContext context, FieldModel model, CodeEditorLanguage language) {
		super(context, model, "TLCodeEditor");
		putState(LANGUAGE, language.clientId());
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		return rawValue != null ? rawValue.toString() : null;
	}

}
