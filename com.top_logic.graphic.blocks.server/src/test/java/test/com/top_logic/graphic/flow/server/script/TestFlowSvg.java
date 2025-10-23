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
 * Test case for {@link FlowFactory#svg(Diagram, String, double, Double, Double)}.
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
		BinaryData svg = FlowFactory.svg(diagram, "test.svg", 12.0, null, null);

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

		BinaryData svg = FlowFactory.svg(diagram, "large-text.svg", 20.0, null, null);

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

		BinaryData svg = FlowFactory.svg(diagram, "fixed.svg", 12.0, 800.0, 600.0);

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
		BinaryData svg1 = FlowFactory.svg(diagram, "no-extension", 12.0, null, null);
		assertEquals("Should add .svg extension", "no-extension.svg", svg1.getName());

		// Test with extension already present
		BinaryData svg2 = FlowFactory.svg(diagram, "has-extension.svg", 12.0, null, null);
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

		BinaryData svg = FlowFactory.svg(diagram, "complex.svg", 12.0, null, null);

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

		BinaryData svg = FlowFactory.svg(diagram, "valid.svg", 12.0, null, null);
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
