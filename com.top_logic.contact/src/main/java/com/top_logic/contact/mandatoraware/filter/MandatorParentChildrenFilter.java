/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.filter;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.col.Filter;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.util.AllElementVisitor;
import com.top_logic.element.structured.wrap.Mandator;

/**
 * Accepts Mandators that are the initial mandator, one of the parents, or a child (optional)
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class MandatorParentChildrenFilter implements Filter {
	private Mandator mandator;
	private Set      parentsChildren;
	private boolean  includeChildren;
	private boolean  includeParents;
	
	/** 
	 * CTor
	 * 
	 * @param aMandator		initial mandator
	 * @param doIncChildren	if true, accept children as well (only mandator and parents otherwise)
	 */
	public MandatorParentChildrenFilter(Mandator aMandator, boolean doInclParents, boolean doIncChildren) {
		this.mandator        = aMandator;
		this.includeParents  = doInclParents;
		this.includeChildren = doIncChildren;
	}
	
	/** 
	 * @see com.top_logic.basic.col.Filter#accept(java.lang.Object)
	 */
	@Override
	public boolean accept(Object anObject) {
		if (!(anObject instanceof Mandator)) {
			return false;
		}
		
		if (this.mandator == null) {
			return true;	// No filtering
		}
		
		Mandator theMandator = (Mandator) anObject;
		return this.getMandators().contains(theMandator);
	}
	
	/** 
	 * Get the Mandators to be accepted
	 * Note: do not call if initial Mandator is <code>null</code>!
	 * 
	 * @return the Mandators
	 */
	private Set getMandators() {
		if (this.parentsChildren == null) {
			Set theParentsChildren = new HashSet();

			theParentsChildren.add(this.mandator);
			
			if (this.includeParents) {
				// Handle parents
				Mandator theParent = (Mandator) this.mandator.getParent();
				while (theParent != null) {
					theParentsChildren.add(theParent);
					theParent = (Mandator) theParent.getParent();
				}
			}
				
			if (this.includeChildren) {
				AllElementVisitor theVis = new AllElementVisitor();
    			TraversalFactory.traverse(this.mandator, theVis, TraversalFactory.BREADTH_FIRST);
    			theParentsChildren.addAll(theVis.getList());
			}
			
			this.parentsChildren = theParentsChildren;
		}
		
		return this.parentsChildren;
	}
}