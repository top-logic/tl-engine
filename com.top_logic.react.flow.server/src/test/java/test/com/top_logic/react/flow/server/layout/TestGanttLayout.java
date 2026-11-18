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
import com.top_logic.model.annotate.ui.ValueColor;
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
import com.top_logic.react.flow.data.GanttItem;
import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttLineDecoration;
import com.top_logic.react.flow.data.GanttPoint;
import com.top_logic.react.flow.data.GanttRangeDecoration;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.GanttSpan;
import com.top_logic.react.flow.data.LOD;
import com.top_logic.react.flow.data.LODVariant;
import com.top_logic.react.flow.data.Padding;
import com.top_logic.react.flow.data.Sticky;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.data.VerticalLayout;
import com.top_logic.react.flow.data.Widget;
import com.top_logic.react.flow.operations.ColorRoles;
import com.top_logic.react.flow.operations.StickyOperations;
import com.top_logic.react.flow.operations.drag.DragController;
import com.top_logic.react.flow.operations.layout.GanttLayoutOperations;
import com.top_logic.react.flow.operations.layout.GanttRowLayout;
import com.top_logic.react.flow.server.script.FlowFactory;
import com.top_logic.react.flow.server.script.ToColorRole;
import com.top_logic.react.flow.server.svg.SvgTagWriter;
import com.top_logic.react.flow.server.ui.AWTContext;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.event.MouseButton;
import com.top_logic.react.flow.svg.event.Registration;
import com.top_logic.react.flow.svg.event.SVGClickEvent;
import com.top_logic.react.flow.svg.event.SVGClickHandler;
import com.top_logic.react.flow.svg.event.SVGKeyEvent;
import com.top_logic.react.flow.svg.event.SVGKeyHandler;
import com.top_logic.react.flow.svg.event.SVGWheelEvent;
import com.top_logic.react.flow.svg.event.SVGWheelHandler;
import com.top_logic.util.error.TopLogicException;

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

	/** The content of the group holding the edges of a chart (up to its end tag). */
	private static String edgeGroup(String svg) {
		int start = svg.indexOf("tl-gantt-edges");
		assertTrue("edge group present", start >= 0);
		return svg.substring(start, groupEnd(svg, start));
	}

	/** The position of the end tag of the group whose start tag contains the given position. */
	private static int groupEnd(String svg, int pos) {
		int depth = 1;
		int next = svg.indexOf('>', pos) + 1;
		while (true) {
			int open = svg.indexOf("<g", next);
			int close = svg.indexOf("</g>", next);
			assertTrue("group closed", close >= 0);
			if (open >= 0 && open < close) {
				if (svg.charAt(svg.indexOf('>', open) - 1) != '/') {
					depth++;
				}
				next = open + 2;
			} else {
				depth--;
				if (depth == 0) {
					return close;
				}
				next = close + 4;
			}
		}
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
	// Level of detail in row labels
	// -----------------------------------------------------------------------

	/** Zoom from which the detailed variant of {@link #lodLabel()} is chosen. */
	private static final double LOD_DETAIL_ZOOM = 16.0;

	/** Index of the detailed variant of {@link #lodLabel()}. */
	private static final int LOD_DETAIL_INDEX = 0;

	/** Index of the fallback variant of {@link #lodLabel()}. */
	private static final int LOD_FALLBACK_INDEX = 1;

	/** Width of the widest variant of {@link #lodLabel()} in the {@link TestingRenderContext}. */
	private static final double LOD_WIDEST_WIDTH = "Monat".length() * 12.0;

	/**
	 * A level-of-detail box showing "Tag" from {@link #LOD_DETAIL_ZOOM} on and the wider "Monat"
	 * below.
	 */
	private static LOD lodLabel() {
		return LOD.create()
			.addVariant(LODVariant.create().setContent(Text.create().setValue("Tag")).setMinZoom(LOD_DETAIL_ZOOM))
			.addVariant(LODVariant.create().setContent(Text.create().setValue("Monat")));
	}

	/**
	 * A chart with an axis row labeled by the given label and a data row labeled "Daten", time
	 * range 0..100, without minimum label column width.
	 */
	private static GanttLayout lodLabelLayout(Box axisLabel) {
		GanttLayout layout = GanttLayout.create()
			.setRowLabelMinWidth(0)
			.setRowLabelPadding(ROW_LABEL_PADDING)
			.setRangeMin(0).setRangeMax(100);
		layout.setRootRows(Arrays.asList(
			GanttRow.create().setId("axis").setLabel(axisLabel),
			GanttRow.create().setId("data").setLabel(Text.create().setValue("Daten"))));
		layout.addContent(axisLabel);
		layout.addContent(layout.getRootRows().get(1).getLabel());
		return layout;
	}

	/**
	 * A row label chooses its level-of-detail variant by the zoom of the time axis, while the label
	 * column keeps the width of the widest variant.
	 */
	public void testRowLabelChoosesVariantByZoom() {
		LOD label = lodLabel();
		GanttLayout layout = lodLabelLayout(label);
		Diagram d = Diagram.create().setRoot(layout);
		RenderContext context = new TestingRenderContext();
		double expectedColumnWidth = LOD_WIDEST_WIDTH + 2 * ROW_LABEL_PADDING;

		layout.setZoom(20.0);
		d.layout(context);
		assertEquals("detailed variant at zoom 20", LOD_DETAIL_INDEX, label.getChosenIndex());
		assertEquals("column width at zoom 20", expectedColumnWidth, layout.getColumnWidth(), 1e-9);
		assertEquals("label x", ROW_LABEL_PADDING, label.getX(), 1e-9);
		assertEquals("label fills the column", LOD_WIDEST_WIDTH, label.getWidth(), 1e-9);

		layout.setZoom(2.0);
		d.layout(context);
		assertEquals("fallback variant at zoom 2", LOD_FALLBACK_INDEX, label.getChosenIndex());
		assertEquals("column width at zoom 2", expectedColumnWidth, layout.getColumnWidth(), 1e-9);
		assertEquals("label x", ROW_LABEL_PADDING, label.getX(), 1e-9);
		assertEquals("label fills the column", LOD_WIDEST_WIDTH, label.getWidth(), 1e-9);
	}

	/**
	 * A row label without level of detail determines the label column by its own width and is
	 * placed at the column padding, independent of the zoom.
	 */
	public void testPlainRowLabelIndependentOfZoom() {
		Text label = Text.create().setValue("Kalenderwoche");
		GanttLayout layout = lodLabelLayout(label);
		Diagram d = Diagram.create().setRoot(layout);
		RenderContext context = new TestingRenderContext();
		double labelWidth = "Kalenderwoche".length() * 12.0;

		for (double zoom : new double[] { 2.0, 20.0 }) {
			layout.setZoom(zoom);
			d.layout(context);
			assertEquals("column width at zoom " + zoom, labelWidth + 2 * ROW_LABEL_PADDING,
				layout.getColumnWidth(), 1e-9);
			assertEquals("label x at zoom " + zoom, ROW_LABEL_PADDING, label.getX(), 1e-9);
			assertEquals("label width at zoom " + zoom, labelWidth, label.getWidth(), 1e-9);
			assertEquals("total width at zoom " + zoom, labelWidth + 2 * ROW_LABEL_PADDING + 100 * zoom,
				layout.getWidth(), 1e-9);
		}
	}

	/**
	 * The zoom of a fitted time axis does not depend on the variant a row label chooses, because the
	 * label column reserves the widest variant.
	 */
	public void testFitWidthZoomIndependentOfLabelVariant() {
		LOD label = lodLabel();
		GanttLayout layout = lodLabelLayout(label);
		layout.setFitWidth(true);
		Diagram d = Diagram.create().setRoot(layout);
		RenderContext context = new TestingRenderContext();
		double columnWidth = LOD_WIDEST_WIDTH + 2 * ROW_LABEL_PADDING;

		d.layout(context, columnWidth + 100 * 10.0, 1000);
		assertEquals("fallback variant", LOD_FALLBACK_INDEX, label.getChosenIndex());
		assertEquals("fitted zoom", 10.0, layout.getZoom(), 1e-9);
		assertEquals("column width", columnWidth, layout.getColumnWidth(), 1e-9);

		d.layout(context, columnWidth + 100 * 20.0, 1000);
		assertEquals("detailed variant", LOD_DETAIL_INDEX, label.getChosenIndex());
		assertEquals("fitted zoom", 20.0, layout.getZoom(), 1e-9);
		assertEquals("column width", columnWidth, layout.getColumnWidth(), 1e-9);
	}

	/**
	 * A level-of-detail box on the time axis chooses its variant by the zoom and takes the size of
	 * the chosen variant, not of the widest one.
	 */
	public void testItemLodTakesChosenVariant() {
		LOD itemBox = lodLabel();
		GanttPoint point = GanttPoint.create().setId("p").setRowId("data").setAt(50).setBox(itemBox);
		GanttLayout layout = lodLabelLayout(Text.create().setValue("Achse"));
		layout.setItems(Arrays.asList(point));
		layout.addContent(itemBox);
		Diagram d = Diagram.create().setRoot(layout);
		RenderContext context = new TestingRenderContext();

		layout.setZoom(20.0);
		d.layout(context);
		assertEquals("detailed variant at zoom 20", LOD_DETAIL_INDEX, itemBox.getChosenIndex());
		assertEquals("width of the detailed variant", "Tag".length() * 12.0, itemBox.getWidth(), 1e-9);

		layout.setZoom(2.0);
		d.layout(context);
		assertEquals("fallback variant at zoom 2", LOD_FALLBACK_INDEX, itemBox.getChosenIndex());
		assertEquals("width of the fallback variant", LOD_WIDEST_WIDTH, itemBox.getWidth(), 1e-9);
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
			section(svg, "-" + GanttLayoutOperations.SCROLL_DECORATION_LABELS_PURPOSE + "\"",
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
			"-" + GanttLayoutOperations.SCROLL_DECORATION_LABELS_PURPOSE + "\"",
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
		assertTrue("vertical scrollbar", svg.contains(GanttLayoutOperations.VERTICAL_THUMB_PURPOSE));
		assertFalse("no horizontal scrollbar", svg.contains(GanttLayoutOperations.HORIZONTAL_THUMB_PURPOSE));
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
		assertTrue("vertical", tallSvg.contains(GanttLayoutOperations.VERTICAL_THUMB_PURPOSE));
		assertFalse("not horizontal", tallSvg.contains(GanttLayoutOperations.HORIZONTAL_THUMB_PURPOSE));

		GanttLayout wide = wideChart();
		Diagram wideDiagram = Diagram.create().setRoot(wide);
		wideDiagram.layout(new TestingRenderContext(), 300, 1000);
		String wideSvg = renderToSvg(wideDiagram);
		assertTrue("horizontal", wideSvg.contains(GanttLayoutOperations.HORIZONTAL_THUMB_PURPOSE));
		assertFalse("not vertical", wideSvg.contains(GanttLayoutOperations.VERTICAL_THUMB_PURPOSE));

		Diagram exported = Diagram.create().setRoot(manyRows(0));
		exported.layout(new TestingRenderContext());
		assertFalse("no scrollbars in an export",
			renderToSvg(exported).contains(GanttLayoutOperations.SCROLLBAR_CSS_CLASS));

		GanttLayout both = wideChart();
		both.setZoom(20.0);
		Diagram bothDiagram = Diagram.create().setRoot(both);
		bothDiagram.layout(new TestingRenderContext(), 300, 60);
		String bothSvg = renderToSvg(bothDiagram);
		assertTrue("horizontal", bothSvg.contains(GanttLayoutOperations.HORIZONTAL_THUMB_PURPOSE));
		assertTrue("vertical", bothSvg.contains(GanttLayoutOperations.VERTICAL_THUMB_PURPOSE));
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

		assertEquals("thumb at start", 0.0, thumbTranslation(layout, GanttLayoutOperations.VERTICAL_THUMB_PURPOSE)[1], 1e-9);
		assertTrue("press on thumb pans", controller.canPan(trackX, top + 1));
		assertTrue("press at thumb end pans", controller.canPan(trackX, top + thumbLength - 1));
		assertFalse("press on track below thumb clicks", controller.canPan(trackX, top + thumbLength + 1));

		layout.setScrollY(maxScrollY / 2);
		assertEquals("thumb in the middle", travel / 2,
			thumbTranslation(layout, GanttLayoutOperations.VERTICAL_THUMB_PURPOSE)[1], 1e-9);
		assertFalse("press on track above thumb clicks", controller.canPan(trackX, top + travel / 2 - 1));
		assertTrue("press on moved thumb pans", controller.canPan(trackX, top + travel / 2 + 1));

		layout.setScrollY(maxScrollY);
		assertEquals("thumb at end", travel,
			thumbTranslation(layout, GanttLayoutOperations.VERTICAL_THUMB_PURPOSE)[1], 1e-9);
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
			thumbTranslation(layout, GanttLayoutOperations.VERTICAL_THUMB_PURPOSE)[1], 1e-6);
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
			thumbTranslation(layout, GanttLayoutOperations.HORIZONTAL_THUMB_PURPOSE)[0], 1e-6);
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
		assertTrue("thumb moved", below._translations.containsKey(GanttLayoutOperations.VERTICAL_THUMB_PURPOSE));

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
		Diagram.create().setRoot(layout).layout(new TestingRenderContext(), 2000, 500);
		return layout;
	}

	/** A chart of two rows with a time range of 100 at zoom 2 and a label column of 200. */
	private GanttLayout wideChart() {
		GanttLayout layout = buildLayoutWithSpan(span("s1", "r1", 10.0, 30.0, "Task"), "r1", "r2");
		layout.setInitialZoom(2.0).setZoom(2.0);
		return layout;
	}

	/** The translation the chart gives the thumb group with the given purpose. */
	private static double[] thumbTranslation(GanttLayout layout, String purpose) {
		double[] result = translations(layout, layout).get(purpose);
		assertNotNull("thumb " + purpose + " translated", result);
		return result;
	}

	/**
	 * The translations the chart gives the groups of the given owner on a scroll, by their
	 * purpose.
	 */
	private static java.util.Map<String, double[]> translations(GanttLayout layout, Widget owner) {
		java.util.Map<String, double[]> translations = new java.util.HashMap<>();
		((DragController) layout).renderPan((translated, purpose, tx, ty) -> {
			if (translated == owner) {
				translations.put(purpose, new double[] { tx, ty });
			}
		});
		return translations;
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

		boolean _relayout;

		/** The translations by the purpose of the translated group. */
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
		public void updateTransform(Widget owner, String purpose, double translateX, double translateY) {
			_translations.put(purpose, new double[] { translateX, translateY });
		}

		@Override
		public void requestRelayout() {
			_relayout = true;
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
	// Sticky content
	// -----------------------------------------------------------------------

	/** Zoom of the sticky charts: 2 pixels per day. */
	private static final double STICKY_ZOOM = 2.0;

	/** Width of the label column of the sticky charts. */
	private static final double STICKY_COLUMN_WIDTH = 200.0;

	/** Width of the visible time range of the sticky charts: 90 days. */
	private static final double STICKY_VIEW_WIDTH = 90 * STICKY_ZOOM;

	/** Width of the text {@code "Jahr 1"} in the {@link TestingRenderContext}. */
	private static final double STICKY_TEXT_WIDTH = 6 * 12.0;

	/**
	 * Without scrolling, the content of a sticky box stays at the start of its span.
	 */
	public void testStickyWithoutScroll() throws Exception {
		Sticky sticky = Sticky.create().setContent(Text.create().setValue("Jahr 1"));
		GanttLayout layout = stickyChart(sticky);

		assertEquals("sticky takes the span width", 365 * STICKY_ZOOM - 2, sticky.getWidth(), 1e-6);
		assertEquals("content keeps its natural width", STICKY_TEXT_WIDTH, sticky.getContent().getWidth(), 1e-6);
		assertEquals("no shift", 0.0, sticky.getShift(), 1e-9);
		assertEquals("no translation", 0.0, stickyTranslation(layout, sticky), 1e-9);
	}

	/**
	 * Scrolled into the middle of a span, the content of a sticky box starts at the left edge of
	 * the visible time range, and the border around it stays in place.
	 */
	public void testStickyFollowsLeftEdge() throws Exception {
		Sticky sticky = Sticky.create().setContent(Text.create().setValue("Jahr 1"));
		GanttLayout layout = stickyChart(sticky);
		Box border = (Box) sticky.getParent();
		double borderX = border.getX();

		layout.setScrollX(180);
		double visibleLeft = layout.getX() + STICKY_COLUMN_WIDTH + 180 * STICKY_ZOOM;
		double translation = stickyTranslation(layout, sticky);
		assertEquals("content at the left edge", visibleLeft, sticky.getContent().getX() + translation, 1e-6);
		assertEquals("shift stored", translation, sticky.getShift(), 1e-9);
		assertEquals("border stays in place", borderX, border.getX(), 1e-9);

		String svg = renderToSvg(layout.getDiagram());
		assertTrue("drawn with the current shift: " + svg,
			svg.contains("-" + StickyOperations.CONTENT_PURPOSE + "\" transform=\"translate("
				+ translation + ",0.0)\""));
	}

	/**
	 * Scrolled so far that the end of the span is just right of the left edge of the visible
	 * range, the content of a sticky box ends at the end of the span.
	 */
	public void testStickyStopsAtSpanEnd() throws Exception {
		Sticky sticky = Sticky.create().setContent(Text.create().setValue("Jahr 1"));
		GanttLayout layout = stickyChart(sticky);

		layout.setScrollX(360);
		double translation = stickyTranslation(layout, sticky);
		assertEquals("content ends at the end of the sticky box", sticky.getX() + sticky.getWidth(),
			sticky.getContent().getX() + translation + STICKY_TEXT_WIDTH, 1e-6);
	}

	/**
	 * A span scrolled completely out of the visible range to the left keeps the content of its
	 * sticky box at its end.
	 */
	public void testStickyOfSpanLeftOfView() throws Exception {
		Sticky sticky = Sticky.create().setContent(Text.create().setValue("Jahr 1"));
		GanttLayout layout = stickyChart(sticky);

		layout.setScrollX(500);
		double translation = stickyTranslation(layout, sticky);
		assertEquals("maximal shift", sticky.getWidth() - STICKY_TEXT_WIDTH, translation, 1e-6);
	}

	/**
	 * A span without a sticky box is laid out as before: its box fills the span, its content
	 * starts at the start of the span.
	 */
	public void testWithoutStickyUnchanged() throws Exception {
		Text plain = Text.create().setValue("Jahr 1");
		Box plainBorder = Border.create().setContent(plain);
		GanttLayout plainLayout = stickyChartWithBox(plainBorder);

		Sticky sticky = Sticky.create().setContent(Text.create().setValue("Jahr 1"));
		GanttLayout stickyLayout = stickyChart(sticky);
		Box stickyBorder = (Box) sticky.getParent();

		assertEquals("same x", stickyBorder.getX(), plainBorder.getX(), 1e-9);
		assertEquals("same y", stickyBorder.getY(), plainBorder.getY(), 1e-9);
		assertEquals("same width", stickyBorder.getWidth(), plainBorder.getWidth(), 1e-9);
		assertEquals("same height", stickyBorder.getHeight(), plainBorder.getHeight(), 1e-9);
		assertEquals("same row height", stickyLayout.getHeaderHeight(), plainLayout.getHeaderHeight(), 1e-9);

		plainLayout.setScrollX(180);
		Diagram d = plainLayout.getDiagram();
		String svg = renderToSvg(d);
		assertEquals("content stays at the span start", plainBorder.getX() + 1, plain.getX(), 1e-9);
		assertFalse("no sticky group", svg.contains("-" + StickyOperations.CONTENT_PURPOSE + "\""));
	}

	/**
	 * Content of a sticky box wider than the span gets the width of the span, so that a text cut
	 * to its width ends with an ellipsis at the end of the span and does not move.
	 */
	public void testStickyCutsTextToSpanWidth() throws Exception {
		StringBuilder longValue = new StringBuilder();
		for (int n = 0; n < 70; n++) {
			longValue.append('x');
		}
		Text text = Text.create().setValue(longValue.toString()).setEllipsis(true);
		Sticky sticky = Sticky.create().setContent(text);
		GanttLayout layout = stickyChart(sticky);

		assertEquals("content limited to the sticky box", sticky.getWidth(), text.getWidth(), 1e-6);
		assertNotNull("text cut", text.getShownValue());
		assertTrue("cut text fits", (text.getShownValue().length()) * 12.0 <= sticky.getWidth());

		layout.setScrollX(180);
		assertEquals("no room to move", 0.0, stickyTranslation(layout, sticky), 1e-9);
	}

	/**
	 * A level-of-detail box in the content of a sticky box chooses its variant by the width of
	 * the whole span, not by its visible part.
	 */
	public void testStickyLodChoosesBySpanWidth() throws Exception {
		LOD lod = LOD.create().setVariants(Arrays.asList(
			LODVariant.create().setMinWidth(500).setContent(Text.create().setValue("Jahr 1 (lang)")),
			LODVariant.create().setContent(Text.create().setValue("J1"))));
		Sticky sticky = Sticky.create().setContent(lod);
		GanttLayout layout = stickyChart(sticky);
		assertEquals("rich variant for the whole span", 0, lod.getChosenIndex());

		layout.setScrollX(200);
		layout.getDiagram().layout(new TestingRenderContext(), STICKY_COLUMN_WIDTH + STICKY_VIEW_WIDTH, 400);
		assertEquals("rich variant while only a part of the span is visible", 0, lod.getChosenIndex());
		assertEquals("content of the chosen variant at the left edge",
			layout.getX() + STICKY_COLUMN_WIDTH + 200 * STICKY_ZOOM, lod.getX() + stickyTranslation(layout, sticky),
			1e-6);
	}

	/**
	 * A chart with a frozen header row holding a year span 0..365 with the given sticky box in a
	 * border, showing 90 days of its time range 0..730.
	 */
	private static GanttLayout stickyChart(Sticky sticky) {
		return stickyChartWithBox(Border.create().setContent(sticky));
	}

	private static GanttLayout stickyChartWithBox(Box box) {
		GanttSpan year = GanttSpan.create().setId("y1").setRowId("axis").setStart(0).setEnd(365).setBox(box);
		GanttSpan next = span("y2", "axis", 365, 730, "Jahr 2");
		GanttLayout layout = GanttLayout.create()
			.setRowLabelMinWidth(STICKY_COLUMN_WIDTH)
			.setRangeMin(0).setRangeMax(730)
			.setInitialZoom(STICKY_ZOOM).setZoom(STICKY_ZOOM)
			.setFrozenRows(1)
			.setItems(Arrays.asList(year, next));
		addRowsWithLabels(layout, Arrays.asList(row("axis", "Jahr"), row("data", "Daten")));
		layout.addContent(year.getBox());
		layout.addContent(next.getBox());
		Diagram.create().setRoot(layout).layout(new TestingRenderContext(),
			STICKY_COLUMN_WIDTH + STICKY_VIEW_WIDTH, 400);
		assertEquals("label column", STICKY_COLUMN_WIDTH, layout.getColumnWidth(), 1e-9);
		return layout;
	}

	/** The translation the chart gives the content of the given sticky box on a scroll. */
	private static double stickyTranslation(GanttLayout layout, Sticky sticky) {
		double[] result = translations(layout, sticky).get(StickyOperations.CONTENT_PURPOSE);
		assertNotNull("sticky content translated", result);
		assertEquals("only horizontal", 0.0, result[1], 1e-9);
		return result[0];
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

	// -----------------------------------------------------------------------
	// Edge routing through the gaps between rows
	// -----------------------------------------------------------------------

	/**
	 * An edge from the first to the third row runs horizontally in the gap below the first row,
	 * not through the item filling the second row.
	 */
	public void testEdgeToRowBelowRunsInGapBelowSource() throws Exception {
		GanttSpan a = span("a", "r1", 10.0, 30.0, "A");
		GanttSpan middle = span("m", "r2", 0.0, 100.0, "M");
		GanttSpan b = span("b", "r3", 40.0, 60.0, "B");
		GanttLayout layout = threeRowChart(Arrays.asList(a, middle, b), edge("e1", "a", "b"));

		double y = horizontalY(edgeGroup(render(layout)), 0);
		assertTrue("above the middle item: " + y, y < middle.getBox().getY());
		assertTrue("below the source item: " + y, y > a.getBox().getY() + a.getBox().getHeight());
		assertEquals("centered in the gap", DEFAULT_ROW_TOTAL_HEIGHT, y, 1e-9);
	}

	/**
	 * An edge from the third to the first row runs horizontally in the gap above the third row.
	 */
	public void testEdgeToRowAboveRunsInGapAboveSource() throws Exception {
		GanttSpan a = span("a", "r1", 10.0, 30.0, "A");
		GanttSpan middle = span("m", "r2", 0.0, 100.0, "M");
		GanttSpan b = span("b", "r3", 40.0, 60.0, "B");
		GanttLayout layout = threeRowChart(Arrays.asList(a, middle, b), edge("e1", "b", "a"));

		double y = horizontalY(edgeGroup(render(layout)), 0);
		assertTrue("below the middle item: " + y, y > middle.getBox().getY() + middle.getBox().getHeight());
		assertTrue("above the source item: " + y, y < b.getBox().getY());
		assertEquals("centered in the gap", 2 * DEFAULT_ROW_TOTAL_HEIGHT, y, 1e-9);
	}

	/**
	 * Edges passing through the same gap with overlapping horizontal ranges run on separate lanes
	 * within the gap.
	 */
	public void testOverlappingEdgesInSameGapUseSeparateLanes() throws Exception {
		GanttSpan a1 = span("a1", "r1", 10.0, 30.0, "A1");
		GanttSpan a2 = span("a2", "r1", 12.0, 32.0, "A2");
		GanttSpan b = span("b", "r3", 40.0, 60.0, "B");
		GanttLayout layout = threeRowChart(Arrays.asList(a1, a2, b), edge("e1", "a1", "b"), edge("e2", "a2", "b"));

		String edges = edgeGroup(render(layout));
		double y1 = horizontalY(edges, 0);
		double y2 = horizontalY(edges, 1);
		assertTrue("separate lanes: " + y1 + ", " + y2, Math.abs(y1 - y2) > 1.0);
		double gapTop = DEFAULT_ROW_TOTAL_HEIGHT - ROW_PADDING;
		double gapBottom = DEFAULT_ROW_TOTAL_HEIGHT + ROW_PADDING;
		for (double y : new double[] { y1, y2 }) {
			assertTrue("within the gap: " + y, y > gapTop && y < gapBottom);
		}
	}

	/**
	 * Edges passing through the same gap without overlapping horizontal ranges share a lane.
	 */
	public void testDisjointEdgesInSameGapShareLane() throws Exception {
		GanttSpan a1 = span("a1", "r1", 10.0, 20.0, "A1");
		GanttSpan b1 = span("b1", "r3", 25.0, 30.0, "B1");
		GanttSpan a2 = span("a2", "r1", 50.0, 60.0, "A2");
		GanttSpan b2 = span("b2", "r3", 70.0, 80.0, "B2");
		GanttLayout layout =
			threeRowChart(Arrays.asList(a1, b1, a2, b2), edge("e1", "a1", "b1"), edge("e2", "a2", "b2"));

		String edges = edgeGroup(render(layout));
		assertEquals("same lane", horizontalY(edges, 0), horizontalY(edges, 1), 1e-9);
		assertEquals("centered in the gap", DEFAULT_ROW_TOTAL_HEIGHT, horizontalY(edges, 0), 1e-9);
	}

	/**
	 * An edge between adjacent rows runs halfway between its endpoints, as before.
	 */
	public void testEdgeBetweenAdjacentRowsUnchanged() throws Exception {
		String edges = edgeGroup(renderEdge(GanttEdge.create(), 10.0, 30.0, 40.0, 60.0));
		double[] line = pathCoordinates(pathElement(edges, 0));
		double sy = line[1];
		double ty = line[line.length - 1];
		assertEquals("halfway between the endpoints", (sy + ty) / 2, horizontalY(edges, 0), 1e-9);
	}

	/**
	 * The drop areas follow the rows of the last layout pass, and dropping an item into another
	 * row discards the rows of the last layout pass.
	 */
	public void testDragUsesRowsOfLastLayout() {
		GanttSpan span = span("s1", "r1", 10.0, 30.0, "Task");
		GanttLayout layout = buildLayoutWithSpan(span, "r1", "r2");
		Diagram d = Diagram.create().setRoot(layout);
		RenderContext context = new AWTContext(12f);
		d.layout(context);
		DragController drag = layout;
		assertEquals("second row", DEFAULT_ROW_TOTAL_HEIGHT, drag.getDropAreas(span.getBox()).get(1).getY(), 1e-9);

		layout.getRootRows().get(0).setMinContentHeight(100);
		d.layout(context);
		double grownRow = 100 + 2 * ROW_PADDING;
		assertEquals("second row below the grown row", grownRow, drag.getDropAreas(span.getBox()).get(1).getY(),
			1e-9);
		double[] moved = drag.constrainMove(span.getBox(), span.getBox().getX(), grownRow + 10);
		assertEquals("snapped into the second row", grownRow + ROW_PADDING, moved[1], 1e-9);

		drag.commitDrag(span.getBox(), span.getBox().getX(), moved[1], span.getBox().getWidth(),
			span.getBox().getHeight());
		assertEquals("dropped into the second row", "r2", span.getRowId());
		assertNull("rows of the last layout discarded", layout.getRowLayout());

		d.layout(context);
		assertNotNull("rows placed by the layout", layout.getRowLayout());
		assertEquals("span moved to the second row", grownRow + ROW_PADDING, span.getBox().getY(), 1e-9);
	}

	/** Builds a chart with three rows r1, r2, r3, the given items and edges. */
	private static GanttLayout threeRowChart(List<? extends com.top_logic.react.flow.data.GanttItem> items,
			GanttEdge... edges) {
		GanttLayout layout = GanttLayout.create()
			.setRowLabelMinWidth(200.0)
			.setRangeMin(0).setRangeMax(100).setZoom(1.0)
			.setEdges(Arrays.asList(edges));
		for (com.top_logic.react.flow.data.GanttItem item : items) {
			layout.addItem(item);
		}
		addRowsWithLabels(layout, Arrays.asList(row("r1", "Row 1"), row("r2", "Row 2"), row("r3", "Row 3")));
		for (com.top_logic.react.flow.data.GanttItem item : items) {
			layout.addContent(item.getBox());
		}
		return layout;
	}

	/** An edge without symbols from the end of the given source to the start of the given target. */
	private static GanttEdge edge(String id, String source, String target) {
		return GanttEdge.create()
			.setId(id)
			.setSourceItemId(source)
			.setSourceEndpoint(GanttEndpoint.END)
			.setTargetItemId(target)
			.setTargetEndpoint(GanttEndpoint.START);
	}

	private static String render(GanttLayout layout) throws Exception {
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));
		return renderToSvg(d);
	}

	/**
	 * The Y coordinate of the horizontal segment of the edge whose line is the path with the given
	 * index in the edge group.
	 */
	private static double horizontalY(String edges, int index) {
		double[] line = pathCoordinates(pathElement(edges, index));
		// Points: start, source stub, source corner, target corner, target stub, end.
		assertEquals("horizontal segment", line[5], line[7], 1e-9);
		return line[5];
	}

	// -----------------------------------------------------------------------
	// Color roles of edges and decorations
	// -----------------------------------------------------------------------

	/** An edge without color and role is drawn in the neutral role. */
	public void testEdgeWithoutColorIsNeutral() throws Exception {
		String line = pathElement(edgeGroup(renderEdge(GanttEdge.create(), 10.0, 30.0, 40.0, 60.0)), 0);
		assertTrue("neutral role: " + line, line.contains("data-tl-role=\"neutral\""));
		assertTrue("neutral substitute color: " + line,
			line.contains("stroke=\"" + ColorRoles.NEUTRAL_LINE_FALLBACK + "\""));
	}

	/** A violated edge without color and role is drawn in the error role, its symbol as well. */
	public void testViolatedEdgeWithoutColorIsError() throws Exception {
		GanttEdge edge = GanttEdge.create().setTargetSymbol(ConnectorSymbol.FILLED_ARROW);
		// Source ends at 50, target starts at 40: the dependency is violated.
		String edges = edgeGroup(renderEdge(edge, 10.0, 50.0, 40.0, 60.0));
		for (int n = 0; n <= 1; n++) {
			String path = pathElement(edges, n);
			assertTrue("error role: " + path, path.contains("data-tl-role=\"error\""));
			assertTrue("error substitute color: " + path,
				path.contains("stroke=\"" + ColorRoles.lineFallback(ColorRoles.ERROR) + "\""));
		}
	}

	/** A violated edge is drawn in its violated role, whatever its enforcement mode. */
	public void testViolatedEdgeIndependentOfEnforcement() throws Exception {
		GanttSpan a = span("a", "r1", 10.0, 50.0, "A");
		GanttSpan b = span("b", "r2", 40.0, 60.0, "B");
		GanttEdge edge = edge("e1", "a", "b").setEnforce(GanttEnforce.NONE).setViolatedStrokeRole("warning");
		String line = pathElement(edgeGroup(render(threeRowChart(Arrays.asList(a, b), edge))), 0);
		assertTrue("violated role: " + line, line.contains("data-tl-role=\"warning\""));
	}

	/** A fixed color takes precedence over the role: the edge carries no role. */
	public void testEdgeWithColorHasNoRole() throws Exception {
		GanttEdge edge = GanttEdge.create().setStrokeColor("#123456").setStrokeRole("brand");
		String line = pathElement(edgeGroup(renderEdge(edge, 10.0, 30.0, 40.0, 60.0)), 0);
		assertFalse("no role: " + line, line.contains("data-tl-role"));
		assertTrue("fixed color: " + line, line.contains("stroke=\"#123456\""));
	}

	/** A range decoration in a role carries the role and is filled in its substitute color. */
	public void testRangeDecorationWithRole() throws Exception {
		GanttRangeDecoration range = GanttRangeDecoration.create()
			.setId("range")
			.setFrom(60.0).setTo(80.0)
			.setColorRole("category-3");
		String rect = decorationElement(renderDecoration(range), "<rect");
		assertTrue("role: " + rect, rect.contains("data-tl-role=\"category-3\""));
		assertTrue("substitute color: " + rect,
			rect.contains("fill=\"" + ColorRoles.areaFallback("category-3") + "\""));
	}

	/** A line decoration without color and role is drawn in the neutral role. */
	public void testLineDecorationWithoutColorIsNeutral() throws Exception {
		GanttLineDecoration line = GanttLineDecoration.create().setId("line").setAt(50.0);
		String path = decorationElement(renderDecoration(line), "<path");
		assertTrue("neutral role: " + path, path.contains("data-tl-role=\"neutral\""));
	}

	/** A line decoration with a fixed color carries no role. */
	public void testLineDecorationWithColorHasNoRole() throws Exception {
		GanttLineDecoration line =
			GanttLineDecoration.create().setId("line").setAt(50.0).setColor("#3070d0").setColorRole("error");
		String path = decorationElement(renderDecoration(line), "<path");
		assertFalse("no role: " + path, path.contains("data-tl-role"));
	}

	/**
	 * The script function of a decoration sets its default color only if neither color nor role
	 * is given.
	 */
	public void testDecorationFactoryDefaultColorOnlyWithoutRole() {
		GanttRangeDecoration plain = FlowFactory.ganttRangeDeco("plain", 0, 10, null, null, null, null, null, null);
		assertFalse("default color", ColorRoles.isEmpty(plain.getColor()));
		assertNull("no role", plain.getColorRole());

		GanttRangeDecoration withRole =
			FlowFactory.ganttRangeDeco("role", 0, 10, null, null, null, null, null, "category-3");
		assertTrue("no color", ColorRoles.isEmpty(withRole.getColor()));
		assertEquals("role", "category-3", withRole.getColorRole());

		GanttLineDecoration lineWithRole =
			FlowFactory.ganttLineDeco("line", 5, null, null, null, null, null, null, "error");
		assertTrue("no color", ColorRoles.isEmpty(lineWithRole.getColor()));
		assertEquals("role", "error", lineWithRole.getColorRole());
	}

	/** Renders a chart with a single row and the given decoration. */
	private static String renderDecoration(GanttDecoration decoration) throws Exception {
		GanttLayout layout = GanttLayout.create()
			.setRangeMin(0).setRangeMax(100).setZoom(1.0)
			.setDecorations(Arrays.asList(decoration));
		addRowsWithLabels(layout, Arrays.asList(row("r1", "Row 1")));
		return render(layout);
	}

	/** The start tag of the first element with the given tag start in the decoration group. */
	private static String decorationElement(String svg, String tagStart) {
		int group = svg.indexOf("tl-gantt-decorations");
		assertTrue("decoration group present", group >= 0);
		int pos = svg.indexOf(tagStart, group);
		assertTrue(tagStart + " present", pos >= 0);
		return svg.substring(pos, svg.indexOf(">", pos) + 1);
	}

	/**
	 * Dragging the target of an edge before its source turns the edge into its violated style
	 * after the next layout: the role of the violated state, also for its symbol.
	 */
	public void testDragIntoViolationRedrawsInViolatedRole() throws Exception {
		GanttRow r1 = FlowFactory.ganttRow("r1", cell("Row 1"), null, null, null, null, null, null);
		GanttRow r2 = FlowFactory.ganttRow("r2", cell("Row 2"), null, null, null, null, null, null);
		GanttSpan build = FlowFactory.ganttSpan("build", "r1", cell("Build"), 10, 30,
			null, null, null, null, null, null, null, null, null, null);
		GanttPoint beta = FlowFactory.ganttPoint("beta", "r2", cell("Beta"), 50, null, null, null, null, null, null);
		GanttEdge edge = FlowFactory.ganttEdge("e", "build", GanttEndpoint.END, "beta", GanttEndpoint.START, null,
			null, null, null, null, 2.0, null, null, ConnectorSymbol.FILLED_ARROW, null, null);
		GanttLayout layout = FlowFactory.gantt(Arrays.asList(r1, r2), Arrays.asList(build, beta),
			Arrays.asList(edge), null, 0, 100, 1.0, null, null);
		Diagram d = Diagram.create().setRoot(layout);
		RenderContext context = new AWTContext(12f);
		d.layout(context);
		String before = edgeGroup(renderToSvg(d));
		assertTrue("not violated: " + before, pathElement(before, 0).contains("data-tl-role=\"neutral\""));

		Box box = beta.getBox();
		((DragController) layout).commitDrag(box, box.getX() - 40, box.getY(), box.getWidth(), box.getHeight());
		d.layout(context);
		String after = edgeGroup(renderToSvg(d));
		for (int n = 0; n <= 1; n++) {
			String path = pathElement(after, n);
			assertTrue("violated role: " + path, path.contains("data-tl-role=\"error\""));
			assertTrue("violated width: " + path, path.contains("stroke-width=\"2"));
		}
	}

	/**
	 * A chart with a parent row "p" (children "c1", "c2") and a root row "q" below it. Items: "pa"
	 * in "p", "c1a" in "c1", "c2a" in "c2", "qa" in "q". Edges: "pa" to "qa" (both rows stay
	 * visible), "c1a" to "c2a" (inside the parent), "c1a" to "qa" (from a child into a visible
	 * row). Decorations: "only-child" relevant for "c1" only, "child-and-q" relevant for "c2" and
	 * "q".
	 */
	private static GanttLayout collapsibleChart() {
		GanttRow parent = row("p", "Parent");
		parent.setChildren(Arrays.asList(row("c1", "Child 1"), row("c2", "Child 2")));
		List<GanttItem> items = Arrays.asList(
			span("pa", "p", 0, 10, "P task"),
			span("c1a", "c1", 10, 20, "C1 task"),
			span("c2a", "c2", 30, 40, "C2 task"),
			span("qa", "q", 50, 60, "Q task"));
		GanttLayout layout = GanttLayout.create()
			.setRowLabelMinWidth(100.0)
			.setRangeMin(0).setRangeMax(100).setZoom(1.0)
			.setIndentWidth(16.0)
			.setItems(items)
			.setEdges(Arrays.asList(
				collapseEdge("e-pq", "pa", "qa"),
				collapseEdge("e-c1c2", "c1a", "c2a"),
				collapseEdge("e-c1q", "c1a", "qa")))
			.setDecorations(Arrays.asList(
				GanttLineDecoration.create().setId("only-child").setAt(15.0).setColor("#00FF00")
					.setRelevantFor(Arrays.asList("c1")).setLabel(Text.create().setValue("Only child")),
				GanttLineDecoration.create().setId("child-and-q").setAt(45.0).setColor("#0000FF")
					.setRelevantFor(Arrays.asList("c2", "q")).setLabel(Text.create().setValue("Child and Q"))));
		addRowsWithLabels(layout, Arrays.asList(parent, row("q", "Q row")));
		for (GanttItem item : items) {
			layout.addContent(item.getBox());
		}
		return layout;
	}

	private static GanttEdge collapseEdge(String id, String source, String target) {
		return GanttEdge.create()
			.setId(id)
			.setSourceItemId(source).setSourceEndpoint(GanttEndpoint.END)
			.setTargetItemId(target).setTargetEndpoint(GanttEndpoint.START)
			.setEnforce(GanttEnforce.STRICT);
	}

	private static GanttRow rootRow(GanttLayout layout, String id) {
		for (GanttRow root : layout.getRootRows()) {
			if (root.getId().equals(id)) {
				return root;
			}
		}
		throw new AssertionError("No root row " + id);
	}

	/** Lays out and draws the diagram of the given chart, creating the diagram on first use. */
	private static String layoutAndRender(GanttLayout layout) throws Exception {
		Diagram d = layout.getDiagram();
		if (d == null) {
			d = Diagram.create().setRoot(layout);
		}
		d.layout(new AWTContext(12f));
		return renderToSvg(d);
	}

	/**
	 * Without collapsed rows, all rows, items, edges and decorations are shown.
	 */
	public void testExpandedRowsShowChildren() throws Exception {
		GanttLayout layout = collapsibleChart();

		String svg = layoutAndRender(layout);

		GanttRowLayout rows = layout.getRowLayout();
		assertEquals("visible rows", 4, rows.size());
		assertEquals("no hidden rows", 0, rows.hiddenRows().size());
		assertEquals("q is the fourth row", 3, rows.indexOf("q"));
		assertEquals("height", 4 * DEFAULT_ROW_TOTAL_HEIGHT, layout.getHeight(), 0.01);
		for (String text : Arrays.asList("Child 1", "Child 2", "C1 task", "C2 task", "Q task", "Only child",
			"Child and Q")) {
			assertTrue("drawn: " + text, svg.contains(">" + text + "<"));
		}
		assertEquals("edges", 3, countPaths(edgeGroup(svg)));
		assertEquals("decoration lines", 2, countPaths(section(svg, "tl-gantt-decorations", "</g>")));
	}

	/**
	 * A collapsed row hides its children with their items, the edges of these items and the
	 * decorations relevant only for them. The row below starts directly below the collapsed row.
	 */
	public void testCollapsedRowHidesChildren() throws Exception {
		GanttLayout layout = collapsibleChart();
		rootRow(layout, "p").setCollapsed(true);

		String svg = layoutAndRender(layout);

		GanttRowLayout rows = layout.getRowLayout();
		assertEquals("visible rows", 2, rows.size());
		assertEquals("q directly below the collapsed row", 1, rows.indexOf("q"));
		assertEquals("q starts at the end of p", rows.yEnd(0), rows.yStart(1), 0.01);
		assertEquals("height", 2 * DEFAULT_ROW_TOTAL_HEIGHT, layout.getHeight(), 0.01);
		assertFalse("c1 not in the display order", rows.contains("c1"));
		assertTrue("c1 hidden", rows.isHidden("c1"));
		assertTrue("c2 hidden", rows.isHidden("c2"));
		assertNull("no geometry of a hidden row", rows.geometry("c2"));

		for (String text : Arrays.asList("Child 1", "Child 2", "C1 task", "C2 task", "Only child")) {
			assertFalse("not drawn: " + text, svg.contains(">" + text + "<"));
		}
		for (String text : Arrays.asList("Parent", "P task", "Q task", "Child and Q")) {
			assertTrue("drawn: " + text, svg.contains(">" + text + "<"));
		}

		// Only the edge between the two visible rows remains.
		String edges = edgeGroup(svg);
		assertEquals("edges", 1, countPaths(edges));
		double[] edge = pathCoordinates(pathElement(edges, 0));
		GanttItem pa = layout.getItems().get(0);
		GanttItem qa = layout.getItems().get(3);
		assertEquals("edge starts at the parent task", pa.getBox().getX() + pa.getBox().getWidth(), edge[0], 0.01);
		assertEquals("edge ends at the q task", qa.getBox().getX(), edge[edge.length - 2], 0.01);

		// The decoration relevant for a hidden and a visible row spans the visible row only.
		String decorations = section(svg, "tl-gantt-decorations", "</g>");
		assertEquals("decoration lines", 1, countPaths(decorations));
		double[] line = pathCoordinates(pathElement(decorations, 0));
		assertEquals("line from the top of q", rows.yStart(1), line[1], 0.01);
		assertEquals("line to the bottom of q", rows.yEnd(1), line[3], 0.01);
	}

	/**
	 * Collapsing a row does not change the label column, it is measured over all rows.
	 */
	public void testCollapseKeepsLabelColumn() throws Exception {
		GanttLayout layout = collapsibleChart();
		GanttRow wide = row("c3", "A child row with a very long label");
		rootRow(layout, "p").addChildren(wide);
		layout.addContent(wide.getLabel());

		layoutAndRender(layout);
		double expandedWidth = layout.getColumnWidth();
		assertTrue("wide label widens the column", expandedWidth > 100.0);

		rootRow(layout, "p").setCollapsed(true);
		layoutAndRender(layout);
		assertEquals("column width", expandedWidth, layout.getColumnWidth(), 0.01);
	}

	/**
	 * In a chart with a parent row, the labels leave room for the collapse toggle: a label is
	 * indented by one step more than its depth.
	 */
	public void testLabelsLeaveRoomForToggle() throws Exception {
		GanttLayout layout = collapsibleChart();

		layoutAndRender(layout);

		double base = layout.getX() + ROW_LABEL_PADDING;
		GanttRow parent = rootRow(layout, "p");
		assertEquals("root label", base + 16.0, parent.getLabel().getX(), 0.01);
		assertEquals("child label", base + 32.0, parent.getChildren().get(0).getLabel().getX(), 0.01);
		assertEquals("root without children", base + 16.0, rootRow(layout, "q").getLabel().getX(), 0.01);

		GanttLayout flat = GanttLayout.create().setRangeMin(0).setRangeMax(100).setZoom(1.0);
		addRowsWithLabels(flat, Arrays.asList(row("a", "A"), row("b", "B")));
		layoutAndRender(flat);
		assertEquals("flat chart without toggle column", flat.getX() + ROW_LABEL_PADDING,
			flat.getRootRows().get(0).getLabel().getX(), 0.01);
	}

	/**
	 * A row with children has a focusable toggle button named by the label of its row, which
	 * reports whether the children are shown.
	 */
	public void testToggleIsAccessibleButton() throws Exception {
		GanttLayout layout = collapsibleChart();

		String svg = layoutAndRender(layout);

		int pos = svg.indexOf("class=\"" + GanttLayoutOperations.ROW_TOGGLE_CSS_CLASS + "\"");
		assertTrue("toggle present", pos >= 0);
		assertEquals("one toggle", pos,
			svg.lastIndexOf("class=\"" + GanttLayoutOperations.ROW_TOGGLE_CSS_CLASS + "\""));
		String toggle = startTag(svg, svg.lastIndexOf("<g", pos));
		assertTrue(toggle, toggle.contains("tabindex=\"0\""));
		assertTrue(toggle, toggle.contains("role=\"button\""));
		assertTrue(toggle, toggle.contains("aria-expanded=\"true\""));

		String marker = "aria-labelledby=\"";
		int idStart = toggle.indexOf(marker);
		assertTrue("named by a label: " + toggle, idStart >= 0);
		idStart += marker.length();
		String labelId = toggle.substring(idStart, toggle.indexOf('"', idStart));
		int labelPos = svg.indexOf("id=\"" + labelId + "\"");
		assertTrue("label group present", labelPos >= 0);
		assertTrue("label group holds the row label",
			svg.substring(labelPos, svg.indexOf("</g>", labelPos)).contains(">Parent<"));

		// The chevron has fixed paint, so that an export without stylesheet shows it.
		String chevron = startTag(svg, svg.indexOf("<path", pos));
		assertTrue(chevron, chevron.contains("stroke=\"" + GanttLayoutOperations.ROW_TOGGLE_COLOR + "\""));

		rootRow(layout, "p").setCollapsed(true);
		String collapsed = layoutAndRender(layout);
		int collapsedPos = collapsed.indexOf("class=\"" + GanttLayoutOperations.ROW_TOGGLE_CSS_CLASS + "\"");
		assertTrue("collapsed state",
			startTag(collapsed, collapsed.lastIndexOf("<g", collapsedPos)).contains("aria-expanded=\"false\""));
	}

	/**
	 * A click on the toggle, Enter, or Space collapses and expands the row and requests a new
	 * layout; other keys are left alone.
	 */
	public void testToggleSwitchesCollapsed() throws Exception {
		GanttLayout layout = collapsibleChart();
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));
		ToggleRecorder recorder = new ToggleRecorder();
		draw(d, recorder);
		GanttRow parent = rootRow(layout, "p");

		ClickEvent click = new ClickEvent(0, 0);
		recorder._click.onClick(click);
		assertTrue("collapsed by click", parent.isCollapsed());
		assertTrue("click consumed", click._stopped);
		assertTrue("relayout requested", click._relayout);

		KeyEvent other = new KeyEvent("a");
		recorder._key.onKeyDown(other);
		assertTrue("other key ignored", parent.isCollapsed());
		assertFalse("other key not stopped", other._stopped);
		assertFalse("other key keeps its default", other._prevented);
		assertFalse("no relayout for another key", other._relayout);

		KeyEvent enter = new KeyEvent(SVGKeyEvent.KEY_ENTER);
		recorder._key.onKeyDown(enter);
		assertFalse("expanded by Enter", parent.isCollapsed());
		assertTrue("Enter consumed", enter._stopped && enter._prevented);
		assertTrue("relayout requested", enter._relayout);

		KeyEvent space = new KeyEvent(SVGKeyEvent.KEY_SPACE);
		recorder._key.onKeyDown(space);
		assertTrue("collapsed by Space", parent.isCollapsed());
		assertTrue("Space does not scroll the page", space._prevented);
	}

	/**
	 * An edge is drawn as a group naming the items it connects, and the box of an item as a group
	 * naming the item, so that the diagram control can emphasise the edges of an item. Only a
	 * violated edge is marked as violated.
	 */
	public void testEdgesNameTheirItems() throws Exception {
		GanttLayout layout = collapsibleChart();
		// Move "C2 task" before "C1 task" ends: the edge from c1a to c2a is violated.
		((GanttSpan) layout.getItems().get(2)).setStart(15).setEnd(25);

		String svg = layoutAndRender(layout);
		String edges = edgeGroup(svg);

		String violated = edgeStartTag(edges, "c1a", "c2a");
		assertTrue(violated, violated.contains("class=\"" + GanttLayoutOperations.EDGE_CSS_CLASS + " "
			+ GanttLayoutOperations.EDGE_VIOLATED_CSS_CLASS + "\""));
		String satisfied = edgeStartTag(edges, "pa", "qa");
		assertTrue(satisfied, satisfied.contains("class=\"" + GanttLayoutOperations.EDGE_CSS_CLASS + "\""));
		assertFalse("no opacity in the drawing: " + edges, edges.contains("opacity"));

		for (String item : Arrays.asList("pa", "c1a", "c2a", "qa")) {
			int pos = svg.indexOf(GanttLayoutOperations.ITEM_ID_ATTR + "=\"" + item + "\"");
			assertTrue("item group present: " + item, pos >= 0);
		}
		int qaGroup = svg.lastIndexOf("<g", svg.indexOf(GanttLayoutOperations.ITEM_ID_ATTR + "=\"qa\""));
		assertTrue("item group holds the item box",
			svg.substring(qaGroup, groupEnd(svg, qaGroup)).contains(">Q task<"));
	}

	/**
	 * In a chart with a viewport, the item boxes are drawn in groups naming their items as well.
	 */
	public void testViewportItemsNameTheirItems() throws Exception {
		GanttLayout layout = manyRows(0);
		GanttSpan task = span("t", "r5", 10, 20, "Task");
		layout.addItem(task);
		layout.addContent(task.getBox());

		String svg = renderViewport(layout);

		String content = contentSection(svg);
		int pos = content.indexOf(GanttLayoutOperations.ITEM_ID_ATTR + "=\"t\"");
		assertTrue("item group in the content group", pos >= 0);
	}

	/**
	 * The stylesheet of an exported diagram does not subdue edges: an exported chart shows all
	 * edges in full.
	 */
	public void testExportDoesNotSubdueEdges() throws Exception {
		GanttLayout layout = collapsibleChart();
		Diagram d = Diagram.create().setRoot(layout);

		String svg;
		try (java.io.InputStream in = FlowFactory.toSvg(d, "gantt.svg", 12.0, null, null).getStream()) {
			svg = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		}

		assertTrue("edges exported", svg.contains("class=\"" + GanttLayoutOperations.EDGE_CSS_CLASS));
		assertFalse("export styles do not address edges", svg.contains("." + GanttLayoutOperations.EDGE_CSS_CLASS));
		assertFalse("no opacity on edges", edgeGroup(svg).contains("opacity"));
	}

	/** The start tag of the group of the edge between the given items. */
	private static String edgeStartTag(String edges, String source, String target) {
		int pos = edges.indexOf(GanttLayoutOperations.EDGE_SOURCE_ATTR + "=\"" + source + "\" "
			+ GanttLayoutOperations.EDGE_TARGET_ATTR + "=\"" + target + "\"");
		assertTrue("edge " + source + " -> " + target + " present: " + edges, pos >= 0);
		return startTag(edges, edges.lastIndexOf("<g", pos));
	}

	/** {@link SvgTagWriter} recording the handlers of the collapse toggle. */
	private static class ToggleRecorder extends SvgTagWriter {

		SVGClickHandler _click;

		SVGKeyHandler _key;

		ToggleRecorder() {
			super(new TagWriter(new StringWriter()));
		}

		@Override
		public Registration attachOnClick(SVGClickHandler handler, Object sender) {
			if (sender instanceof GanttRow) {
				_click = handler;
			}
			return Registration.NONE;
		}

		@Override
		public Registration attachOnKeyDown(SVGKeyHandler handler, Object sender) {
			_key = handler;
			return Registration.NONE;
		}
	}

	/** {@link SVGKeyEvent} recording what the handler did with it. */
	private static class KeyEvent implements SVGKeyEvent {

		private final String _key;

		boolean _stopped;

		boolean _prevented;

		boolean _relayout;

		KeyEvent(String key) {
			_key = key;
		}

		@Override
		public String getKey() {
			return _key;
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
			_prevented = true;
		}

		@Override
		public void requestRelayout() {
			_relayout = true;
		}
	}

	/**
	 * A color role given to a TL-Script function: a missing argument stays "no role", so that the
	 * default of the element applies (and not the first role of {@link ValueColor}).
	 */
	public void testColorRoleConversion() {
		ToColorRole conversion = new ToColorRole();
		assertNull("no role", conversion.fromScript(null));
		assertEquals("error", conversion.fromScript(ValueColor.ERROR));
		assertEquals("category-3", conversion.fromScript("category-3"));
		try {
			conversion.fromScript("no-such-role");
			fail("Unknown role must be rejected.");
		} catch (TopLogicException ex) {
			// expected
		}
	}
}
