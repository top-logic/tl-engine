/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.select;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The shape a selection is offered in: how the options of a select field are laid out and picked
 * from.
 *
 * <p>
 * All shapes edit the same value and accept the same number of values; they differ in how much of
 * the option list is visible without acting, and therefore in how many options they suit. A list
 * that opens on demand takes one line whatever the number of options, while a cloud of toggles and
 * a bar of segments show every option at all times and are read without opening anything.
 * </p>
 *
 * @see ReactDropdownSelectControl
 */
public enum SelectDisplay implements ExternallyNamed {

	/**
	 * Offer the options in a list that opens on demand, with a filter to type in.
	 *
	 * <p>
	 * The shape for an option list of any length: it takes the room of one field, and the list is
	 * searched rather than scanned.
	 * </p>
	 */
	DROPDOWN("dropdown"),

	/**
	 * Offer every option as a toggle of its own, laid out as a cloud that wraps onto as many lines
	 * as it needs.
	 *
	 * <p>
	 * The shape for a handful of options that are picked from repeatedly - tags of an article, the
	 * days a course is held on: what is on and what is off is read at a glance, and each is changed
	 * with one click and without opening anything.
	 * </p>
	 */
	CHIPS("chips"),

	/**
	 * Offer the options as the segments of one bar, the selected one marked by a slider that moves
	 * to it.
	 *
	 * <p>
	 * The shape for a few mutually exclusive options that belong together as one setting - a period
	 * of day, week or month, a view shown as a list or a table: the bar reads as one control whose
	 * position is the value.
	 * </p>
	 */
	SEGMENTED("segmented");

	private final String _externalName;

	private SelectDisplay(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * Whether this shape shows every option at all times, rather than only those the user asks to
	 * see.
	 *
	 * <p>
	 * Such a display has nothing to open and therefore no moment at which it could fetch the
	 * options: it is handed the complete list as soon as it is displayed, and again whenever that
	 * list would otherwise go stale.
	 * </p>
	 */
	public boolean showsAllOptions() {
		return this != DROPDOWN;
	}

}
