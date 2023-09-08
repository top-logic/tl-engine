/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

/**
 * Default implementation of {@link DynamicValues}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DynamicValuesImpl extends DynamicValues {

	/** @see #getDynamicValues() */
	private FlexData _dynamicValues;

	/**
	 * Creates a new {@link DynamicValuesImpl}.
	 */
	public DynamicValuesImpl(long minValidity, long maxValidity, Object[] data) {
		super(minValidity, maxValidity, data);
	}

	@Override
	public synchronized boolean needsToBeLoaded(long dataRevision) {
		return _dynamicValues == null;
	}

	/**
	 * Returns the current dynamic values. Must not be modified.
	 */
	@Override
	public synchronized FlexData getDynamicValues() {
		return _dynamicValues;
	}

	/**
	 * Sets the value of {@link #getDynamicValues()}
	 */
	@Override
	public synchronized void setDynamicValues(FlexData dynamicValues) {
		_dynamicValues = dynamicValues;
	}

	/**
	 * Initializes value {@link #getDynamicValues()} if currently <code>null</code>.
	 * 
	 * @param initialValues
	 *        The initial values. Must not be modified after set.
	 */
	@Override
	public synchronized void initDynamicValues(FlexData initialValues) {
		if (_dynamicValues == null) {
			_dynamicValues = initialValues;
		}
	}

}

