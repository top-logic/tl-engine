/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.document;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Superclass for {@link TSNode} which are not of type {@link Node#ELEMENT_NODE}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class NonElementNode extends TSNode {

	public NonElementNode(Node sourceNode, Node parent) {
		super(sourceNode, parent);
	}

	@Override
	public NamedNodeMap getAttributes() {
		return null;
	}

	@Override
	public boolean hasAttributes() {
		return false;
	}

}
