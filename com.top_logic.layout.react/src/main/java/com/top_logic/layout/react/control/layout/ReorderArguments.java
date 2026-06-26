/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@link ReactDashboardControl#REORDER_COMMAND} command: the new display
 * order of the dashboard tiles after a drag-and-drop reorder.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Reorder tiles to {order}")
public interface ReorderArguments extends ReactCommandArguments {

	/** @see #getOrder() */
	String ORDER = "order";

	/**
	 * The tile ids in their new display order.
	 */
	@Name(ORDER)
	@Mandatory
	List<String> getOrder();

}
