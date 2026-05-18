/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.graphic.flow.server.script;

import java.io.IOException;

import junit.framework.TestCase;

import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.graphic.flow.data.Border;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.HorizontalLayout;
import com.top_logic.graphic.flow.data.Text;
import com.top_logic.graphic.flow.server.script.FlowFactory;

/**
 * Test case for {@link FlowFactory#toSvg(Diagram, String, double, Double, Double)}.
 */
public class TestFlowSvg extends TestCase {

	/**
	 * Test basic SVG generation with default parameters.
	 */
	public void testBasicSvgGeneration() throws IOException {
		// Create a simple diagram
		Diagram diagram = Diagram.create()
			.setRoot(Border.create()
				.setContent(Text.create().setValue("Hello World")));

		// Generate SVG
		BinaryData svg = FlowFactory.toSvg(diagram, "test.svg", 12.0, null, null);

		// Verify result
		assertNotNull("SVG should not be null", svg);
		assertEquals("Content type should be image/svg+xml", "image/svg+xml", svg.getContentType());
		assertEquals("Filename should match", "test.svg", svg.getName());

		// Read SVG content
		String svgContent = StreamUtilities.readAllFromStream(svg);

		// Verify SVG structure
		assertTrue("SVG should start with <svg", svgContent.contains("<svg"));
		assertTrue("SVG should end with </svg>", svgContent.contains("</svg>"));
		assertTrue("SVG should contain text content", svgContent.contains("Hello World"));
	}

	/**
	 * Test SVG generation with custom text size.
	 */
	public void testCustomTextSize() throws IOException {
		Diagram diagram = Diagram.create()
			.setRoot(Text.create().setValue("Test"));

		BinaryData svg = FlowFactory.toSvg(diagram, "large-text.svg", 20.0, null, null);

		assertNotNull("SVG should not be null", svg);
		String svgContent = StreamUtilities.readAllFromStream(svg);
		assertTrue("SVG should contain content", svgContent.contains("<svg"));
	}

	/**
	 * Test SVG generation with fixed dimensions.
	 */
	public void testFixedDimensions() throws IOException {
		Diagram diagram = Diagram.create()
			.setRoot(Text.create().setValue("Fixed Size"));

		BinaryData svg = FlowFactory.toSvg(diagram, "fixed.svg", 12.0, 800.0, 600.0);

		assertNotNull("SVG should not be null", svg);
		String svgContent = StreamUtilities.readAllFromStream(svg);
		assertTrue("SVG should have viewBox", svgContent.contains("viewBox"));
	}

	/**
	 * Test filename with automatic .svg extension.
	 */
	public void testFilenameExtension() {
		Diagram diagram = Diagram.create()
			.setRoot(Text.create().setValue("Test"));

		// Test without extension
		BinaryData svg1 = FlowFactory.toSvg(diagram, "no-extension", 12.0, null, null);
		assertEquals("Should add .svg extension", "no-extension.svg", svg1.getName());

		// Test with extension already present
		BinaryData svg2 = FlowFactory.toSvg(diagram, "has-extension.svg", 12.0, null, null);
		assertEquals("Should keep .svg extension", "has-extension.svg", svg2.getName());
	}

	/**
	 * Test complex diagram with multiple elements.
	 */
	public void testComplexDiagram() throws IOException {
		Diagram diagram = Diagram.create()
			.setRoot(HorizontalLayout.create()
				.addContent(Border.create()
					.setContent(Text.create().setValue("Left")))
				.addContent(Border.create()
					.setContent(Text.create().setValue("Right"))));

		BinaryData svg = FlowFactory.toSvg(diagram, "complex.svg", 12.0, null, null);

		assertNotNull("SVG should not be null", svg);
		String svgContent = StreamUtilities.readAllFromStream(svg);
		assertTrue("SVG should contain Left text", svgContent.contains("Left"));
		assertTrue("SVG should contain Right text", svgContent.contains("Right"));
	}

	/**
	 * Test that SVG is valid XML.
	 */
	public void testValidXml() throws IOException {
		Diagram diagram = Diagram.create()
			.setRoot(Text.create().setValue("XML Test"));

		BinaryData svg = FlowFactory.toSvg(diagram, "valid.svg", 12.0, null, null);
		String svgContent = StreamUtilities.readAllFromStream(svg);

		// Basic XML validation
		assertTrue("SVG must start with opening tag", svgContent.trim().startsWith("<"));
		assertTrue("SVG must end with closing tag", svgContent.trim().endsWith(">"));

		// Count opening and closing svg tags explicitly
		int openCount = countOccurrences(svgContent, "<svg");
		int closeCount = countOccurrences(svgContent, "</svg>");
		assertEquals("Opening and closing svg tags should match", openCount, closeCount);
		assertTrue("Must have at least one svg tag", openCount > 0);
	}

	/**
	 * The generated SVG must inject a default {@code font-size} CSS rule that matches the
	 * measurement scale ({@code textSize} points converted to pixels). Otherwise a {@code <text>}
	 * element without explicit {@code font-size} would inherit the browser default (typically
	 * 16px) regardless of {@code textSize}, leading to text that overflows its measured box.
	 */
	public void testDefaultFontCssInjected() throws IOException {
		Diagram diagram = Diagram.create().setRoot(Text.create().setValue("Hello"));

		// 12pt * 96/72 = 16px
		String svg12 = readSvg(FlowFactory.toSvg(diagram, "d12.svg", 12.0, null, null));
		assertTrue("Default font-family CSS missing: " + svg12,
			svg12.contains("text:not([font-family]):not([class]){font-family:Arial;}"));
		assertTrue("Default font-size CSS for 12pt should be 16px: " + svg12,
			svg12.contains("text:not([font-size]):not([class]){font-size:16px;}"));

		// 24pt * 96/72 = 32px
		String svg24 = readSvg(FlowFactory.toSvg(diagram, "d24.svg", 24.0, null, null));
		assertTrue("Default font-size CSS for 24pt should be 32px: " + svg24,
			svg24.contains("text:not([font-size]):not([class]){font-size:32px;}"));
	}

	/**
	 * The auto-sized viewBox width must grow proportionally with the requested {@code textSize}.
	 * If measurement is hard-coded to a fixed font size (independent of {@code textSize}), the
	 * viewBox width would not change.
	 */
	public void testBoxScalesWithTextSize() throws IOException {
		Diagram d1 = Diagram.create().setRoot(Text.create().setValue("Hello World"));
		Diagram d2 = Diagram.create().setRoot(Text.create().setValue("Hello World"));

		FlowFactory.toSvg(d1, "small.svg", 12.0, null, null);
		FlowFactory.toSvg(d2, "large.svg", 24.0, null, null);

		double w12 = d1.getViewBoxWidth();
		double w24 = d2.getViewBoxWidth();

		assertTrue("Expected viewBox width > 0 for 12pt, got " + w12, w12 > 0);
		assertTrue("Doubling textSize must roughly double the measured width; got "
			+ w12 + " vs " + w24, w24 > 1.8 * w12 && w24 < 2.2 * w12);
	}

	/**
	 * A bold text must measure wider than the same text in the default weight; otherwise the
	 * {@code fontWeight} from the {@link Text} is ignored during measurement.
	 */
	public void testBoldTextIsWider() throws IOException {
		Diagram normal = Diagram.create()
			.setRoot(Text.create().setValue("Hello World"));
		Diagram bold = Diagram.create()
			.setRoot(Text.create().setValue("Hello World").setFontWeight("bold"));

		FlowFactory.toSvg(normal, "normal.svg", 12.0, null, null);
		FlowFactory.toSvg(bold, "bold.svg", 12.0, null, null);

		assertTrue("Bold text must measure wider than regular; got "
			+ normal.getViewBoxWidth() + " vs " + bold.getViewBoxWidth(),
			bold.getViewBoxWidth() > normal.getViewBoxWidth());
	}

	/**
	 * A text with an explicit {@code font-size} must produce a box that scales with that size,
	 * independent of the context's default {@code textSize}.
	 */
	public void testPerTextFontSizeRespected() throws IOException {
		Diagram small = Diagram.create()
			.setRoot(Text.create().setValue("Hello").setFontSize("16px"));
		Diagram large = Diagram.create()
			.setRoot(Text.create().setValue("Hello").setFontSize("32px"));

		FlowFactory.toSvg(small, "small-text.svg", 12.0, null, null);
		FlowFactory.toSvg(large, "large-text.svg", 12.0, null, null);

		assertTrue("Text at 32px must measure roughly twice as wide as text at 16px; got "
			+ small.getViewBoxWidth() + " vs " + large.getViewBoxWidth(),
			large.getViewBoxWidth() > 1.8 * small.getViewBoxWidth()
				&& large.getViewBoxWidth() < 2.2 * small.getViewBoxWidth());
	}

	private static String readSvg(BinaryData svg) throws IOException {
		return StreamUtilities.readAllFromStream(svg);
	}

	/**
	 * Counts the number of non-overlapping occurrences of a substring in a string.
	 */
	private int countOccurrences(String text, String substring) {
		int count = 0;
		int index = 0;
		while ((index = text.indexOf(substring, index)) != -1) {
			count++;
			index += substring.length();
		}
		return count;
	}
}
