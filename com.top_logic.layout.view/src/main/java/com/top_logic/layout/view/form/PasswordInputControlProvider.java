/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.control.form.ReactPasswordInputControl;

/**
 * {@link ReactFieldControlProvider} that renders a masked password input for string attributes.
 *
 * <p>
 * Selected for an attribute via a {@code <input-control><impl class="..."/></input-control>}
 * annotation. Reuses the generic {@link ReactPasswordInputControl}.
 * </p>
 *
 * <p>
 * The {@link #previewText(FieldSpec, Object) preview} of a password never reveals it: a password
 * that is set is stood for by {@link #MASK}, whatever it is.
 * </p>
 */
public class PasswordInputControlProvider implements ReactFieldControlProvider {

	/**
	 * Stands for a password that is set, whatever its value and length.
	 */
	public static final String MASK = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022";

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		return new ReactPasswordInputControl(context, model);
	}

	@Override
	public String previewText(FieldSpec field, Object value) {
		return value == null || value.toString().isEmpty() ? "" : MASK;
	}
}
