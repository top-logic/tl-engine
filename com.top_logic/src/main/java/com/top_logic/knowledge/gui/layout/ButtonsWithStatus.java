/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

/**
 * Classes shall implement this if they offer the
 * {@link #getStatusMessage()} for their button component with status
 * to display any kind of status message in the button component.
 *
 * @author   <a href="mailto:dkh@top-logic.com">Dirk K&ouml;hlhoff</a>
 */
public interface ButtonsWithStatus {

	/**
	 * deliver a status message (already translated and HTML-formatted) to be displayed 
	 * in the button component.
	 * 
	 * @return The status message.
	 */
	public String getStatusMessage();

}
