/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

/**
 * {@link Exception} that signals that the maximal number of request has trespassed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MaxRequestNumberException extends Exception {
	
	public MaxRequestNumberException(String reason) {
		super(reason);
	}

}

