/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What activating an element of a layout offers - an item of an activatable
 * {@link ReactLayoutControl}, a tile of a {@link ReactDashboardControl}: the name the element is
 * announced under, whether the activation is currently refused, and the text explaining it.
 *
 * <p>
 * The client draws every activatable element the same way from the {@link #toState() state} of
 * its action.
 * </p>
 *
 * @param label
 *        The name the activation is offered under, {@code null} for none.
 * @param disabled
 *        Whether the activation is currently refused: the element shows it, and activating it has
 *        no effect.
 * @param tooltip
 *        The text explaining the activation - why it is refused, for a disabled one; {@code null}
 *        for none.
 */
public record ItemAction(String label, boolean disabled, String tooltip) {

	/** State key holding the {@link #label()}. */
	public static final String LABEL = "label";

	/** State key holding whether the activation is {@link #disabled()}. */
	public static final String DISABLED = "disabled";

	/** State key holding the {@link #tooltip()}. */
	public static final String TOOLTIP = "tooltip";

	/**
	 * The client state of this action.
	 */
	public Map<String, Object> toState() {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(LABEL, label);
		result.put(DISABLED, Boolean.valueOf(disabled));
		result.put(TOOLTIP, tooltip);
		return result;
	}

}
