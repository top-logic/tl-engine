/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.react.ReactButtonControl;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A composite {@link ReactControl} that renders the {@code TLFieldToggles} React component.
 *
 * <p>
 * Demonstrates React-level composition: the {@code TLFieldToggles} React component receives the
 * control IDs of three child {@link ReactButtonControl}s and composes them as {@code TLButton}
 * sub-components, each wrapped in its own {@code TLControlContext}. This way each button sends
 * commands to its own server-side {@link ReactButtonControl}, which delegates to a plain
 * {@link com.top_logic.layout.basic.Command Command}.
 * </p>
 */
public class DemoFieldTogglesControl extends ReactControl {

	private final List<ReactButtonControl> _childButtons;

	/**
	 * Creates a new {@link DemoFieldTogglesControl}.
	 *
	 * @param field
	 *        The form field whose properties are toggled.
	 */
	public DemoFieldTogglesControl(FormField field) {
		super(field, "TLFieldToggles");

		_childButtons = new ArrayList<>();

		_childButtons.add(new ReactButtonControl("Disabled", context -> {
			field.setDisabled(!field.isDisabled());
			return HandlerResult.DEFAULT_RESULT;
		}));

		_childButtons.add(new ReactButtonControl("Immutable", context -> {
			field.setImmutable(!field.isImmutable());
			return HandlerResult.DEFAULT_RESULT;
		}));

		_childButtons.add(new ReactButtonControl("Mandatory", context -> {
			field.setMandatory(!field.isMandatory());
			return HandlerResult.DEFAULT_RESULT;
		}));
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		// Write each child button control so it gets a DOM mount point and SSE registration.
		// The child divs are hidden because TLFieldToggles will compose TLButton visually.
		for (ReactButtonControl child : _childButtons) {
			child.write(context, out);
		}

		// Build the children descriptor for the React component.
		List<Map<String, Object>> children = new ArrayList<>();
		for (ReactButtonControl child : _childButtons) {
			Map<String, Object> childDesc = new LinkedHashMap<>();
			childDesc.put("controlId", child.getID());
			childDesc.put("state", child.getReactState());
			children.add(childDesc);
		}
		getReactState().put("children", children);

		// Write the composite mount point.
		super.internalWrite(context, out);
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Children handle their own revalidation via SSE.
	}

}
