/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.table.CellContent;

/**
 * Maps a UI-neutral {@link CellContent} (from the green-field table model) to a concrete
 * cell {@link ReactControl}.
 *
 * <p>
 * This is the React tier's half of the rendering seam: the model tier describes
 * <em>what</em> to show via {@link CellContent}, and this adapter decides <em>how</em> to
 * render it. An {@link com.top_logic.table.CellContent.Editable} cell backed by a boolean
 * {@link FieldModel} renders as an interactive {@link ReactCheckboxControl}; other field types
 * currently render their value read-only. A {@link com.top_logic.table.CellContent.Raw} cell whose
 * payload is a {@link CellControlFactory} renders whatever control the factory builds (the escape
 * hatch for bespoke cells such as typed field inputs or action buttons).
 * </p>
 */
public final class CellContentReactAdapter {

	private CellContentReactAdapter() {
		// Utility class.
	}

	/**
	 * Creates a cell control for the given content.
	 */
	public static ReactControl toControl(ReactContext context, CellContent content) {
		if (content instanceof CellContent.Text text) {
			return new ReactTextControl(context, text.text());
		}
		if (content instanceof CellContent.Labeled labeled) {
			return new ReactTextControl(context, labeled.text(), labeled.cssClass());
		}
		if (content instanceof CellContent.Editable editable) {
			FieldModel field = editable.field();
			if (field.getValue() instanceof Boolean) {
				return new ReactCheckboxControl(context, field);
			}
			// Non-boolean inline editors are not wired yet; show the value read-only.
			Object value = field.getValue();
			return new ReactTextControl(context, value == null ? "" : String.valueOf(value));
		}
		if (content instanceof CellContent.Raw raw) {
			Object payload = raw.payload();
			if (payload instanceof CellControlFactory factory) {
				return factory.create(context);
			}
			return new ReactTextControl(context, payload == null ? "" : String.valueOf(payload));
		}
		return new ReactTextControl(context, "");
	}

}
