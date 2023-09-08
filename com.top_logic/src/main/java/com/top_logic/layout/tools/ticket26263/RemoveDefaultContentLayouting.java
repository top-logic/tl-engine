/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.ticket26263;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.layout.tools.XPathFileTransformer;

/**
 * Removes the default content layouting value for XML layouts.
 * 
 * @see "Ant task z_remove_default_content_layouting"
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class RemoveDefaultContentLayouting extends XPathFileTransformer {

	/**
	 * Creating a {@link RemoveDefaultContentLayouting} instance.
	 */
	public RemoveDefaultContentLayouting() throws XPathExpressionException {
		super();
	}

	/**
	 * Entry-point of the {@link RemoveDefaultContentLayouting} tool.
	 */
	public static void main(String[] args) throws Exception {
		new RemoveDefaultContentLayouting().runMainCommandLine(args);
	}

	@Override
	protected void transform(NodeList nodes) {
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			node.getParentNode().removeChild(node);
		}
	}

	@Override
	protected String getXPathExpression() {
		return "//content-layouting[@class='com.top_logic.layout.structure.ContentLayouting' and ./contextMenuFactory[@class='com.top_logic.layout.basic.contextmenu.component.factory.PlainComponentContextMenuFactory' and ./customCommands[@class='com.top_logic.layout.basic.contextmenu.config.NoContextMenuCommands']]]";
	}
}
