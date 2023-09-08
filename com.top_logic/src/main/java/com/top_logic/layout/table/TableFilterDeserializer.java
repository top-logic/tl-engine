/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.LinkedList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.FilterConfigurationType;
import com.top_logic.layout.table.filter.TableFilterModel;

/**
 * Deserializer, which restores internal state of a {@link TableFilterModel}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableFilterDeserializer {

	@SuppressWarnings("unchecked")
	static final void restoreState(TableFilterModel model, Object serializedState) {
		try {
			List<Object> serializedStateList = (List<Object>)serializedState;
			setGlobalFilterInversionState(model, (List<Object>) serializedStateList.get(0));
			setSubFilterStates(model, (List<Object>) serializedStateList.get(1));
		} catch (ClassCastException e) {
			propagateConfigurationReset(model, "Filter configuration could not be restored from serialized state, because the" +
						 				"structure of the serialized state does not match filter serialization convention");
		}
	}
	
	private static void setGlobalFilterInversionState(TableFilterModel model, List<Object> serializedInversionState) {
		try {
			if (model.isShowingInversionOption()) {
				model.setInversionStateIsActive((Boolean) serializedInversionState.get(0));
			}
		} catch (ClassCastException ex) {
			propagateConfigurationReset(model,
				"Filter configuration could not be restored from serialized state, because the" +
 										"structure of the serialized state does not match filter configuration");
		} catch (IndexOutOfBoundsException ext) {
			propagateConfigurationReset(model,
				"Filter configuration could not be restored from serialized state, because the" +
										"structure of the serialized state does not match filter configuration");
		}
	}

	private static void propagateConfigurationReset(TableFilterModel model, String cause) {
		if (Logger.isDebugEnabled(TableFilter.class)) {
			Logger.debug(cause, TableFilter.class);
		}

		model.clearAllFilters();
	}

	@SuppressWarnings("unchecked")
	private static void setSubFilterStates(TableFilterModel model, List<Object> serializedSubFilterStates) {
		List<ConfiguredFilter> filterSearchScope = new LinkedList<>();
		filterSearchScope.addAll(model.getSubFilters());

		for (int i = 0, size = serializedSubFilterStates.size(); i < size; i++) {
			List<List<?>> configuredFilterState = (List<List<?>>)serializedSubFilterStates.get(i);
			List<String> configTypeInformation = (List<String>)configuredFilterState.get(0);
			
			// Find appropriate filter for current internal filter configuration
			boolean filterFound = false;
			ConfiguredFilter configuredFilter = null;
			for (int j = 0, filterSize = filterSearchScope.size(); !filterFound && (j < filterSize); j++) {
				configuredFilter = filterSearchScope.get(j);
				FilterConfigurationType configType = configuredFilter.getFilterConfiguration().getConfigurationType();
				String jsonTransformerType = (configType.getJsonTransformerType() != null) ? configType.getJsonTransformerType().toString() : "null";
				if(configType.getConfigurationRawType().toString().equals(configTypeInformation.get(0)) &&
				   jsonTransformerType.equals(configTypeInformation.get(1))) {
					configuredFilter.getFilterConfiguration().setSerializedState((List<Object>)configuredFilterState.get(1));
					filterFound = true;
				}
			}
			
			// Remove restored filter from filter search scope
			if(filterFound) {
				filterSearchScope.remove(configuredFilter);
			}
			
			// Log error, if no filter was found for the serialized configuration
			else {
				propagateConfigurationReset(model,
											"Filter configuration could not be restored from serialized state, because no appropriate" +
						     				"sub filter for the sub filter configuration '" + configuredFilterState.toString() +
						     				"' was found!");
				return;
			}
		}

		// If still are filters in search scope
		if (!filterSearchScope.isEmpty()) {
			propagateConfigurationReset(model,
										"Filter configuration could not be fully restored from serialized state, because the serialized" +
								  		"state did not contain the complete internal configuration state of this table filter");
		}
	}
}
