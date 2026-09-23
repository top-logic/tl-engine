/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.Set;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.model.annotate.ui.BooleanPresentation;

/**
 * A {@link ReactFormFieldControl} for a boolean field displayed as a box that is ticked or a switch
 * that is flipped.
 *
 * <p>
 * Both show the value in place and take a click to change it, and both name it by the label beside
 * them rather than by an option of their own; a boolean offered as a choice between labelled values
 * is {@link ReactBooleanChoiceControl} instead. Which of the two shapes it is, is the
 * {@link #getPresentation() presentation} of the field.
 * </p>
 */
public class ReactCheckboxControl extends ReactFormFieldControl {

	/** State key telling the client that the checkbox has a third, "no value" state. */
	private static final String TRI_STATE = "triState";

	/**
	 * State key naming the shape the client draws, the external name of the
	 * {@link #getPresentation() presentation}. Absent for the box that is ticked.
	 */
	private static final String DISPLAY = "display";

	private final boolean _triState;

	private final BooleanPresentation _presentation;

	/**
	 * Creates a two-valued {@link ReactCheckboxControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 */
	public ReactCheckboxControl(ReactContext context, FieldModel model) {
		this(context, model, false);
	}

	/**
	 * Creates a {@link ReactCheckboxControl}.
	 *
	 * @param triState
	 *        Whether the field has a third state for "no value" (a tri-state boolean). The client
	 *        then shows an unset checkbox as indeterminate and cycles through the states on click:
	 *        checked, unchecked, unset - the order a boolean field is cycled through everywhere
	 *        else.
	 * @see #ReactCheckboxControl(ReactContext, FieldModel)
	 */
	public ReactCheckboxControl(ReactContext context, FieldModel model, boolean triState) {
		this(context, model, BooleanPresentation.CHECKBOX, triState);
	}

	/**
	 * Creates a {@link ReactCheckboxControl} of the given shape.
	 *
	 * @param presentation
	 *        A box that is ticked ({@link BooleanPresentation#CHECKBOX}) or a switch that is
	 *        flipped ({@link BooleanPresentation#SWITCH}); a presentation of another kind is not
	 *        one of this control's shapes and is drawn as a box. A {@code triState} field is drawn
	 *        as a box whatever is asked for, a switch having no third position to show "no value"
	 *        in, which {@link #getPresentation()} then reports.
	 * @see #ReactCheckboxControl(ReactContext, FieldModel, boolean)
	 */
	public ReactCheckboxControl(ReactContext context, FieldModel model, BooleanPresentation presentation,
			boolean triState) {
		super(context, model, "TLCheckbox");
		_triState = triState;
		_presentation = presentation == BooleanPresentation.SWITCH && !triState
			? BooleanPresentation.SWITCH
			: BooleanPresentation.CHECKBOX;
		if (triState) {
			putState(TRI_STATE, Boolean.TRUE);
		}
		if (_presentation == BooleanPresentation.SWITCH) {
			putState(DISPLAY, BooleanPresentation.SWITCH.getExternalName());
		}
	}

	/**
	 * The shape this control is drawn as: a box that is ticked, or a switch that is flipped.
	 */
	public BooleanPresentation getPresentation() {
		return _presentation;
	}

	/**
	 * The shape the field is drawn in is how it looks, not what it says: a switch and a box report
	 * the same two values.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), DISPLAY);
	}

	/**
	 * Handles a checkbox toggle: its value is a boolean, so it has its own typed arguments rather
	 * than the base field's text value.
	 */
	@ReactCommandHandler(CMD_VALUE_CHANGED)
	void handleChecked(CheckboxValueArguments args) {
		clientValueChanged(parseClientValue(args.getChecked()));
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		if (_triState && rawValue == null) {
			// The third state is the absence of a value, not "false".
			return null;
		}
		return Boolean.valueOf(Boolean.TRUE.equals(rawValue));
	}

}
