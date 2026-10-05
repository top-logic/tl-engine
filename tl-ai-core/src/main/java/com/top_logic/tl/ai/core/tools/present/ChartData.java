/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.present;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.security.ReadFilter;

/**
 * Data a TL-Script computed, as the Chart.js configuration a chart draws it with.
 *
 * <p>
 * There is one format: the configuration a {@code <chartjs>} component's or a {@code <chart>}
 * element's {@code data} script returns - <code>{'type': 'bar', 'data': {'labels': [...],
 * 'datasets': [{'label': ..., 'data': [...]}]}, 'options': {...}}</code>. A query that returns such a
 * configuration is taken as written, with everything Chart.js can draw: stacked bars, a line over
 * bars, a second axis. Simpler results are short forms of it and are turned into it here:
 * </p>
 * <ul>
 * <li>a dictionary from label to number - what {@code groupBy(key, g -> $g.size())} returns - is one
 * series;</li>
 * <li>a dictionary from label to a dictionary of numbers - {@code groupBy(key, g -> {"count": ...,
 * "sum": ...})} - is one series per inner key;</li>
 * <li>a list of records, <code>[{"label": "Jan", "planned": 5, "actual": 3}, ...]</code>, is one
 * series per numeric key. The label is the key {@code label}, or else the record's first value that
 * is not a number.</li>
 * </ul>
 *
 * <p>
 * Whatever the form, what leaves this class is plain data: texts, numbers, lists and dictionaries.
 * A model object that stands as a label becomes its display label - the engine's chart refuses an
 * object anywhere but in a dataset's {@code metadata} - and one the user may not read becomes
 * nothing, as {@link ReadFilter} has it. Objects stay only in {@code metadata}, where a page reads
 * them as the object a click on the point opens, and only those the user may read. Handler and
 * tooltip names ({@code onClick}, {@code onLegendClick}, {@code tooltip}) are dropped: which handlers
 * exist is the page's business, not the model's.
 * </p>
 *
 * <p>
 * The data is bounded: at most {@link #MAX_POINTS} points per series and {@link #MAX_SERIES} series.
 * What lies beyond is cut, and {@link #cut()} says so, so that the model can tell the user rather
 * than present a part as the whole.
 * </p>
 *
 * <p>
 * Every refusal is an {@link IllegalArgumentException} whose message is one sentence for the model:
 * what is wrong, and where.
 * </p>
 */
public final class ChartData {

	/** How many points one series shows; the rest is cut. */
	public static final int MAX_POINTS = 200;

	/** How many series a chart shows; the rest is cut. */
	public static final int MAX_SERIES = 20;

	/**
	 * The chart types Chart.js draws without a plugin, by the names a configuration writes them.
	 */
	public static final Set<String> TYPES =
		Set.of("bar", "line", "pie", "doughnut", "polarArea", "radar", "scatter", "bubble");

	/** The dataset keys naming a page's handlers, which a model does not get to choose. */
	private static final Set<String> HANDLER_KEYS = Set.of("onClick", "onLegendClick", "tooltip");

	private static final String LABEL = "label";

	private final Map<String, Object> _config;

	private final int _allLabels;

	private final int _allSeries;

	private ChartData(Map<String, Object> config, int allLabels, int allSeries) {
		_config = config;
		_allLabels = allLabels;
		_allSeries = allSeries;
	}

	/**
	 * Whether a query result is data rather than model objects: a dictionary, or a list of
	 * dictionaries.
	 */
	public static boolean isData(Object result) {
		if (result instanceof Map<?, ?>) {
			return true;
		}
		if (result instanceof Collection<?> many && !many.isEmpty()) {
			for (Object element : many) {
				if (!(element instanceof Map<?, ?>)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * The chart of a query result that {@link #isData(Object) is data}.
	 *
	 * @param result
	 *        The query result: a Chart.js configuration, a dictionary or a list of records.
	 * @param kind
	 *        The kind of chart a dictionary or a list of records is drawn as; a configuration names
	 *        its own.
	 * @param seriesName
	 *        What a single series of a dictionary of numbers is called, or {@code null}.
	 * @param filter
	 *        The rights the result is shown with.
	 * @throws IllegalArgumentException
	 *         If the result cannot be drawn; the message says why, for the model.
	 */
	public static ChartData of(Object result, ChartKind kind, String seriesName, ReadFilter filter) {
		if (isConfiguration(result)) {
			return configuration((Map<?, ?>) result, filter);
		}
		if (result instanceof Map<?, ?> map) {
			return dictionary(map, kind, seriesName, filter);
		}
		if (isData(result)) {
			return records((Collection<?>) result, kind, filter);
		}
		throw new IllegalArgumentException("This is no chart data.");
	}

	/**
	 * The chart of one series of points, each standing for an object - what a chart of objects
	 * computes.
	 *
	 * @param kind
	 *        The kind of chart.
	 * @param seriesName
	 *        What the series is called, or {@code null}.
	 * @param labels
	 *        The labels of the points.
	 * @param values
	 *        The values of the points, as many as there are labels.
	 * @param objects
	 *        The object of each point, which a click on it opens, as many as there are labels.
	 */
	public static ChartData ofPoints(ChartKind kind, String seriesName, List<String> labels, List<? extends Number> values,
			List<? extends TLObject> objects) {
		Map<String, Object> dataset = new LinkedHashMap<>();
		dataset.put(LABEL, seriesName == null ? "" : seriesName);
		dataset.put("data", new ArrayList<>(values));
		dataset.put("metadata", new ArrayList<>(objects));
		return new ChartData(config(kind.chartJsType(), new ArrayList<>(labels), List.of(dataset)),
			labels.size(), 1);
	}

	/**
	 * Whether a value is a whole Chart.js configuration, as a chart component's script returns it.
	 */
	private static boolean isConfiguration(Object result) {
		return result instanceof Map<?, ?> map && map.get("type") instanceof CharSequence
			&& map.get("data") instanceof Map<?, ?>;
	}

	private static ChartData configuration(Map<?, ?> raw, ReadFilter filter) {
		String type = raw.get("type").toString();
		checkType(type, "The chart's 'type'");

		Map<?, ?> rawData = (Map<?, ?>) raw.get("data");
		if (!(rawData.get("datasets") instanceof List<?> rawDatasets) || rawDatasets.isEmpty()) {
			throw new IllegalArgumentException("The chart's 'data' has no 'datasets'. Write data: {'labels': [...],"
				+ " 'datasets': [{'label': ..., 'data': [numbers]}]}.");
		}

		Map<String, Object> config = plainMap(raw, Set.of("data"), filter);
		Map<String, Object> data = plainMap(rawData, Set.of("datasets"), filter);
		config.put("data", data);
		int allLabels = 0;
		if (data.get("labels") instanceof List<?> labels) {
			allLabels = labels.size();
			data.put("labels", cut(labels));
		}

		List<Object> datasets = new ArrayList<>();
		for (int n = 0; n < rawDatasets.size() && n < MAX_SERIES; n++) {
			if (!(rawDatasets.get(n) instanceof Map<?, ?> rawDataset)) {
				throw new IllegalArgumentException("Dataset " + (n + 1) + " is not a dictionary. Each dataset is"
					+ " {'label': ..., 'data': [numbers]}.");
			}
			datasets.add(dataset(n, rawDataset, filter));
			allLabels = Math.max(allLabels, ((List<?>) rawDataset.get("data")).size());
		}
		data.put("datasets", datasets);
		return new ChartData(config, allLabels, rawDatasets.size());
	}

	private static Map<String, Object> dataset(int index, Map<?, ?> raw, ReadFilter filter) {
		String where = "Dataset " + (index + 1);
		if (raw.get("type") instanceof CharSequence type) {
			checkType(type.toString(), where + "'s 'type'");
		}
		if (!(raw.get("data") instanceof List<?> values)) {
			throw new IllegalArgumentException(where + " has no 'data' list. Each dataset is {'label': ...,"
				+ " 'data': [numbers]}.");
		}
		for (int n = 0; n < values.size(); n++) {
			Object value = values.get(n);
			if (value != null && !(value instanceof Number) && !(value instanceof Map<?, ?>)) {
				throw new IllegalArgumentException(where + ", point " + (n + 1) + ": " + describe(value)
					+ " is not a number. A dataset's 'data' holds numbers, or points such as {'x': 1, 'y': 2}.");
			}
		}

		Map<String, Object> dataset = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : raw.entrySet()) {
			String key = String.valueOf(entry.getKey());
			if (HANDLER_KEYS.contains(key)) {
				continue;
			}
			if ("metadata".equals(key)) {
				if (entry.getValue() instanceof Collection<?> objects) {
					List<Object> readable = new ArrayList<>();
					for (Object object : objects) {
						// Kept in place, so that the n-th object still belongs to the n-th point.
						readable.add(object instanceof TLObject tl && filter.canRead(tl) ? tl : null);
					}
					dataset.put(key, cut(readable));
				}
				continue;
			}
			Object value = plain(entry.getValue(), filter);
			dataset.put(key, "data".equals(key) ? cut((List<?>) value) : value);
		}
		return dataset;
	}

	private static ChartData dictionary(Map<?, ?> map, ChartKind kind, String seriesName, ReadFilter filter) {
		if (map.isEmpty()) {
			throw new IllegalArgumentException("The query returned an empty dictionary: there is nothing to show.");
		}
		List<Map.Entry<?, ?>> entries = new ArrayList<>();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			// A key the user may not read contributes nothing, not even its number.
			if (!(entry.getKey() instanceof TLObject key) || filter.canRead(key)) {
				entries.add(entry);
			}
		}
		sortClassifiers(entries);

		boolean nested = false;
		boolean flat = false;
		for (Map.Entry<?, ?> entry : entries) {
			Object value = entry.getValue();
			if (value instanceof Map<?, ?>) {
				nested = true;
			} else if (value == null || value instanceof Number) {
				flat = true;
			} else {
				throw new IllegalArgumentException("The value for '" + text(entry.getKey()) + "' is " + describe(value)
					+ ", not a number." + (value instanceof Collection<?>
						? " Compute the number in the query, e.g. groupBy(x -> ..., g -> $g.size())."
						: ""));
			}
		}
		if (nested && flat) {
			throw new IllegalArgumentException("The dictionary mixes numbers and dictionaries as values. Return"
				+ " either {label: number} or {label: {series: number}} for every label.");
		}

		List<Object> labels = new ArrayList<>();
		List<Map<?, ?>> rows = new ArrayList<>();
		for (Map.Entry<?, ?> entry : entries) {
			labels.add(text(entry.getKey()));
			rows.add(nested ? (Map<?, ?>) entry.getValue() : null);
		}
		if (!nested) {
			List<Object> values = new ArrayList<>();
			for (Map.Entry<?, ?> entry : entries) {
				values.add(entry.getValue());
			}
			Map<String, Object> dataset = new LinkedHashMap<>();
			dataset.put(LABEL, seriesName == null ? "" : seriesName);
			dataset.put("data", cut(values));
			return new ChartData(config(kind.chartJsType(), cut(labels), List.of(dataset)), labels.size(), 1);
		}
		return series(kind, labels, rows, null, "the value for '%s'");
	}

	private static ChartData records(Collection<?> records, ChartKind kind, ReadFilter filter) {
		List<Map<?, ?>> rows = new ArrayList<>();
		for (Object record : records) {
			rows.add((Map<?, ?>) record);
		}
		String labelKey = labelKey(rows.get(0));
		List<Object> labels = new ArrayList<>();
		List<Map<?, ?>> kept = new ArrayList<>();
		for (int n = 0; n < rows.size(); n++) {
			Map<?, ?> row = rows.get(n);
			if (!row.containsKey(labelKey)) {
				throw new IllegalArgumentException("Record " + (n + 1) + " has no '" + labelKey + "'. Every record"
					+ " names its point under the same key.");
			}
			Object label = row.get(labelKey);
			if (label instanceof TLObject object && !filter.canRead(object)) {
				// A point named by an object the user may not read is not shown at all.
				continue;
			}
			labels.add(text(label));
			kept.add(row);
		}
		return series(kind, labels, kept, labelKey, "record '%s'");
	}

	/**
	 * The key a record names its point by: {@code label}, or else the key of its first value that is
	 * not a number.
	 */
	private static String labelKey(Map<?, ?> first) {
		if (first.containsKey(LABEL)) {
			return LABEL;
		}
		for (Map.Entry<?, ?> entry : first.entrySet()) {
			if (!(entry.getValue() instanceof Number)) {
				return String.valueOf(entry.getKey());
			}
		}
		throw new IllegalArgumentException("A record needs a text that names its point, e.g. {\"label\": \"Jan\","
			+ " \"planned\": 5, \"actual\": 3}; this one holds only numbers.");
	}

	/**
	 * One series per key of the rows, other than the label key, in the order the keys first occur.
	 */
	private static ChartData series(ChartKind kind, List<Object> labels, List<Map<?, ?>> rows, String labelKey,
			String rowName) {
		Set<String> keys = new LinkedHashSet<>();
		for (Map<?, ?> row : rows) {
			for (Object key : row.keySet()) {
				String name = String.valueOf(key);
				if (!name.equals(labelKey)) {
					keys.add(name);
				}
			}
		}
		if (keys.isEmpty()) {
			throw new IllegalArgumentException("There is no number to draw: every record holds only its label.");
		}
		List<Object> datasets = new ArrayList<>();
		for (String key : keys) {
			if (datasets.size() == MAX_SERIES) {
				break;
			}
			List<Object> values = new ArrayList<>();
			for (int n = 0; n < rows.size(); n++) {
				Object value = valueOf(rows.get(n), key);
				if (value != null && !(value instanceof Number)) {
					throw new IllegalArgumentException(String.format(rowName, labels.get(n)) + ": '" + key + "' is "
						+ describe(value) + ", which is not a number. Besides its label, a record holds numbers"
						+ " only.");
				}
				values.add(value);
			}
			Map<String, Object> dataset = new LinkedHashMap<>();
			dataset.put(LABEL, key);
			dataset.put("data", cut(values));
			datasets.add(dataset);
		}
		return new ChartData(config(kind.chartJsType(), cut(labels), datasets), labels.size(), keys.size());
	}

	private static Object valueOf(Map<?, ?> row, String key) {
		for (Map.Entry<?, ?> entry : row.entrySet()) {
			if (key.equals(String.valueOf(entry.getKey()))) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Puts the values of an enumeration in the enumeration's order: a grouping by status is read in
	 * the order the statuses follow each other, not in the order the first object of each was found.
	 */
	private static void sortClassifiers(List<Map.Entry<?, ?>> entries) {
		Object owner = null;
		for (Map.Entry<?, ?> entry : entries) {
			if (!(entry.getKey() instanceof TLClassifier classifier)
				|| (owner != null && owner != classifier.getOwner())) {
				return;
			}
			owner = classifier.getOwner();
		}
		entries.sort(Comparator.comparingInt(entry -> {
			TLClassifier classifier = (TLClassifier) entry.getKey();
			return classifier.getOwner().getClassifiers().indexOf(classifier);
		}));
	}

	private static Map<String, Object> config(String type, List<Object> labels, List<Object> datasets) {
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("labels", labels);
		data.put("datasets", datasets);
		Map<String, Object> config = new LinkedHashMap<>();
		config.put("type", type);
		config.put("data", data);
		return config;
	}

	private static void checkType(String type, String where) {
		if (!TYPES.contains(type)) {
			throw new IllegalArgumentException(where + " is '" + type + "', which this chart cannot draw. Use one of: "
				+ String.join(", ", TYPES.stream().sorted().toList()) + ".");
		}
	}

	private static <T> List<T> cut(List<T> values) {
		return values.size() > MAX_POINTS ? new ArrayList<>(values.subList(0, MAX_POINTS)) : values;
	}

	/**
	 * A value as JSON-like data: texts, numbers, booleans, lists and dictionaries with text keys. Any
	 * other value becomes its display label, an object the user may not read becomes {@code null}.
	 */
	private static Object plain(Object value, ReadFilter filter) {
		if (value == null || value instanceof String || value instanceof Boolean) {
			return value;
		}
		if (value instanceof Number number) {
			double asDouble = number.doubleValue();
			// JSON has no NaN and no infinity; a division by zero is a gap, not a broken page.
			return Double.isNaN(asDouble) || Double.isInfinite(asDouble) ? null : number;
		}
		if (value instanceof Map<?, ?> map) {
			return plainMap(map, Set.of(), filter);
		}
		if (value instanceof Collection<?> many) {
			List<Object> result = new ArrayList<>(many.size());
			for (Object element : many) {
				result.add(plain(element, filter));
			}
			return result;
		}
		if (value instanceof TLObject object && !filter.canRead(object)) {
			return null;
		}
		return text(value);
	}

	/** A dictionary as {@link #plain(Object, ReadFilter) plain data}, without the given keys. */
	private static Map<String, Object> plainMap(Map<?, ?> map, Set<String> without, ReadFilter filter) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			String key = String.valueOf(entry.getKey());
			if (!without.contains(key)) {
				result.put(key, plain(entry.getValue(), filter));
			}
		}
		return result;
	}

	/**
	 * A value as the text that names a point.
	 */
	static String text(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof CharSequence text) {
			return text.toString();
		}
		if (value instanceof Number number) {
			return number(number);
		}
		return MetaLabelProvider.INSTANCE.getLabel(value);
	}

	/** A number as a person writes it: TL-Script computes in doubles, and 7.0 is 7. */
	static String number(Number value) {
		double asDouble = value.doubleValue();
		if (asDouble == Math.rint(asDouble) && !Double.isInfinite(asDouble) && Math.abs(asDouble) < 1e15) {
			return Long.toString((long) asDouble);
		}
		return String.valueOf(value);
	}

	private static String describe(Object value) {
		if (value instanceof CharSequence) {
			return "the text '" + value + "'";
		}
		if (value instanceof Map<?, ?>) {
			return "a dictionary";
		}
		if (value instanceof Collection<?> many) {
			return "a list of " + many.size();
		}
		if (value instanceof TLObject object) {
			return "the object '" + text(object) + "'";
		}
		return "a value of type " + value.getClass().getSimpleName();
	}

	/**
	 * The Chart.js configuration: {@code type}, {@code data} with {@code labels} and
	 * {@code datasets}, and the {@code options} the query wrote, if any.
	 *
	 * <p>
	 * A copy the caller may change: a page adds its own look and its handlers to it.
	 * </p>
	 */
	public Map<String, Object> config() {
		return copy(_config);
	}

	/**
	 * The chart's type, as Chart.js names it.
	 */
	public String type() {
		return (String) _config.get("type");
	}

	/**
	 * The labels of the points, as shown.
	 */
	public List<String> labels() {
		List<String> result = new ArrayList<>();
		if (data().get("labels") instanceof List<?> labels) {
			for (Object label : labels) {
				result.add(text(label));
			}
		}
		return result;
	}

	/**
	 * The series, as shown: each dataset's label and values.
	 */
	public List<Series> series() {
		List<Series> result = new ArrayList<>();
		for (Object dataset : (List<?>) data().get("datasets")) {
			Map<?, ?> map = (Map<?, ?>) dataset;
			Object label = map.get(LABEL);
			result.add(new Series(label == null ? "" : text(label), (List<?>) map.get("data")));
		}
		return result;
	}

	/**
	 * Whether the data reads as a table: a label per row and a number per series in every row - a
	 * chart over categories rather than over points with coordinates of their own.
	 */
	public boolean isTabular() {
		List<String> labels = labels();
		if (labels.isEmpty()) {
			return false;
		}
		for (Series series : series()) {
			if (series.values().size() > labels.size()) {
				return false;
			}
			for (Object value : series.values()) {
				if (value != null && !(value instanceof Number)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * What was cut to keep the chart within {@link #MAX_POINTS} and {@link #MAX_SERIES}, as a
	 * sentence for the model, or {@code null} when everything is shown.
	 */
	public String cut() {
		List<String> parts = new ArrayList<>();
		if (_allLabels > MAX_POINTS) {
			parts.add("the first " + MAX_POINTS + " of " + _allLabels + " points per series");
		}
		if (_allSeries > MAX_SERIES) {
			parts.add("the first " + MAX_SERIES + " of " + _allSeries + " series");
		}
		if (parts.isEmpty()) {
			return null;
		}
		return "Only " + String.join(" and ", parts) + " are shown. Tell the user, or group the data more coarsely.";
	}

	private Map<?, ?> data() {
		return (Map<?, ?>) _config.get("data");
	}

	@SuppressWarnings("unchecked")
	private static <T> T copy(T value) {
		if (value instanceof Map<?, ?> map) {
			Map<String, Object> result = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				result.put((String) entry.getKey(), copy(entry.getValue()));
			}
			return (T) result;
		}
		if (value instanceof List<?> list) {
			List<Object> result = new ArrayList<>(list.size());
			for (Object element : list) {
				result.add(copy(element));
			}
			return (T) result;
		}
		return value;
	}

	/**
	 * One series of a chart.
	 *
	 * @param name
	 *        What the series is called; empty when nothing names it.
	 * @param values
	 *        Its values, one per label: numbers, {@code null} for a gap, or points with coordinates of
	 *        their own.
	 */
	public record Series(String name, List<?> values) {
		// Sole record.
	}
}
