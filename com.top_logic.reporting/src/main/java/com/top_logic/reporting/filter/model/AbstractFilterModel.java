/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.filter.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link AbstractFilterModel} implements all methods of the {@link FilterModel}.
 * Additional the method {@link #fireModelEvent(FilterModelEvent)} can be used to 
 * fire events. Any {@link FilterModelEvent} are dispatched to all registered
 * {@link FilterModelListener}.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class AbstractFilterModel implements FilterModel {

	/** See {@link #getModelListener()}. */
	private List modelListener;
	
	/**
	 * Creates a new AbstractFilterModel without {@link FilterModelListener}s.
	 */
	public AbstractFilterModel() {
		super();
		
		this.modelListener = new ArrayList();
	}

	/**
	 * This method informs all registered listener about the given
	 * {@link FilterModelEvent}.
	 * 
	 * @param modelEvent
	 *            A {@link FilterModelEvent} must NOT be <code>null</code>.
	 */
	protected void fireModelEvent(FilterModelEvent modelEvent) {
		for (Iterator iterator = getModelListener().iterator(); iterator.hasNext();) {
			((FilterModelListener) iterator.next()).handleModelEvent(modelEvent);
		}
	}
	
	@Override
	public boolean addFilterModelListener(FilterModelListener listener) {
		if (getModelListener().contains(listener)) return true;
		
		return getModelListener().add(listener);
	}

	@Override
	public boolean removeFilterModelListener(FilterModelListener listener) {
		return getModelListener().remove(listener);
	}

	/**
	 * This method returns a list of {@link FilterModelListener}s. Listener can
	 * be added/removed by the methods
	 * {@link #addFilterModelListener(FilterModelListener)}/
	 * {@link #removeFilterModelListener(FilterModelListener)}.
	 * 
	 * @return Returns a list of {@link FilterModelListener}s.
	 */
	protected List getModelListener() {
		return this.modelListener;
	}
	
}
