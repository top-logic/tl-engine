/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office.basic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.reporting.office.ExpansionContext;
import com.top_logic.reporting.office.ExpansionEngine;
import com.top_logic.reporting.office.ExpansionObject;


/**
 * {@link ExpansionObject} containing further inner {@link ExpansionObject}s, e.g. for a field
 * with multiple symbols.
 *
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class StackedExpansionObject extends ExpansionObject {

    private List innerExpansionObjects;

    public StackedExpansionObject(String aFieldKey) {
        super(aFieldKey);
        innerExpansionObjects = new ArrayList(5);
    }

    /**
     * Whether this object has inner {@link ExpansionObject}s.
     */
    public boolean hasInnerExpansionObjects () {
        return !innerExpansionObjects.isEmpty();
    }
    
    /**
     * Adds an inner {@link ExpansionObject}.
     */
    public void addExpansionObject (ExpansionObject anExpansionObject) {
        innerExpansionObjects.add(anExpansionObject);
    }
    
    /**
     * The inner {@link ExpansionObject}s of this object.
     */
    public List getInnerExpansionObjects() {
        return innerExpansionObjects;
    }
    /**
     * Expanding this type of objects requires expanding of all possible inner expansion objects also!
     * 
     * @see com.top_logic.reporting.office.ExpansionObject#expand(com.top_logic.reporting.office.ExpansionEngine, com.top_logic.reporting.office.ExpansionContext)
     */
    @Override
	public void expand(ExpansionEngine anEngine, ExpansionContext aContext) {
        // first expand ourself.
        super.expand(anEngine, aContext);
        if (hasInnerExpansionObjects()) {
            Iterator iter = innerExpansionObjects.iterator();
            while (iter.hasNext()) {
                ExpansionObject element = (ExpansionObject) iter.next();
                element.expand(anEngine,aContext);
            }
        }
    }   
}
