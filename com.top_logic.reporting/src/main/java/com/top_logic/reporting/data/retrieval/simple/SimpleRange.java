/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.retrieval.simple;

import com.top_logic.reporting.data.retrieval.Range;

/**
 * Simple implementation of a range.
 * 
 * A range defines an comparable element to be used for the values.
 *
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public class SimpleRange implements Range {

    // Attribute

    /** The held comparable. */
	private Comparable comparable;

	/**
	 * Constructs a new RangeObject.
     * 
	 * @param    aComparable    The comparable wich should be wrapped.
	 */
	public SimpleRange (Comparable aComparable) {
		this.comparable = aComparable;
	}

	/**
	 * @see com.top_logic.reporting.data.retrieval.Range#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return this.comparable.toString();
	}

	/**
	 * @see com.top_logic.reporting.data.retrieval.Range#getComparable()
	 */
	@Override
	public Comparable getComparable() {
		return this.comparable;
	}

	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(Object arg0) {
		SimpleRange tmp = (SimpleRange) arg0;
		return this.comparable.compareTo(tmp.getComparable());
	}

	/**
	 * @see com.top_logic.reporting.data.retrieval.Range#equals(Object)
	 */
	@Override
	public boolean equals(Object anObject){
		if (anObject instanceof SimpleRange){
			SimpleRange anotherRange = (SimpleRange)anObject;
			return this.getComparable().equals(anotherRange.getComparable());
		}
		return false;
	}

	/**
	 * @see com.top_logic.reporting.data.retrieval.Range#hashCode()
	 */
	@Override
	public int hashCode(){
		return this.getComparable().hashCode();
	}
}
