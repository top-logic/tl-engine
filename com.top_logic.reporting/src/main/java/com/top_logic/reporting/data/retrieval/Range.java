/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.retrieval;


/**
 * An Object which is to be used as Range (key) Object within a ValueHolder
 * has to implement this interface. Typically the implementation of Range will wrap
 * the object to be used and so add the functionality of this interface.
 * 
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public interface Range extends Comparable {
	
	/**
	 * the DisplayName of this Range object
	 */
	public String getDisplayName();

	/**
	 * the wrapped Comparable
	 */
	public Comparable getComparable();

	/**
	 * it might be neccessary to overload the equals method
	 * The implementor has to ensure that two range objects with the same meaning
	 * are equal and as such return the same hashCode
	 */
	@Override
	public boolean equals(Object obj);

	/**
	 * it might be neccessary to overload the hashCode method
	 * The implementor has to ensure that two range objects with the same meaning
	 * are equal and as such return the same hashCode
	 */
	@Override
	public int hashCode();
}
