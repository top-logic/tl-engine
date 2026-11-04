/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.util;

import java.util.Map;


/** 
 * This class fills a {@link com.top_logic.dob.DataObject} from a Map of attribute/value pairs.
 * 
 * @author   <a href="mailto:jza@top-logic.com">Jamel Zakraoui</a>
 */
public  class MapDataObjectFiller extends DataObjectFiller {

    /** The map used to feed the data object */
	protected Map<?, ?> map;

    /**
     * Create a new {@link MapDataObjectFiller} with given map.
     */
	public MapDataObjectFiller(Map<?, ?> aMap)
	{
		map = aMap;
	}

   /**
   	* returns the value of a key in the map. this value is needed by the caller
	* to set its attribute value. see {@link DataObjectFiller}
	* the map can contain other nested maps as attribute values
   	*/
    @Override
	public  Object getStringValue(String attr) 
	{
        return map.get(attr);
	}
		
  }
