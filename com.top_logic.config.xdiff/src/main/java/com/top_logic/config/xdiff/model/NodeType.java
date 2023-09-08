/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

/**
 * Enumeration of types of concrete {@link Node}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum NodeType {

	/**
	 * A {@link Document}.
	 */
	DOCUMENT,

	/**
	 * A {@link Fragment}.
	 */
	FRAGMENT,

	/**
	 * An {@link Element}.
	 */
	ELEMENT,

	/**
	 * A {@link Text}.
	 */
	TEXT,

	/**
	 * A {@link Comment}.
	 */
	COMMENT,

	/**
	 * An {@link EntityReference}.
	 */
	ENTITY_REFERENCE;
	
}
