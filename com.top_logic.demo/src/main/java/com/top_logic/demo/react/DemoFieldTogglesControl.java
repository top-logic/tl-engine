/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.react.ReactButtonControl;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A composite {@link ReactControl} that renders the {@code TLFieldToggles} React component.
 *
 * <p>
 * Demonstrates React-level composition with named child slots: the server puts each child
 * {@link ReactButtonControl} directly into the React state. The framework automatically handles ID
 * allocation, SSE registration, serialization as child descriptors, and cleanup.
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

		getReactState().put("disabledButton", new ReactButtonControl("Disabled", context -> {
			field.setDisabled(!field.isDisabled());
			return HandlerResult.DEFAULT_RESULT;
		}));

		getReactState().put("immutableButton", new ReactButtonControl("Immutable", context -> {
			field.setImmutable(!field.isImmutable());
			return HandlerResult.DEFAULT_RESULT;
		}));

		getReactState().put("mandatoryButton", new ReactButtonControl("Mandatory", context -> {
			field.setMandatory(!field.isMandatory());
			return HandlerResult.DEFAULT_RESULT;
		}));
	}

}
