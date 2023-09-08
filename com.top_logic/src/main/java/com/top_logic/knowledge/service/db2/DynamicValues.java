/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

/**
 * {@link Values} that hold an additional storage for dynamic values.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class DynamicValues extends ValuesImpl {

	/**
	 * Creates a new {@link DynamicValues}.
	 * 
	 * @see ValuesImpl#ValuesImpl(long, long, Object[])
	 * 
	 */
	public DynamicValues(long minValidity, long maxValidity, Object[] data) {
		super(minValidity, maxValidity, data);
	}

	/**
	 * Whether the dynamic values for the given revision need to be loaded before they can be
	 * accessed.
	 */
	public abstract boolean needsToBeLoaded(long dataRevision);

	/**
	 * Returns the current dynamic values. Must not be modified.
	 */
	public abstract FlexData getDynamicValues();

	/**
	 * Sets the value of {@link #getDynamicValues()}
	 */
	public abstract void setDynamicValues(FlexData dynamicValues);

	/**
	 * Initializes value {@link #getDynamicValues()} if currently <code>null</code>.
	 * 
	 * @param initialValues
	 *        The initial values. Must not be modified after set.
	 */
	public abstract void initDynamicValues(FlexData initialValues);

}

