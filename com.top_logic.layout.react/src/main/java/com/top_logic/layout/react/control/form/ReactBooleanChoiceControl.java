/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.model.annotate.ui.BooleanPresentation;
import com.top_logic.util.Resources;

/**
 * A {@link ReactFormFieldControl} for a boolean field that is displayed as a choice between its
 * values - radio buttons or a yes/no select - instead of as a checkbox.
 *
 * <p>
 * Which of the two it is, is the attribute's decision
 * ({@link com.top_logic.model.annotate.ui.BooleanDisplay}); a checkbox stays with
 * {@link ReactCheckboxControl}. The options are labelled the way a boolean value is labelled
 * everywhere else, so a field reads like the table cell over the same attribute.
 * </p>
 */
public class ReactBooleanChoiceControl extends ReactFormFieldControl {

	/** State key naming the presentation the client renders ({@code radio} or {@code select}). */
	private static final String PRESENTATION = "presentation";

	/** State key for the offered values, each a map of {@code value} and {@code label}. */
	private static final String OPTIONS = "options";

	/**
	 * Creates a {@link ReactBooleanChoiceControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 * @param presentation
	 *        Radio buttons or a select; {@link BooleanPresentation#CHECKBOX} is not one of this
	 *        control's presentations and is treated as radio buttons.
	 * @param nullable
	 *        Whether the field has a third state for "no value" (a tri-state boolean), which is
	 *        then offered as a further option.
	 */
	public ReactBooleanChoiceControl(ReactContext context, FieldModel model, BooleanPresentation presentation,
			boolean nullable) {
		super(context, model, "TLBooleanChoice");
		putState(PRESENTATION,
			presentation == BooleanPresentation.SELECT ? BooleanPresentation.SELECT.getExternalName()
				: BooleanPresentation.RADIO.getExternalName());
		putState(OPTIONS, options(nullable));
	}

	private static List<Map<String, Object>> options(boolean nullable) {
		List<Map<String, Object>> result = new ArrayList<>(3);
		result.add(option(Boolean.TRUE, MetaLabelProvider.INSTANCE.getLabel(Boolean.TRUE)));
		result.add(option(Boolean.FALSE, MetaLabelProvider.INSTANCE.getLabel(Boolean.FALSE)));
		if (nullable) {
			result.add(option(null, Resources.getInstance().getString(I18NConstants.VALUE_NONE)));
		}
		return result;
	}

	private static Map<String, Object> option(Boolean value, String label) {
		// A LinkedHashMap, not Map.of(...): the "no value" option has a null value.
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("value", value);
		result.put("label", label);
		return result;
	}

	/**
	 * Handles a value change: the value is a boolean (or {@code null} for "no value"), so it has
	 * its own typed arguments rather than the base field's text value.
	 */
	@ReactCommandHandler(CMD_VALUE_CHANGED)
	void handleChosen(CheckboxValueArguments args) {
		clientValueChanged(args.getChecked());
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		if (rawValue == null) {
			// The absence of a value, not "false" - a two-valued field never sends it.
			return null;
		}
		return Boolean.valueOf(Boolean.TRUE.equals(rawValue));
	}

}
