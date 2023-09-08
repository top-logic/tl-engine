/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;

/**
 * A simple filter for selection models, to determine selectable objects.
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public class SelectionModelFilter implements Filter<Object> {

	private final Set<? extends Object> nonSelectableObjects;
	
	/**
	 * Create a new SelectionModelFilter 
	 * 
	 * @param nonSelectableObjects - the collection of objects, which are not selectable
	 */
	public SelectionModelFilter(Collection<? extends Object> nonSelectableObjects) {
		this.nonSelectableObjects = CollectionUtil.toSet(nonSelectableObjects);
	}
	
	/**
	 * This method determines, whether an given object is selectable, or not
	 * 
	 * @param anObject - the potentially selectable object
	 * @return true, if the object is selectable, false otherwise
	 * 
	 * @see com.top_logic.basic.col.Filter#accept(java.lang.Object)
	 */
	@Override
	public boolean accept(Object anObject) {		
		return !nonSelectableObjects.contains(anObject);
	}
}
