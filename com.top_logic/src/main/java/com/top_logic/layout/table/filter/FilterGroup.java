/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.table.TableFilter;

/**
 * Group of sub filters of a {@link TableFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FilterGroup {

	private ResKey _label;
	private List<ConfiguredFilter> _filters;

	/**
	 * Create a new {@link FilterGroup}.
	 */
	public FilterGroup(ResKey label) {
		assert label != null : "Label of filter group must not be null!";
		_label = label;
		_filters = new ArrayList<>();
	}
	
	/**
	 * Adds given filter to the end of this group.
	 */
	public void addFilter(ConfiguredFilter filter) {
		_filters.add(filter);
	}
	
	/**
	 * Adds given filters to the end of this group.
	 */
	public void addFilters(Collection<? extends ConfiguredFilter> filters) {
		_filters.addAll(filters);
	}

	/**
	 * Removes the given filter from this group.
	 */
	public void removeFilter(ConfiguredFilter filter) {
		_filters.remove(filter);
	}

	/**
	 * label of this group, never <code>null</code>.
	 */
	public ResKey getLabel() {
		return _label;
	}
	
	/**
	 * filters, belonging to this group, never <code>null</code>.
	 */
	public List<ConfiguredFilter> getFilters() {
		return _filters;
	}

}
