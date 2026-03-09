/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.List;
import java.util.Map;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.react.ReactContext;

/**
 * A {@link ReactFormFieldControl} for select fields that includes options in the field state.
 *
 * <p>
 * Options are passed as a list of maps with {@code "value"} and {@code "label"} entries. They are
 * included in the field state under the key {@code "options"} so that the React {@code TLSelect}
 * component can render them.
 * </p>
 */
public class ReactSelectFormFieldControl extends ReactFormFieldControl {

	/** State key for the select options list. */
	private static final String OPTIONS = "options";

	private List<Map<String, Object>> _options;

	/**
	 * Creates a new {@link ReactSelectFormFieldControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The form field.
	 * @param options
	 *        The select options, each a map with {@code "value"} and {@code "label"} entries.
	 */
	public ReactSelectFormFieldControl(ReactContext context, FormField model, List<Map<String, Object>> options) {
		super(context, model, "TLSelect");
		_options = options;
		putState(OPTIONS, _options);
	}

	/**
	 * Updates the available options.
	 *
	 * @param options
	 *        The new select options.
	 */
	public void setOptions(List<Map<String, Object>> options) {
		_options = options;
		putState(OPTIONS, _options);
	}

}
