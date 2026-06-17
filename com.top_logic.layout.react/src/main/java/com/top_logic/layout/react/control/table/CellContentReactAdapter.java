/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.table.CellContent;

/**
 * Maps a UI-neutral {@link CellContent} (from the green-field table model) to a concrete
 * cell {@link ReactControl}.
 *
 * <p>
 * This is the React tier's half of the rendering seam: the model tier describes
 * <em>what</em> to show via {@link CellContent}, and this adapter decides <em>how</em> to
 * render it. Inline editing ({@link CellContent.Editable}) currently renders the field
 * value read-only; an editable control is wired in a later step.
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
			Object value = editable.field().getValue();
			return new ReactTextControl(context, value == null ? "" : String.valueOf(value));
		}
		if (content instanceof CellContent.Raw raw) {
			Object payload = raw.payload();
			return new ReactTextControl(context, payload == null ? "" : String.valueOf(payload));
		}
		return new ReactTextControl(context, "");
	}

}
