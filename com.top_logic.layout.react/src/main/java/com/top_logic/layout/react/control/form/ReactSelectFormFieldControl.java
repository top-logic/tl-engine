/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.SelectFieldModel;
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

	private static final String OPTIONS = "options";

	private final LabelProvider _labelProvider;

	/**
	 * Creates a new select control.
	 *
	 * @param context
	 *        The React context.
	 * @param model
	 *        The select field model providing options.
	 * @param labelProvider
	 *        Provider for option labels.
	 */
	public ReactSelectFormFieldControl(ReactContext context, SelectFieldModel model,
			LabelProvider labelProvider) {
		super(context, model, "TLSelect");
		_labelProvider = labelProvider;
		putState(OPTIONS, buildOptionsList(model.getOptions()));
		model.addOptionsListener((source, newOptions) ->
			putState(OPTIONS, buildOptionsList(newOptions)));
	}

	private List<Map<String, Object>> buildOptionsList(List<?> options) {
		List<Map<String, Object>> result = new ArrayList<>();
		for (Object option : options) {
			result.add(Map.of(
				"value", option != null ? option : "",
				"label", _labelProvider.getLabel(option)));
		}
		return result;
	}

}
