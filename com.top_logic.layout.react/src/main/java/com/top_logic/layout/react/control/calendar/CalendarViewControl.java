/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.calendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;

/**
 * React control rendering a {@link CalendarModel} as an Outlook-style calendar.
 *
 * <p>
 * The control keeps a {@link Granularity} (day, work week, week, month or year) and an anchor date
 * that together determine the displayed interval. It queries the {@link CalendarModel} for the
 * events overlapping that interval, pushes them to the client together with the layout parameters,
 * and translates the pointer gestures reported by the client (navigation, selection, drag-move,
 * resize, slot creation) back into {@link CalendarModel} calls.
 * </p>
 *
 * @see CalendarModel
 * @see CalendarEvent
 */
public class CalendarViewControl extends ReactControl {

	/**
	 * The zoom level of a {@link CalendarViewControl}, selecting which interval is displayed and how
	 * navigation steps.
	 */
	public enum Granularity {
		/** A single day. */
		DAY,

		/** The working days of the week containing the anchor. */
		WORK_WEEK,

		/** The seven days of the week containing the anchor. */
		WEEK,

		/** The six-week grid covering the month containing the anchor. */
		MONTH,

		/** The twelve months of the year containing the anchor. */
		YEAR;
	}

	/** Navigation direction sent by the client. */
	private static final String DIR_PREV = "PREV";

	private static final String DIR_NEXT = "NEXT";

	private static final String DIR_TODAY = "TODAY";

	// State keys (control -> client).
	private static final String GRANULARITY = "granularity";

	private static final String TITLE = "title";

	private static final String RANGE_START = "rangeStart";

	private static final String RANGE_END = "rangeEnd";

	private static final String ANCHOR = "anchor";

	private static final String NOW = "now";

	private static final String FIRST_DAY_OF_WEEK = "firstDayOfWeek";

	private static final String DAY_START_HOUR = "dayStartHour";

	private static final String DAY_END_HOUR = "dayEndHour";

	private static final String NON_WORKING_DAYS = "nonWorkingDays";

	private static final String LOCALE = "locale";

	private static final String EVENTS = "events";

	private static final String EDITABLE = "editable";

	// Event field keys (part of the EVENTS state).
	private static final String EVENT_ID = "id";

	private static final String EVENT_START = "start";

	private static final String EVENT_END = "end";

	private static final String EVENT_ALL_DAY = "allDay";

	private static final String EVENT_TITLE = "title";

	private static final String EVENT_TOOLTIP = "tooltip";

	private static final String EVENT_CATEGORY = "category";

	private static final String EVENT_MOVABLE = "movable";

	private static final String EVENT_RESIZABLE = "resizable";

	private static final String EVENT_SELECTED = "selected";

	// Command names (client -> control).
	private static final String CMD_NAVIGATE = "navigate";

	private static final String CMD_GOTO = "goto";

	private static final String CMD_SWITCH_GRANULARITY = "switchGranularity";

	private static final String CMD_SELECT_EVENT = "selectEvent";

	private static final String CMD_MOVE_EVENT = "moveEvent";

	private static final String CMD_RESIZE_EVENT = "resizeEvent";

	private static final String CMD_CREATE_SLOT = "createSlot";

	// Command argument keys.
	private static final String ARG_DIRECTION = "direction";

	private static final String ARG_DATE = "date";

	private static final String ARG_GRANULARITY = "granularity";

	private static final String ARG_EVENT_ID = "eventId";

	private static final String ARG_START = "start";

	private static final String ARG_END = "end";

	private static final String ARG_ALL_DAY = "allDay";

	private final CalendarModel _model;

	private final CalendarModelListener _modelListener = source -> rebuild();

	private final TimeZone _timeZone;

	private final Locale _locale;

	private Granularity _granularity = Granularity.WEEK;

	private Date _anchor = new Date();

	private int _dayStartHour = 8;

	private int _dayEndHour = 18;

	private List<Integer> _nonWorkingDays = new ArrayList<>(List.of(Integer.valueOf(0), Integer.valueOf(6)));

	private boolean _editable = true;

	private Object _selectedKey;

	private final Map<String, CalendarEvent> _eventsById = new LinkedHashMap<>();

	private SelectionListener _selectionListener;

	/**
	 * Notified when the selected event changes, e.g. to write the selection onto a channel.
	 */
	public interface SelectionListener {
		/**
		 * Announces that the selected event changed.
		 *
		 * @param businessObject
		 *        The {@link CalendarEvent#getBusinessObject() business object} of the newly selected
		 *        event, or <code>null</code> if the selection was cleared.
		 */
		void selectionChanged(Object businessObject);
	}

	/**
	 * Creates a {@link CalendarViewControl} rendering the given model in the current session's locale
	 * and time zone.
	 *
	 * @param context
	 *        The React context.
	 * @param model
	 *        The event source and edit sink.
	 */
	public CalendarViewControl(ReactContext context, CalendarModel model) {
		super(context, null, "TLCalendar");
		_model = model;
		_timeZone = ThreadContext.getTimeZone();
		_locale = ThreadContext.getLocale();
		_model.addCalendarModelListener(_modelListener);
		buildState();
	}

	/**
	 * The initially displayed {@link Granularity}.
	 *
	 * @param granularity
	 *        The granularity to show.
	 * @return This control, for chaining.
	 */
	public CalendarViewControl setGranularity(Granularity granularity) {
		_granularity = granularity;
		rebuild();
		return this;
	}

	/**
	 * The date whose period is initially displayed.
	 *
	 * @param anchor
	 *        A date within the interval to show.
	 * @return This control, for chaining.
	 */
	public CalendarViewControl setAnchor(Date anchor) {
		_anchor = anchor;
		rebuild();
		return this;
	}

	/**
	 * Sets the working-hours band highlighted in the time grid.
	 *
	 * @param startHour
	 *        The first working hour (0..24).
	 * @param endHour
	 *        The hour after the last working hour (0..24).
	 * @return This control, for chaining.
	 */
	public CalendarViewControl setWorkingHours(int startHour, int endHour) {
		_dayStartHour = startHour;
		_dayEndHour = endHour;
		rebuild();
		return this;
	}

	/**
	 * Sets the weekdays shaded as non-working, as JavaScript weekday numbers (0 = Sunday .. 6 =
	 * Saturday).
	 *
	 * @param weekdays
	 *        The non-working weekdays.
	 * @return This control, for chaining.
	 */
	public CalendarViewControl setNonWorkingDays(List<Integer> weekdays) {
		_nonWorkingDays = new ArrayList<>(weekdays);
		rebuild();
		return this;
	}

	/**
	 * Whether the user may edit events by dragging.
	 *
	 * @param editable
	 *        Whether editing is allowed.
	 * @return This control, for chaining.
	 */
	public CalendarViewControl setEditable(boolean editable) {
		_editable = editable;
		rebuild();
		return this;
	}

	/**
	 * Registers the listener notified when the selected event changes.
	 *
	 * @param listener
	 *        The listener, or <code>null</code> to remove.
	 */
	public void setSelectionListener(SelectionListener listener) {
		_selectionListener = listener;
	}

	/**
	 * Selects the event backed by the given business object, e.g. in reaction to a selection channel
	 * change.
	 *
	 * @param businessObject
	 *        The business object to select, or <code>null</code> to clear the selection.
	 */
	public void setSelectedBusinessObject(Object businessObject) {
		if (!equalKeys(_selectedKey, businessObject)) {
			_selectedKey = businessObject;
			rebuild();
		}
	}

	private void rebuild() {
		buildState();
	}

	private void buildState() {
		Calendar cal = calendar(_anchor);
		long rangeStart = displayStart(cal);
		long rangeEnd = displayEnd(cal);

		putState(GRANULARITY, _granularity.name());
		putState(TITLE, title(rangeStart, rangeEnd));
		putState(RANGE_START, Long.valueOf(rangeStart));
		putState(RANGE_END, Long.valueOf(rangeEnd));
		putState(ANCHOR, Long.valueOf(_anchor.getTime()));
		putState(NOW, Long.valueOf(System.currentTimeMillis()));
		putState(FIRST_DAY_OF_WEEK, Integer.valueOf(cal.getFirstDayOfWeek() - 1));
		putState(DAY_START_HOUR, Integer.valueOf(_dayStartHour));
		putState(DAY_END_HOUR, Integer.valueOf(_dayEndHour));
		putState(NON_WORKING_DAYS, new ArrayList<>(_nonWorkingDays));
		putState(LOCALE, _locale.toLanguageTag());
		putState(EDITABLE, Boolean.valueOf(_editable));
		putState(EVENTS, buildEvents(rangeStart, rangeEnd));
	}

	private List<Map<String, Object>> buildEvents(long rangeStart, long rangeEnd) {
		_eventsById.clear();
		List<Map<String, Object>> result = new ArrayList<>();
		int index = 0;
		for (CalendarEvent event : _model.getEvents(new Date(rangeStart), new Date(rangeEnd))) {
			String id = "evt_" + index++;
			_eventsById.put(id, event);

			Map<String, Object> state = new LinkedHashMap<>();
			state.put(EVENT_ID, id);
			state.put(EVENT_START, Long.valueOf(event.getStart().getTime()));
			state.put(EVENT_END, Long.valueOf(event.getEnd().getTime()));
			state.put(EVENT_ALL_DAY, Boolean.valueOf(event.isAllDay()));
			state.put(EVENT_TITLE, event.getTitle() != null ? event.getTitle() : "");
			if (event.getTooltip() != null) {
				state.put(EVENT_TOOLTIP, event.getTooltip());
			}
			if (event.getCategory() != null) {
				state.put(EVENT_CATEGORY, event.getCategory());
			}
			state.put(EVENT_MOVABLE, Boolean.valueOf(_editable && event.isMovable()));
			state.put(EVENT_RESIZABLE, Boolean.valueOf(_editable && event.isResizable()));
			state.put(EVENT_SELECTED, Boolean.valueOf(isSelected(event)));
			result.add(state);
		}
		return result;
	}

	private boolean isSelected(CalendarEvent event) {
		return _selectedKey != null && equalKeys(_selectedKey, event.getBusinessObject());
	}

	private static boolean equalKeys(Object a, Object b) {
		return a == null ? b == null : a.equals(b);
	}

	// Date math.

	private Calendar calendar(Date date) {
		return CalendarUtil.createCalendar(date, _timeZone, _locale);
	}

	private static void toMidnight(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
	}

	private static long startOfWeek(Calendar cal) {
		toMidnight(cal);
		int firstDayOfWeek = cal.getFirstDayOfWeek();
		while (cal.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
			cal.add(Calendar.DAY_OF_MONTH, -1);
		}
		return cal.getTimeInMillis();
	}

	private long displayStart(Calendar cal) {
		Calendar work = (Calendar) cal.clone();
		switch (_granularity) {
			case DAY:
				toMidnight(work);
				return work.getTimeInMillis();
			case WORK_WEEK:
			case WEEK:
				return startOfWeek(work);
			case MONTH:
				work.set(Calendar.DAY_OF_MONTH, 1);
				return startOfWeek(work);
			case YEAR:
				work.set(Calendar.DAY_OF_YEAR, 1);
				toMidnight(work);
				return work.getTimeInMillis();
		}
		throw new IllegalStateException("Unhandled granularity: " + _granularity);
	}

	private long displayEnd(Calendar cal) {
		Calendar work = calendar(new Date(displayStart(cal)));
		switch (_granularity) {
			case DAY:
				work.add(Calendar.DAY_OF_MONTH, 1);
				break;
			case WORK_WEEK:
			case WEEK:
				work.add(Calendar.DAY_OF_MONTH, 7);
				break;
			case MONTH:
				work.add(Calendar.DAY_OF_MONTH, 42);
				break;
			case YEAR:
				work.add(Calendar.YEAR, 1);
				break;
		}
		return work.getTimeInMillis();
	}

	private String title(long rangeStart, long rangeEnd) {
		switch (_granularity) {
			case DAY:
				return format("EEEE, d MMMM yyyy", rangeStart);
			case WORK_WEEK:
			case WEEK: {
				// The displayed week spans seven days from rangeStart.
				Calendar last = calendar(new Date(rangeStart));
				last.add(Calendar.DAY_OF_MONTH, 6);
				return format("d MMM", rangeStart) + " – " + format("d MMM yyyy", last.getTimeInMillis());
			}
			case MONTH:
				// The anchor's month, not the grid start (which may lie in the previous month).
				return format("MMMM yyyy", _anchor.getTime());
			case YEAR:
				return format("yyyy", rangeStart);
		}
		throw new IllegalStateException("Unhandled granularity: " + _granularity);
	}

	private String format(String pattern, long millis) {
		SimpleDateFormat format = CalendarUtil.newSimpleDateFormat(pattern, _locale);
		format.setTimeZone(_timeZone);
		return format.format(new Date(millis));
	}

	// Command handlers.

	@ReactCommandHandler(CMD_NAVIGATE)
	void handleNavigate(Map<String, Object> args) {
		String direction = (String) args.get(ARG_DIRECTION);
		if (DIR_TODAY.equals(direction)) {
			_anchor = new Date();
		} else {
			int step = DIR_PREV.equals(direction) ? -1 : 1;
			Calendar cal = calendar(_anchor);
			switch (_granularity) {
				case DAY:
					cal.add(Calendar.DAY_OF_MONTH, step);
					break;
				case WORK_WEEK:
				case WEEK:
					cal.add(Calendar.DAY_OF_MONTH, 7 * step);
					break;
				case MONTH:
					cal.add(Calendar.MONTH, step);
					break;
				case YEAR:
					cal.add(Calendar.YEAR, step);
					break;
			}
			_anchor = cal.getTime();
		}
		rebuild();
	}

	@ReactCommandHandler(CMD_GOTO)
	void handleGoto(Map<String, Object> args) {
		Long date = asLong(args.get(ARG_DATE));
		if (date != null) {
			_anchor = new Date(date.longValue());
		}
		String granularity = (String) args.get(ARG_GRANULARITY);
		if (granularity != null) {
			_granularity = Granularity.valueOf(granularity);
		}
		rebuild();
	}

	@ReactCommandHandler(CMD_SWITCH_GRANULARITY)
	void handleSwitchGranularity(Map<String, Object> args) {
		String granularity = (String) args.get(ARG_GRANULARITY);
		if (granularity != null) {
			_granularity = Granularity.valueOf(granularity);
			rebuild();
		}
	}

	@ReactCommandHandler(CMD_SELECT_EVENT)
	void handleSelectEvent(Map<String, Object> args) {
		String eventId = (String) args.get(ARG_EVENT_ID);
		CalendarEvent event = eventId != null ? _eventsById.get(eventId) : null;
		if (applySelection(event != null ? event.getBusinessObject() : null)) {
			rebuild();
		}
	}

	/**
	 * Updates the selected business object and notifies the {@link SelectionListener}, without
	 * rebuilding the client state.
	 *
	 * @param key
	 *        The newly selected business object, or <code>null</code> to clear.
	 * @return Whether the selection actually changed.
	 */
	private boolean applySelection(Object key) {
		if (equalKeys(_selectedKey, key)) {
			return false;
		}
		_selectedKey = key;
		if (_selectionListener != null) {
			_selectionListener.selectionChanged(key);
		}
		return true;
	}

	@ReactCommandHandler(CMD_MOVE_EVENT)
	void handleMoveEvent(Map<String, Object> args) {
		if (!_editable) {
			return;
		}
		CalendarEvent event = _eventsById.get(args.get(ARG_EVENT_ID));
		Long start = asLong(args.get(ARG_START));
		Long end = asLong(args.get(ARG_END));
		if (event != null && event.isMovable() && start != null && end != null) {
			_model.moveEvent(event, new Date(start.longValue()), new Date(end.longValue()));
			rebuild();
		}
	}

	@ReactCommandHandler(CMD_RESIZE_EVENT)
	void handleResizeEvent(Map<String, Object> args) {
		if (!_editable) {
			return;
		}
		CalendarEvent event = _eventsById.get(args.get(ARG_EVENT_ID));
		Long end = asLong(args.get(ARG_END));
		if (event != null && event.isResizable() && end != null) {
			_model.resizeEvent(event, new Date(end.longValue()));
			rebuild();
		}
	}

	@ReactCommandHandler(CMD_CREATE_SLOT)
	void handleCreateSlot(Map<String, Object> args) {
		if (!_editable) {
			return;
		}
		Long start = asLong(args.get(ARG_START));
		Long end = asLong(args.get(ARG_END));
		boolean allDay = Boolean.TRUE.equals(args.get(ARG_ALL_DAY));
		if (start != null && end != null) {
			CalendarEvent created = _model.createEvent(new Date(start.longValue()), new Date(end.longValue()), allDay);
			if (created != null) {
				// Select the freshly created event so its detail view opens immediately.
				applySelection(created.getBusinessObject());
			}
			rebuild();
		}
	}

	private static Long asLong(Object value) {
		if (value instanceof Number number) {
			return Long.valueOf(number.longValue());
		}
		if (value instanceof String text && !text.isEmpty()) {
			return Long.valueOf(Long.parseLong(text));
		}
		return null;
	}

	@Override
	protected void onCleanup() {
		_model.removeCalendarModelListener(_modelListener);
		super.onCleanup();
	}

}
