/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.basic.util.ResKey;

import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.Option;

/**
 * A {@link ColumnFilter} accepting cell values contained in a user-selected set of
 * {@link Option}s. Reports {@link #countsMatches()} so a view can show facet counts.
 *
 * <p>
 * The options are either fixed (e.g. the classifiers of an enumeration), or the values
 * {@link #occurring(Function, BoundCodec, int) occurring} in the rows of the table. A cell value
 * stands for the option values its {@link #keys(Object) keys} name: the value itself by default,
 * the elements of a cell holding several values, or the day of a point in time. A cell matches if
 * one of its keys is selected.
 * </p>
 *
 * @param <V>
 *        The cell value type.
 */
public class OptionsColumnFilter<V> implements ColumnFilter<V> {

	/** The option standing for the cells holding no value, see {@link Option#NO_VALUE}. */
	private static final Option NO_VALUE_OPTION = new Option(Option.NO_VALUE, I18NConstants.VALUE_NONE);

	/** The {@link #keys(Object) keys} of a cell holding no value. */
	private static final List<Object> NO_VALUE_KEYS = List.of(Option.NO_VALUE);

	private final List<Option> _options;

	private final Function<? super V, ? extends Collection<?>> _keys;

	private final BoundCodec<Object> _codec;

	private final int _maxValues;

	private final Comparator<Object> _order;

	/**
	 * Creates an {@link OptionsColumnFilter} over the given options, a cell value standing for
	 * itself.
	 */
	public OptionsColumnFilter(List<Option> options) {
		this(options, OptionsColumnFilter::self);
	}

	/**
	 * Creates an {@link OptionsColumnFilter} over the given options.
	 *
	 * @param options
	 *        The options offered.
	 * @param keys
	 *        The option values a cell value stands for, see {@link #keys(Object)}.
	 */
	public OptionsColumnFilter(List<Option> options, Function<? super V, ? extends Collection<?>> keys) {
		this(List.copyOf(options), keys, null, 0, null);
	}

	private OptionsColumnFilter(List<Option> options, Function<? super V, ? extends Collection<?>> keys,
			BoundCodec<Object> codec, int maxValues, Comparator<Object> order) {
		_options = options;
		_keys = keys;
		_codec = codec;
		_maxValues = maxValues;
		_order = order;
	}

	/**
	 * An {@link OptionsColumnFilter} whose options are the values occurring in the rows of the
	 * table, the {@link MatchCounts#values() facet values} of its column.
	 *
	 * <p>
	 * It is only {@link #offered(MatchCounts) offered} while the column holds at most the given
	 * number of distinct values.
	 * </p>
	 *
	 * @param keys
	 *        The option values a cell value stands for, see {@link #keys(Object)}. A cell value
	 *        that is no single value of the column (e.g. a collection of values) must be mapped to
	 *        the keys of its elements.
	 * @param codec
	 *        Writes an option value as its label and to and from its persistent form. An option
	 *        value it cannot write persistently ({@link BoundCodec#toJson(Object)} answering
	 *        {@code null}) makes a selection holding it not persistable.
	 * @param maxValues
	 *        The maximum number of distinct values the filter is offered for.
	 */
	public static <V, K> OptionsColumnFilter<V> occurring(Function<? super V, ? extends Collection<? extends K>> keys,
			BoundCodec<K> codec, int maxValues) {
		return occurring(keys, codec, maxValues, null);
	}

	/**
	 * An {@link OptionsColumnFilter} whose options are the values occurring in the rows of the
	 * table, as {@link #occurring(Function, BoundCodec, int)}, listed in the given order.
	 *
	 * @param order
	 *        The order of the values, see {@link #facetOrder()}; {@code null} for the order of the
	 *        column.
	 */
	@SuppressWarnings("unchecked")
	public static <V, K> OptionsColumnFilter<V> occurring(Function<? super V, ? extends Collection<? extends K>> keys,
			BoundCodec<K> codec, int maxValues, Comparator<? super K> order) {
		return new OptionsColumnFilter<>(null, keys, (BoundCodec<Object>) codec, maxValues,
			(Comparator<Object>) order);
	}

	@Override
	public Comparator<Object> facetOrder() {
		return _order;
	}

	private static Collection<Object> self(Object value) {
		return value == null ? List.of() : List.of(value);
	}

	/**
	 * Whether the options are the values occurring in the rows of the table, instead of fixed
	 * {@link #options()}.
	 */
	public boolean isOccurring() {
		return _options == null;
	}

	/**
	 * The fixed options offered by this filter, empty for a filter offering the
	 * {@link #isOccurring() occurring values}.
	 */
	public List<Option> options() {
		return _options == null ? List.of() : _options;
	}

	/**
	 * The options a filter editor offers for a column with the given facet counts and the given
	 * current selection.
	 *
	 * <p>
	 * These are the {@link #options() fixed options}, or the {@link MatchCounts#values() values
	 * occurring} in the rows, followed by the selected values that no longer occur, so that a
	 * selection is never lost by opening the editor.
	 * </p>
	 *
	 * @param counts
	 *        The facet counts of the column as this filter buckets its values.
	 * @param current
	 *        The current selection, or {@code null}.
	 */
	public List<Option> options(MatchCounts counts, OptionsFilterState current) {
		if (!isOccurring()) {
			boolean noValue = counts.isAvailable() && counts.count(Option.NO_VALUE) > 0
				|| current != null && current.selected().contains(Option.NO_VALUE);
			if (!noValue) {
				return _options;
			}
			List<Option> result = new ArrayList<>(_options);
			result.add(NO_VALUE_OPTION);
			return result;
		}
		Set<Object> values = new LinkedHashSet<>(counts.values());
		if (current != null) {
			values.addAll(current.selected());
		}
		// The option for no value follows the values, also when only the selection holds it.
		boolean noValue = values.remove(Option.NO_VALUE);
		List<Option> result = new ArrayList<>(values.size() + 1);
		for (Object value : values) {
			result.add(new Option(value, ResKey.text(label(value))));
		}
		if (noValue) {
			result.add(NO_VALUE_OPTION);
		}
		return result;
	}

	private String label(Object value) {
		String label = _codec.format(value);
		return label == null ? String.valueOf(value) : label;
	}

	/**
	 * The option values the given cell value stands for: the cell matches if one of them is
	 * selected.
	 *
	 * <p>
	 * A cell holding no value - nothing, an empty text, an empty collection - stands for
	 * {@link Option#NO_VALUE}.
	 * </p>
	 */
	public Collection<?> keys(V value) {
		if (isNoValue(value)) {
			return NO_VALUE_KEYS;
		}
		Collection<?> keys = _keys.apply(value);
		return keys == null ? List.of() : keys;
	}

	/**
	 * Whether the given cell value is no value: nothing, an empty text, or an empty collection of
	 * values.
	 */
	public static boolean isNoValue(Object value) {
		return value == null || value instanceof CharSequence text && text.isEmpty()
			|| value instanceof Collection<?> collection && collection.isEmpty();
	}

	@Override
	public FilterInput input() {
		return new FilterInput.Options(options(), isOccurring());
	}

	/**
	 * A filter over fixed options is always offered, one over the occurring values while the
	 * column holds at most the maximum number of distinct values; the cells holding
	 * {@link Option#NO_VALUE no value} do not count as a value of their own.
	 */
	@Override
	public boolean offered(MatchCounts counts) {
		if (!isOccurring()) {
			return true;
		}
		if (!counts.isAvailable()) {
			return false;
		}
		List<Object> values = counts.values();
		int distinct = values.contains(Option.NO_VALUE) ? values.size() - 1 : values.size();
		return distinct <= _maxValues;
	}

	@Override
	public Predicate<V> predicate(FilterState state) {
		Set<Object> selected = ((OptionsFilterState) state).selected();
		return value -> {
			for (Object key : keys(value)) {
				// No option stands for an empty cell, and the immutable selection rejects a null
				// lookup.
				if (key != null && selected.contains(key)) {
					return true;
				}
			}
			return false;
		};
	}

	@Override
	public boolean countsMatches() {
		return true;
	}

	@Override
	public Collection<Object> facetKeys(V value) {
		List<Object> result = new ArrayList<>();
		for (Object key : keys(value)) {
			if (key != null) {
				result.add(key);
			}
		}
		return result;
	}

	/**
	 * The selection of the option, or the options, the given value names.
	 *
	 * @see OptionsFilterState#select(Object, Collection)
	 */
	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * A filter over the {@link #isOccurring() occurring values} expresses no declared value: the
	 * values it offers depend on the rows, so a declared value is left to another kind of filter
	 * of the column, e.g. a text filter matching the label of the value.
	 * </p>
	 */
	@Override
	public FilterState stateFor(Object value) {
		if (isOccurring()) {
			return null;
		}
		return OptionsFilterState.select(value, optionValues());
	}

	private Collection<Object> optionValues() {
		List<Object> values = new ArrayList<>(_options.size());
		for (Option option : _options) {
			values.add(option.value());
		}
		return values;
	}

	/**
	 * Serializes the selection as the indices of the selected options. Option values can be
	 * arbitrary business objects (classifiers, enums); their position in the stable option list is a
	 * type-agnostic identity that round-trips without an object-serialization strategy.
	 *
	 * <p>
	 * A selection of {@link #isOccurring() occurring values} has no stable option list: it is
	 * serialized as the list of the persistent forms of its values, see
	 * {@link BoundCodec#toJson(Object)}, or not at all if one of them has none.
	 * </p>
	 *
	 * <p>
	 * The option standing for no value, {@link Option#NO_VALUE}, is written as {@code null} in
	 * either list.
	 * </p>
	 */
	@Override
	public Object toJson(FilterState state) {
		Set<Object> selected = ((OptionsFilterState) state).selected();
		if (isOccurring()) {
			List<Object> values = new ArrayList<>(selected.size());
			for (Object value : selected) {
				if (value == Option.NO_VALUE) {
					values.add(null);
					continue;
				}
				Object json = _codec.toJson(value);
				if (json == null) {
					return null;
				}
				values.add(json);
			}
			return values;
		}
		List<Object> indices = new ArrayList<>();
		for (int n = 0; n < _options.size(); n++) {
			if (selected.contains(_options.get(n).value())) {
				indices.add(Integer.valueOf(n));
			}
		}
		if (selected.contains(Option.NO_VALUE)) {
			indices.add(null);
		}
		return indices;
	}

	/**
	 * Restores a selection {@link #toJson(FilterState) serialized} as a list.
	 *
	 * <p>
	 * A stored value of {@link #isOccurring() occurring values} that cannot be read back (e.g. an
	 * object deleted in the meantime) is dropped from the selection.
	 * </p>
	 */
	@Override
	public FilterState fromJson(Object json) {
		if (!(json instanceof List<?> indices)) {
			return null;
		}
		if (isOccurring()) {
			Set<Object> values = new LinkedHashSet<>();
			for (Object stored : indices) {
				if (stored == null) {
					values.add(Option.NO_VALUE);
					continue;
				}
				Object value = _codec.fromJson(stored);
				if (value != null) {
					values.add(value);
				}
			}
			return new OptionsFilterState(values);
		}
		Set<Object> selected = new LinkedHashSet<>();
		for (Object index : indices) {
			if (index == null) {
				selected.add(Option.NO_VALUE);
			} else if (index instanceof Number number) {
				int n = number.intValue();
				if (n >= 0 && n < _options.size()) {
					selected.add(_options.get(n).value());
				}
			}
		}
		return new OptionsFilterState(selected);
	}

}
