/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.filter.StaticFilterWrapper;
import com.top_logic.layout.table.filter.StaticFilterWrapperViewBuilder;
import com.top_logic.util.Resources;

/**
 * {@link TableFilterProvider} that adds an additional filter for {@link ProtectedValue} for which
 * the user has not enough rights.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ProtectedValueTableFilterProvider implements TableFilterProvider {

	private final TableFilterProvider _filterProvider;

	ProtectedValueTableFilterProvider(TableFilterProvider filterProvider) {
		_filterProvider = filterProvider;
	}

	@Override
	public TableFilter createTableFilter(TableViewModel aTableViewModel, String filterPosition) {
		TableFilter tableFilter = _filterProvider.createTableFilter(aTableViewModel, filterPosition);
		StaticFilterWrapper filter = createProtectedValueNoAccessFilter();
		tableFilter.addSubFilter(filter, true);
		return tableFilter;
	}

	private StaticFilterWrapper createProtectedValueNoAccessFilter() {
		return new StaticFilterWrapper(staticFilter(), filterDescription(), supportedTypes(), viewBuilder());
	}

	private StaticFilterWrapperViewBuilder viewBuilder() {
		return new StaticFilterWrapperViewBuilder();
	}

	private Filter<Object> staticFilter() {
		return ProtectedValueReplacement.getFilter();
	}

	private List<Class<?>> supportedTypes() {
		return Collections.<Class<?>> singletonList(ProtectedValueReplacement.class);
	}

	private DisplayValue filterDescription() {
		String blockedText = ProtectedValueRenderer.getBlockedText(Resources.getInstance());
		return new ConstantDisplayValue(blockedText);
	}
}
