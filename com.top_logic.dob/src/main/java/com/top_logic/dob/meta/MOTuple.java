/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.List;

import com.top_logic.dob.MetaObject;

/**
 * A tuple type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MOTuple extends MetaObject {

	/**
	 * The types of the tuple entries.
	 */
	List<MetaObject> getEntryTypes();
	
}
