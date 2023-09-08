/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.xpath;

import com.top_logic.config.xdiff.model.NodeType;

/**
 * Base class for XPath node tests.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class NodeTest extends XPathNode {

	/**
	 * The {@link NodeType} that is tested for.
	 */
	public abstract NodeType getType();

}