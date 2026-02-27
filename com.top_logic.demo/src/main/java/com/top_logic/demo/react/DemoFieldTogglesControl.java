/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.react.ReactControl;
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
	 * @param field
	 *        The form field whose properties are toggled.
	 */
	public DemoFieldTogglesControl(FormField field) {
		super(field, "TLFieldToggles");

		getReactState().put("disabledButton", new ReactToggleButtonControl("Disabled", field.isDisabled(), (ctx, active) -> {
			field.setDisabled(!active);
			return !active;
		}));

		getReactState().put("immutableButton", new ReactToggleButtonControl("Immutable", field.isImmutable(), (ctx, active) -> {
			field.setImmutable(!active);
			return !active;
		}));

		getReactState().put("mandatoryButton", new ReactToggleButtonControl("Mandatory", field.isMandatory(), (ctx, active) -> {
			field.setMandatory(!active);
			return !active;
		}));

		getReactState().put("hiddenButton", new ReactToggleButtonControl("Hidden", !field.isVisible(), (ctx, active) -> {
			field.setVisible(active);
			return !active;
		}));
	}

}
