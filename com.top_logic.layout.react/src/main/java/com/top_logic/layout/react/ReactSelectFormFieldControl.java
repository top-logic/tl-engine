/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.util.List;
import java.util.Map;

import com.top_logic.layout.form.FormField;

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

	private final List<Map<String, Object>> _options;

	/**
	 * Creates a new {@link ReactSelectFormFieldControl}.
	 *
	 * @param model
	 *        The form field.
	 * @param options
	 *        The select options, each a map with {@code "value"} and {@code "label"} entries.
	 */
	public ReactSelectFormFieldControl(FormField model, List<Map<String, Object>> options) {
		super(model, "TLSelect");
		_options = options;
	}

	@Override
	protected Map<String, Object> buildFullFieldState() {
		Map<String, Object> state = super.buildFullFieldState();
		state.put("options", _options);
		return state;
	}

}
