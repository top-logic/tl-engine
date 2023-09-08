/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.JSONTransformer;
import com.top_logic.layout.table.filter.TableFilterModel;

/**
 * Serializer for the internal state of a {@link TableFilterModel}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableFilterSerializer {

	static final Object getState(TableFilterModel model) {
		List<Object> serializedState = new LinkedList<>();
		serializedState.add(getGlobalFilterInversionState(model));
		serializedState.add(getSubFilterStates(model));

		return serializedState;
	}

	private static List<Boolean> getGlobalFilterInversionState(TableFilterModel model) {
		return Collections.singletonList(model.isInversionStateActive());
	}

	private static List<Object> getSubFilterStates(TableFilterModel model) {
		List<Object> serializedState = new LinkedList<>();
		List<ConfiguredFilter> filters = model.getSubFilters();
		for (int i = 0, size = filters.size(); i < size; i++) {
			ConfiguredFilter configuredFilter = filters.get(i);

			List<Object> configuredFilterState = new LinkedList<>();
			List<String> configuredFilterType = new ArrayList<>(2);

			// Add filter configuration type information
			configuredFilterType.add(configuredFilter.getFilterConfiguration().getConfigurationType()
				.getConfigurationRawType().toString());
			Class<? extends JSONTransformer> jsonTransformerType =
				configuredFilter.getFilterConfiguration().getConfigurationType().getJsonTransformerType();
			configuredFilterType.add((jsonTransformerType != null) ? jsonTransformerType.toString() : "null");
			configuredFilterState.add(configuredFilterType);

			// Add filter configuration information
			configuredFilterState.add(configuredFilter.getFilterConfiguration().getSerializedState());

			// Add single filter state to global states list
			serializedState.add(configuredFilterState);
		}

		return serializedState;
	}

}
