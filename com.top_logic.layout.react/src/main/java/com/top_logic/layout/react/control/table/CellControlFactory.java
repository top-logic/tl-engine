/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * React-tier escape hatch for a green-field table cell that needs a bespoke control.
 *
 * <p>
 * A green-field {@link com.top_logic.table.Column}'s renderer stays toolkit-agnostic by returning a
 * {@link com.top_logic.table.CellContent.Raw} whose payload is a {@link CellControlFactory}; the
 * React adapter ({@link CellContentReactAdapter}) then asks the factory to build the actual cell
 * {@link ReactControl}. This keeps the model tier free of React types while letting React-tier
 * controls (e.g. typed field inputs, action buttons) supply arbitrary cell contents.
 * </p>
 */
@FunctionalInterface
public interface CellControlFactory {

	/**
	 * Builds the cell control for the current render.
	 *
	 * @param context
	 *        The React context to create the control in.
	 * @return The control rendering the cell.
	 */
	ReactControl create(ReactContext context);

}
