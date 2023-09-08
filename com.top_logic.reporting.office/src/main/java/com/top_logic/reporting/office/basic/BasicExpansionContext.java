/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office.basic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.reporting.office.ExpansionContext;


/**
 * Most basic 
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class BasicExpansionContext implements ExpansionContext {
    
    private Locale locale;
    private Map businessObjects;
    
    public BasicExpansionContext (Map environmentMap) {
        init (environmentMap);
    }
    protected void init (Map environmentMap) {
        // first the language:
        if (environmentMap.containsKey("locale")) {
            Object theLocale = environmentMap.remove("locale");
            if (theLocale instanceof String) {
                locale = ResourcesModule.localeFromString((String)theLocale);
            }
            if (theLocale instanceof Locale) {
                locale = (Locale)theLocale;
            }
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        
        // the rest of the object in the map are the relevant business objects so
        // simply remember them :)
        businessObjects = new HashMap (environmentMap);
    }
    
    @Override
	public Locale getReportLocale (){
        return locale;
    }
    /**
     * @see com.top_logic.reporting.office.ExpansionContext#getBusinessObjects()
     */
    @Override
	public Map getBusinessObjects () {
        return businessObjects;
    }
    
}
