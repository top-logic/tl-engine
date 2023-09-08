/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import java.text.Collator;


/**
 * This class presents intervals (start-value, end-value) that are described by a name.
 * 
 *
 * @author <a href="mailto:hbo@top-logic.com">hbo</a>
 */
public class NamedIntervall implements Comparable {
	/** The collator that is used for comparing name-attributes. */
	private Collator collator;

	/** Name of the interval. */
	private String name;

	/** Start-integer of the interval (inclusive). */
	private int start;

	/** End-integer of the interval (exclusive). */
	private int end;
	

	/**
	 * Public constructor setting all attributes. 
	 * @param aName		the interval name
	 * @param aStart	the interval start
	 * @param anEnd		the interval end
	 * @param aCollator for sorting...
	 */
	public NamedIntervall(String aName, int aStart, int anEnd, Collator aCollator){
		this.name = aName;
		this.start = aStart;
		this.end = anEnd;
		this.collator = aCollator;
	}

	/**
	 * Get-method for attribute end.
	 * @return the end of the interval
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Get-method for attribute name.
	 * @return the name of the interval
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get-method for attribute start.
	 * @return the start of the interval
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Set-method for attribute end.
	 * @param i the end of the interval
	 */
	public void setEnd(int i) {
		end = i;
	}

	/**
	 * Set-method for attribute name.
	 * @param string the name of the interval
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * Set-method for attribute start.
	 * @param i the start of the interval
	 */
	public void setStart(int i) {
		start = i;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object anObject){
		if (anObject instanceof NamedIntervall){
			NamedIntervall theIntervall = (NamedIntervall)(anObject);
			return this.collator.compare(this.getName(), theIntervall.getName());
		}
		else if (anObject instanceof String){
			String theString = (String) anObject;
			return this.collator.compare(this.getName(), theString);
		}
		else{
			throw new ClassCastException("Comparision of NamedIntervall: Wrong type");
		}
	}

}
