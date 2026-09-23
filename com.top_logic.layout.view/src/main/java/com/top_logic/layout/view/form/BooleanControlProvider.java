/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
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
 * A checkbox by default, a switch, radio buttons or a yes/no select when the field is displayed
 * that way. What the field {@link FieldSpec#getBooleanPresentation() says} decides, which for a
 * model attribute is its {@link BooleanDisplay} annotation; a {@link Config#getDisplay() display}
 * configured here overrides it, so a single form field deviates from how the attribute is displayed
 * elsewhere.
 * </p>
 *
 * <p>
 * A {@link FieldSpec#isTriState() tri-state} field keeps a state for "no value": the checkbox gets a
 * third state, the choice a third option. A switch has no third position to show it in and stays a
 * checkbox.
 * </p>
 */
public class BooleanControlProvider implements ReactFieldControlProvider {

	/**
	 * Configuration options for {@link BooleanControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<BooleanControlProvider> {

		/** Configuration name for {@link #getDisplay()}. */
		String DISPLAY = "display";

		@Override
		@ClassDefault(BooleanControlProvider.class)
		Class<? extends BooleanControlProvider> getImplementationClass();

		/**
		 * How the value is displayed, or nothing to display it the way the edited attribute asks
		 * for.
		 *
		 * <p>
		 * A checkbox and a switch show the value in place and are named by the label beside them,
		 * while radio buttons and a select offer it as a choice between labelled values. Stated
		 * here, the display holds for this field alone; stated as the {@link BooleanDisplay}
		 * annotation of an attribute, it holds wherever that attribute is shown.
		 * </p>
		 *
		 * <p>
		 * A value that may also be unknown is displayed as a checkbox even where a switch is asked
		 * for, a switch having no third position for "no value".
		 * </p>
		 */
		@Name(DISPLAY)
		@Nullable
		@NullDefault
		BooleanPresentation getDisplay();
	}

	private final BooleanPresentation _display;

	/**
	 * Creates a {@link BooleanControlProvider} displaying every value the way its attribute asks
	 * for.
	 */
	public BooleanControlProvider() {
		_display = null;
	}

	/**
	 * Creates a configured {@link BooleanControlProvider}.
	 */
	@CalledByReflection
	public BooleanControlProvider(InstantiationContext context, Config config) {
		_display = config.getDisplay();
	}

	/**
	 * How the values edited here are displayed, or {@code null} to follow what each field says.
	 */
	public BooleanPresentation getDisplay() {
		return _display;
	}

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		BooleanPresentation presentation = _display == null ? field.getBooleanPresentation() : _display;
		if (presentation == BooleanPresentation.RADIO || presentation == BooleanPresentation.SELECT) {
			return new ReactBooleanChoiceControl(context, model, presentation, field.isTriState());
		}
		return new ReactCheckboxControl(context, model, presentation, field.isTriState());
	}

}
