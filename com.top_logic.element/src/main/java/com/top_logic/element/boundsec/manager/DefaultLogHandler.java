/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.io.BasicFileLog;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;

/**
 * Default {@link LogHandler} implementation.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class DefaultLogHandler implements LogHandler {

    private static final String SECURITY_UPDATE = "securityUpdate";

    /**
     * @see com.top_logic.element.boundsec.manager.LogHandler#logSecurityUpdate(java.util.Map, java.util.Map, java.util.Map, java.util.Set)
     */
    @Override
	public void logSecurityUpdate(Map someNew, Map someRemoved, Map aRulesToObjectsMap,
            Set anInvalidObjects) {
        
        BasicFileLog theLog = BasicFileLog.getInstance();
        long start = System.currentTimeMillis();
        theLog.appendIntoLogFile(SECURITY_UPDATE, "=== Start: "+(new Date(start)).toString() + "\n");
        theLog.appendIntoLogFile(SECURITY_UPDATE, "- new: "+someNew.size() + "\n");    
        theLog.appendIntoLogFile(SECURITY_UPDATE, "- removed: "+someRemoved.size() + "\n");    
        theLog.appendIntoLogFile(SECURITY_UPDATE, "- rules: "+aRulesToObjectsMap.size() + "\n");  
        for (Iterator theIt = aRulesToObjectsMap.entrySet().iterator(); theIt.hasNext();) {
            Map.Entry  theEntry   = (Map.Entry) theIt.next();
            RoleProvider   theRule    = (RoleProvider) theEntry.getKey();
            Collection theObjects = (Collection) theEntry.getValue();
            theLog.appendIntoLogFile(SECURITY_UPDATE, "-- rule: "+theRule.getId()+" => "+theObjects.size() + "\n"); 
            
        }
        theLog.appendIntoLogFile(SECURITY_UPDATE, "- invalidated: "+aRulesToObjectsMap.size() + "\n");
        long end = System.currentTimeMillis();
        theLog.appendIntoLogFile(SECURITY_UPDATE, "=== time: "+ (end-start)  + "ms\n");
    }

}

