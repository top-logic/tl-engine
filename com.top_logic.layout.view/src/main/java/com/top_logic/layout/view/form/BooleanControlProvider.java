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
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.model.annotate.ui.BooleanDisplay;
import com.top_logic.model.annotate.ui.BooleanPresentation;

/**
 * {@link ReactFieldControlProvider} for boolean attributes.
 *
 * <p>
 * A checkbox by default, radio buttons or a yes/no select when the field
 * {@link FieldSpec#getBooleanPresentation() asks} for it (a model attribute says so through its
 * {@link BooleanDisplay} annotation). A {@link FieldSpec#isTriState() tri-state} field keeps a state
 * for "no value": the checkbox gets a third state, the choice a third option.
 * </p>
 */
public class BooleanControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		BooleanPresentation presentation = field.getBooleanPresentation();
		if (presentation == BooleanPresentation.CHECKBOX) {
			return new ReactCheckboxControl(context, model, field.isTriState());
		}
		return new ReactBooleanChoiceControl(context, model, presentation, field.isTriState());
	}

}
