/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import java.text.Collator;
import java.util.Collections;
import java.util.List;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;

/** 
 * Class that that is used to partition a list of FlexWrappers.
 * The attribute, that is used for this partition is name.
 *
 * @author    <a href="mailto:hbo@top-logic.com">hbo</a>
 */
public class FlexWrapperListPartition extends ListPartition{
	
	/**
	 * Public constructor.
	 * Set the used collator.
	 * @param aCollator for sorting...
	 */
	public FlexWrapperListPartition(Collator aCollator){
		super(aCollator);
	}

	/**
	 * @see com.top_logic.element.meta.gui.ListPartition#getName(java.lang.Object)
	 */
	@Override
	public String getName(Object anObject){
		try {
		    Wrapper theFlexWrapper = (Wrapper)anObject;
			return theFlexWrapper.getName();
		}
		catch (Exception ex) {
			return String.valueOf(anObject);
		}
	}
	
	/**
	 * Overwrites and invoke the method of superclass.
	 * The list that should be partition has to be sorted.
	 * If it is sorted in an ascending order, the method of superclass
	 * is invoked. If it is sorted in descending order, that list 
	 * is reversed, then the method of superclass is invoked and the the
	 * list is reversed again.
	 * @see com.top_logic.element.meta.gui.ListPartition#partition(java.util.List, int)
	 */
	@Override
	public List partition(List aList, int aPartionSize){
		List theList;
		
		if (aList != null && aList.size() > 1){
			try {
				String theName0 = ((Wrapper) aList.get(0)).getName();
				String theName1 = ((Wrapper) aList.get(aList.size() - 1)).getName();
				if (this.collator.compare(theName0, theName1) > 0){
					Collections.reverse(aList);
					theList = super.partition(aList, aPartionSize);
					Collections.reverse(aList);
					return theList;
				}
			}
			catch (WrapperRuntimeException wex) {
				return aList;
			}
		}
		theList = super.partition(aList, aPartionSize);
		return theList;
	}

}
