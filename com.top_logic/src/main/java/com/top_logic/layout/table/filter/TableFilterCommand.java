/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.basic.Command;
import com.top_logic.layout.table.TableFilter;

/**
 * {@link Command}, that is triggered by user to change state of a {@link TableFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class TableFilterCommand implements Command {

	private List<FilterViewControl> filterControls = new ArrayList<>();

	/**
	 * Register {@link FilterViewControl}, that shall be affected by this {@link TableFilterCommand}
	 * .
	 */
	public final void registerFilterControl(FilterViewControl filterControl) {
		filterControls.add(filterControl);
	}

	/**
	 * @see #registerFilterControl(FilterViewControl)
	 */
	public final void unregisterFilterControl(FilterViewControl filterControl) {
		filterControls.remove(filterControl);
	}

	/**
	 * @see #registerFilterControl(FilterViewControl)
	 */
	public final void unregisterAllFilterControls() {
		filterControls.clear();
	}

	/**
	 * @see #registerFilterControl(FilterViewControl)
	 */
	protected final List<FilterViewControl> getFilterControls() {
		return filterControls;
	}
}
