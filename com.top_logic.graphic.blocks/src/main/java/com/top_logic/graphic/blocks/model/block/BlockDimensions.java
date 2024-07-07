/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.block;

import com.top_logic.graphic.blocks.model.connector.ConnectorKind;

/**
 * Constants defining the graphical outlines of {@link Block}s and their content.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BlockDimensions {

	/**
	 * X radius of the rounded corners of the left side of a block.
	 */
	double BLOCK_LEFT_ROUND_X = 2;

	/**
	 * Y radius of the rounded corners of the left side of a block.
	 */
	double BLOCK_LEFT_ROUND_Y = 2;

	/**
	 * X radius of the rounded corners of the right side of a block.
	 */
	double BLOCK_RIGHT_ROUND_X = 2;

	/**
	 * Y radius of the rounded corners of the right side of a block.
	 */
	double BLOCK_RIGHT_ROUND_Y = 2;

	/**
	 * Minimum padding to use at the top side of a block.
	 * 
	 * <p>
	 * The top connector of the block may require more space. If this is the case, the top padding
	 * is automatically increased.
	 * </p>
	 */
	double BLOCK_MIN_PADDING_TOP = Math.max(BLOCK_LEFT_ROUND_Y, BLOCK_RIGHT_ROUND_Y);

	/**
	 * Padding at the bottom side of a block.
	 */
	double BLOCK_PADDING_BOTTOM = Math.max(BLOCK_LEFT_ROUND_Y, BLOCK_RIGHT_ROUND_Y) + 1;

	/**
	 * Padding at the left side of a block.
	 */
	double BLOCK_PADDING_LEFT = BLOCK_LEFT_ROUND_X + 2;

	/**
	 * Padding at the right side of a block.
	 */
	double BLOCK_PADDING_RIGHT = BLOCK_RIGHT_ROUND_X + 2;

	/**
	 * Minimum width to use for a block that has no content at all, or for a block whose content is
	 * smaller.
	 */
	double BLOCK_MINIMUM_CONTENT_WIDTH = 50;

	/**
	 * Space that vertically separates two consecutive blocks in a block list.
	 */
	double BLOCKLIST_SPACING_Y = 1;

	/**
	 * Offset of a block's connector from the left border of its owning block and offset of a
	 * mouth's connector from the left border of it's owning mouth.
	 */
	double CONNECTOR_OFFSET = 10;

	/**
	 * Width that a connector is allowed (and requested) to use wihin the outline of a block and
	 * mouth.
	 */
	double CONNECTOR_WIDTH = 20;

	/**
	 * The height of a connector with a notch outline.
	 */
	double CONNECTOR_MAX_HEIGHT = 5;

	/**
	 * Height of the sensitive area above (or below depending on the {@link ConnectorKind}) a
	 * connector where a matching block can snap in when being dropped.
	 */
	double CONNECTOR_SENSITIVE_HEIGHT = 10;

	/**
	 * Padding that the sensitive area of a connector is wider than the {@value #CONNECTOR_WIDTH}
	 * (on both sides of a connector).
	 */
	double CONNECTOR_SENSITIVE_PADDING_X = 10;

	/**
	 * The distance the highlight outline of an active connector is moved to the inside of the block
	 * when drawn.
	 * 
	 * <p>
	 * Since the highlight outline stroke width is larger than the regular outline stroke width, it
	 * must be moved to the inside of the block to prevent the highlight outline from overlapping
	 * the block outline an being cropped by adjacent blocks.
	 * </p>
	 */
	double CONNECTOR_HIGHLIGHT_OFFSET_Y = 1.0;

	/**
	 * The distance of the left border of a mouth from the left border of its owning block.
	 */
	double MOUTH_PADDING_LEFT = 20.0;

	/**
	 * The distance between the left outline of a mouth from the left border of its contained
	 * blocks.
	 */
	double MOUTH_SPACING_X = BLOCKLIST_SPACING_Y;

	/**
	 * X radius of the inner (left) corners of a mouth's outline.
	 */
	double MOUTH_INNER_ROUND_X = BLOCK_LEFT_ROUND_X + MOUTH_SPACING_X;

	/**
	 * Y radius of the inner (left) corners of a mouth's outline.
	 */
	double MOUTH_INNER_ROUND_Y = BLOCK_LEFT_ROUND_Y + BLOCKLIST_SPACING_Y;

	/**
	 * X radius of the outer (right) corners of a mouth's outline.
	 */
	double MOUTH_OUTER_ROUND_X = BLOCK_RIGHT_ROUND_X;

	/**
	 * Y radius of the outer (right) corners of a mouth's outline.
	 */
	double MOUTH_OUTER_ROUND_Y = BLOCK_RIGHT_ROUND_Y;

	/**
	 * Distance of the top of a mouth's occupied area to the top outline of the mouth being drawn.
	 */
	double MOUTH_PADDING_TOP = Math.max(MOUTH_INNER_ROUND_Y, MOUTH_OUTER_ROUND_Y) + 1;

	/**
	 * Distance of bottom outline of the mouth being drawn to the bottom of a mouth's occupied area.
	 */
	double MOUTH_PADDING_BOTTOM = Math.max(MOUTH_INNER_ROUND_Y, MOUTH_OUTER_ROUND_Y) + 3;

	/**
	 * The content height of an empty mouth (containing no blocks).
	 */
	double MOUTH_EMPTY_CONTENT_HEIGHT = 15.0;

	/**
	 * Padding on the left side of a block's content row before the first label or input is being
	 * drawn.
	 */
	double ROW_PADDING_LEFT = 5;

	/**
	 * Distance between vertical borders of a text input field and its contained text.
	 */
	double TEXT_PADDING_HORIZONTAL = 2.0;

	/**
	 * Rounded corner radius of a text input field.
	 */
	double TEXT_RADIUS = 1.0;

	/**
	 * Height of the triangle in a select input field shown after the text of the selected option.
	 * 
	 * <p>
	 * The width of the triangle is twice as wide as its height.
	 * </p>
	 */
	double SELECT_MARKER_HEIGHT = 3.0;

	/**
	 * Padding around the text of a displayed option in a select input field.
	 * 
	 * @see #SELECT_PADDING_RIGHT
	 */
	double SELECT_PADDING = 3.0;

	/**
	 * Padding on the right side of the text of a displayed option in a select input field.
	 * 
	 * <p>
	 * This padding must include space for displaying the select marker.
	 * </p>
	 * 
	 * @see #SELECT_MARKER_HEIGHT
	 */
	double SELECT_PADDING_RIGHT = 15.0;

	/**
	 * Radius of the rounded corners of a select input field's border.
	 */
	double SELECT_RADIUS = 1.0;

}
