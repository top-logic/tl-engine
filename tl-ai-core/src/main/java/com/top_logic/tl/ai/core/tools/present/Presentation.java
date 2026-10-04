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
 * What the agent asked a page to show: a set of objects, and whether as a table, a chart or a form -
 * or data a query computed, as a chart or a table.
 *
 * <p>
 * The value the {@code present} tool hands to a page. It carries the objects themselves, already
 * reduced to the ones the acting user may read, or the computed data as {@link ChartData}, never the
 * expression that found them - a page re-evaluating the query would ask a second time, at another
 * moment and possibly with other rights, and could show something the agent never saw. The
 * {@link #query() query} is kept as text only to say where the objects or the data came from.
 * </p>
 *
 * <p>
 * Every chart reaches the page in one format, the Chart.js configuration of {@link #chartData()}:
 * a chart of objects computes it from its objects, a chart of data carries it.
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

	private final ChartData _data;

	private Presentation(PresentAs as, List<TLObject> objects, String title, String query, ChartSpec chart,
			QueryExecutor labelFunction, QueryExecutor valueFunction, ChartData data) {
		_as = as;
		_objects = List.copyOf(objects);
		_title = title;
		_query = query;
		_chart = chart;
		_labelFunction = labelFunction;
		_valueFunction = valueFunction;
		_data = data;
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
		return new Presentation(as, objects, title, query, null, null, null, null);
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
		return new Presentation(PresentAs.CHART, objects, title, query, chart, label, value, null);
	}

	/**
	 * A chart or a table of data a query computed.
	 *
	 * @param as
	 *        {@link PresentAs#CHART} or {@link PresentAs#TABLE}; a table takes data that
	 *        {@link ChartData#isTabular() reads as one}.
	 * @param data
	 *        The data, already reduced to what the user may read.
	 * @param title
	 *        What the page calls this view, or {@code null} to let the page decide.
	 * @param query
	 *        The expression that computed the data.
	 * @throws IllegalArgumentException
	 *         If the data cannot be shown as asked; the message says why, for the model.
	 */
	public static Presentation data(PresentAs as, ChartData data, String title, String query) {
		switch (as) {
			case FORM:
				throw new IllegalArgumentException("a FORM shows one object, and the query computed data. Use CHART"
					+ " or TABLE.");
			case TABLE:
				if (!data.isTabular()) {
					throw new IllegalArgumentException("a TABLE shows a label and numbers per row, and this chart's"
						+ " points have coordinates of their own. Use CHART.");
				}
				break;
			default:
				break;
		}
		return new Presentation(as, List.of(), title, query, null, null, null, data);
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
	 * Whether this shows data a query computed rather than objects.
	 */
	public boolean isData() {
		return _data != null;
	}

	/**
	 * How the objects are shown.
	 */
	public PresentAs as() {
		return _as;
	}

	/**
	 * The objects, in the order they are shown; empty for {@link #isData() data}.
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
	 * What a chart of objects computes from each of them, or {@code null} for a table, a form, or
	 * {@link #isData() data}.
	 */
	public ChartSpec chart() {
		return _chart;
	}

	/**
	 * The chart as Chart.js draws it, or {@code null} for a table or a form of objects.
	 *
	 * <p>
	 * For a chart of objects computed from the objects as they are now, with each object as the
	 * {@code metadata} of its point; for data, the data the query computed.
	 * </p>
	 *
	 * @throws IllegalArgumentException
	 *         If a function of a chart of objects fails; see {@link #points()}.
	 */
	public ChartData chartData() {
		if (_data != null) {
			return _data;
		}
		if (_chart == null) {
			return null;
		}
		List<Point> points = points();
		List<String> labels = new ArrayList<>(points.size());
		List<Number> values = new ArrayList<>(points.size());
		List<TLObject> objects = new ArrayList<>(points.size());
		for (Point point : points) {
			labels.add(point.label());
			values.add(point.value());
			objects.add(point.object());
		}
		return ChartData.ofPoints(_chart.kind(), _title, labels, values, objects);
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
			// A label function may return an object - a status, a customer: it is named by its
			// display label, not by its technical name.
			result.add(new Point(object, ChartData.text(label),
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
		if (_data != null) {
			return _as.name().toLowerCase() + " of data (" + _data.labels().size() + " label(s))";
		}
		return _as.name().toLowerCase() + " of " + _objects.size() + " object(s)";
	}
}
