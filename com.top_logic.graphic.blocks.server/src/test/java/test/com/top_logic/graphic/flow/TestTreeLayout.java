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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.graphic.blocks.server.svg.SvgTagWriter;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Align;
import com.top_logic.graphic.flow.data.Alignment;
import com.top_logic.graphic.flow.data.Border;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.EdgeDecoration;
import com.top_logic.graphic.flow.data.Padding;
import com.top_logic.graphic.flow.data.Text;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.TreeLayout;
import com.top_logic.graphic.flow.data.VerticalLayout;
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
					.setChild(connector(n1)))
				.addConnection(TreeConnection.create()
					.setParent(connector(root))
					.setChild(connector(n2)))
				.addConnection(TreeConnection.create()
					.setParent(connector(root))
					.setChild(connector(n3)))));
		return diagram;
	}

	private void assertMini(String svg) {
		assertEquals(
			"""
			<?xml version="1.0" encoding="utf-8" ?>
			
			<svg
				xmlns="http://www.w3.org/2000/svg"
				height="100%"
				version="1.1"
				viewBox="0.0 0.0 176.0 152.0"
				width="100%"
			>
				<g transform="translate(20.0,20.0)">
					<g transform="translate(0.0,0.0)">
						<text
							x="6.0"
							y="16.0"
						>Root</text>
						<path
							d="M 0.5,0.5 L 59.5,0.5 L 59.5,23.5 L 0.5,23.5 z"
							fill="none"
							stroke="black"
							stroke-width="1.0"
						/>
					</g>
					<g transform="translate(100.0,0.0)">
						<text
							x="6.0"
							y="16.0"
						>N1</text>
						<path
							d="M 0.5,0.5 L 35.5,0.5 L 35.5,23.5 L 0.5,23.5 z"
							fill="none"
							stroke="black"
							stroke-width="1.0"
						/>
					</g>
					<g transform="translate(100.0,44.0)">
						<text
							x="6.0"
							y="16.0"
						>N2</text>
						<path
							d="M 0.5,0.5 L 35.5,0.5 L 35.5,23.5 L 0.5,23.5 z"
							fill="none"
							stroke="black"
							stroke-width="1.0"
						/>
					</g>
					<g transform="translate(100.0,88.0)">
						<text
							x="6.0"
							y="16.0"
						>N3</text>
						<path
							d="M 0.5,0.5 L 35.5,0.5 L 35.5,23.5 L 0.5,23.5 z"
							fill="none"
							stroke="black"
							stroke-width="1.0"
						/>
					</g>
					<path
						d="M 100.0,12.0 H 80.0 V 12.0 H 60.0"
						fill="none"
						stroke="black"
						stroke-width="1.0"
					/>
					<path
						d="M 100.0,56.0 H 80.0 V 12.0 H 60.0"
						fill="none"
						stroke="black"
						stroke-width="1.0"
					/>
					<path
						d="M 100.0,100.0 H 80.0 V 12.0 H 60.0"
						fill="none"
						stroke="black"
						stroke-width="1.0"
					/>
				</g>
			</svg>""", svg);
	}

	public void testDecorations() throws IOException {
		// One root with three children; each connection carries a label decoration. The labels
		// vary in width so the column gap must accommodate the widest one.
		TreeLayout tree = TreeLayout.create();

		Box root = node("Root");
		tree.addNode(root);

		String[] labels = { "short", "medium label", "very wide label here" };
		for (int i = 0; i < labels.length; i++) {
			Box child = node("N" + (i + 1));
			tree.addNode(child);
			tree.addConnection(TreeConnection.create()
				.setParent(connector(root))
				.setChild(connector(child))
				.addDecoration(EdgeDecoration.create().setContent(node(labels[i]))));
		}

		Diagram diagram = Diagram.create().setRoot(Padding.create().setAll(20).setContent(tree));
		writeToFile(diagram, "./target/TestTreeLayout-decorations.svg");
	}

	public void testGridFanout() throws IOException {
		// 12 children directly under one root; with childSplitThreshold=4 this triggers a 4x3
		// column-wise grid. Some children carry their own small subtrees so the column widths are
		// non-uniform.
		Diagram diagram = Diagram.create().setRoot(Padding.create().setAll(20).setContent(
			buildGridFanoutTree(false)));
		writeToFile(diagram, "./target/TestTreeLayout-grid-fanout.svg");
	}

	public void testGridFanoutRowWise() throws IOException {
		// 12 children directly under one root; childSplitThreshold=3 with rowWise=true splits the
		// children row-major over 3 sub-columns and routes all subtrees into a single post-grid
		// column behind one shared bus.
		Diagram diagram = Diagram.create().setRoot(Padding.create().setAll(20).setContent(
			buildGridFanoutTree(true)));
		writeToFile(diagram, "./target/TestTreeLayout-grid-fanout-rowwise.svg");
	}

	public void testGridFanoutRowWiseStartCol() throws IOException {
		// Same tree as testGridFanoutRowWise but with subGridStartCol=1 so child 0 lands in
		// sub-column 1 (not 0). Verifies that the offset propagates through colW computation,
		// child placement, and all Y-stack constraints.
		TreeLayout tree = TreeLayout.create()
			.setChildSplitThreshold(3)
			.setRowWise(true)
			.setSubGridStartCol(1);

		Box root = node("Root");
		tree.addNode(root);
		for (int i = 1; i <= 12; i++) {
			Box child = node("C" + i);
			tree.addNode(child);
			tree.addConnection(TreeConnection.create()
				.setParent(connector(root))
				.setChild(connector(child)));
		}

		Diagram diagram = Diagram.create().setRoot(Padding.create().setAll(20).setContent(tree));
		writeToFile(diagram, "./target/TestTreeLayout-grid-fanout-rowwise-startcol.svg");
	}

	public void testGridFanoutRowWiseMixedTallNodeWithSubtree() throws IOException {
		// 6 sub-grid children in 2 cols × 3 rows. Every sub-grid child has its own subtree (so
		// stems go from each to childBusX). One sub-grid child (C4 in col 1, row 1) has a tall
		// box (long label above the anchor). The tall child is placed AFTER C3 (col 0, row 1)
		// which has a subtree: C3's stem to its post-grid descendants crosses col 1 at
		// C3.anchorMidY, and that Y must not land inside C4's label region. Verifies that the
		// row-wise adaptive Y constraint propagates not only the parent → child stub Y to
		// earlier columns but also the child → subtree stem Y to later columns when the child
		// is subtree-bearing.
		TreeLayout tree = TreeLayout.create()
			.setChildSplitThreshold(2)
			.setRowWise(true)
			.setSubGridCols(2);

		LabelAnchorNode root = labelAnchorNode("root");
		tree.addNode(root.box);

		for (int i = 1; i <= 6; i++) {
			// C4 is the tall one (multi-line label above its anchor); the others are gear-only.
			LabelAnchorNode child = (i == 4) ? tallLabelAnchorNode("C" + i) : gearOnlyNode("C" + i);
			tree.addNode(child.box);
			tree.addConnection(TreeConnection.create()
				.setParent(TreeConnector.create().setAnchor(root.anchor).setConnectPosition(0.5))
				.setChild(TreeConnector.create().setAnchor(child.anchor).setConnectPosition(0.5)));

			// Each sub-grid child gets one post-grid descendant so a stem from this child to
			// childBusX is rendered. The descendant nodes are simple gear-only boxes.
			Box grand = node("D" + i);
			tree.addNode(grand);
			tree.addConnection(TreeConnection.create()
				.setParent(TreeConnector.create().setAnchor(child.anchor).setConnectPosition(0.5))
				.setChild(TreeConnector.create().setAnchor(grand).setConnectPosition(0.5)));
		}

		Diagram diagram = Diagram.create().setRoot(Padding.create().setAll(20).setContent(tree));
		writeToFile(diagram, "./target/TestTreeLayout-grid-fanout-rowwise-tall-with-subtree.svg");
	}

	public void testGridFanoutRowWiseLabelAboveAnchor() throws IOException {
		// Each "node" consists of a text label stacked above a border-wrapped content box (the
		// gear icon stand-in). The TreeLayout's node is the whole VerticalLayout; the connector's
		// anchor is the inner Border (the lower part). This means anchor != box: the bus stub
		// hits the lower part, but the box (label + border) extends higher than the anchor.
		// Verifies that:
		// (1) the adaptive Y-step in row-wise mode uses the anchor mid-Y (not the box mid-Y) for
		//     stub clearance, so the label of a future child does not collide with a past
		//     child's stub coming from a higher sub-column.
		// (2) the parent → main-bus segment starts at the parent's ANCHOR right edge, not the
		//     full box right edge.
		TreeLayout tree = TreeLayout.create()
			.setChildSplitThreshold(2)
			.setRowWise(true)
			.setSubGridCols(2);

		LabelAnchorNode rootNode = labelAnchorNode("Root");
		tree.addNode(rootNode.box);

		for (int i = 1; i <= 10; i++) {
			LabelAnchorNode child = labelAnchorNode("C" + i);
			tree.addNode(child.box);
			tree.addConnection(TreeConnection.create()
				.setParent(TreeConnector.create().setAnchor(rootNode.anchor).setConnectPosition(0.5))
				.setChild(TreeConnector.create().setAnchor(child.anchor).setConnectPosition(0.5)));
		}

		Diagram diagram = Diagram.create().setRoot(Padding.create().setAll(20).setContent(tree));
		writeToFile(diagram, "./target/TestTreeLayout-grid-fanout-rowwise-label-above-anchor.svg");
	}

	public void testGridFanoutRowWiseVaryingHeight() throws IOException {
		// Same row-wise grid as testGridFanoutRowWise, but sub-grid children have different
		// box heights. Used to verify per-child heights are honored in the adaptive Y-step
		// constraints (no box overlap, stub clearance maintained).
		TreeLayout tree = TreeLayout.create()
			.setChildSplitThreshold(3)
			.setRowWise(true);

		Box root = node("Root");
		tree.addNode(root);

		for (int i = 1; i <= 12; i++) {
			// Some children are tall (more vertical padding), others are normal.
			boolean tall = (i == 2 || i == 5 || i == 8);
			Box child = tall ? tallNode("C" + i) : node("C" + i);
			tree.addNode(child);
			tree.addConnection(TreeConnection.create()
				.setParent(connector(root))
				.setChild(connector(child)));
		}

		Diagram diagram = Diagram.create().setRoot(Padding.create().setAll(20).setContent(tree));
		writeToFile(diagram, "./target/TestTreeLayout-grid-fanout-rowwise-varying-height.svg");
	}

	private TreeLayout buildGridFanoutTree(boolean rowWise) {
		TreeLayout tree = TreeLayout.create()
			.setChildSplitThreshold(rowWise ? 3 : 4)
			.setRowWise(rowWise)
			.setBridgeGapY(20);

		Box root = node("Root");
		tree.addNode(root);

		for (int i = 1; i <= 12; i++) {
			Box child = node("C" + i);
			tree.addNode(child);
			tree.addConnection(TreeConnection.create()
				.setParent(connector(root))
				.setChild(connector(child)));

			// Some children get two grand-children so we exercise the bbox-based column widths.
			if (i == 1 || i == 5 || i == 9) {
				Box ga = node("C" + i + "a");
				Box gb = node("C" + i + "b");
				tree.addNode(ga);
				tree.addNode(gb);
				tree.addConnection(TreeConnection.create()
					.setParent(connector(child))
					.setChild(connector(ga)));
				tree.addConnection(TreeConnection.create()
					.setParent(connector(child))
					.setChild(connector(gb)));
			}
		}
		return tree;
	}

	public void testCompactSiblingsAfterDeepZigZagSubtree() throws IOException {
		// #29372: Root's first child S1 carries a large zig-zag subtree one level deeper in the
		// tree (S1 → T → Z1..Z12 as a row-wise sub-grid). The following siblings S2/S3 carry
		// small two-node chains (Sn → Sna) ending in T's column strip; S4's chain has a third
		// node reaching into the zig-zag columns. With compact=true, S2/S3 fit completely into
		// the empty space below S1/T and are placed there — the shared sibling column raster
		// reserves the Sna box width in T's strip, so the zig-zag's main bus runs right of the
		// Sna boxes. S4 does not fit: the zig-zag columns are densely covered by boxes and
		// connection stubs, so it stays below the complete zig-zag; squeezing it into a gap
		// between zig-zag rows is not desired.
		// Split threshold 4: Root's four children stay in a plain (compact) column; only T's
		// twelve children exceed the threshold and form the zig-zag sub-grid.
		TreeLayout tree = TreeLayout.create()
			.setCompact(true)
			.setChildSplitThreshold(4)
			.setRowWise(true)
			.setSubGridCols(3);

		Box root = node("Root");
		tree.addNode(root);

		// S1 → T → large zig-zag: the deep subtree that spans the full height.
		Box s1 = node("S1");
		tree.addNode(s1);
		tree.addConnection(TreeConnection.create()
			.setParent(connector(root))
			.setChild(connector(s1)));

		Box t = node("T");
		tree.addNode(t);
		tree.addConnection(TreeConnection.create()
			.setParent(connector(s1))
			.setChild(connector(t)));

		for (int i = 1; i <= 12; i++) {
			Box z = node("Z" + i);
			tree.addNode(z);
			tree.addConnection(TreeConnection.create()
				.setParent(connector(t))
				.setChild(connector(z)));
		}

		// Following siblings S2/S3 with small two-node chains ending in T's column; S4's chain
		// has a third node reaching the zig-zag columns.
		for (int i = 2; i <= 4; i++) {
			Box s = node("S" + i);
			tree.addNode(s);
			tree.addConnection(TreeConnection.create()
				.setParent(connector(root))
				.setChild(connector(s)));

			Box a = node("S" + i + "a");
			tree.addNode(a);
			tree.addConnection(TreeConnection.create()
				.setParent(connector(s))
				.setChild(connector(a)));

			if (i == 4) {
				Box aa = node("S" + i + "aa");
				tree.addNode(aa);
				tree.addConnection(TreeConnection.create()
					.setParent(connector(a))
					.setChild(connector(aa)));
			}
		}

		Diagram diagram = Diagram.create().setRoot(Padding.create().setAll(20).setContent(tree));
		String svg = writeToFile(diagram, "./target/TestTreeLayout-compact-zigzag.svg");

		double[] z12 = boxBounds(svg, "Z12");
		assertTrue("S2 must be compacted beside the zig-zag.", boxBounds(svg, "S2a")[3] < z12[1]);
		assertTrue("S3 must be compacted beside the zig-zag.", boxBounds(svg, "S3a")[3] < z12[1]);
		assertTrue("S4 must stay below the zig-zag.", boxBounds(svg, "S4")[1] > z12[3]);

		assertNoLineCrossesBox(svg, "S2a");
		assertNoLineCrossesBox(svg, "S3a");
		assertNoLineCrossesBox(svg, "S4a");
	}

	public void testCompactWideSiblingAfterZigZagSubtree() throws IOException {
		// Second aspect of #29372: Root has a child A whose children form a row-wise ("zig-zag")
		// sub-grid, followed by wide childless siblings B1/B2. With compact=true the wide
		// siblings slide up beside A's deep sub-grid subtree. Since sibling buses are aligned
		// right of the widest sibling (alignSiblingBuses), A's sub-grid bus and the stubs to the
		// sub-grid children start right of B1/B2 and must not cross the sibling boxes.
		TreeLayout tree = TreeLayout.create()
			.setCompact(true)
			.setChildSplitThreshold(3)
			.setRowWise(true);

		Box root = node("Root");
		tree.addNode(root);

		Box a = node("A");
		tree.addNode(a);
		tree.addConnection(TreeConnection.create()
			.setParent(connector(root))
			.setChild(connector(a)));

		// High fan-out under A triggers the row-wise sub-grid; each sub-grid child carries one
		// descendant so the post-grid column is populated, too.
		for (int i = 1; i <= 9; i++) {
			Box child = node("A" + i);
			tree.addNode(child);
			tree.addConnection(TreeConnection.create()
				.setParent(connector(a))
				.setChild(connector(child)));

			Box grand = node("A" + i + "a");
			tree.addNode(grand);
			tree.addConnection(TreeConnection.create()
				.setParent(connector(child))
				.setChild(connector(grand)));
		}

		// Wide childless siblings of A: compacted up beside A's sub-grid subtree.
		Box b1 = node("Wide childless sibling B1");
		tree.addNode(b1);
		tree.addConnection(TreeConnection.create()
			.setParent(connector(root))
			.setChild(connector(b1)));

		Box b2 = node("Wide childless sibling B2");
		tree.addNode(b2);
		tree.addConnection(TreeConnection.create()
			.setParent(connector(root))
			.setChild(connector(b2)));

		Diagram diagram = Diagram.create().setRoot(Padding.create().setAll(20).setContent(tree));
		String svg = writeToFile(diagram, "./target/TestTreeLayout-compact-zigzag-wide-sibling.svg");

		assertNoLineCrossesBox(svg, "Wide childless sibling B1");
		assertNoLineCrossesBox(svg, "Wide childless sibling B2");
	}

	/**
	 * Asserts that no bus, stub, or connection line segment crosses the box of the node with the
	 * given label.
	 *
	 * <p>
	 * Line paths (buses, stubs, connections) consist of M/H/V commands in tree-layout
	 * coordinates; node borders are L-based outline paths in group-local coordinates and are
	 * skipped. The box bounds are recovered from the node group's translation and its border
	 * outline.
	 * </p>
	 */
	private void assertNoLineCrossesBox(String svg, String label) {
		double[] bounds = boxBounds(svg, label);
		double left = bounds[0];
		double top = bounds[1];
		double right = bounds[2];
		double bottom = bounds[3];

		Matcher paths = Pattern.compile("d=\"([^\"]+)\"").matcher(svg);
		while (paths.find()) {
			String d = paths.group(1);
			if (d.contains("L")) {
				// Node border outline in group-local coordinates.
				continue;
			}
			String[] tokens = d.trim().split("[ ,]+");
			double x = 0;
			double y = 0;
			for (int i = 0; i < tokens.length; i++) {
				switch (tokens[i]) {
					case "M":
						x = Double.parseDouble(tokens[++i]);
						y = Double.parseDouble(tokens[++i]);
						break;
					case "H": {
						double nx = Double.parseDouble(tokens[++i]);
						assertFalse("Segment at y=" + y + " of '" + d + "' crosses box '" + label
							+ "' (" + left + ", " + top + ", " + right + ", " + bottom + ").",
							y > top && y < bottom && Math.min(x, nx) < right && Math.max(x, nx) > left);
						x = nx;
						break;
					}
					case "V": {
						double ny = Double.parseDouble(tokens[++i]);
						assertFalse("Segment at x=" + x + " of '" + d + "' crosses box '" + label
							+ "' (" + left + ", " + top + ", " + right + ", " + bottom + ").",
							x > left && x < right && Math.min(y, ny) < bottom && Math.max(y, ny) > top);
						y = ny;
						break;
					}
					default:
						fail("Unsupported path command in '" + d + "': " + tokens[i]);
				}
			}
		}
	}

	/**
	 * Bounds {@code [left, top, right, bottom]} of the labeled node's box in tree-layout
	 * coordinates, recovered from the SVG markup: the node group's translation plus the extent
	 * of its border outline path.
	 */
	private double[] boxBounds(String svg, String label) {
		int labelPos = svg.indexOf(">" + label + "</text>");
		assertTrue("Node '" + label + "' not found.", labelPos >= 0);

		int groupStart = svg.lastIndexOf("<g transform=\"translate(", labelPos);
		Matcher translate = Pattern.compile("translate\\(([0-9.-]+),([0-9.-]+)\\)")
			.matcher(svg.substring(groupStart, labelPos));
		assertTrue(translate.find());
		double x = Double.parseDouble(translate.group(1));
		double y = Double.parseDouble(translate.group(2));

		Matcher border = Pattern.compile("d=\"M 0\\.5,0\\.5 L ([0-9.]+),0\\.5 L \\1,([0-9.]+)")
			.matcher(svg.substring(labelPos));
		assertTrue("Border outline of '" + label + "' not found.", border.find());
		double width = Double.parseDouble(border.group(1)) + 0.5;
		double height = Double.parseDouble(border.group(2)) + 0.5;

		return new double[] { x, y, x + width, y + height };
	}

	public void testCompactRowWiseStartColFirstChildZigZag() throws IOException {
		// #29372: row-wise sub-grid with subGridCols=2 and subGridStartCol=1, so the FIRST
		// sub-grid child C1 is rendered in the right column. The zig-zag node B follows a
		// non-zig-zag sibling A, and C1 itself carries a zig-zag subtree; all other sub-grid
		// children are childless. The post-grid routing of C1's descendants anchors on C1's
		// first child D1 — with subGridStartCol=1 that child sits in the RIGHT column of C1's
		// nested grid, so the nested LEFT column (D2) lands back inside the outer sub-grid's
		// right column, and the nested main bus lands left of C1's own box: its stubs cross
		// the boxes of C1 and C3.
		TreeLayout tree = TreeLayout.create()
			.setCompact(true)
			.setChildSplitThreshold(2)
			.setRowWise(true)
			.setSubGridCols(2)
			.setSubGridStartCol(1);

		Box root = node("Root");
		tree.addNode(root);

		// Non-zig-zag sibling: a plain chain.
		Box a = node("A");
		tree.addNode(a);
		tree.addConnection(TreeConnection.create()
			.setParent(connector(root))
			.setChild(connector(a)));
		Box aa = node("Aa");
		tree.addNode(aa);
		tree.addConnection(TreeConnection.create()
			.setParent(connector(a))
			.setChild(connector(aa)));

		// Zig-zag node B: three children in a 2x2 sub-grid starting at column 1.
		Box b = node("B");
		tree.addNode(b);
		tree.addConnection(TreeConnection.create()
			.setParent(connector(root))
			.setChild(connector(b)));

		for (int i = 1; i <= 3; i++) {
			Box c = node("C" + i);
			tree.addNode(c);
			tree.addConnection(TreeConnection.create()
				.setParent(connector(b))
				.setChild(connector(c)));

			if (i == 1) {
				// The first sub-grid child (rendered in the right column) has a zig-zag
				// subtree of its own.
				for (int j = 1; j <= 3; j++) {
					Box d = node("D" + j);
					tree.addNode(d);
					tree.addConnection(TreeConnection.create()
						.setParent(connector(c))
						.setChild(connector(d)));
				}
			}
		}

		Diagram diagram = Diagram.create().setRoot(Padding.create().setAll(20).setContent(tree));
		writeToFile(diagram, "./target/TestTreeLayout-compact-rowwise-startcol-nested.svg");
	}

	public void testRandomTree() throws IOException {
		Diagram diagramCompfort =
			Diagram.create().setRoot(Padding.create().setAll(20).setContent(createRandomTree().setCompact(false)));
		writeToFile(diagramCompfort, "./target/TestTreeLayout-random-compfort.svg");

		Diagram diagramCompact =
			Diagram.create().setRoot(
				Padding.create().setAll(20)
					.setContent(createRandomTree().setCompact(true).setSibblingGapY(15).setSubtreeGapY(30)));
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

			for (int n = 0; n < cnt; n++) {
				Box child = randomNode(rnd);
				tree.addNode(child);

				tree.addConnection(TreeConnection.create()
					.setParent(connector(parent))
					.setChild(connector(child)));

				buildTree(tree, level + 1, child, rnd);
			}

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

	private Box tallNode(String name) {
		return Border.create().setContent(
			Padding.create().setLeft(5).setRight(5).setTop(25).setBottom(25).setContent(
				Text.create().setValue(name)));
	}

	/**
	 * A node whose visible box stacks a text label on top of a smaller "anchor" box (the gear
	 * stand-in). {@link #box} is what gets added to the {@link TreeLayout} as a node; {@link #anchor}
	 * is the sub-box used as the {@link TreeConnector#getAnchor() connector anchor}.
	 */
	private static final class LabelAnchorNode {
		final Box box;
		final Box anchor;

		LabelAnchorNode(Box box, Box anchor) {
			this.box = box;
			this.anchor = anchor;
		}
	}

	private LabelAnchorNode labelAnchorNode(String name) {
		// Anchor: a small bordered box (the gear stand-in). Wrapped in an Align with horizontal
		// MIDDLE alignment so it stays at its intrinsic width (and centered) when placed inside
		// the surrounding VerticalLayout next to a wider label.
		Box anchor = Border.create().setContent(
			Padding.create().setAll(8).setContent(
				Text.create().setValue(name)));
		Box anchorAligned = Align.create()
			.setXAlign(Alignment.MIDDLE)
			.setContent(anchor);
		// Box: a vertical layout with a wider label above the anchor. Without Align the
		// VerticalLayout stretches the anchor to the box width; with Align the anchor keeps its
		// intrinsic width so anchor.right < box.right and the parent → bus segment must
		// terminate at anchor.right (not box.right) to land on the visible anchor edge.
		Box box = VerticalLayout.create()
			.addContent(Text.create().setValue("Long label for " + name + " (above the anchor)"))
			.addContent(anchorAligned);
		return new LabelAnchorNode(box, anchor);
	}

	private LabelAnchorNode gearOnlyNode(String name) {
		Box anchor = Border.create().setContent(
			Padding.create().setAll(8).setContent(
				Text.create().setValue(name)));
		return new LabelAnchorNode(anchor, anchor);
	}

	/**
	 * Like {@link #labelAnchorNode(String)} but with a much taller label region (simulating a
	 * multi-line text block above the small anchor). Used to exercise the row-wise layout
	 * constraints with extreme anchor offsets.
	 */
	private LabelAnchorNode tallLabelAnchorNode(String name) {
		Box anchor = Border.create().setContent(
			Padding.create().setAll(8).setContent(
				Text.create().setValue(name)));
		Box anchorAligned = Align.create()
			.setXAlign(Alignment.MIDDLE)
			.setContent(anchor);
		// Inflate the label area with explicit top padding so the box gets significantly taller
		// than the anchor (anchor mid Y sits deep inside the box).
		Box tallLabelArea = Padding.create().setTop(80).setBottom(0).setLeft(0).setRight(0).setContent(
			Text.create().setValue("Tall label for " + name));
		Box box = VerticalLayout.create()
			.addContent(tallLabelArea)
			.addContent(anchorAligned);
		return new LabelAnchorNode(box, anchor);
	}
}
