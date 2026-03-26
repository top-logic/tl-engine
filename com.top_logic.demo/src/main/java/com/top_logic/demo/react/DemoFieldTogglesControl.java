/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.toggle.ReactToggleButtonControl;

/**
 * A composite {@link ReactControl} that renders the {@code TLFieldToggles} React component.
 *
 * <p>
 * Demonstrates React-level composition with named child slots: the server puts each child
 * {@link ReactToggleButtonControl} directly into the React state. The framework automatically
 * handles ID allocation, SSE registration, serialization as child descriptors, and cleanup.
 * </p>
 */
public class DemoFieldTogglesControl extends ReactControl {

	/**
	 * Creates a new {@link DemoFieldTogglesControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param field
	 *        The form field whose properties are toggled.
	 */
	public DemoFieldTogglesControl(ReactContext context, FormField field) {
		super(context, field, "TLFieldToggles");

		putState("disabledButton", new ReactToggleButtonControl(context, "Disabled", field.isDisabled(), (ctx, active) -> {
			field.setDisabled(!active);
			return !active;
		}));

		putState("immutableButton", new ReactToggleButtonControl(context, "Immutable", field.isImmutable(), (ctx, active) -> {
			field.setImmutable(!active);
			return !active;
		}));

		putState("mandatoryButton", new ReactToggleButtonControl(context, "Mandatory", field.isMandatory(), (ctx, active) -> {
			field.setMandatory(!active);
			return !active;
		}));

		putState("hiddenButton", new ReactToggleButtonControl(context, "Hidden", !field.isVisible(), (ctx, active) -> {
			field.setVisible(active);
			return !active;
		}));
	}

}
