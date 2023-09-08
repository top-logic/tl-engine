/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.NoBubblingEventType;
import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.layout.inspector.model.nodes.InspectorTreeNode;

/**
 * Model to be used by the inspector components.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class InspectorModel extends PropertyObservableBase {

	/** The changed filter property. */
	public static final EventType<InspectorFilterChanged, InspectorModel, Filter<? super InspectorTreeNode>> PROP_FILTER_CHANGED =
		new NoBubblingEventType<>(
			"filterChanged") {

			@Override
			protected void internalDispatch(InspectorFilterChanged listener, InspectorModel sender,
					Filter<? super InspectorTreeNode> oldValue, Filter<? super InspectorTreeNode> newValue) {
				listener.handleFilterChanged(sender, oldValue, newValue);
			}

		};

	private final Object _inspected;

	private Filter<? super InspectorTreeNode> _filter;

	/**
	 * Creates a {@link InspectorModel}.
	 */
	public InspectorModel(Object inspected, Filter<? super InspectorTreeNode> filter) {
		_inspected = inspected;

		setFilter(filter);
	}

	/**
	 * This method returns the inspected.
	 * 
	 * @return Returns the inspected.
	 */
	public Object getInspected() {
		return _inspected;
	}

	/**
	 * This method returns the filter.
	 * 
	 * @return Returns the filter.
	 */
	public Filter<? super InspectorTreeNode> getFilter() {
		return _filter;
	}

	/**
	 * This method sets the filter.
	 * 
	 * @param filter
	 *        The filter to set.
	 */
	public void setFilter(Filter<? super InspectorTreeNode> filter) {
		if (filter != _filter) {
			Filter<? super InspectorTreeNode> oldFilter = _filter;
			_filter = filter;
			notifyListeners(PROP_FILTER_CHANGED, this, oldFilter, _filter);
		}
	}

	/**
	 * Register the given listener to be notified when filter has been changed.
	 */
	public void register(InspectorFilterChanged listener) {
		addListener(PROP_FILTER_CHANGED, listener);
	}

	/**
	 * Register the given listener.
	 */
	public void unregister(InspectorFilterChanged listener) {
		removeListener(PROP_FILTER_CHANGED, listener);
	}

}
