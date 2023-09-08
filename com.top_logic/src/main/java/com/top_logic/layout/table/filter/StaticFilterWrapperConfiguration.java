/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.LinkedList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.layout.DisplayValue;

/**
 * A configuration used by static filter wrappers
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class StaticFilterWrapperConfiguration extends FilterConfiguration {

	private boolean isActive;
	private DisplayValue filterDescription;
	private int matchCount;
		
	/**
	 * Create a new StaticFilterWrapperConfiguration
	 * 
	 * @param filterDescription - the filter description
	 */
	protected StaticFilterWrapperConfiguration(DisplayValue filterDescription) {
		this.filterDescription = filterDescription;
		isActive = false;
		matchCount = SingleEmptyValueMatchCounter.EMPTY_VALUE;
	}
	
	/**
	 * This method sets the status of the static filter
	 * 
	 * @param isActive - the status of the static filter
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
		notifyValueChanged();
	}
	
	/**
	 * This method determines, whether the static filter is active, or not
	 * 
	 * @return status of active filter
	 */
	public boolean isActive() {
		return isActive;
	}
	
	/**
	 * count of matches of this filter (updated during filtering).
	 */
	public int getMatchCount() {
		return matchCount;
	}

	/**
	 * @see #getMatchCount()
	 */
	public void setMatchCount(int matchCount) {
		this.matchCount = matchCount;
		notifyValueChanged();
	}

	/**
	 * Getter for filter description
	 * 
	 * @return filter description
	 */
	public DisplayValue getFilterDescription() {
		return filterDescription;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public FilterConfigurationType getConfigurationType() {
		return new FilterConfigurationType(this.getClass(), null);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> getSerializedState() {
		List<Object> serializedState = new LinkedList<>();
		serializedState.add(isActive);
		return serializedState;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setSerializedState(List<Object> state) {
				
		// Restore serialized settings
		if(state.size() == 1) {
			
			// Backup current configuration state for failsave
			boolean currentIsActive = isActive;
			try {
				isActive = (Boolean) state.get(0);
			} catch (ClassCastException e) {
				Logger.warn("Recovery of the filter configuration failed, because of invalid recovery parameters. " +
								"[Boolean] was expected, but " + state.toString() + " was found.",
							 e, StaticFilterWrapperConfiguration.class);
				
				// Restore original configuration state
				isActive = currentIsActive;
			}
		}
		
		else {
			Logger.warn("Recovery of the filter configuration failed, because of invalid amount of recovery parameters. " +
							"[Boolean] was expected, but " + state.toString() + " was found.",
						 StaticFilterWrapperConfiguration.class);
		}
	}

	@Override
	protected void clearConfiguration() {
		setActive(false);
	}
}