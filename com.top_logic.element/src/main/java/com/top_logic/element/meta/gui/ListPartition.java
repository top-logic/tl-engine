/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;


/** 
 * This class is used to partition a list in smaller parts.
 * The expected size of a partition may be set.
 * The class is abstract so subclasses have to be implemented for the objects
 * the list the has to be partition consists of.
 *
 * @author    <a href="mailto:hbo@top-logic.com">hbo</a>
 */
public abstract class ListPartition {
	/** Collator that is used to get compare-criterions. */
	protected Collator collator;
	
	/**
	 * public constructor.
	 * The used collator will be set.
	 */
	public ListPartition(Collator aCollator){
		this.collator = aCollator;
	}
	
	/**
	 * Starting-point of partition a list.
     * 
	 * The method returns  a list of {@link com.top_logic.element.meta.gui.NamedIntervall}.
     *   
	 * @param aList the list that should to be partitioned. This list has to be ordered.
	 * @param aPartionSize the expected size of a part. 
	 * @return a list of NamedIntervall.
	 */
	public List partition(List aList, int aPartionSize){
        List theNamedIntervallList = new ArrayList(30);

        this.collator.setStrength(Collator.PRIMARY);
		this.startPartition(aList, 0, aPartionSize, theNamedIntervallList);

		return theNamedIntervallList; 
	}
	
	/**
	 * Abstract method that has to be overwrited in the subclass.
     * 
	 * It returns the names of the object of the list.
     * 
	 * @return    The name of the given object, if contained in the list.
	 */
	protected abstract String getName(Object anObject);
	
	private void startPartition(List aList, int aStart, int aPartionSize, List aNamedIntervallList){
		if (aList != null && !aList.isEmpty()){
		
			if (aList.size() - aStart < aPartionSize){
				this.addToList(aList, aStart, aList.size(), aNamedIntervallList);
			}
			else{
				this.buildUpPartition(aList, aStart, aStart, aPartionSize, aNamedIntervallList);
				
			}
		}	
	}
	
	/**
	 * Create a new NamedIntervall and add it to a list of NamedIntervall.
	 * 
	 * @param aList
	 *        a list that is partitioned.
	 * @param aStart
	 *        the start-position of the partition in the partitioned list.
	 * @param anEnd
	 *        the end-position of the partition in the partitioned list.
	 * @param aNamedIntervallList
	 *        a list of named intervals
	 */
	private void addToList(List aList, int aStart, int anEnd,  List aNamedIntervallList) {
		String theStartElementName = this.getName(aList.get(aStart));
		String theEndElementName = this.getName(aList.get(anEnd-1));
		String theStartString = this.getFirstLetterOfString(theStartElementName);
		String theEndString = this.getFirstLetterOfString(theEndElementName);
		String theKeyString = theStartString +"..."+theEndString;
		aNamedIntervallList.add(new NamedIntervall(theKeyString, aStart, anEnd, this.collator));
	}
	
	/**
	 * Create a partition.
	 * @param aCompleteList the ordered list that that should be partitioned.
	 * @param aStart the actual position in aCompleteList
	 * @param aPartionBegin the position in aCompleteList in which the actual partition starts
	 * @param aPartionSize the expected size of partition.
	 * @param aNamedIntervallList a list of NamedIntervals
	 */
	private void buildUpPartition(List aCompleteList, int aStart, int aPartionBegin, int aPartionSize, List aNamedIntervallList){
	     boolean characterChange = false;
	     String theStartElement = this.getName(aCompleteList.get(aStart));
		 String theStartString = this.getFirstLetterOfString(theStartElement);
	     int i = aStart;
	     for (i = aStart; i < aCompleteList.size() && !characterChange; i++){
	     	
	     	String theTmp = this.getName(aCompleteList.get(i));
	     	String theStartTmp = this.getFirstLetterOfString(theTmp);
	     	if (this.collator.compare(theStartString, theStartTmp) == 0){
	     		
	     		if (i == aCompleteList.size() - 1){
	     			//this.addToList(aCompleteList, aStart, aCompleteList.size(), aNamedIntervallList);
					this.addToList(aCompleteList, aPartionBegin, aCompleteList.size(), aNamedIntervallList);
	     		}
	     	}
	     	else {
				characterChange = true;
	     		if (i-aPartionBegin > aPartionSize){
	     			this.addToList(aCompleteList, aPartionBegin, i, aNamedIntervallList);
	     			this.startPartition(aCompleteList,i,aPartionSize,aNamedIntervallList);
	     		}
	     		else{
					
	     			this.buildUpPartition(aCompleteList,i, aPartionBegin, aPartionSize,aNamedIntervallList);
	     		}
	     	}
	     }                                          	
	}
	/**
	 * Return the first letter of the given String to upper case.
	 * @param aString the string. Must not be <code>null</code>.
	 * @return the first letter of the given String to upper case.
	 */
	private String getFirstLetterOfString(String aString){
		return Character.toString(aString.charAt(0)).toUpperCase();
	}

}
