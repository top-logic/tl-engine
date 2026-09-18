/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewListener;
import com.top_logic.table.filter.TextFilterState;

/**
 * Two-way binding between the filtering of a {@link TableViewControl} and up to two
 * {@link ViewChannel}s: the {@link NamedFilter#id() identifier} of the filter the table matches, and
 * the text it searches its displayed columns for.
 *
 * <p>
 * Both channels are the filtering as the rest of the view sees it: a query binding puts them into
 * the address of the page, so a filtered table can be linked to, and an input elsewhere edits the
 * search term without the table's own search box. Each of the two is optional, and a binding over
 * one of them leaves the other side of the filtering alone.
 * </p>
 *
 * <p>
 * Which named filter the table matches is not a property it keeps but one derived from its live
 * criteria, so it changes with every filter the user applies, edits or clears, and with every change
 * of the filters the table offers. The binding therefore follows the {@link TableView} rather than
 * the filter bar - which also makes the channels work for a table displaying no bar at all.
 * </p>
 *
 * <p>
 * A value neither channel can be brought to: a term that is searched for while the table matches a
 * named filter ends that match, because the term is one of the criteria a named filter is compared
 * by. The binding then writes what the table actually shows - the identifier of no filter at all -
 * exactly as it does for an identifier no offered filter carries, so a stale or mistyped address
 * corrects itself instead of describing something nobody sees.
 * </p>
 *
 * <p>
 * The table starts out filtered as its definition and the user's personalization say, so at creation
 * a channel holding nothing takes the table's state rather than unfiltering it. A channel that does
 * hold a value describes the table someone asked for, and is applied.
 * </p>
 */
public class TableFilterBinding {

	private final TableViewControl<?> _table;

	/** The channel holding the {@link NamedFilter#id() identifier}, {@code null} without one. */
	private final ViewChannel _activeFilter;

	/** The channel holding the search term, {@code null} without one. */
	private final ViewChannel _searchTerm;

	private final ViewChannel.ChannelListener _channelListener =
		(sender, oldValue, newValue) -> applyChannelValue(sender, newValue);

	private final TableViewListener _viewListener = new TableViewListener() {
		@Override
		public void filterChanged() {
			handleFilterChanged();
		}
	};

	/**
	 * Whether the binding is currently transporting a value between table and channels, so that
	 * neither side answers its own writes.
	 */
	private boolean _syncing;

	/**
	 * Creates a {@link TableFilterBinding} and brings table and channels into one state.
	 *
	 * @param table
	 *        The table whose filtering is bound.
	 * @param activeFilter
	 *        The channel holding the {@link NamedFilter#id() identifier} of the filter the table
	 *        matches, {@code null} to leave it unpublished.
	 * @param searchTerm
	 *        The channel holding the text the table searches for, {@code null} to leave it
	 *        unpublished.
	 */
	public TableFilterBinding(TableViewControl<?> table, ViewChannel activeFilter, ViewChannel searchTerm) {
		_table = table;
		_activeFilter = activeFilter;
		_searchTerm = searchTerm;

		table.getView().addListener(_viewListener);
		if (activeFilter != null) {
			activeFilter.addListener(_channelListener);
		}
		if (searchTerm != null) {
			searchTerm.addListener(_channelListener);
		}

		initialize();
	}

	/**
	 * Detaches from the table and the channels.
	 *
	 * <p>
	 * To be called by the owner when the table goes away - an editable table builds a fresh
	 * {@link TableViewControl} whenever it enters or leaves its edit mode, and binds that one
	 * instead.
	 * </p>
	 */
	public void dispose() {
		_table.getView().removeListener(_viewListener);
		if (_activeFilter != null) {
			_activeFilter.removeListener(_channelListener);
		}
		if (_searchTerm != null) {
			_searchTerm.removeListener(_channelListener);
		}
	}

	/**
	 * Applies what the channels already hold, and publishes the state the table ends up in.
	 */
	private void initialize() {
		_syncing = true;
		try {
			String filterId = text(_activeFilter);
			if (filterId != null) {
				_table.applyNamedFilter(filterId);
			}
			String term = text(_searchTerm);
			if (term != null) {
				_table.search(term);
			}
		} finally {
			_syncing = false;
		}
		publish();
	}

	/**
	 * Applies a value written to one of the channels, and publishes the state the table ends up in.
	 *
	 * @param sender
	 *        The channel the value was written to.
	 * @param value
	 *        Its new value.
	 */
	private void applyChannelValue(ViewChannel sender, Object value) {
		if (_syncing) {
			// The binding's own write, echoed back: the table already is what the value says.
			return;
		}
		_syncing = true;
		try {
			if (sender == _activeFilter) {
				_table.applyNamedFilter(text(value));
			} else {
				_table.search(text(value));
			}
		} finally {
			_syncing = false;
		}
		publish();
	}

	/**
	 * Publishes a change of the table's filtering that did not come from the channels: a filter the
	 * user applied, edited or cleared, or a change of the filters the table offers.
	 */
	private void handleFilterChanged() {
		if (_syncing) {
			// The application of a channel value publishes the outcome itself, once both channels
			// have been applied.
			return;
		}
		publish();
	}

	/**
	 * Writes what the table is filtered by to the channels.
	 */
	private void publish() {
		_syncing = true;
		try {
			TableView<?> view = _table.getView();
			if (_activeFilter != null) {
				NamedFilter active = view.activeNamedFilter();
				_activeFilter.set(active == null ? null : active.id());
			}
			if (_searchTerm != null) {
				TextFilterState search = view.state().getSearch();
				_searchTerm.set(search == null ? null : search.pattern());
			}
		} finally {
			_syncing = false;
		}
	}

	/**
	 * The text a channel holds, {@code null} when it holds nothing or nothing but empty text.
	 */
	private static String text(ViewChannel channel) {
		return channel == null ? null : text(channel.get());
	}

	/**
	 * The given channel value as text, {@code null} when it is nothing or empty text.
	 */
	private static String text(Object value) {
		if (value == null) {
			return null;
		}
		String text = value.toString();
		return text.isEmpty() ? null : text;
	}

}
