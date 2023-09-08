/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.List;

/**
 * An base class for filter configurations, which shall be serializable.
 * 
 * @author <a href="mailto:sts@top-logic.com">sts</a>
 */
public abstract class FilterConfiguration extends FilterModel {
	
	/**
	 * This method produces a serialized form of the filter configuration.
	 * @return serialized filter configuration
	 */
	public abstract List<Object> getSerializedState();
	
	/**
	 * This method restores the filter configuration out of a serialized form
	 * 
	 * @param state - the serialized filter configuration
	 */
	public abstract void setSerializedState(List<Object> state);
	
	/**
	 * the type of the filter configuration
	 */
	public abstract FilterConfigurationType getConfigurationType();

	/**
	 * Sets the {@link FilterConfiguration} to its empty state in the means, that the
	 * {@link ConfiguredFilter}, that is using this {@link FilterConfiguration} is not active
	 */
	protected abstract void clearConfiguration();
}