/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Find all components that match the names in a given Set.
 * 
 * @author     <a href="mailto:kha@top-logic.com">kha</a>
 */
public class FindComponentsNamedVisitor extends DefaultDescendingLayoutVisitor {

    protected Set/* <String> */ names;
    
    protected Collection/* <LayoutComponent> */ found;

    protected int foundCount;

    /** 
     * Create a new FindComponensNamedVisitor with a Set of Names.
     * 
     * @param someNames must not be empty or <code>null</code>.
     * @param into Results will be accumulated here.
     */
    public FindComponentsNamedVisitor(Set someNames, Collection into) {
        if (someNames == null || someNames.isEmpty()) {
            throw new IllegalArgumentException("Set of names must not be empty");
        }
        if (into == null ) {
            throw new NullPointerException("into not be null");
        }
        names      = someNames;
        found      = into;
        foundCount = 0;
    }

    /** 
     * Create a new FindComponensNamedVisitor with a Set of Names and a default ArrayList as result.
     * 
     * @param someNames must not be empty or <code>null</code>.
     */
    public FindComponentsNamedVisitor(Set someNames) {
        this(someNames, new ArrayList(someNames.size()));
    }
    
    @Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
        if (names.contains(aComponent.getName())) {
            foundCount ++;
            found.add(aComponent);
            return foundCount < names.size(); // stop when all names where found
        }
        return super.visitLayoutComponent(aComponent);
    }
    
    /** Return the Collection of LayoutComponents found, will be same as given in CTor.*/
    public Collection getFound() {
        return found;
    }

    /** Return the number of Components found */
    public int getFoundCount() {
        return foundCount;
    }

}
