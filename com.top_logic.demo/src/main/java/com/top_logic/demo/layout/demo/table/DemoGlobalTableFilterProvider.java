/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.table;

import java.util.Collections;
import java.util.List;

import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.AbstractTableFilterProvider;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.filter.AbstractGlobalFilter;
import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.StaticFilterWrapper;

/**
 * Demo {@link TableFilterProvider}, to provide global {@link TableFilter}s.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DemoGlobalTableFilterProvider extends AbstractTableFilterProvider {

	@Override
	protected List<ConfiguredFilter> createFilterList(TableViewModel tableViewModel, String filterPosition) {
		ConfiguredFilter demoGlobalFilter = new StaticFilterWrapper(new AbstractGlobalFilter(tableViewModel) {
			
			@Override
			public boolean accept(Object anObject) {
				return getTableViewModel().getRowOfObject(anObject) % 2 == 0;
			}
		}, new ConstantDisplayValue("Show odd rows")
			/* rows index begins at 0 but is first visible row for user */);
		return Collections.singletonList(demoGlobalFilter);
	}

}
