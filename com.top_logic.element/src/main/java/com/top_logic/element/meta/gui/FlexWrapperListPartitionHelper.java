/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.Configuration;
import com.top_logic.util.TLContext;

/** 
 * This class is a helper-class for the partition of List of FlexWrappers.
 * For this partition the flex-attribute name is regarded.
 *
 * @author    <a href="mailto:hbo@top-logic.com">hbo</a>
 */
public class FlexWrapperListPartitionHelper {
	
	/**
	 * A String-Array that contains the partition-names.
	 * e.g.: A...A, B...L or C...Z
	 */
	private String[] filterNames;
	/** The String-array filterNames + the String "All" in position 0. */
	private String[] allFilterNames;
	
	/**
	 * A list of NamedIntervalls. The attribute name of each interval is an element of string-array
	 * filterNames. The order of this list is the same order as in the array.
	 */
	private List namedIntervalls;
	/** Copy of the list that should be partition. */
	private List allElements;
	/** Expected size of partition. */
	private int partitionSize = -1;
	
	/**
	 * Get-method for attribute allElements.
	 * @return the list elements.
	 */
	public List getAllElements() {
		return allElements;
	}


	/**
	 * Get-method for attribute namedIntervalls.
	 * @return all named intervals
	 */
	public List getNamedIntervalls() {
		return namedIntervalls;
	}

	/**
	 * Set-method for attribute allElements.
	 * @param list the list elements
	 */
	public void setAllElements(List list) {
		allElements = list;
	}

	/**
	 * Set-method for attribute allFilterNames.
	 * @param strings all filter names
	 */
	public void setAllFilterNames(String[] strings) {
		allFilterNames = strings;
	}

	/**
	 * Set-method for attribute filterNames.
	 * @param strings the filter names ('All' will be added automtically)
	 */
	public void setFilterNames(String[] strings) {
		filterNames = strings;
	}

	/**
	 * Set-method for attribute namedIntervalls.
	 * @param list the intervals
	 */
	public void setNamedIntervalls(List list) {
		namedIntervalls = list;
	}
	/**
	 * Create and return an ordered, partition list.
	 * @param filters a String-Array that contains the partition-name at position 0. (e.g. A...C)
	 * @param aList the List the should be partitioned
	 * @param ascending true if aList has an ascending order and false if the list has a descending order.
	 * @return a partitioned list
	 */
	public List helpSorting(String[] filters, List aList, boolean ascending){
	
		String theFilter = filters[0];
		List theTmp = null;
		if (!"All".equals(theFilter)){					
			int thePos = Arrays.binarySearch(this.getFilterNames(aList), theFilter);
			if (thePos < 0){
				thePos = -1 * thePos -1 ;
			}
			if (thePos + 1 > namedIntervalls.size()){
				theTmp = aList;
			}
			else{
			
				NamedIntervall theNI = (NamedIntervall)this.namedIntervalls.get(thePos);
				theTmp = new ArrayList();
		    	if (ascending){
					for (int i = theNI.getStart(); i<theNI.getEnd(); i++){						
						theTmp.add(aList.get(i));
					}
															
				}
				else{
					for (int i = theNI.getEnd() -1 ; i>=theNI.getStart(); i--){						
						theTmp.add(aList.get(aList.size()- 1 - i));
					}							
				}
			}
		}
		else {
			theTmp = aList;	
		}
			
		return theTmp;
	}
	
	/**
	 * Get-method for attribute filterNames. 
	 * (Lazy-initialization)
	 *  
	 * @param aList an ordered list that should be partition.
	 * @return the filter names
	 */
	public String[] getFilterNames(List aList){
		if (this.filterNames == null){
			this.filterNames = this.createFilterNames(aList);
		}
		return this.filterNames;
	}
	
	/**
	 * Create an array of partition-names.
	 * The partition-method of FlexWrapperListPartition is invoked.
	 */		
	public String[] createFilterNames(List aList) {
		int thePartitionSize = this.getPartitionSize();
		this.namedIntervalls = 
			new FlexWrapperListPartition(Collator.getInstance(TLContext.getContext().getCurrentLocale())).
									  partition(aList, thePartitionSize);
		int theSize = this.namedIntervalls.size();
		String[] theFilterNames = new String[theSize];
		for (int i = 0; i<theSize; i++){
			String theFilterName = ((NamedIntervall)this.namedIntervalls.get(i)).getName();
			theFilterNames[i] = theFilterName;
		}

		return theFilterNames;
	}
	
	/**
	* Get-method for attribute allFilterNames. 
	* (Lazy-initialization)
	*  
	* @param aList an ordered list that should be partition.
	* @return all filter names (adds 'All' as a filter if only filterNames is defined)
	*/
	public String[] getAllFilterNames(List aList){
		if (this.allFilterNames == null ){
			this.filterNames = this.getFilterNames(aList);
			this.allFilterNames = new String[this.filterNames.length + 1];
			this.allFilterNames[0] = "All";
			for (int i = 0; i < this.filterNames.length; i++){
				this.allFilterNames[i + 1] = filterNames[i];
			}
		}
		return this.allFilterNames;
			
	}
	/**
	 * Set the given list for attribute allElements and set the attributes
	 * allFilterNames and filterNames null. 
	 * This forces a new partition.
	 */
	public void resetAttributes(List aList){
	
		this.setAllElements(aList);
		this.setAllFilterNames(null);
		this.setFilterNames(null);
		
	}	
	/**
	 * Return the expected partition-size from config.xml
	 * @return the partition size
	 */
	private int getPartitionSize(){
		if (this.partitionSize == -1){
			Configuration theConfig = Configuration.getConfiguration(this.getClass());
			String theString = theConfig.getValue("size", "25");
			this.partitionSize = Integer.parseInt(theString);
		}
		return this.partitionSize;					
	}

}
