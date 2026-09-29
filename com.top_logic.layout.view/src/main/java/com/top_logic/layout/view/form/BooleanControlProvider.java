/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactBooleanChoiceControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.ui.BooleanDisplay;
import com.top_logic.model.annotate.ui.BooleanPresentation;

/**
 * {@link ReactFieldControlProvider} for boolean attributes.
 *
 * <p>
 * A checkbox by default, radio buttons or a yes/no select when the attribute's
 * {@link BooleanDisplay} annotation asks for it. A tri-state attribute keeps its state for
 * "no value": the checkbox gets a third state, the choice a third option.
 * </p>
 */
public class BooleanControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		boolean triState = isTriState(part);
		BooleanPresentation presentation = presentation(part);
		if (presentation == BooleanPresentation.CHECKBOX) {
			return new ReactCheckboxControl(context, model, triState);
		}
		return new ReactBooleanChoiceControl(context, model, presentation, triState);
	}

	/**
	 * How the given attribute asks to be displayed, {@link BooleanPresentation#CHECKBOX} when it
	 * says nothing.
	 *
	 * <p>
	 * An annotation at the attribute wins over the one of its type, which is what lets a single
	 * attribute deviate from how its type is displayed everywhere else.
	 * </p>
	 */
	private static BooleanPresentation presentation(TLStructuredTypePart part) {
		if (part == null) {
			return BooleanPresentation.CHECKBOX;
		}
		BooleanDisplay annotation = part.getAnnotation(BooleanDisplay.class);
		if (annotation == null) {
			TLType type = part.getType();
			annotation = type == null ? null : type.getAnnotation(BooleanDisplay.class);
		}
		if (annotation == null || annotation.getPresentation() == null) {
			return BooleanPresentation.CHECKBOX;
		}
		return annotation.getPresentation();
	}

	private static boolean isTriState(TLStructuredTypePart part) {
		if (part == null) {
			return false;
		}
		TLType type = part.getType();
		return type instanceof TLPrimitive primitive && primitive.getKind() == TLPrimitive.Kind.TRISTATE;
	}

}
