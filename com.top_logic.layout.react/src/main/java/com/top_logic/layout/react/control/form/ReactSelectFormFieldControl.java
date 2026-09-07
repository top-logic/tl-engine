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
import com.top_logic.layout.form.model.FieldModel;
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

	private final SelectFieldModel _selectModel;

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
		_selectModel = model;
		putState(OPTIONS, buildOptionsList(model.getOptions()));
		model.addOptionsListener((source, newOptions) ->
			putState(OPTIONS, buildOptionsList(newOptions)));
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		super.handleModelValueChanged(source, oldValue, newValue);

		// The value has a say in what is offered, because an option is added for a value none of
		// the options names - see #buildOptionsList(List). So a new value can change the list.
		putState(OPTIONS, buildOptionsList(_selectModel.getOptions()));
	}

	/**
	 * The options to send to the client: the model's own, plus the value actually held where none
	 * of them names it.
	 *
	 * <p>
	 * A field must not keep quiet about the value it has. The client matches the value against the
	 * options it was given ({@code TLSelect}), so a value that is not among them is shown as nothing
	 * at all while read-only - and, while editable, as whatever a {@code <select>} falls back to,
	 * which is its first option. The field then displays a value nobody configured, and saving the
	 * form would make that display true.
	 * </p>
	 *
	 * <p>
	 * Options are what may be <em>chosen</em>, and that set can legitimately be narrower than what
	 * exists: it is computed for the case at hand, and a value may predate it, come from a wider
	 * configuration, or simply no longer qualify. None of that makes the value untrue, so it is
	 * added - first, where it cannot be overlooked - rather than silently replaced.
	 * </p>
	 */
	private List<Map<String, Object>> buildOptionsList(List<?> options) {
		List<Map<String, Object>> result = new ArrayList<>();
		Object value = getFieldModel().getValue();
		if (value != null && !options.contains(value)) {
			result.add(option(value));
		}
		for (Object option : options) {
			result.add(option(option));
		}
		return result;
	}

	private Map<String, Object> option(Object option) {
		return Map.of(
			"value", option != null ? option : "",
			"label", _labelProvider.getLabel(option));
	}

}
