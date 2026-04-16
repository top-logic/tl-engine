/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.layout;

import java.util.Arrays;

import junit.framework.TestCase;

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

	@SuppressWarnings("unused")
	private static GanttSpan span(String id, String rowId, double start, double end, String label) {
		return GanttSpan.create()
			.setId(id).setRowId(rowId)
			.setStart(start).setEnd(end)
			.setBox(cell(label));
	}
}
