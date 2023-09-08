/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;


/**
 * Exception indicating a timeout during a {@link KnowledgeBase#refetch()} operation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RefetchTimeout extends Exception {

	/**
	 * Creates a {@link RefetchTimeout}.
	 */
	public RefetchTimeout() {
		super();
	}

	/**
	 * Creates a {@link RefetchTimeout}.
	 * 
	 * @param message
	 *        See {@link #getMessage()}.
	 */
	public RefetchTimeout(String message) {
		super(message);
	}

	
}
