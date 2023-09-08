/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * Possible processing kinds.
 * 
 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public enum ProcessingKind {
	/**
	 * user triggered command of a component
	 */
	COMMAND_EXECUTION,

	/**
	 * technical command (e.g. open tree node)
	 */
	TECHNICAL_COMMAND_EXECUTION,

	/**
	 * rendering of a component (esp. via JSP)
	 */
	COMPONENT_RENDERING,

	/**
	 * JSP rendering without component context
	 */
	JSP_RENDERING;
}