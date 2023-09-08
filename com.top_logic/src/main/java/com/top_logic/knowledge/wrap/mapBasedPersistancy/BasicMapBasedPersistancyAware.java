/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.mapBasedPersistancy;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the base class for map based persistancy aware classes.
 * The key functionallity provided here is sthe handling of the class information.
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public abstract class BasicMapBasedPersistancyAware implements MapBasedPersistancyAware {
	
	/**
	 * Add the filter class.
	 * 
     * See com.top_logic.element.meta.query.CollectionFilter#getValueMap()
     */
    @Override
	public Map<String, Object> getValueMap() {
        Map theMap = new HashMap();
        theMap.put (KEY_CLASS , this.getClass().getName());
        
        return theMap;
    }

}
