/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

/**
 * {@link Exception} complaining about a node template being expanded in attribute context.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ElementInAttributeContextError extends Exception {

	/**
	 * Creates a {@link ElementInAttributeContextError}.
	 */
	public ElementInAttributeContextError() {
		super();
	}

}
