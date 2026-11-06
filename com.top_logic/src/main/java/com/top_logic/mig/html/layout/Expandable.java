/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

/**
 * Object that can be displayed expanded or collapsed.
 *
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public interface Expandable {

	/**
	 * Whether this object is currently displayed expanded.
	 */
	public boolean isExpanded();
	
}
