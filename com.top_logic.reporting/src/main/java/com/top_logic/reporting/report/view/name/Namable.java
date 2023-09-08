/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.name;

/**
 * A {@link Namable} has a name that can be shown to the gui.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface Namable {

	/**
	 * This method returns the name of the object that can be shown to the gui.
	 * Null or an empty string is permitted.
	 * 
	 * @return Returns the name of the object that can be shown to the gui.
	 */
	public String getName();
	
}
