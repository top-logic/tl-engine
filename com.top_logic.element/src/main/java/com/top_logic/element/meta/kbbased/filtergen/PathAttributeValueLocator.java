/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Locate a value following an attribute name path.
 * All values on the path must be Wrappers.
 * The path parts are separated by dots.
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class PathAttributeValueLocator extends CustomSingleSourceValueLocator {

	public static final String PATH_SEPARATOR = ".";
	
	protected String[] pathConfig;
	
	/** 
	 * Default CTor
	 */
	protected PathAttributeValueLocator() {
		super();
	}
	
	/** 
	 * Create a new PathAttributeValueLocator with attribute path config
	 * 
	 * @param aConfig the config
	 */
	public PathAttributeValueLocator(String aConfig) {
		super();
		this.pathConfig = this.getPath(aConfig);
	}
	
	protected String[] getPath(String aConfig) {
		String[] theConfig = StringServices.toArray(aConfig, PATH_SEPARATOR);
		if (StringServices.isEmpty(theConfig)) {
			throw new IllegalArgumentException("path config must not be empty");
		}
		
		return theConfig;
	}

	@Override
	public Object internalLocateAttributeValue(Object anObject) {
		{
            Wrapper theWrap   = (Wrapper) anObject;
    		int     theLength = this.pathConfig.length - 1;

            // Dereference all but the last path elements
    		for (int i = 0; i < theLength; i++) {
    			theWrap = (Wrapper) this.getValue(theWrap, this.pathConfig[i]);
    		}

    		if (theWrap == null) {
                return (null);
            }
            else {
                return (this.getValue(theWrap, this.pathConfig[theLength]));
            }
        }
	}
	
	/**
	 * Get the value from the Wrapper
	 * 
	 * @param aWrap		the Wrapper, must not be <code>null</code>
	 * @param aConfig	the attribute name, must not be <code>null</code>
	 * @return the value of the Wrapper for the attribute
	 */
	protected Object getValue(Wrapper aWrap, String aConfig) {
		return aWrap.getValue(aConfig);
	}
}
