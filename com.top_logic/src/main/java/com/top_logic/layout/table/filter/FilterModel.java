/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.basic.col.InlineList;

/**
 * Model of table related filter.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class FilterModel {

	private static final FilterViewControl<?>[] TYPE = new FilterViewControl[0];

	private Object _filterControls = InlineList.newInlineList();

	/**
	 * Registered {@link FilterViewControl} will be informed about value changes of this
	 * {@link FilterModel}.
	 */
	public final void addValueListener(FilterViewControl<FilterModel> valueListener) {
		_filterControls = InlineList.add(FilterViewControl.class, _filterControls, valueListener);
	}

	/**
	 * @see #addValueListener(FilterViewControl)
	 */
	public final void removeValueListener(FilterViewControl<FilterModel> valueListener) {
		_filterControls = InlineList.remove(_filterControls, valueListener);
	}

	/**
	 * true, if any listener is registered to this {@link FilterModel}, false otherwise.
	 */
	final boolean hasValueListeners() {
		return !InlineList.isEmpty(_filterControls);
	}

	/**
	 * @see #addValueListener(FilterViewControl)
	 */
	public final void notifyValueChanged() {
		for (FilterViewControl<?> filterControl : InlineList.toArray(_filterControls, TYPE)) {
			filterControl.valueChanged();
		}
	}
}
