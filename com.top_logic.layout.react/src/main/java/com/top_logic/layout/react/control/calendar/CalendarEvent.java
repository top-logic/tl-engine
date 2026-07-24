/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.calendar;

import java.util.Date;

/**
 * A single appointment rendered by the {@link CalendarViewControl}.
 *
 * <p>
 * An event occupies the half-open time interval <code>[{@link #getStart()}, {@link #getEnd()})</code>.
 * When {@link #isAllDay()} is <code>true</code>, only the calendar days touched by that interval are
 * relevant and the time-of-day parts are ignored.
 * </p>
 *
 * <p>
 * This is the model-level contract of the calendar control: an application may implement it directly
 * (see {@link DefaultCalendarEvent}), while the declarative <code>&lt;calendar&gt;</code> element
 * supplies an implementation that derives every property from a business object through configured
 * expressions.
 * </p>
 *
 * @see CalendarModel
 */
public interface CalendarEvent {

	/**
	 * The inclusive start of this event.
	 */
	Date getStart();

	/**
	 * The exclusive end of this event.
	 *
	 * <p>
	 * Must not be before {@link #getStart()}. A zero-length interval (end equal to start) renders as a
	 * point-in-time marker.
	 * </p>
	 */
	Date getEnd();

	/**
	 * Whether this event spans whole days and is rendered in the all-day area instead of the time grid.
	 */
	boolean isAllDay();

	/**
	 * The short label shown on the event's bar.
	 */
	String getTitle();

	/**
	 * The extended description shown on hover, or <code>null</code> for none.
	 */
	String getTooltip();

	/**
	 * An application-defined category key selecting the event's color, or <code>null</code> for the
	 * default color.
	 *
	 * <p>
	 * The key is mapped to a concrete color by the rendering theme; it is not itself a color value.
	 * </p>
	 */
	String getCategory();

	/**
	 * Whether the user may reschedule this event by dragging it to another time.
	 *
	 * @see CalendarModel#moveEvent(CalendarEvent, Date, Date)
	 */
	boolean isMovable();

	/**
	 * Whether the user may change this event's duration by dragging its edge.
	 *
	 * @see CalendarModel#resizeEvent(CalendarEvent, Date)
	 */
	boolean isResizable();

	/**
	 * The domain object this event represents, or <code>null</code> if the event is not backed by one.
	 *
	 * <p>
	 * This is the value published on the calendar's selection channel when the event is selected, so
	 * that a master/detail view can react to it exactly as it would to a selected table row.
	 * </p>
	 */
	Object getBusinessObject();

}
