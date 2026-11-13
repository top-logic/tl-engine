/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.layout;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.react.flow.data.Border;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.ConnectorSymbol;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.DragEdge;
import com.top_logic.react.flow.data.DropArea;

import com.top_logic.react.flow.data.GanttDecoration;
import com.top_logic.react.flow.data.GanttEdge;
import com.top_logic.react.flow.data.GanttEndpoint;
import com.top_logic.react.flow.data.GanttEnforce;
import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttLineDecoration;
import com.top_logic.react.flow.data.GanttRangeDecoration;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.GanttSpan;
import com.top_logic.react.flow.data.Padding;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.data.VerticalLayout;
import com.top_logic.react.flow.operations.drag.DragController;
import com.top_logic.react.flow.operations.layout.GanttLayoutOperations;
import com.top_logic.react.flow.server.svg.SvgTagWriter;
import com.top_logic.react.flow.server.ui.AWTContext;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.event.MouseButton;
import com.top_logic.react.flow.svg.event.Registration;
import com.top_logic.react.flow.svg.event.SVGClickEvent;
import com.top_logic.react.flow.svg.event.SVGClickHandler;
import com.top_logic.react.flow.svg.event.SVGWheelEvent;
import com.top_logic.react.flow.svg.event.SVGWheelHandler;

/** Tests for {@link com.top_logic.react.flow.operations.layout.GanttLayoutOperations}. */
public class TestGanttLayout extends TestCase {

	/** Default rowMinContentHeight used by GanttLayout. */
	private static final double ROW_MIN_CONTENT_HEIGHT = 24.0;

	/** Default rowPadding used by GanttLayout. */
	private static final double ROW_PADDING = 4.0;

	/** Default rowLabelPadding used by GanttLayout. */
	private static final double ROW_LABEL_PADDING = 4.0;

	/** Default row total height = ROW_MIN_CONTENT_HEIGHT + 2 * ROW_PADDING. */
	private static final double DEFAULT_ROW_TOTAL_HEIGHT = ROW_MIN_CONTENT_HEIGHT + 2 * ROW_PADDING; // 32

	public void testLayoutHeightScalesWithRows() {
		GanttLayout layout = GanttLayout.create()
			.setRowLabelMinWidth(200.0)
			.setRangeMin(0).setRangeMax(100).setZoom(1.0);
		addRowsWithLabels(layout, Arrays.asList(
			row("r1", "Row 1"),
			row("r2", "Row 2"),
			row("r3", "Row 3")));

		Diagram d = Diagram.create().setRoot(layout);

		RenderContext context = new AWTContext(12f);
		d.layout(context);

		// 3 empty rows * (24 + 2*4) = 3*32 = 96
		assertEquals("height", 3 * DEFAULT_ROW_TOTAL_HEIGHT, layout.getHeight());
	}

	/** Creates a row with a Box label wrapping the given text string. */
	private static GanttRow row(String id, String label) {
		Box labelBox = Border.create().setContent(Padding.create().setAll(2.0).setContent(Text.create().setValue(label)));
		return GanttRow.create().setId(id).setLabel(labelBox);
	}

	/** Sets the root rows on the layout and adds all label boxes to the layout's contents list. */
	private static void addRowsWithLabels(GanttLayout layout, java.util.List<GanttRow> rows) {
		layout.setRootRows(rows);
		for (GanttRow root : rows) {
			addRowLabels(layout, root);
		}
		// Add decoration label boxes to contents so they are rendered via standard dispatch.
		java.util.List<GanttDecoration> decos = layout.getDecorations();
		if (decos != null) {
			for (GanttDecoration deco : decos) {
				if (deco.getLabel() != null) {
					layout.addContent(deco.getLabel());
				}
			}
		}
	}

	private static void addRowLabels(GanttLayout layout, GanttRow row) {
		Box label = row.getLabel();
		if (label != null) {
			layout.addContent(label);
		}
		for (GanttRow child : row.getChildren()) {
			addRowLabels(layout, child);
		}
	}

	private static Box cell(String label) {
		return Border.create().setContent(Padding.create().setAll(2.0).setContent(Text.create().setValue(label)));
	}

	public void testSpanPositionedAtTimeAndRow() {
		GanttSpan span = span("s1", "r2", 10.0, 30.0, "Task 1");

		GanttLayout layout = GanttLayout.create()
			.setRowLabelMinWidth(200.0)
			.setRangeMin(0).setRangeMax(100).setZoom(1.0)
			.setItems(Arrays.asList(span));
		addRowsWithLabels(layout, Arrays.asList(
			row("r1", "Row 1"),
			row("r2", "Row 2")));
		layout.addContent(span.getBox());

		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		// r2 is the second row (index 1).
		// Row 0 (r1): no items -> content height = ROW_MIN_CONTENT_HEIGHT=24, total = 32.
		// Row 1 (r2): span intrinsic height <= ROW_MIN_CONTENT_HEIGHT -> content height = 24, total = 32.
		// span.y = row0Total + rowPadding = 32 + 4 = 36.
		// span.x = columnWidth (>=200) + 10 * zoom. Labels are small, so columnWidth = rowLabelMinWidth = 200.
		assertEquals("span.x", 210.0, span.getBox().getX(), 0.5);
		assertEquals("span.y", DEFAULT_ROW_TOTAL_HEIGHT + ROW_PADDING, span.getBox().getY(), 0.5);
		// Width = (end - start) * zoom = 20.
		assertEquals("span.width", 20.0, span.getBox().getWidth(), 0.5);
	}

	private static GanttSpan span(String id, String rowId, double start, double end, String label) {
		return GanttSpan.create()
			.setId(id).setRowId(rowId)
			.setStart(start).setEnd(end)
			.setBox(cell(label));
	}

	private static String renderToSvg(Diagram d) throws Exception {
		StringWriter buffer = new StringWriter();
		try (TagWriter tagWriter = new TagWriter(buffer);
				SvgTagWriter svgWriter = new SvgTagWriter(tagWriter)) {
			d.draw(svgWriter);
		}
		return buffer.toString();
	}

	public void testRowLanesRenderLabels() throws Exception {
		GanttLayout layout = GanttLayout.create()
			.setRangeMin(0).setRangeMax(100).setZoom(1.0);
		addRowsWithLabels(layout, Arrays.asList(
			row("r1", "Alpha"),
			row("r2", "Bravo")));
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		String svg = renderToSvg(d);

		// Label boxes are in contents and drawn via standard dispatch — Text.value appears in SVG.
		assertTrue("row label 'Alpha' present", svg.contains(">Alpha<"));
		assertTrue("row label 'Bravo' present", svg.contains(">Bravo<"));
	}

	public void testRowLanesIndentNestedRows() throws Exception {
		GanttRow child = row("c", "Child");
		GanttRow parent = row("p", "Parent");
		parent.setChildren(Arrays.asList(child));

		GanttLayout layout = GanttLayout.create()
			.setRangeMin(0).setRangeMax(100).setZoom(1.0)
			.setIndentWidth(16.0);
		addRowsWithLabels(layout, Arrays.asList(parent));
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		String svg = renderToSvg(d);
		assertTrue("Parent label present", svg.contains(">Parent<"));
		assertTrue("Child label present", svg.contains(">Child<"));
		// Indentation is reflected in the label box x position (depth * indentWidth offset).
		// We confirm both labels render; visual verification via browser.
	}

	public void testEdgesRenderInOutput() throws Exception {
		GanttSpan a = span("a", "r1", 10.0, 30.0, "A");
		GanttSpan b = span("b", "r2", 40.0, 60.0, "B");

		GanttEdge edge = GanttEdge.create()
			.setId("e1")
			.setSourceItemId("a")
			.setSourceEndpoint(GanttEndpoint.END)
			.setTargetItemId("b")
			.setTargetEndpoint(GanttEndpoint.START)
			.setEnforce(GanttEnforce.STRICT);

		GanttLayout layout = GanttLayout.create()
			.setRangeMin(0).setRangeMax(100).setZoom(1.0)
			.setItems(Arrays.asList(a, b))
			.setEdges(Arrays.asList(edge));
		addRowsWithLabels(layout, Arrays.asList(row("r1", "Row 1"), row("r2", "Row 2")));
		layout.addContent(a.getBox());
		layout.addContent(b.getBox());

		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		String svg = renderToSvg(d);
		assertTrue("edge group present", svg.contains("tl-gantt-edges"));
		// Inside the edge group, an SVG <path> must appear (orthogonal route).
		int groupStart = svg.indexOf("tl-gantt-edges");
		String afterGroup = svg.substring(groupStart);
		assertTrue("path element present in edge group", afterGroup.indexOf("<path") >= 0);
	}

	public void testEdgeWithoutSymbolsDrawsOnlyTheLine() throws Exception {
		String edges = edgeGroup(renderEdge(GanttEdge.create(), 10.0, 30.0, 40.0, 60.0));
		assertEquals("only the line path", 1, countPaths(edges));
	}

	public void testEdgeTargetSymbolDrawsFilledArrow() throws Exception {
		GanttEdge edge = GanttEdge.create()
			.setStrokeColor("#123456")
			.setTargetSymbol(ConnectorSymbol.FILLED_ARROW);
		String edges = edgeGroup(renderEdge(edge, 10.0, 30.0, 40.0, 60.0));
		assertEquals("line and arrow path", 2, countPaths(edges));
		String arrow = pathElement(edges, 1);
		assertTrue("arrow stroked in the stroke color: " + arrow, arrow.contains("stroke=\"#123456\""));
		assertTrue("arrow filled in the stroke color: " + arrow, arrow.contains("fill=\"#123456\""));
		assertFalse("arrow drawn solid: " + arrow, arrow.contains("stroke-dasharray"));
	}

	public void testThickEdgeKeepsStubBeyondTargetArrow() throws Exception {
		GanttEdge edge = GanttEdge.create()
			.setStrokeWidth(4.0)
			.setTargetSymbol(ConnectorSymbol.FILLED_ARROW);
		String edges = edgeGroup(renderEdge(edge, 10.0, 30.0, 40.0, 60.0));
		assertEquals("line and arrow path", 2, countPaths(edges));

		double[] line = pathCoordinates(pathElement(edges, 0));
		double[] arrow = pathCoordinates(pathElement(edges, 1));
		// The tip of the arrow is the second point of its path, at the start of the target item.
		double tipX = arrow[2];
		double lineEndX = line[line.length - 2];
		double cornerX = line[line.length - 4];
		assertTrue("stub corner " + cornerX + " left of the line end " + lineEndX, cornerX < lineEndX);
		assertTrue("line end " + lineEndX + " left of the arrow tip " + tipX, lineEndX < tipX);
	}

	public void testEdgeSymbolsUseViolatedStyle() throws Exception {
		GanttEdge edge = GanttEdge.create()
			.setStrokeColor("#123456")
			.setViolatedStrokeColor("#ff0000")
			.setViolatedDashes(Arrays.asList(4.0, 2.0))
			.setSourceSymbol(ConnectorSymbol.DIAMOND)
			.setTargetSymbol(ConnectorSymbol.FILLED_ARROW);
		// Source ends at 50, target starts at 40: the STRICT constraint is violated.
		String edges = edgeGroup(renderEdge(edge, 10.0, 50.0, 40.0, 60.0));
		assertEquals("line and two symbol paths", 3, countPaths(edges));
		assertTrue("line is dashed", pathElement(edges, 0).contains("stroke-dasharray"));
		for (int n = 1; n <= 2; n++) {
			String symbol = pathElement(edges, n);
			assertTrue("symbol in the violated color: " + symbol, symbol.contains("stroke=\"#FF0000\""));
			assertFalse("symbol drawn solid: " + symbol, symbol.contains("stroke-dasharray"));
		}
		assertTrue("filled arrow in the violated color", pathElement(edges, 2).contains("fill=\"#FF0000\""));
	}

	/**
	 * Renders a chart with a span "a" in a first and a span "b" in a second row, connected by the
	 * given edge from the end of "a" to the start of "b" with a strict constraint.
	 */
	private static String renderEdge(GanttEdge edge, double aStart, double aEnd, double bStart, double bEnd)
			throws Exception {
		GanttSpan a = span("a", "r1", aStart, aEnd, "A");
		GanttSpan b = span("b", "r2", bStart, bEnd, "B");
		edge
			.setId("e1")
			.setSourceItemId("a")
			.setSourceEndpoint(GanttEndpoint.END)
			.setTargetItemId("b")
			.setTargetEndpoint(GanttEndpoint.START)
			.setEnforce(GanttEnforce.STRICT);

		GanttLayout layout = GanttLayout.create()
			.setRangeMin(0).setRangeMax(100).setZoom(1.0)
			.setItems(Arrays.asList(a, b))
			.setEdges(Arrays.asList(edge));
		addRowsWithLabels(layout, Arrays.asList(row("r1", "Row 1"), row("r2", "Row 2")));
		layout.addContent(a.getBox());
		layout.addContent(b.getBox());

		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));
		return renderToSvg(d);
	}

	/** The content of the group holding the edges of a chart. */
	private static String edgeGroup(String svg) {
		int start = svg.indexOf("tl-gantt-edges");
		assertTrue("edge group present", start >= 0);
		int end = svg.indexOf("</g>", start);
		return svg.substring(start, end);
	}

	private static int countPaths(String svg) {
		int count = 0;
		for (int pos = svg.indexOf("<path"); pos >= 0; pos = svg.indexOf("<path", pos + 1)) {
			count++;
		}
		return count;
	}

	/** The coordinates in the path data of the given path start tag, as alternating x and y. */
	private static double[] pathCoordinates(String pathElement) {
		int start = pathElement.indexOf(" d=\"") + 4;
		String data = pathElement.substring(start, pathElement.indexOf('"', start));
		String[] parts = data.replaceAll("[A-Za-z,]", " ").trim().split("\\s+");
		double[] result = new double[parts.length];
		for (int n = 0; n < parts.length; n++) {
			result[n] = Double.parseDouble(parts[n]);
		}
		return result;
	}

	/** The start tag of the path element with the given index. */
	private static String pathElement(String svg, int index) {
		int pos = svg.indexOf("<path");
		for (int n = 0; n < index; n++) {
			pos = svg.indexOf("<path", pos + 1);
		}
		assertTrue("path " + index + " present", pos >= 0);
		return svg.substring(pos, svg.indexOf(">", pos) + 1);
	}

	public void testRangeDecorationRendersWithLabel() throws Exception {
		GanttRangeDecoration freeze = GanttRangeDecoration.create()
			.setId("freeze")
			.setFrom(60.0).setTo(80.0)
			.setColor("rgba(255,80,80,0.3)")
			.setLabel(Text.create().setValue("Freeze"));

		GanttLayout layout = GanttLayout.create()
			.setRangeMin(0).setRangeMax(100).setZoom(1.0)
			.setDecorations(Arrays.asList(freeze));
		addRowsWithLabels(layout, Arrays.asList(row("r1", "Row 1")));
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		String svg = renderToSvg(d);
		assertTrue("decoration group", svg.contains("tl-gantt-decorations"));
		assertTrue("decoration label", svg.contains(">Freeze<"));
	}

	public void testLineDecorationRendersAtPosition() throws Exception {
		GanttLineDecoration today = GanttLineDecoration.create()
			.setId("today")
			.setAt(50.0)
			.setColor("#3070d0")
			.setLabel(Text.create().setValue("Today"));

		GanttLayout layout = GanttLayout.create()
			.setRangeMin(0).setRangeMax(100).setZoom(1.0)
			.setDecorations(Arrays.asList(today));
		addRowsWithLabels(layout, Arrays.asList(row("r1", "Row 1")));
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		String svg = renderToSvg(d);
		assertTrue("line decoration label", svg.contains(">Today<"));
	}

	public void testDistributeSizeUsesGivenWidth() {
		GanttLayout layout = GanttLayout.create()
			.setRowLabelMinWidth(200.0)
			.setRangeMin(0).setRangeMax(100).setZoom(1.0);
		addRowsWithLabels(layout, Arrays.asList(row("r1", "Row 1")));

		Diagram d = Diagram.create().setRoot(layout);
		d.setViewBoxWidth(800);
		d.setViewBoxHeight(100);
		d.layout(new AWTContext(12f));

		// Intrinsic width = columnWidth + (rangeMax - rangeMin) * zoom >= 200 + 100 * 1 = 300.
		// Since ViewBox is 800, distributeSize should expand width to 800.
		// DiagramOperations.layout() calls distributeSize with intrinsic width, NOT viewBox width.
		// So we call distributeSize directly to verify the expand-to-fill behaviour.
		RenderContext context = new AWTContext(12f);
		layout.distributeSize(context, 0, 0, 800, 100);
		assertEquals("width", 800.0, layout.getWidth(), 0.5);
	}

	/**
	 * Verifies that a row grows taller when it contains an item whose intrinsic height exceeds
	 * {@code rowMinContentHeight}.
	 *
	 * <p>
	 * We use a tall item box (intrinsic height = 60) placed in row "r1". The expected row total
	 * height is {@code 60 + 2 * rowPadding = 68}. The overall layout height should be
	 * {@code row0Total + row1Total = 68 + 32 = 100} (row "r2" has no items
	 * so it stays at the minimum 32).
	 * </p>
	 */
	public void testRowGrowsWithTallItem() {
		// Create a tall box: we use a Border with inner Padding and a fixed-height spacer.
		// To produce a reliably tall intrinsic height we construct a Box subclass that
		// reports a fixed height from computeIntrinsicSize and ignores distributeSize for height.
		double tallIntrinsicHeight = 60.0;
		double spanWidth = 20.0; // (end=30 - start=10) * zoom=1

		TallBox tallBox = new TallBox(tallIntrinsicHeight);

		GanttSpan tallSpan = GanttSpan.create()
			.setId("tall").setRowId("r1")
			.setStart(10.0).setEnd(30.0)
			.setBox(tallBox);

		GanttLayout layout = GanttLayout.create()
			.setRowLabelMinWidth(200.0)
			.setRangeMin(0).setRangeMax(100).setZoom(1.0)
			.setItems(Arrays.asList(tallSpan));
		addRowsWithLabels(layout, Arrays.asList(
			row("r1", "Row 1"),
			row("r2", "Row 2")));
		layout.addContent(tallBox);

		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		double row0Total = tallIntrinsicHeight + 2 * ROW_PADDING; // 68
		double row1Total = ROW_MIN_CONTENT_HEIGHT + 2 * ROW_PADDING; // 32
		double expectedHeight = row0Total + row1Total; // 100

		assertEquals("layout height grows with tall item", expectedHeight, layout.getHeight(), 0.5);

		// The item's distributed height must equal the row content height (= tallIntrinsicHeight).
		assertEquals("tall item height = row content height", tallIntrinsicHeight, tallBox.getHeight(), 0.5);

		// The item's y position must be rowPadding = 4.
		assertEquals("tall item y", ROW_PADDING, tallBox.getY(), 0.5);

		// Width must be forced to span width.
		assertEquals("tall item width", spanWidth, tallBox.getWidth(), 0.5);
	}

	/**
	 * Verifies that the label column grows when a row label has an intrinsic width larger than
	 * {@code rowLabelMinWidth}.
	 *
	 * <p>
	 * We place a {@link WideBox} label (intrinsic width = 320) into a row. With
	 * {@code rowLabelMinWidth = 200} and {@code rowLabelPadding = 4}, the effective column width
	 * should be {@code max(200, 320 + 2*4) = 328}. The overall layout width should then be
	 * {@code 328 + (rangeMax - rangeMin) * zoom = 328 + 100 = 428}.
	 * </p>
	 */
	public void testRowLabelColumnGrowsWithWideLabel() {
		double wideIntrinsicWidth = 320.0;
		WideBox wideLabel = new WideBox(wideIntrinsicWidth);

		GanttRow wideRow = GanttRow.create().setId("wide").setLabel(wideLabel);

		GanttLayout layout = GanttLayout.create()
			.setRowLabelMinWidth(200.0)
			.setRowLabelPadding(ROW_LABEL_PADDING)
			.setRangeMin(0).setRangeMax(100).setZoom(1.0);
		layout.setRootRows(Arrays.asList(wideRow));
		layout.addContent(wideLabel);

		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		// columnWidth = max(200, 320 + 2*4) = 328
		double expectedColumnWidth = Math.max(200.0, wideIntrinsicWidth + 2 * ROW_LABEL_PADDING);
		// total width = columnWidth + (100 - 0) * 1 = 428
		double expectedWidth = expectedColumnWidth + 100.0;
		assertEquals("layout width grows with wide label", expectedWidth, layout.getWidth(), 0.5);
		assertEquals("column width stored on layout", expectedColumnWidth, layout.getColumnWidth(), 0.5);
	}

	// -----------------------------------------------------------------------
	// DragController tests
	// -----------------------------------------------------------------------

	public void testCanMoveTrueByDefault() {
		GanttSpan span = span("s1", "r1", 10.0, 30.0, "Task");
		GanttLayout layout = buildLayoutWithSpan(span, "r1", "r2");
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));
		assertTrue("canMove", ((DragController) layout).canMove(span.getBox()));
	}

	public void testCanMoveFalseWhenBothDisabled() {
		GanttSpan span = span("s1", "r1", 10.0, 30.0, "Task");
		span.setCanMoveTime(false);
		span.setCanMoveRow(false);
		GanttLayout layout = buildLayoutWithSpan(span, "r1", "r2");
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));
		assertFalse("canMove disabled", ((DragController) layout).canMove(span.getBox()));
	}

	public void testCanResizeSpanEast() {
		GanttSpan span = span("s1", "r1", 10.0, 30.0, "Task");
		GanttLayout layout = buildLayoutWithSpan(span, "r1");
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));
		assertTrue("resize east", ((DragController) layout).canResize(span.getBox(), DragEdge.E));
		assertFalse("no resize north", ((DragController) layout).canResize(span.getBox(), DragEdge.N));
	}

	public void testGetDropAreasAllRowsWhenMovable() {
		GanttSpan span = span("s1", "r1", 10.0, 30.0, "Task");
		GanttLayout layout = buildLayoutWithSpan(span, "r1", "r2");
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));
		List<DropArea> areas = ((DragController) layout).getDropAreas(span.getBox());
		assertEquals("one area per row", 2, areas.size());
	}

	public void testGetDropAreasRestrictedWhenCanMoveRowFalse() {
		GanttSpan span = span("s1", "r1", 10.0, 30.0, "Task");
		span.setCanMoveRow(false);
		GanttLayout layout = buildLayoutWithSpan(span, "r1", "r2");
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));
		List<DropArea> areas = ((DragController) layout).getDropAreas(span.getBox());
		assertEquals("only current row", 1, areas.size());
	}

	public void testConstrainMoveSnapsX() {
		GanttSpan span = span("s1", "r1", 10.0, 30.0, "Task");
		GanttLayout layout = buildLayoutWithSpan(span, "r1", "r2");
		layout.setSnapTo(1.0);
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));
		double[] result = ((DragController) layout).constrainMove(span.getBox(), 205.7, span.getBox().getY());
		// snap granularity = 1.0, zoom = 1.0 -> integer snap; chartX0 = columnWidth (200), 205.7 -> 206
		assertEquals("snapped X", 206.0, result[0], 0.5);
	}

	/** Builds a GanttLayout with a span item and the given row IDs. */
	private GanttLayout buildLayoutWithSpan(GanttSpan span, String... rowIds) {
		java.util.List<GanttRow> rows = new java.util.ArrayList<>();
		for (String id : rowIds) {
			rows.add(row(id, "Row " + id));
		}
		GanttLayout layout = GanttLayout.create()
			.setRowLabelMinWidth(200.0)
			.setRangeMin(0).setRangeMax(100).setZoom(1.0)
			.setItems(Arrays.asList(span));
		addRowsWithLabels(layout, rows);
		layout.addContent(span.getBox());
		return layout;
	}

	// -----------------------------------------------------------------------
	// Layout against an offered viewport size
	// -----------------------------------------------------------------------

	/** Number of rows of a chart that is taller than the offered viewports in the tests below. */
	private static final int MANY_ROWS = 200;

	/** Natural height of a chart with {@link #MANY_ROWS} empty rows. */
	private static final double MANY_ROWS_HEIGHT = MANY_ROWS * DEFAULT_ROW_TOTAL_HEIGHT;

	/**
	 * A chart taller than the offered height keeps the offered height and scrolls its data rows.
	 */
	public void testOfferedHeightLimitsBox() {
		GanttLayout layout = manyRows(0);
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new TestingRenderContext(), 2000, 500);

		assertEquals("height", 500.0, layout.getHeight(), 0.01);
		assertEquals("dataHeight", MANY_ROWS_HEIGHT, layout.getDataHeight(), 0.01);
		assertEquals("headerHeight", 0.0, layout.getHeaderHeight(), 0.01);
		// The content fits horizontally, the box takes its natural width next to the scrollbar.
		assertEquals("width", 300.0 + GanttLayoutOperations.SCROLLBAR_SIZE, layout.getWidth(), 0.01);

		DragController controller = (DragController) layout;
		assertTrue("viewport active", controller.canPan());

		controller.startPan(0, 0);
		controller.panTo(0, -100);
		assertEquals("scrollY after pan", 100.0, layout.getScrollY(), 0.01);
		controller.panTo(0, -1_000_000);
		assertEquals("scrollY clamped", MANY_ROWS_HEIGHT - 500, layout.getScrollY(), 0.01);
		assertEquals("no horizontal scroll range", 0.0, layout.getScrollX(), 0.01);

		layout.setScrollY(0);
		WheelEvent wheel = new WheelEvent(0, 50, false);
		((SVGWheelHandler) layout).onWheel(wheel);
		assertEquals("scrollY after wheel", 50.0, layout.getScrollY(), 0.01);
		assertTrue("wheel consumed", wheel._stopped);
	}

	/**
	 * Unlimited space (export) gives the chart its full natural height.
	 */
	public void testUnlimitedLayoutShowsFullHeight() {
		GanttLayout layout = manyRows(0);
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new TestingRenderContext());

		assertEquals("height", MANY_ROWS_HEIGHT, layout.getHeight(), 0.01);
		assertEquals("dataHeight", MANY_ROWS_HEIGHT, layout.getDataHeight(), 0.01);
		assertFalse("viewport inactive", ((DragController) layout).canPan());

		WheelEvent wheel = new WheelEvent(0, 50, false);
		((SVGWheelHandler) layout).onWheel(wheel);
		assertFalse("wheel propagates", wheel._stopped);
		assertEquals("scrollY", 0.0, layout.getScrollY(), 0.01);
	}

	/**
	 * Frozen header rows are excluded from the scrollable data height.
	 */
	public void testFrozenHeaderWithOfferedHeight() {
		GanttLayout layout = manyRows(2);
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new TestingRenderContext(), 2000, 400);

		assertEquals("height", 400.0, layout.getHeight(), 0.01);
		assertEquals("headerHeight", 2 * DEFAULT_ROW_TOTAL_HEIGHT, layout.getHeaderHeight(), 0.01);
		assertEquals("dataHeight", MANY_ROWS_HEIGHT - 2 * DEFAULT_ROW_TOTAL_HEIGHT, layout.getDataHeight(), 0.01);

		DragController controller = (DragController) layout;
		controller.startPan(0, 0);
		controller.panTo(0, -1_000_000);
		assertEquals("scrollY clamped", MANY_ROWS_HEIGHT - 400, layout.getScrollY(), 0.01);
	}

	/**
	 * The label of a decoration spanning all rows is drawn above the frozen rows at the top of the
	 * data rows, in a layer scrolling only horizontally.
	 */
	public void testAllRowsDecorationLabelBelowFrozenRows() throws Exception {
		GanttLayout layout = manyRows(2);
		Text label = Text.create().setValue("Today");
		layout.setDecorations(Arrays.asList(lineDeco(label)));

		String svg = renderViewport(layout);

		double headerHeight = 2 * DEFAULT_ROW_TOTAL_HEIGHT;
		assertEquals("headerHeight", headerHeight, layout.getHeaderHeight(), 0.01);
		assertEquals("label at the top of the data rows", layout.getY() + headerHeight + 10, label.getY(), 0.01);
		assertTrue("label in the decoration label layer",
			section(svg, "\"" + layout.getClientId() + GanttLayoutOperations.SCROLL_DECORATION_LABELS_ID_SUFFIX + "\"",
				"tl-gantt-scrollbar").contains(">Today<"));
		assertFalse("label not in the content group", contentSection(svg).contains(">Today<"));
	}

	/**
	 * The label of a decoration relevant for a data row moves with that row.
	 */
	public void testRowDecorationLabelStaysAtItsRow() throws Exception {
		GanttLayout layout = manyRows(2);
		Text label = Text.create().setValue("Review");
		layout.setDecorations(Arrays.asList(lineDeco(label).setRelevantFor(Arrays.asList("r5"))));

		String svg = renderViewport(layout);

		assertEquals("label at its row", layout.getY() + 5 * DEFAULT_ROW_TOTAL_HEIGHT + 10, label.getY(), 0.01);
		assertTrue("label in the content group", contentSection(svg).contains(">Review<"));
	}

	/**
	 * Without frozen rows, the label of a decoration spanning all rows is at the top of the chart.
	 */
	public void testAllRowsDecorationLabelWithoutFrozenRows() throws Exception {
		GanttLayout layout = manyRows(0);
		Text label = Text.create().setValue("Today");
		layout.setDecorations(Arrays.asList(lineDeco(label)));

		renderViewport(layout);

		assertEquals("label at the top of the chart", layout.getY() + 10, label.getY(), 0.01);
	}

	/**
	 * The label of a decoration drawn above the frozen rows stands on a backdrop of the size of the
	 * label plus padding, drawn before the label.
	 */
	public void testPinnedDecorationLabelHasBackground() throws Exception {
		GanttLayout layout = manyRows(2);
		Text label = Text.create().setValue("Today");
		layout.setDecorations(Arrays.asList(lineDeco(label)));

		String svg = renderViewport(layout);

		String labels = section(svg,
			"\"" + layout.getClientId() + GanttLayoutOperations.SCROLL_DECORATION_LABELS_ID_SUFFIX + "\"",
			"tl-gantt-scrollbar");
		int rectPos = labels.indexOf(GanttLayoutOperations.PINNED_LABEL_BACKGROUND_CSS_CLASS);
		assertTrue("backdrop present", rectPos >= 0);
		assertTrue("backdrop drawn before the label", rectPos < labels.indexOf(">Today<"));

		String rect = startTag(labels, labels.lastIndexOf("<rect", rectPos));
		double padding = GanttLayoutOperations.PINNED_LABEL_PADDING;
		assertEquals("x", label.getX() - padding, numberAttribute(rect, "x"), 0.01);
		assertEquals("y", label.getY() - padding, numberAttribute(rect, "y"), 0.01);
		assertEquals("width", label.getWidth() + 2 * padding, numberAttribute(rect, "width"), 0.01);
		assertEquals("height", label.getHeight() + 2 * padding, numberAttribute(rect, "height"), 0.01);
		assertEquals("rx", GanttLayoutOperations.PINNED_LABEL_RADIUS, numberAttribute(rect, "rx"), 0.01);
	}

	/**
	 * The label of a decoration moving with its data row is drawn without a backdrop.
	 */
	public void testRowDecorationLabelHasNoBackground() throws Exception {
		GanttLayout layout = manyRows(2);
		Text label = Text.create().setValue("Review");
		layout.setDecorations(Arrays.asList(lineDeco(label).setRelevantFor(Arrays.asList("r5"))));

		String svg = renderViewport(layout);

		assertTrue("label drawn", svg.contains(">Review<"));
		assertFalse("no backdrop", svg.contains(GanttLayoutOperations.PINNED_LABEL_BACKGROUND_CSS_CLASS));
	}

	/** The start tag of the element starting at the given position. */
	private static String startTag(String svg, int pos) {
		assertTrue("element present", pos >= 0);
		return svg.substring(pos, svg.indexOf(">", pos) + 1);
	}

	/** The numeric value of the given attribute of a start tag. */
	private static double numberAttribute(String startTag, String name) {
		String marker = " " + name + "=\"";
		int start = startTag.indexOf(marker);
		assertTrue("attribute present: " + name, start >= 0);
		start += marker.length();
		return Double.parseDouble(startTag.substring(start, startTag.indexOf('"', start)));
	}

	private static GanttLineDecoration lineDeco(Box label) {
		return GanttLineDecoration.create()
			.setId("today")
			.setAt(50.0)
			.setColor("#FFC800")
			.setLabel(label);
	}

	/** Lays out the chart in a space smaller than its rows, so that it scrolls, and draws it. */
	private static String renderViewport(GanttLayout layout) throws Exception {
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f), 2000, 400);
		return renderToSvg(d);
	}

	/** The part of the SVG holding the data rows of a chart with a viewport. */
	private static String contentSection(String svg) {
		return section(svg, "tl-gantt-vp-content", "tl-gantt-vp-sidebar");
	}

	/** The part of the SVG from the given start marker to the given end marker or the end. */
	private static String section(String svg, String startMarker, String endMarker) {
		int start = svg.indexOf(startMarker);
		assertTrue("marker present: " + startMarker, start >= 0);
		int end = svg.indexOf(endMarker, start);
		return svg.substring(start, end < 0 ? svg.length() : end);
	}

	/**
	 * A chart below a title gets the height that remains next to the title.
	 */
	public void testGanttBelowTitleTakesRemainingHeight() {
		GanttLayout gantt = manyRows(0);
		Text title = Text.create().setValue("Title");
		VerticalLayout column = VerticalLayout.create();
		column.setGap(10);
		column.addContent(title);
		column.addContent(gantt);

		Diagram d = Diagram.create().setRoot(column);
		d.layout(new TestingRenderContext(), 2000, 600);

		assertTrue("title has a height", title.getHeight() > 0);
		assertEquals("gantt height", 600 - title.getHeight() - 10, gantt.getHeight(), 0.01);
		assertEquals("column height", 600.0, column.getHeight(), 0.01);
		assertEquals("gantt below title", title.getHeight() + 10, gantt.getY(), 0.01);
	}

	/**
	 * Without frozen rows, content larger than the box is drawn in clipped scroll groups.
	 */
	public void testViewportWithoutFrozenRowsDrawsClipGroups() throws Exception {
		GanttLayout layout = manyRows(0);
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new TestingRenderContext(), 2000, 500);

		String svg = renderToSvg(d);
		assertTrue("content group", svg.contains("tl-gantt-vp-content"));
		assertTrue("sidebar group", svg.contains("tl-gantt-vp-sidebar"));
		assertTrue("clip path", svg.contains("clipPath"));
	}

	/**
	 * Content fitting into the offered space is drawn flat, without a viewport.
	 */
	public void testFittingContentDrawsFlat() throws Exception {
		GanttLayout layout = buildLayoutWithSpan(span("s1", "r1", 10.0, 30.0, "Task"), "r1", "r2");
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new TestingRenderContext(), 2000, 2000);

		assertEquals("natural height", 2 * DEFAULT_ROW_TOTAL_HEIGHT, layout.getHeight(), 0.01);
		assertFalse("viewport inactive", ((DragController) layout).canPan());

		String svg = renderToSvg(d);
		assertFalse("no viewport groups", svg.contains("tl-gantt-vp-content"));
		assertTrue("lanes drawn", svg.contains("tl-gantt-lanes"));
	}

	/**
	 * Growing space after scrolling limits the scroll offset to the smaller scroll range, and
	 * repeated layouts with the same space are stable.
	 */
	public void testScrollClampedAfterGrowth() {
		GanttLayout layout = manyRows(0);
		Diagram d = Diagram.create().setRoot(layout);
		RenderContext context = new TestingRenderContext();
		d.layout(context, 2000, 500);
		d.layout(context, 2000, 500);
		assertEquals("stable height", 500.0, layout.getHeight(), 0.01);

		layout.setScrollY(MANY_ROWS_HEIGHT - 500);
		d.layout(context, 2000, 3000);
		assertEquals("height", 3000.0, layout.getHeight(), 0.01);
		assertEquals("scrollY clamped", MANY_ROWS_HEIGHT - 3000, layout.getScrollY(), 0.01);

		d.layout(context);
		assertEquals("full height", MANY_ROWS_HEIGHT, layout.getHeight(), 0.01);
		assertEquals("no scroll range", 0.0, layout.getScrollY(), 0.01);
	}

	/**
	 * Without an explicit zoom, the time axis is fitted into the offered width until the user
	 * zooms.
	 */
	public void testFitWidth() {
		GanttLayout layout = buildLayoutWithSpan(span("s1", "r1", 10.0, 30.0, "Task"), "r1", "r2");
		layout.setFitWidth(true).setInitialZoom(1.0);
		Diagram d = Diagram.create().setRoot(layout);
		RenderContext context = new TestingRenderContext();

		// Label column 200, time range 100.
		d.layout(context, 1200, 1000);
		assertEquals("fitted zoom", 10.0, layout.getZoom(), 1e-9);
		assertEquals("width", 1200.0, layout.getWidth(), 0.01);
		assertFalse("content fits", ((DragController) layout).canPan());

		d.layout(context, 700, 1000);
		assertEquals("zoom fitted to other width", 5.0, layout.getZoom(), 1e-9);
		assertEquals("width", 700.0, layout.getWidth(), 0.01);

		d.layout(context);
		assertEquals("unlimited width uses initial zoom", 1.0, layout.getZoom(), 1e-9);
		assertEquals("natural width", 300.0, layout.getWidth(), 0.01);

		d.layout(context, 700, 1000);
		WheelEvent zoomIn = new WheelEvent(0, -100, true);
		((SVGWheelHandler) layout).onWheel(zoomIn);
		assertTrue("zoom gesture consumed", zoomIn._stopped);
		assertTrue("relayout requested", zoomIn._relayout);
		assertFalse("explicit zoom", layout.isFitWidth());
		assertEquals("zoomed in", 5.5, layout.getZoom(), 1e-9);

		d.layout(context, 1200, 1000);
		assertEquals("user zoom sticks", 5.5, layout.getZoom(), 1e-9);
		assertEquals("width", 200 + 550.0, layout.getWidth(), 0.01);

		d.layout(context, 400, 1000);
		assertEquals("user zoom sticks", 5.5, layout.getZoom(), 1e-9);
		assertEquals("width limited", 400.0, layout.getWidth(), 0.01);
		assertTrue("wider content scrolls", ((DragController) layout).canPan());
	}

	/**
	 * An explicit zoom is kept, the offered width only limits the box.
	 */
	public void testExplicitZoomIsKept() {
		GanttLayout layout = buildLayoutWithSpan(span("s1", "r1", 10.0, 30.0, "Task"), "r1", "r2");
		layout.setInitialZoom(2.0).setZoom(2.0);
		Diagram d = Diagram.create().setRoot(layout);
		RenderContext context = new TestingRenderContext();

		d.layout(context, 1200, 1000);
		assertEquals("zoom", 2.0, layout.getZoom(), 1e-9);
		assertEquals("natural width", 400.0, layout.getWidth(), 0.01);

		d.layout(context, 300, 1000);
		assertEquals("zoom", 2.0, layout.getZoom(), 1e-9);
		assertEquals("width limited", 300.0, layout.getWidth(), 0.01);
		assertEquals("height with scrollbar", 2 * DEFAULT_ROW_TOTAL_HEIGHT + GanttLayoutOperations.SCROLLBAR_SIZE,
			layout.getHeight(), 0.01);

		DragController controller = (DragController) layout;
		assertTrue("viewport active", controller.canPan());
		controller.startPan(0, 0);
		controller.panTo(-1_000_000, 0);
		// Content 100 px wider than the box at zoom 2: 50 position units.
		assertEquals("scrollX clamped", 50.0, layout.getScrollX(), 1e-9);
	}

	/**
	 * Boxes without an internal viewport take their natural size regardless of the offered space.
	 */
	public void testNonGanttIgnoresOfferedSize() {
		RenderContext context = new TestingRenderContext();

		VerticalLayout unlimited = flowchartColumn();
		Diagram.create().setRoot(unlimited).layout(context);

		VerticalLayout limited = flowchartColumn();
		Diagram.create().setRoot(limited).layout(context, 10, 10);

		assertTrue("natural width larger than offered", unlimited.getWidth() > 10);
		assertEquals("width", unlimited.getWidth(), limited.getWidth(), 0.01);
		assertEquals("height", unlimited.getHeight(), limited.getHeight(), 0.01);
	}

	/**
	 * A fitted time axis leaves room for the vertical scrollbar, so that it does not need a
	 * horizontal one.
	 */
	public void testFitWidthLeavesRoomForVerticalScrollbar() throws Exception {
		GanttLayout layout = manyRows(0);
		layout.setFitWidth(true);
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new TestingRenderContext(), 1210, 500);

		assertEquals("zoom fitted next to the scrollbar", 10.0, layout.getZoom(), 1e-9);
		assertEquals("width", 1210.0, layout.getWidth(), 0.01);

		String svg = renderToSvg(d);
		assertTrue("vertical scrollbar", svg.contains(GanttLayoutOperations.VERTICAL_THUMB_ID_SUFFIX));
		assertFalse("no horizontal scrollbar", svg.contains(GanttLayoutOperations.HORIZONTAL_THUMB_ID_SUFFIX));
	}

	/**
	 * Scrollbars are drawn only for a viewport, and only for a direction with a scroll range.
	 */
	public void testScrollbarsDrawnForScrollableDirection() throws Exception {
		GanttLayout tall = manyRows(0);
		Diagram tallDiagram = Diagram.create().setRoot(tall);
		tallDiagram.layout(new TestingRenderContext(), 2000, 500);
		String tallSvg = renderToSvg(tallDiagram);
		assertTrue("track", tallSvg.contains(GanttLayoutOperations.SCROLLBAR_TRACK_CSS_CLASS));
		assertTrue("thumb", tallSvg.contains(GanttLayoutOperations.SCROLLBAR_THUMB_CSS_CLASS));
		assertTrue("vertical", tallSvg.contains(GanttLayoutOperations.VERTICAL_THUMB_ID_SUFFIX));
		assertFalse("not horizontal", tallSvg.contains(GanttLayoutOperations.HORIZONTAL_THUMB_ID_SUFFIX));

		GanttLayout wide = wideChart();
		Diagram wideDiagram = Diagram.create().setRoot(wide);
		wideDiagram.layout(new TestingRenderContext(), 300, 1000);
		String wideSvg = renderToSvg(wideDiagram);
		assertTrue("horizontal", wideSvg.contains(GanttLayoutOperations.HORIZONTAL_THUMB_ID_SUFFIX));
		assertFalse("not vertical", wideSvg.contains(GanttLayoutOperations.VERTICAL_THUMB_ID_SUFFIX));

		Diagram exported = Diagram.create().setRoot(manyRows(0));
		exported.layout(new TestingRenderContext());
		assertFalse("no scrollbars in an export",
			renderToSvg(exported).contains(GanttLayoutOperations.SCROLLBAR_CSS_CLASS));

		GanttLayout both = wideChart();
		both.setZoom(20.0);
		Diagram bothDiagram = Diagram.create().setRoot(both);
		bothDiagram.layout(new TestingRenderContext(), 300, 60);
		String bothSvg = renderToSvg(bothDiagram);
		assertTrue("horizontal", bothSvg.contains(GanttLayoutOperations.HORIZONTAL_THUMB_ID_SUFFIX));
		assertTrue("vertical", bothSvg.contains(GanttLayoutOperations.VERTICAL_THUMB_ID_SUFFIX));
	}

	/**
	 * The thumb length is proportional to the visible part (with a minimum length), its position
	 * to the scroll offset.
	 */
	public void testVerticalThumbGeometry() {
		GanttLayout layout = tallChart();
		DragController controller = (DragController) layout;
		double trackX = layout.getX() + layout.getWidth() - GanttLayoutOperations.SCROLLBAR_SIZE / 2;
		double top = layout.getY();
		double maxScrollY = MANY_ROWS_HEIGHT - 500;
		double thumbLength = Math.max(GanttLayoutOperations.MIN_THUMB_LENGTH, 500 * 500 / MANY_ROWS_HEIGHT);
		double travel = 500 - thumbLength;

		assertEquals("thumb at start", 0.0, thumbTranslation(layout, GanttLayoutOperations.VERTICAL_THUMB_ID_SUFFIX)[1], 1e-9);
		assertTrue("press on thumb pans", controller.canPan(trackX, top + 1));
		assertTrue("press at thumb end pans", controller.canPan(trackX, top + thumbLength - 1));
		assertFalse("press on track below thumb clicks", controller.canPan(trackX, top + thumbLength + 1));

		layout.setScrollY(maxScrollY / 2);
		assertEquals("thumb in the middle", travel / 2,
			thumbTranslation(layout, GanttLayoutOperations.VERTICAL_THUMB_ID_SUFFIX)[1], 1e-9);
		assertFalse("press on track above thumb clicks", controller.canPan(trackX, top + travel / 2 - 1));
		assertTrue("press on moved thumb pans", controller.canPan(trackX, top + travel / 2 + 1));

		layout.setScrollY(maxScrollY);
		assertEquals("thumb at end", travel,
			thumbTranslation(layout, GanttLayoutOperations.VERTICAL_THUMB_ID_SUFFIX)[1], 1e-9);
	}

	/**
	 * Dragging the vertical thumb scrolls proportionally in the direction of the pointer, the thumb
	 * follows the pointer, the scroll offset is clamped.
	 */
	public void testDragVerticalThumb() {
		GanttLayout layout = tallChart();
		DragController controller = (DragController) layout;
		double trackX = layout.getX() + layout.getWidth() - GanttLayoutOperations.SCROLLBAR_SIZE / 2;
		double top = layout.getY();
		double maxScrollY = MANY_ROWS_HEIGHT - 500;
		double thumbLength = Math.max(GanttLayoutOperations.MIN_THUMB_LENGTH, 500 * 500 / MANY_ROWS_HEIGHT);
		double travel = 500 - thumbLength;

		controller.startPan(trackX, top + 5);
		controller.panTo(trackX, top + 5 + 100);
		assertEquals("scrolled down proportionally", 100 * maxScrollY / travel, layout.getScrollY(), 1e-6);
		assertEquals("thumb follows the pointer", 100.0,
			thumbTranslation(layout, GanttLayoutOperations.VERTICAL_THUMB_ID_SUFFIX)[1], 1e-6);
		assertEquals("no horizontal scroll", 0.0, layout.getScrollX(), 1e-9);

		controller.panTo(trackX, 1_000_000);
		assertEquals("clamped at the end", maxScrollY, layout.getScrollY(), 1e-6);
		controller.panTo(trackX, -1_000_000);
		assertEquals("clamped at the start", 0.0, layout.getScrollY(), 1e-6);
		controller.endPan();

		// Dragging the content moves it with the pointer, the opposite direction.
		controller.startPan(layout.getX() + 250, top + 200);
		controller.panTo(layout.getX() + 250, top + 100);
		assertEquals("content drag scrolls down", 100.0, layout.getScrollY(), 1e-6);
		controller.endPan();
	}

	/**
	 * Dragging the horizontal thumb scrolls the time axis proportionally.
	 */
	public void testDragHorizontalThumb() {
		GanttLayout layout = wideChart();
		Diagram.create().setRoot(layout).layout(new TestingRenderContext(), 300, 1000);
		DragController controller = (DragController) layout;

		// Label column 200, view width 100, time axis 200 px at zoom 2: thumb 50 px, travel 50 px,
		// scroll range 50 position units.
		double trackY = layout.getY() + layout.getHeight() - GanttLayoutOperations.SCROLLBAR_SIZE / 2;
		double left = layout.getX() + 200;
		assertTrue("press on thumb pans", controller.canPan(left + 10, trackY));
		assertFalse("press on track beside thumb clicks", controller.canPan(left + 60, trackY));

		controller.startPan(left + 10, trackY);
		controller.panTo(left + 30, trackY);
		assertEquals("scrolled right proportionally", 20.0, layout.getScrollX(), 1e-6);
		assertEquals("thumb follows the pointer", 20.0,
			thumbTranslation(layout, GanttLayoutOperations.HORIZONTAL_THUMB_ID_SUFFIX)[0], 1e-6);
		controller.panTo(left + 1_000, trackY);
		assertEquals("clamped", 50.0, layout.getScrollX(), 1e-6);
		assertEquals("no vertical scroll", 0.0, layout.getScrollY(), 1e-9);
		controller.endPan();
	}

	/**
	 * A click on the track beside the thumb scrolls by the visible height towards the click.
	 */
	public void testTrackClickPagesVertically() throws Exception {
		GanttLayout layout = manyRows(0);
		layout.setClientId("gantt");
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new TestingRenderContext(), 2000, 500);
		ClickRecorder recorder = new ClickRecorder();
		draw(d, recorder);
		SVGClickHandler track = recorder.handler(GanttLayoutOperations.SCROLLBAR_TRACK_CSS_CLASS);
		SVGClickHandler thumb = recorder.handler(GanttLayoutOperations.SCROLLBAR_THUMB_CSS_CLASS);
		double trackX = layout.getX() + layout.getWidth() - GanttLayoutOperations.SCROLLBAR_SIZE / 2;
		double bottom = layout.getY() + 500;

		ClickEvent below = new ClickEvent(trackX, bottom - 1);
		track.onClick(below);
		assertEquals("paged down", 500.0, layout.getScrollY(), 1e-6);
		assertTrue("click consumed", below._stopped);
		assertTrue("thumb moved", below._translations.containsKey(
			layout.getClientId() + GanttLayoutOperations.VERTICAL_THUMB_ID_SUFFIX));

		track.onClick(new ClickEvent(trackX, bottom - 1));
		assertEquals("paged down again", 1000.0, layout.getScrollY(), 1e-6);

		track.onClick(new ClickEvent(trackX, layout.getY() + 1));
		assertEquals("paged up", 500.0, layout.getScrollY(), 1e-6);

		layout.setScrollY(MANY_ROWS_HEIGHT - 500 - 100);
		track.onClick(new ClickEvent(trackX, layout.getY() + 1));
		assertEquals("paged up from the end", MANY_ROWS_HEIGHT - 500 - 600, layout.getScrollY(), 1e-6);
		layout.setScrollY(MANY_ROWS_HEIGHT - 500 - 100);
		track.onClick(new ClickEvent(trackX, bottom - 1));
		assertEquals("paging down clamped", MANY_ROWS_HEIGHT - 500, layout.getScrollY(), 1e-6);

		ClickEvent onThumb = new ClickEvent(trackX, bottom - 1);
		thumb.onClick(onThumb);
		assertTrue("click on thumb consumed", onThumb._stopped);
		assertEquals("click on thumb does not scroll", MANY_ROWS_HEIGHT - 500, layout.getScrollY(), 1e-6);
	}

	/** A chart with {@link #MANY_ROWS} rows laid out in a viewport of height 500. */
	private static GanttLayout tallChart() {
		GanttLayout layout = manyRows(0);
		layout.setClientId("gantt");
		Diagram.create().setRoot(layout).layout(new TestingRenderContext(), 2000, 500);
		return layout;
	}

	/** A chart of two rows with a time range of 100 at zoom 2 and a label column of 200. */
	private GanttLayout wideChart() {
		GanttLayout layout = buildLayoutWithSpan(span("s1", "r1", 10.0, 30.0, "Task"), "r1", "r2");
		layout.setInitialZoom(2.0).setZoom(2.0);
		layout.setClientId("gantt");
		return layout;
	}

	/** The translation the chart gives the thumb group with the given ID suffix. */
	private static double[] thumbTranslation(GanttLayout layout, String idSuffix) {
		java.util.Map<String, double[]> translations = new java.util.HashMap<>();
		((DragController) layout).renderPan((id, tx, ty) -> translations.put(id, new double[] { tx, ty }));
		double[] result = translations.get(layout.getClientId() + idSuffix);
		assertNotNull("thumb " + idSuffix + " translated", result);
		return result;
	}

	private static void draw(Diagram d, SvgTagWriter writer) throws Exception {
		d.draw(writer);
		writer.close();
	}

	/** {@link SvgTagWriter} recording the click handlers by the CSS class of their element. */
	private static class ClickRecorder extends SvgTagWriter {

		private final java.util.Map<String, SVGClickHandler> _handlers = new java.util.HashMap<>();

		private String _cssClass;

		ClickRecorder() {
			super(new TagWriter(new StringWriter()));
		}

		@Override
		public void writeCssClass(String cssClass) {
			super.writeCssClass(cssClass);
			_cssClass = cssClass;
		}

		@Override
		public Registration attachOnClick(SVGClickHandler handler, Object sender) {
			// The first handler of a class is the one of the vertical scrollbar.
			_handlers.putIfAbsent(_cssClass, handler);
			return Registration.NONE;
		}

		SVGClickHandler handler(String cssClass) {
			SVGClickHandler result = _handlers.get(cssClass);
			assertNotNull("click handler for " + cssClass, result);
			return result;
		}
	}

	/** {@link SVGClickEvent} of a left click recording what the handler did with it. */
	private static class ClickEvent implements SVGClickEvent {

		private final double _x;

		private final double _y;

		boolean _stopped;

		final java.util.Map<String, double[]> _translations = new java.util.HashMap<>();

		ClickEvent(double x, double y) {
			_x = x;
			_y = y;
		}

		@Override
		public Object getSender() {
			return null;
		}

		@Override
		public void stopPropagation() {
			_stopped = true;
		}

		@Override
		public void preventDefault() {
			// Ignore.
		}

		@Override
		public void updateTransform(String elementId, double translateX, double translateY) {
			_translations.put(elementId, new double[] { translateX, translateY });
		}

		@Override
		public boolean getButton(MouseButton button) {
			return button == MouseButton.LEFT;
		}

		@Override
		public boolean isMetaKey() {
			return false;
		}

		@Override
		public boolean isShiftKey() {
			return false;
		}

		@Override
		public boolean isAltKey() {
			return false;
		}

		@Override
		public boolean isCtrlKey() {
			return false;
		}

		@Override
		public double getOffsetX() {
			return _x;
		}

		@Override
		public double getOffsetY() {
			return _y;
		}
	}

	private static VerticalLayout flowchartColumn() {
		VerticalLayout column = VerticalLayout.create();
		column.setGap(20);
		column.addContent(cell("Start of the process"));
		column.addContent(cell("Next step"));
		column.addContent(cell("End"));
		return column;
	}

	/** A chart with {@link #MANY_ROWS} rows, time range 0..100 at zoom 1, label column 200. */
	private static GanttLayout manyRows(int frozenRows) {
		List<GanttRow> rows = new java.util.ArrayList<>();
		for (int n = 0; n < MANY_ROWS; n++) {
			rows.add(row("r" + n, "Row " + n));
		}
		GanttLayout layout = GanttLayout.create()
			.setRowLabelMinWidth(200.0)
			.setRangeMin(0).setRangeMax(100).setZoom(1.0)
			.setFrozenRows(frozenRows);
		addRowsWithLabels(layout, rows);
		return layout;
	}

	/** {@link SVGWheelEvent} recording what the handler did with it. */
	private static class WheelEvent implements SVGWheelEvent {

		private final double _deltaX;

		private final double _deltaY;

		private final boolean _ctrl;

		boolean _stopped;

		boolean _relayout;

		WheelEvent(double deltaX, double deltaY, boolean ctrl) {
			_deltaX = deltaX;
			_deltaY = deltaY;
			_ctrl = ctrl;
		}

		@Override
		public Object getSender() {
			return null;
		}

		@Override
		public void stopPropagation() {
			_stopped = true;
		}

		@Override
		public void preventDefault() {
			// Ignore.
		}

		@Override
		public double getDeltaX() {
			return _deltaX;
		}

		@Override
		public double getDeltaY() {
			return _deltaY;
		}

		@Override
		public boolean isCtrlKey() {
			return _ctrl;
		}

		@Override
		public boolean isShiftKey() {
			return false;
		}

		@Override
		public double getOffsetX() {
			return 0;
		}

		@Override
		public double getOffsetY() {
			return 0;
		}

		@Override
		public void requestRelayout() {
			_relayout = true;
		}
	}

	// -----------------------------------------------------------------------
	// Helper: a Box whose intrinsic height is fixed (simulates a tall widget).
	// distributeSize stores whatever coordinates the layout assigns.
	// -----------------------------------------------------------------------

	/**
	 * Test-only Box implementation that reports a fixed intrinsic height from
	 * {@code computeIntrinsicSize} and stores whatever dimensions are assigned by
	 * {@code distributeSize}.
	 *
	 * <p>
	 * Extends {@link com.top_logic.react.flow.data.impl.Border_Impl} (a concrete Box subclass)
	 * purely to satisfy the abstract-method contract of the msgbuf hierarchy. The standard
	 * {@code computeIntrinsicSize} / {@code distributeSize} implementations are replaced to give
	 * predictable, test-controlled dimensions.
	 * </p>
	 */
	private static class TallBox extends com.top_logic.react.flow.data.impl.Border_Impl {

		private final double _fixedHeight;

		TallBox(double fixedHeight) {
			_fixedHeight = fixedHeight;
		}

		@Override
		public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY,
				double availableWidth, double availableHeight) {
			setX(offsetX);
			setY(offsetY);
			// Width is intentionally left 0 here; the layout forces span width via distributeSize.
			setWidth(0);
			setHeight(_fixedHeight);
		}

		@Override
		public void distributeSize(RenderContext context, double x, double y, double width, double height) {
			setX(x);
			setY(y);
			setWidth(width);
			setHeight(height);
		}
	}

	/**
	 * Test-only Box implementation that reports a fixed intrinsic width from
	 * {@code computeIntrinsicSize} and stores whatever dimensions are assigned by
	 * {@code distributeSize}.
	 *
	 * <p>
	 * Used to verify that the label column grows to fit a wide label box.
	 * </p>
	 */
	private static class WideBox extends com.top_logic.react.flow.data.impl.Border_Impl {

		private final double _fixedWidth;

		WideBox(double fixedWidth) {
			_fixedWidth = fixedWidth;
		}

		@Override
		public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY,
				double availableWidth, double availableHeight) {
			setX(offsetX);
			setY(offsetY);
			setWidth(_fixedWidth);
			setHeight(0);
		}

		@Override
		public void distributeSize(RenderContext context, double x, double y, double width, double height) {
			setX(x);
			setY(y);
			setWidth(width);
			setHeight(height);
		}
	}
}
