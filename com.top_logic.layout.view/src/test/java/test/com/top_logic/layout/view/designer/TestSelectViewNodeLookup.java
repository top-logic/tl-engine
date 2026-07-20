/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.designer;

import junit.framework.TestCase;

import com.top_logic.layout.view.designer.DesignTreeNode;
import com.top_logic.layout.view.designer.SelectViewCommand;

/**
 * Tests {@link SelectViewCommand#findViewRoot(DesignTreeNode, String)}.
 */
public class TestSelectViewNodeLookup extends TestCase {

	private static final class Node extends DesignTreeNode {
		Node(String sourceFile) {
			super(sourceFile);
		}

		@Override
		public String getTagName() {
			return "test";
		}
	}

	private static Node child(Node parent, String sourceFile) {
		Node node = new Node(sourceFile);
		parent.getChildren().add(node);
		return node;
	}

	public void testFindsBoundaryNodeForReferencedFile() {
		String rootFile = "/WEB-INF/views/app.view.xml";
		String refFile = "/WEB-INF/views/customers/detail.view.xml";

		Node root = new Node(rootFile);
		Node panel = child(root, rootFile);
		Node refRoot = child(panel, refFile);   // view-ref boundary
		child(refRoot, refFile);                 // deeper node in the referenced file

		assertSame("Root file resolves to the tree root", root,
			SelectViewCommand.findViewRoot(root, rootFile));
		assertSame("Referenced file resolves to its boundary node", refRoot,
			SelectViewCommand.findViewRoot(root, refFile));
		assertNull("Unknown file resolves to null",
			SelectViewCommand.findViewRoot(root, "/WEB-INF/views/missing.view.xml"));
	}
}
