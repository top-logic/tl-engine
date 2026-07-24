/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.calendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * In-memory {@link CalendarModel} holding a mutable list of {@link DefaultCalendarEvent}s.
 *
 * <p>
 * Suitable for programmatic use and tests. Edits update the held events in place and notify
 * registered {@link CalendarModelListener}s.
 * </p>
 */
public class DefaultCalendarModel implements CalendarModel {

	private final List<CalendarEvent> _events = new ArrayList<>();

	private final List<CalendarModelListener> _listeners = new ArrayList<>();

	/**
	 * Adds an event to this model and notifies listeners.
	 *
	 * @param event
	 *        The event to add.
	 * @return The added event, for chaining.
	 */
	public CalendarEvent addEvent(CalendarEvent event) {
		_events.add(event);
		fireChanged();
		return event;
	}

	/**
	 * Removes an event from this model and notifies listeners.
	 *
	 * @param event
	 *        The event to remove.
	 */
	public void removeEvent(CalendarEvent event) {
		if (_events.remove(event)) {
			fireChanged();
		}
	}

	@Override
	public Collection<? extends CalendarEvent> getEvents(Date start, Date end) {
		List<CalendarEvent> result = new ArrayList<>();
		for (CalendarEvent event : _events) {
			if (event.getStart().before(end) && event.getEnd().after(start)) {
				result.add(event);
			}
		}
		return result;
	}

	@Override
	public void moveEvent(CalendarEvent event, Date newStart, Date newEnd) {
		if (event instanceof DefaultCalendarEvent mutable) {
			mutable.setStart(newStart);
			mutable.setEnd(newEnd);
			fireChanged();
		}
	}

	@Override
	public void resizeEvent(CalendarEvent event, Date newEnd) {
		if (event instanceof DefaultCalendarEvent mutable) {
			mutable.setEnd(newEnd);
			fireChanged();
		}
	}

	@Override
	public CalendarEvent createEvent(Date start, Date end, boolean allDay) {
		DefaultCalendarEvent event = new DefaultCalendarEvent(null, start, end).setAllDay(allDay);
		return addEvent(event);
	}

	@Override
	public void addCalendarModelListener(CalendarModelListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeCalendarModelListener(CalendarModelListener listener) {
		_listeners.remove(listener);
	}

	/**
	 * Notifies all registered {@link CalendarModelListener}s that this model changed.
	 */
	protected void fireChanged() {
		for (CalendarModelListener listener : new ArrayList<>(_listeners)) {
			listener.handleCalendarModelChanged(this);
		}
	}

}
