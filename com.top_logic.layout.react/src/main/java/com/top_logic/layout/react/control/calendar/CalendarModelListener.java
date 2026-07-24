/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.calendar;

/**
 * Observer of changes to a {@link CalendarModel}'s event set.
 *
 * @see CalendarModel#addCalendarModelListener(CalendarModelListener)
 */
public interface CalendarModelListener {

	/**
	 * Announces that the given model's event set has changed and dependent views should refresh.
	 *
	 * @param source
	 *        The model that changed.
	 */
	void handleCalendarModelChanged(CalendarModel source);

}
