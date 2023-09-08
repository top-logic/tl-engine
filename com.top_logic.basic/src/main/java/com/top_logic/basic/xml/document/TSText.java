/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.document;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Thread safe immutable {@link Text} implementation
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TSText extends TSCharacterData implements Text {

	private final boolean isElementContentWhitespace;

	protected TSText(Text sourceNode, Node parent) {
		super(sourceNode, parent);
		this.isElementContentWhitespace = sourceNode.isElementContentWhitespace();
	}

	@Override
	public boolean isElementContentWhitespace() {
		return this.isElementContentWhitespace;
	}

	@Override
	public String getWholeText() {
		throw unsupportedOperation();
	}

	@Override
	public Text splitText(int aOffset) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public Text replaceWholeText(String aContent) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public String getNodeName() {
		return "#text";
	}

	@Override
	public String getNodeValue() throws DOMException {
		return getData();
	}

	@Override
	public short getNodeType() {
		return Node.TEXT_NODE;
	}
}
