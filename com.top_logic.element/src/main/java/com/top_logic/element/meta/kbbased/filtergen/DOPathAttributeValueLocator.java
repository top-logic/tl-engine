/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.data.DOList;

/**
 * Get DAP config from default value on the Mandator of the Contract
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class DOPathAttributeValueLocator extends CustomSingleSourceValueLocator {

	public static final String PATH_SEPARATOR = ".";
	
	protected String[] pathConfig;
	
	/** 
	 * Default CTor
	 */
	protected DOPathAttributeValueLocator() {
		super();
	}
	
	/** 
	 * Create a new DOPathAttributeValueLocator with attribute path config
	 * 
	 * @param aConfig the config
	 */
	public DOPathAttributeValueLocator(String aConfig) {
		super();
		this.pathConfig = StringServices.toArray(aConfig, PATH_SEPARATOR);
		if (this.pathConfig == null || this.pathConfig.length == 0) {
			throw new IllegalArgumentException("path config must not be empty");
		}
	}

	@Override
	public Object internalLocateAttributeValue(Object anObject) {
		try {
			DataObject theDO = (DataObject) anObject;
			
			// Derefence all but the last path elements
			for (int i=0; i<this.pathConfig.length-1; i++) {
				String theAttName = this.pathConfig[i];
				// Check for DOList
				int thePos = theAttName.indexOf('[');
				if (thePos >= 0 ) {
					String theAttName2 = theAttName.substring(0, thePos);
					int thePos2 = theAttName.indexOf(']', thePos + 1);
					int theIndex = Integer.parseInt(theAttName.substring(thePos + 1, thePos2));
					
					DOList theList = (i==0 && thePos == 0) ? (DOList) theDO : (DOList) ((DataObject) theDO.getAttributeValue(theAttName2));
					theDO = (DataObject) theList.get(theIndex);
				}
				else {
					theDO = (DataObject) theDO.getAttributeValue(theAttName);
				}
			}
			
			return theDO.getAttributeValue(this.pathConfig[this.pathConfig.length-1]);
		}
		catch(Exception ex) {
			String theMsg = "";
			Logger.error(theMsg, ex, this);
			throw new IllegalArgumentException("Failed to locate attribute value");
		}
	}
	
}
