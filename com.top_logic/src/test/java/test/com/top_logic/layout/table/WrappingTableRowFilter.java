/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table;

import java.util.BitSet;
import java.util.Collections;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.table.ColumnFilterHolder;
import com.top_logic.layout.table.FilterResult;
import com.top_logic.layout.table.TableRowFilter;

/**
 * {@link TableRowFilter}, which delegates filtering to a specified inner {@link Filter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class WrappingTableRowFilter extends TableRowFilter {

	private Filter wrappedFilter;

	/**
	 * Create a new {@link WrappingTableRowFilter}.
	 */
	public WrappingTableRowFilter(Filter wrappedFilter) {
		super(Collections.<ColumnFilterHolder> emptyList());
		this.wrappedFilter = wrappedFilter;
	}

	@Override
	public FilterResult check(Object object) {
		BitSet matchMask = new BitSet(1);
		BitSet applicabilityMask = new BitSet(1);
		matchMask.set(0, !wrappedFilter.accept(object));

		return new FilterResult(1, matchMask, applicabilityMask,
			Collections.<ColumnFilterHolder, Object> emptyMap(), object);
	}
}
