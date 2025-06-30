/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.layout.component.model.AbstractSelectionModel;

/**
 * {@link AbstractSelectionModel}, that is able to restrain the set of (de-)selectable elements.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class AbstractRestrainedSelectionModel extends AbstractSelectionModel {

	private Filter<Object> _selectionFilter;
	private Filter<Object> _deselectionFilter;

	/**
	 * Create a new {@link AbstractRestrainedSelectionModel}.
	 */
	public AbstractRestrainedSelectionModel(SelectionModelOwner owner) {
		super(owner);
		_selectionFilter = _deselectionFilter = FilterFactory.trueFilter();
	}

	@Override
	public boolean isSelectable(Object obj) {
		return getSelectionFilter().accept(obj);
	}

	/**
	 * Whether the given object can be selected.
	 */
	public boolean isDeselectable(Object obj) {
		return getDeselectionFilter().accept(obj);
	}

	/**
	 * A setter for a selection filter
	 * 
	 * @param selectionFilter
	 *        The {@link Filter filter}, which defines, whether an object is selectable or not. If
	 *        the filter is <code>null</code>, then every object is selectable.
	 */
	public void setSelectionFilter(Filter<?> selectionFilter) {
		_selectionFilter = nonNull(selectionFilter);
	}

	/**
	 * @see #setSelectionFilter(Filter)
	 */
	public Filter<Object> getSelectionFilter() {
		return _selectionFilter;
	}

	/**
	 * Setter for {@link #getDeselectionFilter()}
	 * 
	 * @param deselectionFilter
	 *        May be <code>null</code>. In that case {@link FilterFactory#trueFilter()} is used.
	 */
	public void setDeselectionFilter(Filter<?> deselectionFilter) {
		_deselectionFilter = nonNull(deselectionFilter);
	}

	/**
	 * The {@link Filter filter}, which defines whether an object can be removed from the selection
	 * or not.
	 */
	public Filter<Object> getDeselectionFilter() {
		return _deselectionFilter;
	}

	/**
	 * Given filter, if it is not <code>null</code>, {@link FilterFactory#trueFilter()} otherwise.
	 */
	@SuppressWarnings("unchecked")
	protected final Filter<Object> nonNull(Filter<?> selectionFilter) {
		return (Filter<Object>) (selectionFilter == null ? FilterFactory.trueFilter() : selectionFilter);
	}

}