/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.config.xdiff.ms;

import static com.top_logic.config.xdiff.util.Utils.*;
import static test.com.top_logic.config.xdiff.TestingUtil.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.config.xdiff.model.AbstractNodeVisitor;
import com.top_logic.config.xdiff.model.Document;
import com.top_logic.config.xdiff.model.FragmentBase;
import com.top_logic.config.xdiff.model.Node;
import com.top_logic.config.xdiff.ms.DocumentPosition;
import com.top_logic.config.xdiff.ms.DocumentPosition.ChildrenTraversal;

/**
 * Test case for {@link DocumentPosition}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDocumentPosition extends TestCase {

	public void testNodeIdentification() {
		Document d1 = fixture(""
			+ "<a>"
			+ "text" + "<!--comment-->" + "text" + "<!--comment-->"
			+ "<b>"
			+ "<c/>"
			+ "<!--comment-->"
			+ "<!--comment-->"
			+ "<c/>"
			+ "</b>"
			+ "<b/>"
			+ "</a>");

		TestingTraversal traversal = new TestingTraversal();
		d1.visit(traversal, null);

		assertEquals(
			list(
				"/",
				"/a[1]",
				"/a[1]/text()[1]",
				"/a[1]/comment()[1]",
				"/a[1]/text()[2]",
				"/a[1]/comment()[2]",
				"/a[1]/b[1]",
				"/a[1]/b[1]/c[1]",
				"/a[1]/b[1]/c[1]/following-sibling::comment()[1]",
				"/a[1]/b[1]/c[1]/following-sibling::comment()[2]",
				"/a[1]/b[1]/c[2]",
				"/a[1]/b[2]"),
			traversal.getAllNodePaths());

	}

	private static class TestingTraversal extends AbstractNodeVisitor<Void, Void> {
		private final List<String> _allNodePaths;

		protected final DocumentPosition _position = new DocumentPosition();

		public TestingTraversal() {
			_allNodePaths = new ArrayList<>();
		}

		@Override
		protected Void visitFragmentBase(FragmentBase node, Void arg) {
			Void result = super.visitFragmentBase(node, arg);

			ChildrenTraversal traversal = _position.childrenTraversal();

			for (Node child : node.getChildren()) {
				traversal.traverseChild(child);

				child.visit(this, arg);
			}

			traversal.stop();

			return result;
		}

		@Override
		protected Void visitNode(Node node, Void arg) {
			String xpath = _position.getCurrentPath().toString();
			getAllNodePaths().add(xpath);
			return null;
		}

		public List<String> getAllNodePaths() {
			return _allNodePaths;
		}
	}


}
