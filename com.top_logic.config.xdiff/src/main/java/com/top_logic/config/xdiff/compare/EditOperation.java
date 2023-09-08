/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.compare;

/**
 * Description what happens with a node in a node list when transforming it from the source to the
 * destination document.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum EditOperation {

	/**
	 * Node is deleted.
	 */
	DELETE,

	/**
	 * Node is inserted.
	 */
	INSERT,

	/**
	 * Node is internally transformed.
	 */
	MATCH;
}
