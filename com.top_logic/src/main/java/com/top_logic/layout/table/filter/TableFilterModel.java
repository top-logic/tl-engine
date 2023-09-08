/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.filter.TableFilterModelEvent.FilterModelEventTypes;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A model for {@link TableFilter}s.
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class TableFilterModel extends FilterModel {
	
	private final class ShowAllCommand extends TableFilterCommand {
		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			clearAllFilters();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	private final class ResetFilterCommand extends TableFilterCommand {
		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			List<FilterViewControl> filterControls = getFilterControls();
			for (FilterViewControl filterControl : filterControls) {
				filterControl.resetFilterSettings();
			}
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	private final class ApplyFilterCommand extends TableFilterCommand {

		@SuppressWarnings({ "synthetic-access", "rawtypes" })
		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			boolean changed = false;
			List<FilterViewControl> filterControls = getFilterControls();
			for (FilterViewControl filterControl : filterControls) {
				changed |= filterControl.applyFilterSettings();
			}

			// Notify table filter about changes
			if (changed) {
				notifyListeners(FilterModelEventTypes.CONFIGURATION_UPDATE);
			} else {
				notifyListeners(FilterModelEventTypes.RE_APPLIANCE);
			}

			return HandlerResult.DEFAULT_RESULT;
		}
	}

	private static final boolean DEFAULT_IS_MATCH_COUNTING_ENABLED = true;

	private Map<Class<?>, List<ConfiguredFilter>> supportedTypes;
	private List<FilterGroup> filterGroups;
	private List<TableFilterModelListener> modelListeners;
	private boolean showInversionOption;
	private boolean inversionState;

	private boolean _isMatchCountingEnabled = DEFAULT_IS_MATCH_COUNTING_ENABLED;

	/**
	 * Create a new TableFilterModel
	 * @param showInversionOption
	 *        - whether the inversion option shall be shown, or not
	 */
	public TableFilterModel(boolean showInversionOption) {
		this(Collections.<ConfiguredFilter> emptyList(), true, showInversionOption);
	}

	/**
	 * Create a new TableFilterModel with a list of configured filters
	 * @param filters
	 *        - the list of internal filters
	 * @param isGroup
	 *        - whether the list of internal filters shall belong to the same group, or not
	 * @param showInversionOption
	 *        - whether the inversion option shall be shown, or not
	 */
	public TableFilterModel(List<? extends ConfiguredFilter> filters, boolean isGroup, boolean showInversionOption) {
		filterGroups = new ArrayList<>();
		this.supportedTypes = new HashMap<>();
		modelListeners = new LinkedList<>();
		this.showInversionOption = showInversionOption;
		inversionState = false;
		
		// Set filters
		if (isGroup) {
			if (filters.size() > 0) {
				FilterGroup filterGroup = new FilterGroup(ResKey.NONE);
				filterGroup.addFilters(filters);
				addFilterGroup(filterGroup);
			}
		} else {
			for (int i = 0, size = filters.size(); i < size; i++) {
				ConfiguredFilter configuredFilter = filters.get(i);
				addFilter(configuredFilter);
			}
		}
	}
	
	/**
	 * Getter for the groups of internal filters of the table filter
	 * 
	 * @return groups of internal filters of the table filter
	 */
	public List<FilterGroup> getInternalFilterGroups() {
		return filterGroups;
	}

	/**
	 * all internal filters, not distinguished by their group membership
	 */
	public List<ConfiguredFilter> getSubFilters() {
		List<FilterGroup> filtergroups = getInternalFilterGroups();
		List<ConfiguredFilter> result = new LinkedList<>();
		for (int i = 0, size = filtergroups.size(); i < size; i++) {
			FilterGroup filterGroup = filtergroups.get(i);
			result.addAll(filterGroup.getFilters());
		}
		return result;
	}

	/**
	 * true, if the inversion option shall be shown, false otherwise.
	 */
	public boolean isShowingInversionOption() {
		return showInversionOption;
	}

	/**
	 * @param showInversionOption - whether the inversion option of the {@link TableFilter} shall be shown, or not.
	 */
	public void setShowInversionOption(boolean showInversionOption) {
		this.showInversionOption = showInversionOption;
	}

	/**
	 * true, if the inversion of the {@link ConfiguredFilter} results is active, or not.
	 */
	public boolean isInversionStateActive() {
		return inversionState;
	}

	/**
	 * @param inversionState - whether the result of the {@link ConfiguredFilter} shall be inverted, or not.
	 */
	public void setInversionStateIsActive(boolean inversionState) {
		this.inversionState = inversionState;
		notifyValueChanged();
	}

	/**
	 * This method adds a filter to the list of internal filters
	 * 
	 * @param filter
	 *        - the filter to add
	 */
	public void addFilter(ConfiguredFilter filter) {
		
		// If the filter shall be added to the existing group, created last
		if (!filterGroups.isEmpty()) {
			filterGroups.get(filterGroups.size() - 1).addFilter(filter);
			registerFilterValueTypes(filter);
		}

		// If there was no filter registered before
		else {
			FilterGroup newFilterGroup = new FilterGroup(ResKey.NONE);
			newFilterGroup.addFilter(filter);
			addFilterGroup(newFilterGroup);
		}
		
	}

	/**
	 * This method adds a filter group to the end of the list of existing filter groups.
	 */
	public void addFilterGroup(FilterGroup filterGroup) {
		filterGroups.add(filterGroup);
		for (ConfiguredFilter filter : filterGroup.getFilters()) {
			registerFilterValueTypes(filter);
		}
	}

	private void registerFilterValueTypes(ConfiguredFilter filter) {
		/*
		 *  Determine object types, supported by this filter
		 */
		List<Class<?>> supportedTypesByFilter = filter.getSupportedObjectTypes();
		
		// Iterate over types and add to global supported types
		for (int i = 0, size = supportedTypesByFilter.size(); i < size; i++) {
			Class<?> type = supportedTypesByFilter.get(i);
			
			// If the type is already registered, add filter to its registration entry
			if(supportedTypes.containsKey(type)) {
				boolean searchSubTypes = true;
				boolean searchSuperTypes = false;
				registerFilterForType(filter, type, searchSubTypes, searchSuperTypes);
			}
			
			// If the type is not registered determine types, that are supertypes of this type,
			// to add the filters of these types to the new type entry, or in case that are subtypes
			// of this type, to add the filter to these type entries
			else {
				boolean searchSubTypes = true;
				boolean searchSuperTypes = true;
				registerFilterForType(filter, type, searchSubTypes, searchSuperTypes);
			}
		}
	}

	private void registerFilterForType(ConfiguredFilter filter, Class<?> type, boolean searchSubTypes,
			boolean searchSuperTypes) {
		List<ConfiguredFilter> filterList = new ArrayList<>();
		addToWhat(filterList, filter);
		
		registerFiltersToTypeHierarchy(filter, type, searchSubTypes, searchSuperTypes, filterList);
	}

	private void registerFiltersToTypeHierarchy(ConfiguredFilter filter, Class<?> type, boolean searchSubTypes,
			boolean searchSuperTypes, List<ConfiguredFilter> newFiltersEntry) {
		Iterator<Class<?>> supportedTypesIterator = supportedTypes.keySet().iterator();
		while(supportedTypesIterator.hasNext()) {
			Class<?> iteratorType = supportedTypesIterator.next();
			
			// If already registered type is a subtype of given type
			if(searchSubTypes && type.isAssignableFrom(iteratorType)) {
				addToWhat(supportedTypes.get(iteratorType), filter);
			}
			// If already registered type is a supertype of given type
			else if(searchSuperTypes && iteratorType.isAssignableFrom(type)) {
				addToFrom(newFiltersEntry, supportedTypes.get(iteratorType));
			}
		}
		
		if (searchSuperTypes && !newFiltersEntry.isEmpty()) {
			supportedTypes.put(type, newFiltersEntry);
		}
	}
	
	private void searchForSuperTypeFilters(Class<?> type) {
		boolean searchSubTypes = false;
		boolean searchSuperTypes = true;
		registerFiltersToTypeHierarchy(null, type, searchSubTypes, searchSuperTypes, new ArrayList<>());
	}

	/**
	 * This method removes a filter from the list of internal filters
	 * 
	 * @param filter - the filter to remove
	 */
	public void removeInternalFilter(ConfiguredFilter filter) {
		for (int i = 0, size = filterGroups.size(); i < size; i++) {
			FilterGroup filterGroup = filterGroups.get(i);
			filterGroup.removeFilter(filter);
		}
		
		// Determine types, associated with this filter and remove the filter from the assocations
		Iterator<Class<?>> supportedTypesIterator = supportedTypes.keySet().iterator();
		List<Class<?>> emptyAssocations = new LinkedList<>();
		while(supportedTypesIterator.hasNext()) {
			Class<?> iteratorType = supportedTypesIterator.next();
			List<ConfiguredFilter> iteratorFilters = supportedTypes.get(iteratorType);
			iteratorFilters.remove(filter);
			
			// If no other filter is associated with this type, mark the association for removal
			if(iteratorFilters.isEmpty()) {
				emptyAssocations.add(iteratorType);
			}
		}
		
		// Remove empty associations
		for (int i = 0, size = emptyAssocations.size(); i < size; i++) {
			Class<?> type = emptyAssocations.get(i);
			supportedTypes.remove(type);
		}
	}
	
	/**
	 * This method determines the {@link ConfiguredFilter}s, which support the specified object type.
	 * 
	 * @param objectType - the object Type, which shall be handled
	 * @return the list of {@link ConfiguredFilter}s, which support the given type, never null
	 */
	public List<ConfiguredFilter> getSupportingFilters(Class<?> objectType) {
		List<ConfiguredFilter> supporters = supportedTypes.get(objectType);
		
		// If the given object type is not in registry, determine, if there are supertypes
		// of this type and register the given object type with the filters of the supertypes
		if(supporters == null) {
			searchForSuperTypeFilters(objectType);
			supporters = supportedTypes.get(objectType);
			if (supporters == null) {
				supporters = Collections.emptyList();
			}
		}
		
		return supporters;
	}

	private void addToFrom(List<ConfiguredFilter> targetList, List<ConfiguredFilter> sourceList) {
		for (ConfiguredFilter configuredFilter : sourceList) {
			addToWhat(targetList, configuredFilter);
		}
	}

	private void addToWhat(List<ConfiguredFilter> targetList, ConfiguredFilter filter) {
		if (!targetList.contains(filter)) {
			targetList.add(filter);
		}
	}
	
	/**
	 * This method notifies all listener about a occuring table filter model event
	 * 
	 * @param eventType - the event type of the table filter model event
	 */
	private final void notifyListeners(FilterModelEventTypes eventType) {
		TableFilterModelEvent event = new TableFilterModelEvent(eventType);
		for (int i = 0, size = modelListeners.size(); i < size; i++) {
			TableFilterModelListener listener = modelListeners.get(i);
			listener.handleTableFilterModelEvent(event);
		}
	}
	
	/**
	 * This method adds a listener to the {@link TableFilterModel}
	 * 
	 * @param listener - the {@link TableFilterModelListener}
	 */
	public void addListener(TableFilterModelListener listener) {
		modelListeners.add(listener);
	}
	
	/**
	 * This method removes a listener from the {@link TableFilterModel}
	 * 
	 * @param listener
	 *        - the {@link TableFilterModelListener}
	 */
	public void removeListener(TableFilterModelListener listener) {
		modelListeners.remove(listener);
	}

	public void clearAllFilters() {
		clearFilterInversionState();
		
		for (int i = 0, size = filterGroups.size(); i < size; i++) {
			List<ConfiguredFilter> subFilters = filterGroups.get(i).getFilters();
			for (int j = 0, groupSize = subFilters.size(); j < groupSize; j++) {
				ConfiguredFilter configuredFilter = subFilters.get(j);
				configuredFilter.clearFilterConfiguration();
			}
		}
		
		notifyListeners(FilterModelEventTypes.DEACTIVATION);
	}

	private void clearFilterInversionState() {
		setInversionStateIsActive(false);
	}

	public TableFilterCommand createApplyFilterCommand() {
		return new ApplyFilterCommand();
	}
	
	public TableFilterCommand createResetFilterCommand() {
		return new ResetFilterCommand();
	}
	
	public TableFilterCommand createShowAllFilterCommand() {
		return new ShowAllCommand();
	}

	/**
	 * Whether the filter should count its matches.
	 */
	public boolean isMatchCountingEnabled() {
		return _isMatchCountingEnabled;
	}

	/**
	 * @see #isMatchCountingEnabled()
	 */
	public void setMatchCountingEnabled(boolean isMatchCountingEnabled) {
		_isMatchCountingEnabled = isMatchCountingEnabled;
	}

}
