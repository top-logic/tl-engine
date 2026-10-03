/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.present;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tl.ai.core.tools.script.QueryLanguage;
import com.top_logic.tl.ai.core.tools.script.TLScriptExecutor;

/**
 * What the agent asked a page to show: a set of objects, and whether as a table, a chart or a form.
 *
 * <p>
 * The value the {@code present} tool hands to a page. It carries the objects themselves, already
 * reduced to the ones the acting user may read, never the expression that found them - a page
 * re-evaluating the query would ask a second time, at another moment and possibly with other rights,
 * and could show something the agent never saw. The {@link #query() query} is kept as text only to
 * say where the objects came from.
 * </p>
 *
 * <p>
 * Immutable. A page keeps one per step of its history, and going back to a step must show what that
 * step showed.
 * </p>
 */
public final class Presentation {

	/**
	 * The label and value of one object, as a chart draws it.
	 *
	 * @param object
	 *        The object the point stands for, which a click on the point opens.
	 * @param label
	 *        What the point is called.
	 * @param value
	 *        The point's number.
	 */
	public record Point(TLObject object, String label, Number value) {
		// Sole record.
	}

	private final PresentAs _as;

	private final List<TLObject> _objects;

	private final String _title;

	private final String _query;

	private final ChartSpec _chart;

	private final QueryExecutor _labelFunction;

	private final QueryExecutor _valueFunction;

	private Presentation(PresentAs as, List<TLObject> objects, String title, String query, ChartSpec chart,
			QueryExecutor labelFunction, QueryExecutor valueFunction) {
		_as = as;
		_objects = List.copyOf(objects);
		_title = title;
		_query = query;
		_chart = chart;
		_labelFunction = labelFunction;
		_valueFunction = valueFunction;
	}

	/**
	 * A table or a form of the given objects.
	 *
	 * @param as
	 *        {@link PresentAs#TABLE} or {@link PresentAs#FORM}; a form takes exactly one object.
	 * @param objects
	 *        The objects, readable by the user the page belongs to.
	 * @param title
	 *        What the page calls this view, or {@code null} to let the page decide.
	 * @param query
	 *        The expression that found the objects, or {@code null} when they were named by id.
	 */
	public static Presentation of(PresentAs as, List<TLObject> objects, String title, String query) {
		if (as == PresentAs.CHART) {
			throw new IllegalArgumentException("A chart needs a ChartSpec, see chart(...).");
		}
		if (as == PresentAs.FORM && objects.size() != 1) {
			throw new IllegalArgumentException("A form shows exactly one object, not " + objects.size() + ".");
		}
		return new Presentation(as, objects, title, query, null, null, null);
	}

	/**
	 * A chart of the given objects.
	 *
	 * @param objects
	 *        The objects, readable by the user the page belongs to.
	 * @param chart
	 *        What the chart computes from each of them.
	 * @param title
	 *        What the page calls this view, or {@code null} to let the page decide.
	 * @param query
	 *        The expression that found the objects, or {@code null} when they were named by id.
	 * @throws IllegalArgumentException
	 *         If a function of the spec does not compile; the message says which and why.
	 */
	public static Presentation chart(List<TLObject> objects, ChartSpec chart, String title, String query) {
		QueryExecutor label = function("label", chart.label());
		QueryExecutor value = function("value", chart.value());
		return new Presentation(PresentAs.CHART, objects, title, query, chart, label, value);
	}

	private static QueryExecutor function(String name, String source) {
		if (source == null || source.isBlank()) {
			throw new IllegalArgumentException("The chart has no " + name + " function. Pass a TL-Script function"
				+ " of one object, e.g. e -> $e.get(`module:Type#attribute`).");
		}
		try {
			return TLScriptExecutor.compileReadOnly(source);
		} catch (TLScriptExecutor.NotReadOnly ex) {
			throw new IllegalArgumentException("The chart's " + name + " function: " + ex.getMessage(), ex);
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("The chart's " + name + " function does not compile: "
				+ QueryLanguage.withReadingHelp(ex.getMessage()), ex);
		}
	}

	/**
	 * How the objects are shown.
	 */
	public PresentAs as() {
		return _as;
	}

	/**
	 * The objects, in the order they are shown.
	 */
	public List<TLObject> objects() {
		return _objects;
	}

	/**
	 * What the page calls this view, or {@code null} when the agent named nothing.
	 */
	public String title() {
		return _title;
	}

	/**
	 * The expression that found the objects, or {@code null} when they were named by id.
	 */
	public String query() {
		return _query;
	}

	/**
	 * What the chart computes, or {@code null} for a table or a form.
	 */
	public ChartSpec chart() {
		return _chart;
	}

	/**
	 * Computes the chart's points from the objects as they are now.
	 *
	 * <p>
	 * Evaluated on every call rather than once, so that a page showing the chart again - after the
	 * user went back to it, or after an object changed - shows the values of now.
	 * </p>
	 *
	 * @return One point per object, in the objects' order; empty for a table or a form.
	 * @throws IllegalArgumentException
	 *         If a function fails on an object, or a value is not a number; the message names the
	 *         object's position and what went wrong.
	 */
	public List<Point> points() {
		if (_chart == null) {
			return List.of();
		}
		List<Point> result = new ArrayList<>(_objects.size());
		for (int n = 0; n < _objects.size(); n++) {
			TLObject object = _objects.get(n);
			Object label = apply("label", _labelFunction, object, n);
			Object value = apply("value", _valueFunction, object, n);
			if (value != null && !(value instanceof Number)) {
				throw new IllegalArgumentException("The chart's value function returned '" + value + "' for object "
					+ (n + 1) + ", which is not a number.");
			}
			result.add(new Point(object, label == null ? "" : String.valueOf(label),
				value == null ? Integer.valueOf(0) : (Number) value));
		}
		return result;
	}

	private static Object apply(String name, QueryExecutor function, TLObject object, int index) {
		try {
			return TLScriptExecutor.evaluate(function, object);
		} catch (RuntimeException ex) {
			throw new IllegalArgumentException("The chart's " + name + " function failed on object " + (index + 1)
				+ ": " + TLScriptExecutor.messageOf(ex), ex);
		}
	}

	@Override
	public String toString() {
		return _as.name().toLowerCase() + " of " + _objects.size() + " object(s)";
	}
}
