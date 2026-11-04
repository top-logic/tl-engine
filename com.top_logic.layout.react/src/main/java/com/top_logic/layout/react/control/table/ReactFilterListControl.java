/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.table.FilterState;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.Option;
import com.top_logic.table.filter.OptionChecks;
import com.top_logic.util.Resources;

/**
 * The value list of a column filter whose input is a choice among values: each value with a check
 * mark and, where the table counts them, the number of rows it matches.
 *
 * <p>
 * A checked value is shown, an unchecked one hidden; without a filter every value is checked. Every
 * gesture takes effect at once: the check marks are turned into the filter state (see
 * {@link OptionChecks}) and handed to the table. "All", "invert" and "remove the filter" are
 * commands of their own, so that each of them is one change of the filter, not one per value.
 * </p>
 *
 * <p>
 * A list of {@link #SEARCH_THRESHOLD} values or more offers a search, which narrows the list in the
 * client; the server always holds every value.
 * </p>
 */
public class ReactFilterListControl extends ReactControl {

	/** Command checking or unchecking one value, see {@link FilterToggleArguments}. */
	public static final String CMD_TOGGLE = "toggle";

	/** Command checking or unchecking every value, see {@link FilterAllArguments}. */
	public static final String CMD_ALL = "all";

	/** Command turning every check mark. */
	public static final String CMD_INVERT = "invert";

	/** Command removing the filter: every value is checked again. */
	public static final String CMD_CLEAR = "clear";

	/** The number of values from which on the list offers a search. */
	public static final int SEARCH_THRESHOLD = 8;

	/** State key of the values, one map per value in the order of the options. */
	private static final String ENTRIES = "entries";

	/** State key of whether the list offers its search. */
	private static final String SEARCHABLE = "searchable";

	/** State key of whether the check marks hide anything, i.e. whether there is a filter to remove. */
	private static final String ACTIVE = "active";

	/** State key of the name of the list, the label of the column whose values it offers. */
	private static final String LABEL = "label";

	/** Per-entry state key of the key a command names the value by. */
	private static final String ENTRY_KEY = "key";

	/** Per-entry state key of the displayed label of the value. */
	private static final String ENTRY_LABEL = "label";

	/** Per-entry state key of the number of rows the value matches; absent where the table does not count. */
	private static final String ENTRY_COUNT = "count";

	/** Per-entry state key of whether the value is checked, i.e. shown. */
	private static final String ENTRY_CHECKED = "checked";

	private final List<Option> _options;

	private MatchCounts _counts;

	private final Consumer<FilterState> _apply;

	/** The values checked now. */
	private final Set<Object> _checked;

	/** The entries last sent, to send them again only when they change. */
	private List<Map<String, Object>> _entries;

	/** The {@link #ACTIVE} state last sent, {@code null} before the first. */
	private Boolean _active;

	/**
	 * Creates a {@link ReactFilterListControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param options
	 *        The values to offer, in their order.
	 * @param counts
	 *        The number of rows each value matches, or {@link MatchCounts#NONE}.
	 * @param current
	 *        The filter of the column now, {@code null} for none; decides the check marks the list
	 *        opens with.
	 * @param apply
	 *        Receives the filter state of the check marks after every change, {@code null} for no
	 *        filter.
	 */
	public ReactFilterListControl(ReactContext context, List<Option> options, MatchCounts counts,
			FilterState current, Consumer<FilterState> apply) {
		super(context, null, "TLFilterList");
		_options = List.copyOf(options);
		_counts = counts;
		_apply = apply;
		_checked = new LinkedHashSet<>(OptionChecks.checked(_options, current));
		putState(SEARCHABLE, Boolean.valueOf(_options.size() >= SEARCH_THRESHOLD));
		pushActive(isActive(current));
		pushEntries();
	}

	/**
	 * Names the list after the column whose values it offers: the values are told apart from the
	 * other content of the page by it.
	 */
	public void setLabel(String label) {
		putState(LABEL, label);
	}

	/**
	 * Shows the filter of the column as it is now, and the counts of the values under the other
	 * criteria now: called whenever the rows of the table were rebuilt - the filter may have been
	 * changed elsewhere (the filter bar clearing every filter), and the other criteria or the data
	 * may have changed the counts.
	 *
	 * @param counts
	 *        The number of rows each value matches, or {@link MatchCounts#NONE}.
	 * @param current
	 *        The filter of the column now, {@code null} for none.
	 */
	public void refresh(MatchCounts counts, FilterState current) {
		_counts = counts;
		_checked.clear();
		_checked.addAll(OptionChecks.checked(_options, current));
		pushActive(isActive(current));
		pushEntries();
	}

	/**
	 * Records a click on a value with the label of the value besides its key: the step reads by the
	 * label and replays by the key.
	 */
	@Override
	public RecordedCommand recordCommand(String command, Map<String, Object> arguments) {
		if (CMD_TOGGLE.equals(command) && arguments != null) {
			Object key = arguments.get(FilterToggleArguments.KEY);
			int index = key instanceof String text ? indexOf(text) : -1;
			if (index >= 0) {
				FilterToggleArguments recorded = TypedConfiguration.newConfigItem(FilterToggleArguments.class);
				recorded.setName(CMD_TOGGLE);
				recorded.setKey((String) key);
				recorded.setLabel(Resources.getInstance().getString(_options.get(index).label()));
				return new RecordedCommand(recorded);
			}
		}
		return super.recordCommand(command, arguments);
	}

	/**
	 * Handles a click on a value: an unchecked value is checked, a checked one unchecked.
	 */
	@ReactCommandHandler(CMD_TOGGLE)
	void handleToggle(FilterToggleArguments args) {
		int index = indexOf(args.getKey());
		if (index < 0) {
			return;
		}
		Object value = _options.get(index).value();
		if (!_checked.remove(value)) {
			_checked.add(value);
		}
		apply();
	}

	/**
	 * Handles a click on "All": checking it shows every value, unchecking it hides every one - so
	 * that the values to show can be checked one by one afterwards.
	 */
	@ReactCommandHandler(CMD_ALL)
	void handleAll(FilterAllArguments args) {
		_checked.clear();
		if (args.isChecked()) {
			_checked.addAll(values());
		}
		apply();
	}

	/**
	 * Handles "invert the selection": every checked value is unchecked and every unchecked one
	 * checked.
	 */
	@ReactCommandHandler(CMD_INVERT)
	void handleInvert() {
		Set<Object> inverted = values();
		inverted.removeAll(_checked);
		_checked.clear();
		_checked.addAll(inverted);
		apply();
	}

	/**
	 * Handles "remove the filter": every value is checked again.
	 */
	@ReactCommandHandler(CMD_CLEAR)
	void handleClear() {
		_checked.addAll(values());
		apply();
	}

	private void apply() {
		FilterState state = OptionChecks.state(_options, _checked);
		pushEntries();
		pushActive(isActive(state));
		// The table filters and rebuilds its rows, which refreshes this list with the stored filter:
		// the same check marks, so nothing is sent twice.
		_apply.accept(state);
	}

	private void pushActive(boolean active) {
		Boolean value = Boolean.valueOf(active);
		if (!value.equals(_active)) {
			_active = value;
			putState(ACTIVE, value);
		}
	}

	private static boolean isActive(FilterState state) {
		return state != null && !state.isEmpty();
	}

	private Set<Object> values() {
		Set<Object> result = new LinkedHashSet<>();
		for (Option option : _options) {
			result.add(option.value());
		}
		return result;
	}

	private int indexOf(String key) {
		int index;
		try {
			index = Integer.parseInt(key);
		} catch (NumberFormatException ex) {
			return -1;
		}
		return index >= 0 && index < _options.size() ? index : -1;
	}

	private void pushEntries() {
		Resources resources = Resources.getInstance();
		List<Map<String, Object>> entries = new ArrayList<>(_options.size());
		for (int n = 0; n < _options.size(); n++) {
			Option option = _options.get(n);
			Map<String, Object> entry = new LinkedHashMap<>();
			// The position names the value: a value itself may be any object, also null.
			entry.put(ENTRY_KEY, Integer.toString(n));
			entry.put(ENTRY_LABEL, resources.getString(option.label()));
			if (_counts.isAvailable()) {
				entry.put(ENTRY_COUNT, Integer.valueOf(_counts.count(option.value())));
			}
			entry.put(ENTRY_CHECKED, Boolean.valueOf(_checked.contains(option.value())));
			entries.add(entry);
		}
		if (!entries.equals(_entries)) {
			_entries = entries;
			putState(ENTRIES, entries);
		}
	}

}
