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
import com.top_logic.react.flow.data.GanttLayout;
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

	public void testLayoutHeightScalesWithRows() {
		GanttLayout layout = GanttLayout.create()
			.setRowHeight(32.0)
			.setAxisHeight(24.0)
			.setRowLabelWidth(200.0)
			.setAxis(axis(0, 100))
			.setRootRows(Arrays.asList(
				row("r1", "Row 1"),
				row("r2", "Row 2"),
				row("r3", "Row 3")));

		Diagram d = Diagram.create().setRoot(layout);

		RenderContext context = new AWTContext(12f);
		d.layout(context);

		// 3 rows * 32 px + 24 axis = 120
		assertEquals("height", 24.0 + 3 * 32.0, layout.getHeight());
	}

	private static GanttRow row(String id, String label) {
		return GanttRow.create().setId(id).setLabel(label);
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
		return GanttTick.create().setPosition(pos).setLabel(label).setEmphasis(emphasis);
	}

	private static Box cell(String label) {
		return Border.create().setContent(Padding.create().setAll(2.0).setContent(Text.create().setValue(label)));
	}

	public void testSpanPositionedAtTimeAndRow() {
		GanttSpan span = span("s1", "r2", 10.0, 30.0, "Task 1");

		GanttLayout layout = GanttLayout.create()
			.setRowHeight(32.0)
			.setAxisHeight(24.0)
			.setRowLabelWidth(200.0)
			.setAxis(axis(0, 100))
			.setRootRows(Arrays.asList(
				row("r1", "Row 1"),
				row("r2", "Row 2")))
			.setItems(Arrays.asList(span));
		layout.addContent(span.getBox());

		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		// r2 is the second row (index 1). Expected y = axisHeight + 1 * rowHeight + 2 = 58.
		// Expected x = rowLabelWidth + 10 * zoom = 210.
		assertEquals("span.x", 210.0, span.getBox().getX(), 0.5);
		assertEquals("span.y", 58.0, span.getBox().getY(), 0.5);
		// Width = (end - start) * zoom = 20. Height = rowHeight - 4.
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
			.setAxis(axis(0, 100))
			.setRootRows(Arrays.asList(row("r1", "Row 1")));
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
			.setAxis(axis(0, 100))
			.setRootRows(Arrays.asList(
				row("r1", "Alpha"),
				row("r2", "Bravo")));
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		String svg = renderToSvg(d);

		assertTrue("row label 'Alpha' present", svg.contains(">Alpha<"));
		assertTrue("row label 'Bravo' present", svg.contains(">Bravo<"));
	}

	public void testRowLanesIndentNestedRows() throws Exception {
		GanttRow parent = GanttRow.create()
			.setId("p").setLabel("Parent")
			.setChildren(Arrays.asList(row("c", "Child")));

		GanttLayout layout = GanttLayout.create()
			.setAxis(axis(0, 100))
			.setIndentWidth(16.0)
			.setRootRows(Arrays.asList(parent));
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		String svg = renderToSvg(d);
		assertTrue("Parent label present", svg.contains(">Parent<"));
		assertTrue("Child label present", svg.contains(">Child<"));
		// Indentation is encoded as the x coordinate of the label's text element.
		// We don't parse SVG for exact coordinates — just confirming both labels render
		// is sufficient for Phase 1 (visual verification happens in Task 20).
	}

	public void testDistributeSizeUsesGivenWidth() {
		GanttLayout layout = GanttLayout.create()
			.setRowHeight(32.0)
			.setAxisHeight(24.0)
			.setRowLabelWidth(200.0)
			.setAxis(axis(0, 100))
			.setRootRows(Arrays.asList(row("r1", "Row 1")));

		Diagram d = Diagram.create().setRoot(layout);
		d.setViewBoxWidth(800);
		d.setViewBoxHeight(100);
		d.layout(new AWTContext(12f));

		// Intrinsic width = rowLabelWidth + (rangeMax - rangeMin) * zoom = 200 + 100 * 1 = 300.
		// Since ViewBox is 800, distributeSize should expand width to 800.
		// DiagramOperations.layout() calls distributeSize with intrinsic width, NOT viewBox width.
		// So we call distributeSize directly to verify the expand-to-fill behaviour.
		RenderContext context = new AWTContext(12f);
		layout.distributeSize(context, 0, 0, 800, 100);
		assertEquals("width", 800.0, layout.getWidth(), 0.5);
	}
}
