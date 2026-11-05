/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Locates an attribute by following a path of attribute names.
 *
 * <p>
 * All but the last path part are followed as reference values starting from the given object.
 * The result is the attribute named by the last path part in the type of the reached object.
 * The path parts are separated by dots.
 * </p>
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class MetaAttributeValueLocator extends CustomSingleSourceValueLocator {

	/** Separator of the parts of the attribute path. */
	public static final String PATH_SEPARATOR = ".";
	
	/** The names of the attributes of the path. */
	protected String[] pathConfig;
	
	/** 
	 * Default CTor
	 */
	protected MetaAttributeValueLocator() {
		super();
	}
	
	/** 
	 * Create a new {@link MetaAttributeValueLocator} with attribute path config
	 * 
	 * @param aConfig the config
	 */
	public MetaAttributeValueLocator(String aConfig) {
		super();
		this.pathConfig = StringServices.toArray(aConfig, PATH_SEPARATOR);
		if (this.pathConfig == null || this.pathConfig.length == 0) {
			throw new IllegalArgumentException("path config must not be empty");
		}
	}

	@Override
	public Object internalLocateAttributeValue(Object anObject) {
		Wrapper theWrap = (Wrapper) anObject;
		
		// Derefence all but the last path elements
		for (int i=0; i<this.pathConfig.length-1; i++) {
			String theAttName = this.pathConfig[i];
			theWrap = (Wrapper) theWrap.getValue(theAttName);
		}
		
		TLStructuredTypePart part =
				((Wrapper) theWrap).tType().getPart(this.pathConfig[this.pathConfig.length - 1]);
		if (part == null) {
			Logger.error("Failed to get attribute " + this.pathConfig[this.pathConfig.length - 1], this);
		}
		return part;
	}
	
}
