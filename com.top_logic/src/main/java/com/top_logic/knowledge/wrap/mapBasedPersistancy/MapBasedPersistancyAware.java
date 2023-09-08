/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.mapBasedPersistancy;

import java.util.Map;

import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * An object, that can provide its current state information in form of a value map and that can
 * resore its state from such a map.
 * 
 * The map must have the following form: Key: of type String Value: of a simple type that can be
 * stored in a flex attribute (see {@link AbstractBoundWrapper})
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface MapBasedPersistancyAware {

    /** Separator for Map key parts. */
    public static final String KEY_SEPARATOR = "_";

    /** key for value Map to store filter class. */
    public static final String KEY_CLASS = "class";

	/**
	 * Initialize the object with the given data
	 * 
	 * @param someData   see class description
	 */
	public void setUpFromValueMap(Map someData);
	
	/**
	 * Get the current state information in map form
	 * 
	 * @return  see class description
	 */
	public Map<String, Object> getValueMap();
}
