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
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.Padding;
import com.top_logic.graphic.flow.data.Text;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.TreeLayout;
import com.top_logic.graphic.flow.operations.tree.TreeLayoutOperations;

/**
 * Test case for {@link TreeLayoutOperations}.
 */
public class TestTreeLayout extends TestCase {

	public void testMiniTreeCompact() throws IOException {
		Diagram diagram = createMini(true);
		String svg = writeToFile(diagram, "./target/TestTreeLayout-mini-compact.svg");
		assertMini(svg);
	}

	public void testMiniTreeComfort() throws IOException {
		Diagram diagram = createMini(false);
		String svg = writeToFile(diagram, "./target/TestTreeLayout-mini-comfort.svg");
		assertMini(svg);
	}

	private Diagram createMini(boolean compact) {
		Box root;
		Box n1;
		Box n2;
		Box n3;
		Diagram diagram = Diagram.create().setRoot(Padding.create().setAll(20).setContent(
			TreeLayout.create()
				.setCompact(compact)
				.addNode(root = node("Root"))
				.addNode(n1 = node("N1"))
				.addNode(n2 = node("N2"))
				.addNode(n3 = node("N3"))
				.addConnection(TreeConnection.create()
					.setParent(connector(root))
					.addChildren(connector(n1))
					.addChildren(connector(n2))
					.addChildren(connector(n3)))));
		return diagram;
	}

	private void assertMini(String svg) {
		assertEquals(
			"""
			<?xml version="1.0" encoding="utf-8" ?>
			
			<svg
				xmlns="http://www.w3.org/2000/svg"
				height="172.0"
				version="1.1"
				viewBox="0.0 0.0 166.0 172.0"
				width="166.0"
			>
				<g transform="translate(20.0,20.0)">
					<text
						x="6.0"
						y="70.0"
					>Root</text>
					<path
						d="M 0.5,54.5 L 59.5,54.5 L 59.5,77.5 L 0.5,77.5 z"
						fill="none"
						stroke="black"
						stroke-width="1.0"
					/>
					<text
						x="96.0"
						y="16.0"
					>N1</text>
					<path
						d="M 90.5,0.5 L 125.5,0.5 L 125.5,23.5 L 90.5,23.5 z"
						fill="none"
						stroke="black"
						stroke-width="1.0"
					/>
					<text
						x="96.0"
						y="70.0"
					>N2</text>
					<path
						d="M 90.5,54.5 L 125.5,54.5 L 125.5,77.5 L 90.5,77.5 z"
						fill="none"
						stroke="black"
						stroke-width="1.0"
					/>
					<text
						x="96.0"
						y="124.0"
					>N3</text>
					<path
						d="M 90.5,108.5 L 125.5,108.5 L 125.5,131.5 L 90.5,131.5 z"
						fill="none"
						stroke="black"
						stroke-width="1.0"
					/>
					<path
						d="M 60.0,66.0 H 75.0 M 90.0,12.0 L 75.0,12.0 L 75.0,66.0 M 90.0,66.0 L 75.0,66.0 L 75.0,66.0 M 90.0,120.0 L 75.0,120.0 L 75.0,66.0"
						fill="none"
						stroke="black"
						stroke-width="1.0"
					/>
				</g>
			</svg>""", svg);
	}

	public void testRandomTree() throws IOException {
		Diagram diagramCompfort =
			Diagram.create().setRoot(Padding.create().setAll(20).setContent(createRandomTree().setCompact(false)));
		writeToFile(diagramCompfort, "./target/TestTreeLayout-random-compfort.svg");

		Diagram diagramCompact =
			Diagram.create().setRoot(Padding.create().setAll(20).setContent(createRandomTree().setCompact(true)));
		writeToFile(diagramCompact, "./target/TestTreeLayout-random-compact.svg");
	}

	private TreeLayout createRandomTree() {
		TreeLayout tree = TreeLayout.create();

		Random rnd = new Random(42);

		for (int n = 0; n < 3; n++) {
			Box root = randomNode(rnd);

			tree.addNode(root);
			buildTree(tree, 0, root, rnd);
		}
		return tree;
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

	private String writeToFile(Diagram diagram, String fileName) throws IOException {
		diagram.layout(new TestingRenderContext());

		TagWriter out = new TagWriter();
		SvgWriter svgOut = new SvgTagWriter(out);
		diagram.draw(svgOut);

		String svg = XMLPrettyPrinter.prettyPrint(out.toString());
		System.out.println(svg);

		try (FileWriter w = new FileWriter(new File(fileName), StandardCharsets.UTF_8)) {
			w.write(svg);
		}
		
		return svg;
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
