/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

/**
 * Callback that is informed about all layout references found during a traversal.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ReferenceHandler {

	/**
	 * Callback method that is invoked for all layout references found during a layout
	 * traversal.
	 * 
	 * @param layout
	 *        Context, from which the reference was resolved.
	 */
	void handle(LayoutDefinition layout);

}