/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.calendar;

import java.util.Collection;
import java.util.Date;

/**
 * Source of {@link CalendarEvent}s rendered by the {@link CalendarViewControl} and sink of the edits
 * the user performs on them.
 *
 * <p>
 * The control asks for the events overlapping the currently displayed interval through
 * {@link #getEvents(Date, Date)} and reports pointer edits back through {@link #moveEvent},
 * {@link #resizeEvent} and {@link #createEvent}. A model notifies registered
 * {@link CalendarModelListener}s whenever its event set changes so the control can repaint.
 * </p>
 *
 * <p>
 * An application may implement this interface directly (see {@link DefaultCalendarModel}); the
 * declarative <code>&lt;calendar&gt;</code> element provides an implementation backed by a channel of
 * business objects.
 * </p>
 *
 * @see CalendarEvent
 */
public interface CalendarModel {

	/**
	 * All events overlapping the half-open interval <code>[start, end)</code>.
	 *
	 * @param start
	 *        The inclusive lower bound of the requested interval.
	 * @param end
	 *        The exclusive upper bound of the requested interval.
	 * @return The events touching the interval, never <code>null</code>. An event is included when it
	 *         starts before <code>end</code> and ends after <code>start</code>.
	 */
	Collection<? extends CalendarEvent> getEvents(Date start, Date end);

	/**
	 * Reschedules an event to a new time interval, keeping its duration unless <code>newEnd</code>
	 * differs.
	 *
	 * <p>
	 * Called when the user drags a {@link CalendarEvent#isMovable() movable} event to another slot.
	 * Implementations that mutate persistent state must open their own transaction.
	 * </p>
	 *
	 * @param event
	 *        The event to reschedule, as previously returned by {@link #getEvents(Date, Date)}.
	 * @param newStart
	 *        The new {@link CalendarEvent#getStart() start}.
	 * @param newEnd
	 *        The new {@link CalendarEvent#getEnd() end}.
	 */
	void moveEvent(CalendarEvent event, Date newStart, Date newEnd);

	/**
	 * Changes an event's duration by moving its end, keeping its start.
	 *
	 * <p>
	 * Called when the user drags the edge of a {@link CalendarEvent#isResizable() resizable} event.
	 * Implementations that mutate persistent state must open their own transaction.
	 * </p>
	 *
	 * @param event
	 *        The event to resize, as previously returned by {@link #getEvents(Date, Date)}.
	 * @param newEnd
	 *        The new {@link CalendarEvent#getEnd() end}.
	 */
	void resizeEvent(CalendarEvent event, Date newEnd);

	/**
	 * Creates a new event covering the interval the user selected in an empty part of the grid.
	 *
	 * <p>
	 * Implementations that mutate persistent state must open their own transaction. A model that does
	 * not support interactive creation may return <code>null</code>.
	 * </p>
	 *
	 * @param start
	 *        The inclusive start of the selected slot.
	 * @param end
	 *        The exclusive end of the selected slot.
	 * @param allDay
	 *        Whether the slot was selected in the all-day area.
	 * @return The created event, or <code>null</code> if creation was not performed.
	 */
	CalendarEvent createEvent(Date start, Date end, boolean allDay);

	/**
	 * Registers a listener notified whenever this model's event set changes.
	 *
	 * @param listener
	 *        The listener to add.
	 */
	void addCalendarModelListener(CalendarModelListener listener);

	/**
	 * Removes a listener previously added through {@link #addCalendarModelListener(CalendarModelListener)}.
	 *
	 * @param listener
	 *        The listener to remove.
	 */
	void removeCalendarModelListener(CalendarModelListener listener);

}
