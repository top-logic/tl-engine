/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.graphic.flow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.graphic.blocks.server.svg.SvgTagWriter;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Border;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.Padding;
import com.top_logic.graphic.flow.data.Text;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.TreeLayout;
import com.top_logic.graphic.flow.model.FlowDiagram;
import com.top_logic.graphic.flow.model.tree.TreeLayoutOperations;

/**
 * Test case for {@link TreeLayoutOperations}.
 */
public class TestTreeLayout extends TestCase {

	public void testMiniTree() throws IOException {
		Box root;
		Box n1;
		Box n2;
		Box n3;
		FlowDiagram diagram = new FlowDiagram().setRoot(TreeLayout.create()
			.addNode(root = node("Root"))
			.addNode(n1 = node("N1"))
			.addNode(n2 = node("N2"))
			.addNode(n3 = node("N3"))
			.addConnection(TreeConnection.create()
				.setParent(connector(root))
				.addChildren(connector(n1))
				.addChildren(connector(n2))
				.addChildren(connector(n3))));

		writeToFile(diagram, "./target/TestTreeLayout-mini.svg");
	}

	public void testRandomTree() throws IOException {
		TreeLayout tree = TreeLayout.create();

		Random rnd = new Random(42);

		for (int n = 0; n < 3; n++) {
			Box root = randomNode(rnd);

			tree.addNode(root);
			buildTree(tree, 0, root, rnd);
		}

		FlowDiagram diagram = new FlowDiagram().setRoot(tree);

		writeToFile(diagram, "./target/TestTreeLayout-random.svg");
	}

	private Box randomNode(Random rnd) {
		return node("N " + "x".repeat(rnd.nextInt(1, 10)));
	}

	private void buildTree(TreeLayout tree, int level, Box parent, Random rnd) {
		if (rnd.nextDouble() > 0.2 + level * 0.1) {
			int cnt = rnd.nextInt(1, 6);

			TreeConnection connection = TreeConnection.create().setParent(connector(parent));
			for (int n = 0; n < cnt; n++) {
				Box child = randomNode(rnd);
				tree.addNode(child);

				connection.addChildren(connector(child));

				buildTree(tree, level + 1, child, rnd);
			}

			tree.addConnection(connection);
		}
	}

	private void writeToFile(FlowDiagram diagram, String fileName) throws IOException {
		diagram.layout(new TestingRenderContext());

		TagWriter out = new TagWriter();
		SvgWriter svgOut = new SvgTagWriter(out);
		diagram.draw(svgOut);

		String svg = XMLPrettyPrinter.prettyPrint(out.toString());
		System.out.println(svg);

		try (FileWriter w = new FileWriter(new File(fileName), StandardCharsets.UTF_8)) {
			w.write(svg);
		}
	}

	private TreeConnector connector(Box root) {
		return TreeConnector.create().setAnchor(root).setConnectPosition(0.5);
	}

	private Box node(String name) {
		return Border.create().setContent(
			Padding.create().setAll(5).setContent(
				Text.create().setValue(name)));
	}
}
