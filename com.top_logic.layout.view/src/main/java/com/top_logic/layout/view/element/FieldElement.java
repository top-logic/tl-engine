/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.form.FieldControl;
import com.top_logic.layout.view.form.FormControl;

/**
 * Declarative {@link UIElement} that creates a {@link FieldControl} for a single model attribute.
 *
 * <p>
 * Must be nested inside a {@link FormElement}. Resolves the enclosing {@link FormControl} from the
 * {@link ViewContext} and creates a chrome-wrapped field control for the configured attribute.
 * </p>
 */
public class FieldElement implements UIElement {

	/**
	 * Configuration for {@link FieldElement}.
	 */
	@TagName("field")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(FieldElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getReadonly()}. */
		String READONLY = "readonly";

		/**
		 * The name of the model attribute to display.
		 */
		@Name(ATTRIBUTE)
		@Mandatory
		String getAttribute();

		/**
		 * Optional label override for the field.
		 *
		 * <p>
		 * If not set, the label is derived from the model attribute.
		 * </p>
		 */
		@Name(LABEL)
		ResKey getLabel();

		/**
		 * Whether the field should always be read-only regardless of form edit mode.
		 */
		@Name(READONLY)
		boolean getReadonly();
	}

	private final Config _config;

	/**
	 * Creates a new {@link FieldElement} from configuration.
	 */
	@CalledByReflection
	public FieldElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// 1. Get FormControl from context.
		FormControl formControl = context.getFormControl();
		if (formControl == null) {
			throw new IllegalStateException(
				"FieldElement must be nested inside a FormElement. No FormControl found in ViewContext.");
		}

		// 2. Create FieldControl and register with form.
		FieldControl fieldControl =
			new FieldControl(formControl, _config.getAttribute(), _config.getLabel(), _config.getReadonly());
		formControl.registerFieldControl(fieldControl);

		// 3. Create the chrome-wrapped control.
		ReactFormFieldChromeControl chrome = fieldControl.createChromeControl();

		return chrome;
	}
}
