/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.List;


/**
 * A transformer for objects, which shall be serialized by JSON.
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public interface JSONTransformer {
	
	/**
	 * This method transforms raw objects into JSON-serializable objects
	 * 
	 * @param rawObjects - list with original objects
	 * @return list with JSON-serializable objects
	 */
	public List<Object> transformToJSON(Object... rawObjects);
	
	/**
	 * This method transforms JSON-serializable objects of the list into the original raw objects.
	 * 
	 * @param jsonObjects - list with JSON-serializable objects
	 * @return list with original objects
	 */
	public List<Object> transformFromJSON(List<Object> jsonObjects);

}
