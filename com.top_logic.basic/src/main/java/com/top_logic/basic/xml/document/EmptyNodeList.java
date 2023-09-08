/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.document;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link NodeList} without elements.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EmptyNodeList implements NodeList {
	
	/**
	 * Singleton {@link EmptyNodeList} instance.
	 */
	public static final EmptyNodeList INSTANCE = new EmptyNodeList();

	private EmptyNodeList() {
		// singleton instance
	}

	@Override
	public Node item(int index) {
		return null;
	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public String toString() {
		return "[]";
	}

}

