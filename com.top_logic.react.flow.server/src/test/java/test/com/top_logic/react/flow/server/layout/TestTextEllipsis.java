/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.layout;

import java.io.StringWriter;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.react.flow.data.Empty;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.data.Tooltip;
import com.top_logic.react.flow.operations.TextOperations;
import com.top_logic.react.flow.operations.TooltipOperations;
import com.top_logic.react.flow.server.svg.SvgTagWriter;

/**
 * Test for a {@link Text} cut to its width with {@link Text#isEllipsis()}.
 *
 * <p>
 * The {@link TestingRenderContext} gives every character a width of 12 pixels.
 * </p>
 */
public class TestTextEllipsis extends TestCase {

	private static final String VALUE = "Work package 12";

	/**
	 * A text that fits its width is drawn completely and has no tooltip.
	 */
	public void testFittingTextUnchanged() throws Exception {
		Text text = layout(Text.create().setValue(VALUE).setEllipsis(true), VALUE.length() * 12);

		assertNull(text.getShownValue());
		String svg = render(text);
		assertTrue(svg, svg.contains(">" + VALUE + "<"));
		assertFalse(svg, svg.contains(TooltipOperations.TOOLTIP_ATTR));
	}

	/**
	 * A text wider than its box is cut to the longest start that fits together with the ellipsis,
	 * and shows the full text as tooltip.
	 */
	public void testCutWithEllipsis() throws Exception {
		Text text = layout(Text.create().setValue(VALUE).setEllipsis(true), 90);

		assertEquals("Work p" + TextOperations.ELLIPSIS, text.getShownValue());
		assertEquals("box keeps the given width", 90.0, text.getWidth(), 1e-9);
		String svg = render(text);
		assertTrue(svg, svg.contains(">Work p" + TextOperations.ELLIPSIS + "<"));
		assertTrue(svg, svg.contains(
			TooltipOperations.TOOLTIP_ATTR + "=\"" + TooltipOperations.TEXT_TOOLTIP_PREFIX + VALUE + "\""));
	}

	/**
	 * White space before the ellipsis is dropped.
	 */
	public void testNoSpaceBeforeEllipsis() {
		Text text = layout(Text.create().setValue(VALUE).setEllipsis(true), 72);

		assertEquals("Work" + TextOperations.ELLIPSIS, text.getShownValue());
	}

	/**
	 * A box too narrow for the ellipsis alone shows no text.
	 */
	public void testTooNarrowForEllipsis() {
		Text text = layout(Text.create().setValue(VALUE).setEllipsis(true), 5);

		assertEquals("", text.getShownValue());
	}

	/**
	 * A text that is not marked for cutting is drawn completely, even if it extends beyond its
	 * box.
	 */
	public void testWithoutEllipsisNotCut() throws Exception {
		Text text = layout(Text.create().setValue(VALUE), 90);

		assertNull(text.getShownValue());
		String svg = render(text);
		assertTrue(svg, svg.contains(">" + VALUE + "<"));
		assertFalse(svg, svg.contains(TooltipOperations.TOOLTIP_ATTR));
	}

	/**
	 * A text cut in a narrow box is drawn completely again when it is laid out in a wide box.
	 */
	public void testRelayoutWider() {
		Text text = layout(Text.create().setValue(VALUE).setEllipsis(true), 90);
		layout(text, 500);

		assertNull(text.getShownValue());
	}

	/**
	 * A {@link Tooltip} box declares its text in the same form as a cut text.
	 */
	public void testTooltipBoxDeclaresText() throws Exception {
		Tooltip tooltip = Tooltip.create().setText("Milestone 1").setContent(Empty.create());
		StringWriter buffer = new StringWriter();
		try (TagWriter tagWriter = new TagWriter(buffer);
				SvgTagWriter svgWriter = new SvgTagWriter(tagWriter)) {
			tooltip.draw(svgWriter);
		}
		String svg = buffer.toString();
		assertTrue(svg, svg.contains(
			TooltipOperations.TOOLTIP_ATTR + "=\"" + TooltipOperations.TEXT_TOOLTIP_PREFIX + "Milestone 1\""));
	}

	private static Text layout(Text text, double width) {
		TestingRenderContext context = new TestingRenderContext();
		text.computeIntrinsicSize(context, 0, 0, width, 100);
		text.distributeSize(context, 0, 0, width, text.getHeight());
		return text;
	}

	private static String render(Text text) throws Exception {
		StringWriter buffer = new StringWriter();
		try (TagWriter tagWriter = new TagWriter(buffer);
				SvgTagWriter svgWriter = new SvgTagWriter(tagWriter)) {
			text.draw(svgWriter);
		}
		return buffer.toString();
	}

}
