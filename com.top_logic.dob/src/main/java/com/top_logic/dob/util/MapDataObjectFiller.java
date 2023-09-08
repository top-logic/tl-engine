/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.util;

import java.util.Map;


/** 
 * This class fills a DataObject from a Map of attribute/value pairs.
 * 
 * @author   <a href="mailto:jza@top-logic.com">Jamel Zakraoui</a>
 */
public  class MapDataObjectFiller extends DataObjectFiller {

    /** The map used to feed the dataObject */
	protected Map<?, ?> map;

    /**
     * Create a new MapDataObjectFiller with given Map aMap.
     */
	public MapDataObjectFiller(Map<?, ?> aMap)
	{
		map = aMap;
	}

   /**
   	* returns the value of a key in the map. this value is needed by the caller
	* to set its attribute value. see DataObjectFiller
	* the map can contain other nested maps as attribute values
   	*/
    @Override
	public  Object getStringValue(String attr) 
	{
        return map.get(attr);
	}
		
  }
