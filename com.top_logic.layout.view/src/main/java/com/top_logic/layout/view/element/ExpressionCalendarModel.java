/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.control.calendar.CalendarEvent;
import com.top_logic.layout.react.control.calendar.CalendarModel;
import com.top_logic.layout.react.control.calendar.CalendarModelListener;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link CalendarModel} backed by a list of business objects and a set of TL-Script expressions that
 * derive each {@link CalendarEvent}'s fields from an object.
 *
 * <p>
 * This is the bridge from the declarative {@link CalendarElement} to the calendar control's plain
 * {@link CalendarModel} contract: every business object is wrapped in an {@link WrappedEvent} whose
 * getters evaluate the configured expressions. Edits run the configured write-back expressions inside
 * a transaction.
 * </p>
 */
public class ExpressionCalendarModel implements CalendarModel {

	/**
	 * The per-field expressions deriving a {@link CalendarEvent} from a business object.
	 *
	 * <p>
	 * Each expression is a single-argument function receiving the business object. Only
	 * {@link #_start} is required; the others may be <code>null</code>.
	 * </p>
	 */
	public static final class EventExprs {

		final QueryExecutor _start;

		final QueryExecutor _end;

		final QueryExecutor _allDay;

		final QueryExecutor _title;

		final QueryExecutor _tooltip;

		final QueryExecutor _category;

		final QueryExecutor _movable;

		final QueryExecutor _resizable;

		/**
		 * Creates an {@link EventExprs} bundle.
		 *
		 * @param start
		 *        Function computing the {@link CalendarEvent#getStart() start}, required.
		 * @param end
		 *        Function computing the {@link CalendarEvent#getEnd() end}, or <code>null</code> for a
		 *        default one-hour duration.
		 * @param allDay
		 *        Function computing {@link CalendarEvent#isAllDay()}, or <code>null</code> for
		 *        <code>false</code>.
		 * @param title
		 *        Function computing the {@link CalendarEvent#getTitle() title}, or <code>null</code>.
		 * @param tooltip
		 *        Function computing the {@link CalendarEvent#getTooltip() tooltip}, or
		 *        <code>null</code>.
		 * @param category
		 *        Function computing the {@link CalendarEvent#getCategory() category}, or
		 *        <code>null</code>.
		 * @param movable
		 *        Function computing {@link CalendarEvent#isMovable()}, or <code>null</code> for
		 *        <code>true</code>.
		 * @param resizable
		 *        Function computing {@link CalendarEvent#isResizable()}, or <code>null</code> for
		 *        <code>true</code>.
		 */
		public EventExprs(QueryExecutor start, QueryExecutor end, QueryExecutor allDay, QueryExecutor title,
				QueryExecutor tooltip, QueryExecutor category, QueryExecutor movable, QueryExecutor resizable) {
			_start = start;
			_end = end;
			_allDay = allDay;
			_title = title;
			_tooltip = tooltip;
			_category = category;
			_movable = movable;
			_resizable = resizable;
		}
	}

	private static final long DEFAULT_DURATION_MS = 3600000L;

	private final List<Object> _objects;

	private final EventExprs _exprs;

	private final QueryExecutor _onMove;

	private final QueryExecutor _onResize;

	private final QueryExecutor _onCreate;

	private final List<CalendarModelListener> _listeners = new ArrayList<>();

	/**
	 * Creates an {@link ExpressionCalendarModel}.
	 *
	 * @param objects
	 *        The initial business objects to render.
	 * @param exprs
	 *        The field-deriving expressions.
	 * @param onMove
	 *        Write-back function <code>object, newStart, newEnd -&gt; ...</code> for a drag-move, or
	 *        <code>null</code> to disable moving.
	 * @param onResize
	 *        Write-back function <code>object, newEnd -&gt; ...</code> for a resize, or
	 *        <code>null</code> to disable resizing.
	 * @param onCreate
	 *        Write-back function <code>start, end, allDay -&gt; object</code> for slot creation, or
	 *        <code>null</code> to disable creation.
	 */
	public ExpressionCalendarModel(Collection<?> objects, EventExprs exprs, QueryExecutor onMove,
			QueryExecutor onResize, QueryExecutor onCreate) {
		_objects = new ArrayList<>(objects);
		_exprs = exprs;
		_onMove = onMove;
		_onResize = onResize;
		_onCreate = onCreate;
	}

	/**
	 * Replaces the rendered business objects and notifies listeners.
	 *
	 * @param objects
	 *        The new business objects.
	 */
	public void setObjects(Collection<?> objects) {
		_objects.clear();
		_objects.addAll(objects);
		fireChanged();
	}

	@Override
	public Collection<? extends CalendarEvent> getEvents(Date start, Date end) {
		List<CalendarEvent> result = new ArrayList<>();
		for (Object object : _objects) {
			// Skip objects deleted concurrently (e.g. the just-deleted selected appointment before the
			// object list is re-queried): evaluating the field expressions on a deleted object throws.
			if (object instanceof TLObject persistent && !persistent.tValid()) {
				continue;
			}
			WrappedEvent event = new WrappedEvent(object, _exprs);
			if (event.getStart() != null && event.getStart().before(end) && event.getEnd().after(start)) {
				result.add(event);
			}
		}
		return result;
	}

	@Override
	public void moveEvent(CalendarEvent event, Date newStart, Date newEnd) {
		if (_onMove == null) {
			return;
		}
		inTransaction(() -> _onMove.execute(event.getBusinessObject(), newStart, newEnd));
		fireChanged();
	}

	@Override
	public void resizeEvent(CalendarEvent event, Date newEnd) {
		if (_onResize == null) {
			return;
		}
		inTransaction(() -> _onResize.execute(event.getBusinessObject(), newEnd));
		fireChanged();
	}

	@Override
	public CalendarEvent createEvent(Date start, Date end, boolean allDay) {
		if (_onCreate == null) {
			return null;
		}
		Object[] created = {null};
		inTransaction(() -> created[0] = _onCreate.execute(start, end, Boolean.valueOf(allDay)));
		if (created[0] != null) {
			_objects.add(created[0]);
		}
		fireChanged();
		return created[0] != null ? new WrappedEvent(created[0], _exprs) : null;
	}

	private static void inTransaction(Runnable action) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			action.run();
			tx.commit();
		}
	}

	@Override
	public void addCalendarModelListener(CalendarModelListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeCalendarModelListener(CalendarModelListener listener) {
		_listeners.remove(listener);
	}

	private void fireChanged() {
		for (CalendarModelListener listener : new ArrayList<>(_listeners)) {
			listener.handleCalendarModelChanged(this);
		}
	}

	/**
	 * {@link CalendarEvent} that reads its fields from a business object through the configured
	 * {@link EventExprs}.
	 */
	private static final class WrappedEvent implements CalendarEvent {

		private final Object _object;

		private final Date _start;

		private final Date _end;

		private final boolean _allDay;

		private final String _title;

		private final String _tooltip;

		private final String _category;

		private final boolean _movable;

		private final boolean _resizable;

		WrappedEvent(Object object, EventExprs exprs) {
			_object = object;
			_start = toDate(eval(exprs._start, object));
			Date end = toDate(eval(exprs._end, object));
			_end = end != null ? end : (_start != null ? new Date(_start.getTime() + DEFAULT_DURATION_MS) : null);
			_allDay = toBoolean(exprs._allDay, object, false);
			_title = toStringOrNull(eval(exprs._title, object));
			_tooltip = toStringOrNull(eval(exprs._tooltip, object));
			_category = toStringOrNull(eval(exprs._category, object));
			_movable = toBoolean(exprs._movable, object, true);
			_resizable = toBoolean(exprs._resizable, object, true);
		}

		private static Object eval(QueryExecutor expr, Object object) {
			return expr == null ? null : expr.execute(object);
		}

		private static Date toDate(Object value) {
			if (value instanceof Date date) {
				return date;
			}
			if (value instanceof Calendar calendar) {
				return calendar.getTime();
			}
			return null;
		}

		private static boolean toBoolean(QueryExecutor expr, Object object, boolean defaultValue) {
			Object value = eval(expr, object);
			if (value == null) {
				return defaultValue;
			}
			return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
		}

		private static String toStringOrNull(Object value) {
			return value == null ? null : value.toString();
		}

		@Override
		public Date getStart() {
			return _start;
		}

		@Override
		public Date getEnd() {
			return _end;
		}

		@Override
		public boolean isAllDay() {
			return _allDay;
		}

		@Override
		public String getTitle() {
			return _title;
		}

		@Override
		public String getTooltip() {
			return _tooltip;
		}

		@Override
		public String getCategory() {
			return _category;
		}

		@Override
		public boolean isMovable() {
			return _movable;
		}

		@Override
		public boolean isResizable() {
			return _resizable;
		}

		@Override
		public Object getBusinessObject() {
			return _object;
		}
	}

}
