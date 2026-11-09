/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.svg;

import java.io.StringWriter;
import java.util.function.Consumer;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.react.flow.server.svg.SvgTagWriter;
import com.top_logic.react.flow.svg.SVGColor;

/**
 * Test case for the portable paint output of {@link SvgTagWriter}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSvgTagWriter extends TestCase {

	public void testOpaqueColor() throws Exception {
		assertRect("fill=\"#FF0000\" stroke=\"#000000\"", out -> {
			out.setFill("red");
			out.setStroke("#000");
		});
	}

	public void testTranslucentColor() throws Exception {
		assertRect("fill=\"#0000FF\" fill-opacity=\"0.4\" stroke=\"#FF0000\" stroke-opacity=\"0.6\"", out -> {
			out.setFill("rgba(0, 0, 255, 0.4)");
			out.setStroke("#FF000099");
		});
	}

	public void testTransparentColor() throws Exception {
		assertRect("fill=\"none\" stroke=\"none\"", out -> {
			out.setFill("transparent");
			out.setStroke(" rgba(1, 2, 3, 0) ");
		});
	}

	public void testSvgColor() throws Exception {
		assertRect("fill=\"#102030\" fill-opacity=\"0.4\"", out -> {
			out.setFill(new SVGColor(0x66102030));
		});
	}

	public void testOpacityCombinedFillFirst() throws Exception {
		assertRect("fill=\"#0000FF\" fill-opacity=\"0.2\"", out -> {
			out.setFill("rgba(0, 0, 255, 0.4)");
			out.setFillOpacity(0.5);
		});
	}

	public void testOpacityCombinedOpacityFirst() throws Exception {
		assertRect("fill=\"#0000FF\" fill-opacity=\"0.2\"", out -> {
			out.setFillOpacity(0.5);
			out.setFill("rgba(0, 0, 255, 0.4)");
		});
	}

	public void testStrokeOpacityCombined() throws Exception {
		assertRect("stroke=\"#00FF00\" stroke-opacity=\"0.3\"", out -> {
			out.setStrokeOpacity(0.5);
			out.setStroke("rgba(0, 255, 0, 0.6)");
		});
	}

	public void testExplicitOpacityOnly() throws Exception {
		assertRect("fill=\"#000000\" fill-opacity=\"0.5\"", out -> {
			out.setFill("black");
			out.setFillOpacity(0.5);
		});
	}

	public void testPassThrough() throws Exception {
		assertRect("fill=\"url(#g)\" stroke=\"none\"", out -> {
			out.setFill("url(#g)");
			out.setStroke("none");
		});
		assertRect("fill=\"currentColor\" fill-opacity=\"0.5\"", out -> {
			out.setFillOpacity(0.5);
			out.setFill("currentColor");
		});
	}

	public void testLastValueWins() throws Exception {
		assertRect("fill=\"#00FF00\"", out -> {
			out.setFill("rgba(255, 0, 0, 0.5)");
			out.setFill("lime");
		});
	}

	public void testRawAttribute() throws Exception {
		assertRect("fill=\"#FF0000\" fill-opacity=\"0.2\"", out -> {
			out.writeAttribute("fill-opacity", "0.5");
			out.writeAttribute("fill", "rgba(255, 0, 0, 0.4)");
		});
	}

	public void testPaintIsPerElement() throws Exception {
		StringWriter buffer = new StringWriter();
		try (TagWriter tagWriter = new TagWriter(buffer);
				SvgTagWriter out = new SvgTagWriter(tagWriter)) {
			out.beginGroup();
			out.setFill("rgba(255, 0, 0, 0.5)");
			out.beginPath();
			out.setStroke("blue");
			out.beginData();
			out.moveToAbs(0, 0);
			out.endData();
			out.endPath();
			out.endGroup();
		}
		assertEquals(
			"<g fill=\"#FF0000\" fill-opacity=\"0.502\"><path d=\"M 0.0,0.0\" stroke=\"#0000FF\"></path></g>",
			buffer.toString());
	}

	private static void assertRect(String expectedPaint, Consumer<SvgTagWriter> paint) throws Exception {
		StringWriter buffer = new StringWriter();
		try (TagWriter tagWriter = new TagWriter(buffer);
				SvgTagWriter out = new SvgTagWriter(tagWriter)) {
			out.beginRect(0, 0, 1, 1);
			paint.accept(out);
			out.endRect();
		}
		assertEquals("<rect x=\"0.0\" y=\"0.0\" width=\"1.0\" height=\"1.0\" " + expectedPaint + "/>",
			buffer.toString());
	}

}
