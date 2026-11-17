/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.layout;

import junit.framework.TestCase;

import com.top_logic.react.flow.data.Border;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.Empty;
import com.top_logic.react.flow.data.LOD;
import com.top_logic.react.flow.data.LODVariant;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.operations.BorderOperations;
import com.top_logic.react.flow.svg.RenderContext;

/**
 * Tests for the intrinsic sizing of {@link BorderOperations}.
 */
public class TestBorderLayout extends TestCase {

	private static final String LONG_LABEL = "September";

	private static final String SHORT_LABEL = "Sep";

	private static final int LONG_INDEX = 0;

	private static final int SHORT_INDEX = 1;

	private static final double OFFSET_X = 10;

	private static final double OFFSET_Y = 20;

	private static final double AVAILABLE_HEIGHT = 100;

	private final RenderContext _context = new TestingRenderContext();

	/**
	 * A variant whose width lies within the thickness of the left line must not be chosen, since the
	 * border including its line would exceed the offered width.
	 */
	public void testLeftLineReducesWidthOfferedToContent() {
		LOD labels = labels();
		Border border = leftLine(labels);

		double available = textWidth(LONG_LABEL) + 0.5;
		border.computeIntrinsicSize(_context, OFFSET_X, OFFSET_Y, available, AVAILABLE_HEIGHT);

		assertEquals(SHORT_INDEX, labels.getChosenIndex());
		assertTrue("Border wider than offered: " + border.getWidth(), border.getWidth() <= available);
		assertEquals(1 + textWidth(SHORT_LABEL), border.getWidth());
	}

	/**
	 * With enough width for the content and the line, the richest variant is chosen.
	 */
	public void testLeftLineWithEnoughWidth() {
		LOD labels = labels();
		Border border = leftLine(labels);

		double available = textWidth(LONG_LABEL) + 1;
		border.computeIntrinsicSize(_context, OFFSET_X, OFFSET_Y, available, AVAILABLE_HEIGHT);

		assertEquals(LONG_INDEX, labels.getChosenIndex());
		assertEquals(available, border.getWidth());
	}

	/**
	 * A border drawn on all sides measures its content inside its lines and adds the lines to the
	 * size of the content.
	 */
	public void testAllSidesOffsetAndReduceContent() {
		double thickness = 3;
		double availableWidth = 200;
		double availableHeight = 50;
		double innerWidth = availableWidth - 2 * thickness;
		double innerHeight = availableHeight - 2 * thickness;

		Text text = Text.create().setValue(SHORT_LABEL);
		LOD content = LOD.create()
			.addVariant(LODVariant.create().setMinWidth(innerWidth + 0.5).setContent(Empty.create()))
			.addVariant(LODVariant.create().setMinHeight(innerHeight + 0.5).setContent(Empty.create()))
			.addVariant(LODVariant.create().setMinWidth(innerWidth).setMinHeight(innerHeight).setContent(text))
			.addVariant(LODVariant.create().setContent(Empty.create()));
		Border border = Border.create().setThickness(thickness).setContent(content);

		border.computeIntrinsicSize(_context, OFFSET_X, OFFSET_Y, availableWidth, availableHeight);

		assertEquals(2, content.getChosenIndex());
		assertEquals(OFFSET_X + thickness, text.getX());
		assertEquals(OFFSET_Y + thickness, text.getY());
		assertEquals(OFFSET_X, border.getX());
		assertEquals(OFFSET_Y, border.getY());
		assertEquals(text.getWidth() + 2 * thickness, border.getWidth());
		assertEquals(text.getHeight() + 2 * thickness, border.getHeight());
	}

	/**
	 * A border without lines measures exactly like its content.
	 */
	public void testNoLines() {
		LOD labels = labels();
		Border border = Border.create()
			.setTop(false).setLeft(false).setBottom(false).setRight(false)
			.setContent(labels);

		double available = textWidth(LONG_LABEL);
		border.computeIntrinsicSize(_context, OFFSET_X, OFFSET_Y, available, AVAILABLE_HEIGHT);

		assertEquals(LONG_INDEX, labels.getChosenIndex());
		assertEquals(OFFSET_X, labels.getX());
		assertEquals(OFFSET_Y, labels.getY());
		assertEquals(labels.getWidth(), border.getWidth());
		assertEquals(labels.getHeight(), border.getHeight());
	}

	private static Border leftLine(Box content) {
		return Border.create()
			.setTop(false).setBottom(false).setRight(false)
			.setThickness(1)
			.setContent(content);
	}

	private static LOD labels() {
		return LOD.create()
			.addVariant(LODVariant.create().setContent(Text.create().setValue(LONG_LABEL)))
			.addVariant(LODVariant.create().setContent(Text.create().setValue(SHORT_LABEL)))
			.addVariant(LODVariant.create().setContent(Empty.create()));
	}

	private double textWidth(String text) {
		return _context.measure(text).getWidth();
	}

}
