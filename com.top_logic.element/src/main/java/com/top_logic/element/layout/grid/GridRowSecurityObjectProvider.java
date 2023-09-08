/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.tool.boundsec.BoundObject;

/**
 * Gets the object, on which the security shall be checked, whether a row can be edited.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public interface GridRowSecurityObjectProvider {

	/**
	 * Gets the object, on which the security shall be checked, whether a row can be edited.
	 *
	 * @param component
	 *        the grid component which checks the editability of a row.
	 * @param rowObject
	 *        the row object which shall be edited
	 * @return the object which shall be used for security checks; may be the rowObject itself and may be
	 *         <code>null</code>, which indicates that no security check is required.
	 */
	public BoundObject getSecurityObject(GridComponent component, Object rowObject);

}
