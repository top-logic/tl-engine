/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.upload;

import com.top_logic.basic.util.ResKey;

/**
 * Strategy to check files name of uploaded files.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FileNameStrategy {
   
	/**
	 * Check whether the given file name is allowed.
	 * 
	 * @param fileName
	 *        the name to be tested.
	 * 
	 * @return The error message key that describes the problem, or <code>null</code> for an allowed
	 *         name.
	 */ 
    public ResKey checkFileName(String fileName);

}
