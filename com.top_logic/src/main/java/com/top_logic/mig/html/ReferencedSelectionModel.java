/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Set;



/**
 * A selection model, that holds beside the current selection a reference to elements,
 * which belong to an external selection set and are a subset of possible current selection
 * elements.
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public interface ReferencedSelectionModel extends SelectionModel {
	
	/**
	 * This method determines, if the given object belongs to an external selection set.
	 * 
	 * @param obj - the object, that needs to be checked
	 * @return true, if the object is part of the referenced selection, false otherwise
	 */
	public boolean isReferencedSelection(Object obj);
	
	/**
	 * This method propagates changes in the referenced selection
	 * 
	 * @param changedSelection - the list of changed elements
	 */
	public void propagateReferencedSelectionChange(Set changedSelection);
}
