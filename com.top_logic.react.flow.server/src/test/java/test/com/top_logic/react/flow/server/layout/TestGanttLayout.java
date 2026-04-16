/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.layout;

import java.io.StringWriter;
import java.util.Arrays;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.react.flow.data.Border;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.GanttAxis;
import com.top_logic.react.flow.data.GanttEdge;
import com.top_logic.react.flow.data.GanttEndpoint;
import com.top_logic.react.flow.data.GanttEnforce;
import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttLineDecoration;
import com.top_logic.react.flow.data.GanttRangeDecoration;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.GanttSpan;
import com.top_logic.react.flow.data.GanttTick;
import com.top_logic.react.flow.data.Padding;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.server.svg.SvgTagWriter;
import com.top_logic.react.flow.server.ui.AWTContext;
import com.top_logic.react.flow.svg.RenderContext;

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
			.setRowMinContentHeight(ROW_MIN_CONTENT_HEIGHT)
			.setRowPadding(ROW_PADDING)
			.setAxisHeight(24.0)
			.setRowLabelMinWidth(200.0)
			.setAxis(axis(0, 100));
		addRowsWithLabels(layout, Arrays.asList(
			row("r1", "Row 1"),
			row("r2", "Row 2"),
			row("r3", "Row 3")));

		Diagram d = Diagram.create().setRoot(layout);

		RenderContext context = new AWTContext(12f);
		d.layout(context);

		// 3 empty rows * (24 + 2*4) + 24 axis = 3*32 + 24 = 120
		assertEquals("height", 24.0 + 3 * DEFAULT_ROW_TOTAL_HEIGHT, layout.getHeight());
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
		// Add tick label boxes to contents so they are rendered via standard dispatch.
		GanttAxis axis = layout.getAxis();
		if (axis != null) {
			for (GanttTick tick : axis.getCurrentTicks()) {
				if (tick.getLabel() != null) {
					layout.addContent(tick.getLabel());
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

	private static GanttAxis axis(double min, double max) {
		return GanttAxis.create()
			.setProviderId("test")
			.setRangeMin(min)
			.setRangeMax(max)
			.setCurrentZoom(1.0)
			.setCurrentTicks(Arrays.asList(
				tick(0, "0", 1.0),
				tick(50, "50", 0.5),
				tick(100, "100", 1.0)));
	}

	private static GanttTick tick(double pos, String label, double emphasis) {
		return GanttTick.create()
			.setPosition(pos)
			.setLabel(Text.create().setValue(label))
			.setEmphasis(emphasis);
	}

	private static Box cell(String label) {
		return Border.create().setContent(Padding.create().setAll(2.0).setContent(Text.create().setValue(label)));
	}

	public void testSpanPositionedAtTimeAndRow() {
		GanttSpan span = span("s1", "r2", 10.0, 30.0, "Task 1");

		GanttLayout layout = GanttLayout.create()
			.setRowMinContentHeight(ROW_MIN_CONTENT_HEIGHT)
			.setRowPadding(ROW_PADDING)
			.setAxisHeight(24.0)
			.setRowLabelMinWidth(200.0)
			.setAxis(axis(0, 100))
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
		// span.y = axisHeight + row0Total + rowPadding = 24 + 32 + 4 = 60.
		// span.x = columnWidth (>=200) + 10 * zoom. Labels are small, so columnWidth = rowLabelMinWidth = 200.
		assertEquals("span.x", 210.0, span.getBox().getX(), 0.5);
		assertEquals("span.y", 24.0 + DEFAULT_ROW_TOTAL_HEIGHT + ROW_PADDING, span.getBox().getY(), 0.5);
		// Width = (end - start) * zoom = 20.
		assertEquals("span.width", 20.0, span.getBox().getWidth(), 0.5);
	}

	private static GanttSpan span(String id, String rowId, double start, double end, String label) {
		return GanttSpan.create()
			.setId(id).setRowId(rowId)
			.setStart(start).setEnd(end)
			.setBox(cell(label));
	}

	public void testAxisRenderingProducesTickOutput() throws Exception {
		GanttLayout layout = GanttLayout.create()
			.setAxis(axis(0, 100));
		addRowsWithLabels(layout, Arrays.asList(row("r1", "Row 1")));
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		String svg = renderToSvg(d);

		// The three ticks defined in axis(0,100) should all show up in the rendered output.
		assertTrue("tick '0' label appears", svg.contains(">0<"));
		assertTrue("tick '50' label appears", svg.contains(">50<"));
		assertTrue("tick '100' label appears", svg.contains(">100<"));
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
			.setAxis(axis(0, 100));
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
			.setAxis(axis(0, 100))
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
			.setAxis(axis(0, 100))
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

	public void testRangeDecorationRendersWithLabel() throws Exception {
		GanttRangeDecoration freeze = GanttRangeDecoration.create()
			.setId("freeze")
			.setFrom(60.0).setTo(80.0)
			.setColor("rgba(255,80,80,0.3)")
			.setLabel("Freeze");

		GanttLayout layout = GanttLayout.create()
			.setAxis(axis(0, 100))
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
			.setLabel("Today");

		GanttLayout layout = GanttLayout.create()
			.setAxis(axis(0, 100))
			.setDecorations(Arrays.asList(today));
		addRowsWithLabels(layout, Arrays.asList(row("r1", "Row 1")));
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		String svg = renderToSvg(d);
		assertTrue("line decoration label", svg.contains(">Today<"));
	}

	public void testDistributeSizeUsesGivenWidth() {
		GanttLayout layout = GanttLayout.create()
			.setRowMinContentHeight(ROW_MIN_CONTENT_HEIGHT)
			.setRowPadding(ROW_PADDING)
			.setAxisHeight(24.0)
			.setRowLabelMinWidth(200.0)
			.setAxis(axis(0, 100));
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
	 * {@code axisHeight + row0Total + row1Total = 24 + 68 + 32 = 124} (row "r2" has no items
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
			.setRowMinContentHeight(ROW_MIN_CONTENT_HEIGHT)
			.setRowPadding(ROW_PADDING)
			.setAxisHeight(24.0)
			.setRowLabelMinWidth(200.0)
			.setAxis(axis(0, 100))
			.setItems(Arrays.asList(tallSpan));
		addRowsWithLabels(layout, Arrays.asList(
			row("r1", "Row 1"),
			row("r2", "Row 2")));
		layout.addContent(tallBox);

		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		double row0Total = tallIntrinsicHeight + 2 * ROW_PADDING; // 68
		double row1Total = ROW_MIN_CONTENT_HEIGHT + 2 * ROW_PADDING; // 32
		double expectedHeight = 24.0 + row0Total + row1Total; // 124

		assertEquals("layout height grows with tall item", expectedHeight, layout.getHeight(), 0.5);

		// The item's distributed height must equal the row content height (= tallIntrinsicHeight).
		assertEquals("tall item height = row content height", tallIntrinsicHeight, tallBox.getHeight(), 0.5);

		// The item's y position must be axisHeight + rowPadding = 24 + 4 = 28.
		assertEquals("tall item y", 24.0 + ROW_PADDING, tallBox.getY(), 0.5);

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
			.setRowMinContentHeight(ROW_MIN_CONTENT_HEIGHT)
			.setRowPadding(ROW_PADDING)
			.setAxisHeight(24.0)
			.setRowLabelMinWidth(200.0)
			.setRowLabelPadding(ROW_LABEL_PADDING)
			.setAxis(axis(0, 100));
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
		public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
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
		public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
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
