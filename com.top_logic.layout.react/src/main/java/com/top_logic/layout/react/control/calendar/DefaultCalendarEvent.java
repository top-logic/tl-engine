/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.calendar;

import java.util.Date;

/**
 * Mutable in-memory {@link CalendarEvent} for programmatic use and tests.
 *
 * @see DefaultCalendarModel
 */
public class DefaultCalendarEvent implements CalendarEvent {

	private Date _start;

	private Date _end;

	private boolean _allDay;

	private String _title;

	private String _tooltip;

	private String _category;

	private boolean _movable = true;

	private boolean _resizable = true;

	private Object _businessObject;

	/**
	 * Creates a {@link DefaultCalendarEvent} with a title and a time interval.
	 *
	 * @param title
	 *        See {@link #getTitle()}.
	 * @param start
	 *        See {@link #getStart()}.
	 * @param end
	 *        See {@link #getEnd()}.
	 */
	public DefaultCalendarEvent(String title, Date start, Date end) {
		_title = title;
		_start = start;
		_end = end;
	}

	@Override
	public Date getStart() {
		return _start;
	}

	/**
	 * @see #getStart()
	 */
	public void setStart(Date start) {
		_start = start;
	}

	@Override
	public Date getEnd() {
		return _end;
	}

	/**
	 * @see #getEnd()
	 */
	public void setEnd(Date end) {
		_end = end;
	}

	@Override
	public boolean isAllDay() {
		return _allDay;
	}

	/**
	 * @see #isAllDay()
	 */
	public DefaultCalendarEvent setAllDay(boolean allDay) {
		_allDay = allDay;
		return this;
	}

	@Override
	public String getTitle() {
		return _title;
	}

	/**
	 * @see #getTitle()
	 */
	public DefaultCalendarEvent setTitle(String title) {
		_title = title;
		return this;
	}

	@Override
	public String getTooltip() {
		return _tooltip;
	}

	/**
	 * @see #getTooltip()
	 */
	public DefaultCalendarEvent setTooltip(String tooltip) {
		_tooltip = tooltip;
		return this;
	}

	@Override
	public String getCategory() {
		return _category;
	}

	/**
	 * @see #getCategory()
	 */
	public DefaultCalendarEvent setCategory(String category) {
		_category = category;
		return this;
	}

	@Override
	public boolean isMovable() {
		return _movable;
	}

	/**
	 * @see #isMovable()
	 */
	public DefaultCalendarEvent setMovable(boolean movable) {
		_movable = movable;
		return this;
	}

	@Override
	public boolean isResizable() {
		return _resizable;
	}

	/**
	 * @see #isResizable()
	 */
	public DefaultCalendarEvent setResizable(boolean resizable) {
		_resizable = resizable;
		return this;
	}

	@Override
	public Object getBusinessObject() {
		return _businessObject;
	}

	/**
	 * @see #getBusinessObject()
	 */
	public DefaultCalendarEvent setBusinessObject(Object businessObject) {
		_businessObject = businessObject;
		return this;
	}

}
