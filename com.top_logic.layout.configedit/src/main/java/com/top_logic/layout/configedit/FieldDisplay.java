/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.layout.form.model.FieldMode;

/**
 * How a configuration form displays one property of the item it edits, beyond what the property
 * declares itself.
 *
 * <p>
 * Lets the user interface fix how a property is offered in one form - shown but not to be changed,
 * hidden, required - without changing the configuration interface, which other forms edit as well.
 * </p>
 *
 * @param mode
 *        The mode the property is displayed in, <code>null</code> for the mode the property
 *        declares itself. {@link FieldMode#ACTIVE} accepts input, {@link FieldMode#DISABLED} shows
 *        an input that cannot be used, {@link FieldMode#IMMUTABLE} shows the value only,
 *        {@link FieldMode#INVISIBLE} hides the property. A group - a nested item or a collection -
 *        in a mode that does not accept input offers no entries to add or remove, and the fields
 *        inside it are displayed in the same mode.
 * @param mandatory
 *        Whether the property must have a value, even if the property does not declare it: the
 *        field is marked as mandatory, and a form without the value refuses to be saved. For a
 *        collection, a value is at least one entry.
 */
public record FieldDisplay(FieldMode mode, boolean mandatory) {

	/**
	 * Whether the property is displayed at all.
	 */
	public boolean isVisible() {
		return mode != FieldMode.INVISIBLE && mode != FieldMode.BLOCKED;
	}

	/**
	 * Whether the property accepts input, as far as this display is concerned.
	 */
	public boolean isAccepting() {
		return mode == null || mode == FieldMode.ACTIVE;
	}

}
