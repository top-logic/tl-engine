/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.PopupHandler;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.table.control.TableControl.SortCommand;
import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.FilterConfiguration;
import com.top_logic.layout.table.filter.FilterDialogBuilder;
import com.top_logic.layout.table.filter.FilterGroup;
import com.top_logic.layout.table.filter.JSONTransformer;
import com.top_logic.layout.table.filter.PopupFilterDialogBuilder;
import com.top_logic.layout.table.filter.TableFilterEvent;
import com.top_logic.layout.table.filter.TableFilterEvent.FilterEventTypes;
import com.top_logic.layout.table.filter.TableFilterListener;
import com.top_logic.layout.table.filter.TableFilterModel;
import com.top_logic.layout.table.filter.TableFilterModelEvent;
import com.top_logic.layout.table.filter.TableFilterModelEvent.FilterModelEventTypes;
import com.top_logic.layout.table.filter.TableFilterModelListener;

/**
 * The TableFilter wraps sub filters to filter table row entries. 
 * 
 * @author     <a href="mailto:dna@top-logic.com">dna</a>
 */
public class TableFilter implements Filter<Object>, TableFilterModelListener {

	public static final ResPrefix RES_PREFIX = ResPrefix.legacyPackage(TableFilter.class);

	private TableFilterModel model;
	private FilterDialogBuilder view;
	private List<TableFilterListener> listeners;

	/**
	 * Create a new TableFilter, which can be inverted
	 * 
	 * @param manager
	 *        - the view of the table filter, must not be null
	 */
	public TableFilter(FilterDialogBuilder manager) {
		this(manager, true);
	}

	/**
	 * Create a new invertible TableFilter
	 * 
	 * @param manager
	 *        - the view of the table filter, must not be null
	 * @param showInversionOption
	 *        - whether the inversion option shall be shown, or not
	 */
	public TableFilter(FilterDialogBuilder manager, boolean showInversionOption) {
		this(manager, Collections.<ConfiguredFilter> emptyList(), true, showInversionOption);
	}

	/**
	 * This {@link TableFilter}'s {@link FilterDialogBuilder}.
	 */
	public FilterDialogBuilder getDialogManager() {
		return view;
	}

	/**
	 * Create a new invertible TableFilter with a list of sub filters
	 * 
	 * @param manager
	 *        - the view of the table filter, must not be null
	 * @param filters
	 *        - the list of subfilters
	 * @param isGroup
	 *        - whether the list of configured filters belong to the same group, or not
	 */
	public TableFilter(FilterDialogBuilder manager, List<? extends ConfiguredFilter> filters, boolean isGroup) {
		this(manager, filters, isGroup, true);
	}

	/**
	 * Create a new TableFilter with a list of sub filters, which can be inverted
	 * 
	 * @param manager
	 *        - the view of the table filter, must not be null
	 * @param filters
	 *        - the list of subfilters
	 * @param isGroup
	 *        - whether the list of configured filters belong to the same group, or not
	 * @param showInversionOption
	 *        - whether the inversion option shall be shown, or not
	 */
	public TableFilter(FilterDialogBuilder manager, List<? extends ConfiguredFilter> filters, boolean isGroup,
						boolean showInversionOption) {
		assert manager != null : "The filter dialog manager must not be null!";
		assert filters != null : "The filter list must not be null!";
		this.view = manager;
		model = new TableFilterModel(filters, isGroup, showInversionOption);
		listeners = new LinkedList<>();
		model.addListener(this);
	}

	/**
	 * Opens the filter dialog for the {@link TableFilter}.
	 * 
	 * @param context
	 *        The display context, must not be <code>null</code>.
	 * @param handler
	 *        The context for opening the popup dialog.
	 * @param sortControl
	 *        Optional {@link BlockControl} for {@link SortCommand}s. If empty the dialog will be opened
	 *        without {@link SortCommand}s.
	 * @return The filter form context.
	 */
	public FormContext openFilterDialog(DisplayContext context, PopupHandler handler, Optional<BlockControl> sortControl) {
		assert context != null : "Display context must not be null!";
		
		FormContext filterContext = new FormContext("filterTable", RES_PREFIX);
		Control contentControl = view.createFilterDialogContent(model, context, filterContext, sortControl);
		view.openFilterDialog(contentControl, context, handler, model, filterContext);
		return filterContext;
	}
	
	public Control createFilterControl(DisplayContext displayContext, FormContext filterContext) {
		assert displayContext != null : "Display context must not be null!";

		Control contentControl = ((PopupFilterDialogBuilder) view).createSidebarContent(model, displayContext, filterContext);
		return contentControl;
	}

	/**
	 * This method adds a subfilter to the {@link TableFilter}
	 * 
	 * @param filter
	 *        - the subfilter to add
	 * @param newGroup
	 *        - whether, the filter shall be added to a new group, or not
	 */
	public void addSubFilter(ConfiguredFilter filter, boolean newGroup) {
		if (!newGroup) {
			model.addFilter(filter);
		} else {
			addSubFilterGroup(Collections.singletonList(filter));
		}
	}
	
	/**
	 * This method adds a group of subfilter to the {@link TableFilter}
	 * 
	 * @param filterGroup - the subfilter group to add
	 */
	public void addSubFilterGroup(List<ConfiguredFilter> filterGroup) {
		addSubFilterGroup(filterGroup, ResKey.NONE);
	}
	
	/**
	 * This method adds a group of subfilter to the {@link TableFilter}
	 * 
	 * @param filters
	 *        - the subfilters, that shall be added as group
	 * @param label
	 *        - label of the group of subfilters
	 * 
	 */
	public void addSubFilterGroup(List<ConfiguredFilter> filters, ResKey label) {
		FilterGroup filterGroup = new FilterGroup(label);
		filterGroup.addFilters(filters);
		model.addFilterGroup(filterGroup);
	}

	/**
	 * This method removes a subfilter from the {@link TableFilter}
	 * 
	 * @param filter
	 *        - the subfilter to remove
	 */
	public void removeSubFilter(ConfiguredFilter filter) {
		model.removeInternalFilter(filter);
	}
	
	/**
	 * This method returns the sub filters of the {@link TableFilter}
	 * 
	 * @return list of subfilters of the table filter, never <code>null</code>.
	 */
	public List<ConfiguredFilter> getSubFilters() {
		return model.getSubFilters();
	}
	
	/**
	 * Adds sub filters of specified source {@link TableFilter} to this {@link TableFilter}. Thereby
	 * sub filter group structure of the source {@link TableFilter} will be preserved.
	 */
	public void addSubFilterFrom(TableFilter sourceFilter) {
		for (FilterGroup filterGroup : sourceFilter.model.getInternalFilterGroups()) {
			addSubFilterGroup(filterGroup.getFilters());
		}
	}

	/**
	 * This method adds a listener to the {@link TableFilter}
	 * 
	 * @param listener - the listener of the table filter
	 */
	public void addTableFilterListener(TableFilterListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * This method removes a listener from the {@link TableFilter}
	 * 
	 * @param listener - the listener of the table filter
	 */
	public void removeTableFilterListener(TableFilterListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Show description and Filter
	 */
	@Override
	public String toString() {
	    return this.getClass().getName() + " filter: " + model.getInternalFilterGroups();
	}
	
	/**
	 * Called at begin of table model / filter revalidation process, used for preparation purposes
	 * (e.g. allocation of caches).
	 */
	public void startFilterRevalidation() {
		for (ConfiguredFilter configuredFilter : getSubFilters()) {
			configuredFilter.startFilterRevalidation(isMatchCountingEnabled());
		}
	}

	/**
	 * Called at the end of table model / filter revalidation process, used for cleanup of caches
	 * and filter model updates.
	 */
	public void stopFilterRevalidation() {
		for (ConfiguredFilter configuredFilter : getSubFilters()) {
			configuredFilter.stopFilterRevalidation();
		}
	}

	/**
	 * Called, when internal matching {@link ConfiguredFilter}s, shall increment their match counter
	 * by 1 during filter process.
	 * 
	 * @param value
	 *        for that the counter shall be increased
	 */
	public void count(Object value) {
		for (ConfiguredFilter configuredFilter : getAppropriateSubfilters(value)) {
			configuredFilter.count(value);
		}
	}

	/**
	 * Delegate to sub filters.
	 */
	@Override
	public boolean accept(Object value) {
		boolean match = false;

		for (ConfiguredFilter configuredFilter : getAppropriateSubfilters(value)) {
			boolean accepts = configuredFilter.accept(value);
			if (configuredFilter.isActive() || !isActive()) {
				match = match || accepts;
			}
		}

		if (!model.isInversionStateActive()) {
			return match;
		} else {
			return !match;
		}
	}

	private List<ConfiguredFilter> getAppropriateSubfilters(Object obj) {
		if (obj != null) {
			return model.getSupportingFilters(obj.getClass());
		} else {
			return getSubFilters();
		}
	}

	/**
	 * This method notifies all listener about a occurring table filter event
	 * 
	 * @param eventType - the event type of the table filter model event
	 */
	private final void notifyListeners(FilterEventTypes eventType) {
		TableFilterEvent event = new TableFilterEvent(this, eventType);
		for (int i = 0, size = listeners.size(); i < size; i++) {
			TableFilterListener listener = listeners.get(i);
			listener.handleTableFilterEvent(event);
		}
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void handleTableFilterModelEvent(TableFilterModelEvent event) {
		
		if(event.getEventType() == FilterModelEventTypes.CONFIGURATION_UPDATE) {
			notifyListeners(FilterEventTypes.CONFIGURATION_UPDATE);
		}
		else if (event.getEventType() == FilterModelEventTypes.RE_APPLIANCE) {
			notifyListeners(FilterEventTypes.RE_APPLIANCE);
		}
		else if (event.getEventType() == FilterModelEventTypes.DEACTIVATION) {
			notifyListeners(FilterEventTypes.DEACTIVATION);
		}
	}
	
	/**
	 * This method returns the json compatible serialized state of the {@link TableFilter}
	 * 
	 * <p>
	 * The structure of the serialized state looks as like as follows:
	 * [[Boolean inversion state], [[{@link FilterConfiguration} raw class, {@link JSONTransformer} class], [serialized {@link ConfiguredFilter} config state]], ...]
	 * </p>
	 * 
	 * @return serialized state of the table filter
	 */
	public Object getSerializedState() {
		return TableFilterSerializer.getState(model);
	}

	/**
	 * This method restores the state of the {@link TableFilter} out of its serialized state.
	 * 
	 * <p>
	 * For detailed information about the structure of the serialized state, take a look at
	 * {@link TableFilter#getSerializedState()}
	 * </p>
	 * 
	 * @param serializedState - the serialized state of the table filter
	 */
	public void setSerializedState(Object serializedState) {
		TableFilterDeserializer.restoreState(model, serializedState);
	}

	/**
	 * This method returns, whether the {@link TableFilter} is active, or not
	 * 
	 * @return true, if the filter is active, false otherwise
	 */
	public boolean isActive() {
		boolean isActive = false;
		List<ConfiguredFilter> filters = getSubFilters();
		for (int i = 0, size = filters.size(); !isActive && (i < size); i++) {
			ConfiguredFilter configuredFilter = filters.get(i);
			isActive = configuredFilter.isActive();
		}
		
		return isActive;
	}

	/**
	 * true, if this {@link TableFilter} currently displayed at GUI, false otherwise.
	 */
	public boolean isVisible() {
		List<ConfiguredFilter> subFilters = getSubFilters();
		if (subFilters.size() > 0) {
			return subFilters.get(0).isVisible();
		}
		return false;
	}

	/**
	 * Makes this filter inactive.
	 */
	public void reset() {
		model.clearAllFilters();
	}

	/**
	 * Sets whether this filter works inverse or not.
	 * 
	 * @param inverse
	 *        {@code true} if the filter should work inverse
	 */
	public void setInverseModeActive(boolean inverse) {
		model.setInversionStateIsActive(inverse);
	}

	/**
	 * true, if this filter works inverse, false otherwise.
	 * 
	 * @see #setInverseModeActive(boolean)
	 */
	public boolean isInverseModeActive() {
		return model.isInversionStateActive();
	}

	/**
	 * @see TableFilterModel#isMatchCountingEnabled()
	 */
	public boolean isMatchCountingEnabled() {
		return model.isMatchCountingEnabled();
	}

	/**
	 * @see #isMatchCountingEnabled()
	 */
	public void setMatchCountingEnabled(boolean isMatchCountingEnabled) {
		model.setMatchCountingEnabled(isMatchCountingEnabled);
	}
}
