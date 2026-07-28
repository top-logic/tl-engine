/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.script;

import java.io.IOException;

import junit.framework.TestCase;

import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.react.flow.data.Border;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.server.script.FlowFactory;

/**
 * Test case for {@link FlowFactory#toSvg(Diagram, String, double, Double, Double)}.
 */
public class TestFlowSvg extends TestCase {

	/**
	 * The generated SVG must declare the font its layout was measured with.
	 *
	 * <p>
	 * A {@code <text>} without an explicit font otherwise inherits the viewer's default (typically
	 * 16px sans-serif) regardless of the requested text size, so the rendered text no longer
	 * matches the box measured for it.
	 * </p>
	 */
	public void testDefaultFontCssInjected() throws IOException {
		String svg12 = readSvg(toSvg(12.0));
		assertTrue("Default font-family CSS missing: " + svg12,
			svg12.contains("text:not([font-family]):not([class]){font-family:Arial;}"));
		assertTrue("Default font-size CSS for text size 12 should be 12px: " + svg12,
			svg12.contains("text:not([font-size]):not([class]){font-size:12px;}"));

		String svg24 = readSvg(toSvg(24.0));
		assertTrue("Default font-size CSS for text size 24 should be 24px: " + svg24,
			svg24.contains("text:not([font-size]):not([class]){font-size:24px;}"));
	}

	private static BinaryData toSvg(double textSize) {
		Diagram diagram = Diagram.create()
			.setRoot(Border.create().setContent(Text.create().setValue("Hello World")));
		return FlowFactory.toSvg(diagram, "TestFlowSvg-default-font.svg", textSize, null, null);
	}

	private static String readSvg(BinaryData svg) throws IOException {
		return StreamUtilities.readAllFromStream(svg);
	}
}
