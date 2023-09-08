/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;

/**
 * Use this to dump/check extra information about your MainLayout.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class DebugLayoutResolvedListener extends LayoutResolvedListener {

    /** remember here all the names of components. */
    private Set componentNameSet;
    
    /** a Map 
     * fom the layout component types (String) 
     * to  the number of instances of this type (int[], used to allow the int primitives in the map)
     */ 
    private Map componentTypeCount;
        
    /** 
     * Creates a {@link DebugLayoutResolvedListener}.
     * 
     */
    public DebugLayoutResolvedListener() {
        componentNameSet   = new HashSet(256);
        componentTypeCount = new HashMap(64);
    }
    
    /**
     * Process the layout whith the given component as root.
     * 
     * @param aRootLayout  the root of the layout tree
     */
    @Override
	public void process(LayoutComponent aRootLayout) {
        Logger.info("Start postprocessing (" + DateUtil.getIso8601DateFormat().format(new Date()) + ").", this);
        
        super.process(aRootLayout);
        
        Logger.info(this.componentNameSet.size() + " LayoutComponents created.", this);
        this.componentNameSet = null;
        
        printTypeCount();
        this.componentTypeCount = null;
        
        Logger.info("End postprocessing (" + DateUtil.getIso8601DateFormat().format(new Date()) + ").", this);
    }
    
    /**
     * Handle the given component
     *
     * @see com.top_logic.mig.html.layout.LayoutComponentVisitor#visitLayoutComponent(com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
        assertComponentName(aComponent);
        countType(aComponent);

        return super.visitLayoutComponent(aComponent);
    }
    
    /**
     * Update the componentTypeCount for the given component.
     * 
     * @param aComponent the current component to handle
     */
    protected void countType(LayoutComponent aComponent) {

        if(this.componentTypeCount == null) {
            this.componentTypeCount = new HashMap();
        }
        
        String theType = aComponent.getClass().getName();
        int[] theCount = (int[]) this.componentTypeCount.get(theType);
        if (theCount == null) {
            theCount = new int[] { 0 };
            this.componentTypeCount.put(theType, theCount);
        }
        theCount[0]++;
        
    }
    
    /**
     * Tests if the given component name is unique.
     * 
     * @param aComponent the component to test.
     */
    protected void assertComponentName(LayoutComponent aComponent) {

        if(this.componentNameSet == null) {
            this.componentNameSet = new HashSet();
        }
        
		ComponentName theName = aComponent.getName();
        if(theName == null) {
            Logger.error("Found component without name!", aComponent);
            return;
        }
        
        if(this.componentNameSet.contains(theName)) {
            Logger.error("a component name is not unique: " + theName, aComponent);
        }
        else {
            this.componentNameSet.add(theName);
        }
    }
    
    /**
     * Print the count of all component types to Logger.info
     */
    protected void printTypeCount() {
        Logger.info("Used Components:", this);
        Iterator theIt = (new TreeSet(this.componentTypeCount.keySet())).iterator();
        while (theIt.hasNext()) {
            String theKey = (String) theIt.next();
            int theCount = ((int[]) this.componentTypeCount.get(theKey))[0];
            Logger.debug("    "+theKey+": "+theCount, this);
        }
    }

}

