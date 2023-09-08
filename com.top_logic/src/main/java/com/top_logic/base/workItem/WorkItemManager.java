/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.workItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class WorkItemManager {
    
    private static final String CONF_KEY_PREFIX_PROVIDER = "provider:"; 
    
    private static WorkItemManager manager;
    
    private Collection workItemProvider;

    private WorkItemManager() {
        super();
        this.workItemProvider = new ArrayList();
		IterableConfiguration theConf = Configuration.getConfiguration(WorkItemManager.class);
        for (Iterator theIt = theConf.getNames().iterator(); theIt.hasNext(); ) {
            String theKey = (String) theIt.next();
            if (theKey.startsWith(CONF_KEY_PREFIX_PROVIDER)) {
                String theValue = theConf.getValue(theKey);
                if (!StringServices.isEmpty(theValue)) {
                    try {
                        Class            theClass    = Class.forName(theValue);
                        WorkItemProvider theProvider = (WorkItemProvider) theClass.newInstance();
                        this.workItemProvider.add(theProvider);
                    }
                    catch (Exception e) {
                        Logger.error("Invalid configuration for work item provider. Key: " + theKey + " Value: " + theValue, e, this);
                    }
                }
            }
        }
    }
    
    public static synchronized WorkItemManager getManager() {
        if (manager == null) {
            manager = new WorkItemManager();
        }
        return manager;
    }
    
    public Collection getWorkItems(final Person aPerson) {
        Collection theResult = new HashSet();
        for (Iterator theIt = this.workItemProvider.iterator(); theIt.hasNext();) {
            WorkItemProvider theProvider = (WorkItemProvider) theIt.next();
            theResult.addAll(theProvider.getWorkItems(aPerson));
        }
        return theResult;
    }

}
