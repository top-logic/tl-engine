/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.layout;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.Label;

/**
 * {@link LabelLayout} that places a {@link Label} relative to an {@link Edge}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface EdgeSegmentLabelLayout extends LabelLayout {

	/**
	 * @see #getDistance()
	 */
	String DISTANCE = "distance";

	/**
	 * @see #getOffset()
	 */
	String OFFSET = "offset";

	/**
	 * @see #getAngle()
	 */
	String ANGLE = "angle";

	/**
	 * @see #getAutoRotation()
	 */
	String AUTO_ROTATION = "autoRotation";

	/**
	 * @see #getSide()
	 */
	String SIDE = "side";

	/**
	 * @see #getSegmentIndex()
	 */
	String SEGMENT_INDEX = "segmentIndex";

	/**
	 * @see #getSegmentRatio()
	 */
	String SEGMENT_RATIO = "segmentRatio";

	/**
	 * @see #getPosition()
	 */
	String POSITION = "position";

	/**
	 * On which side of the edge to place the label.
	 */
	@Name(SIDE)
	EdgeSide getSide();

	/**
	 * Where on the edge to place the label.
	 */
	@Name(POSITION)
	EdgePosition getPosition();

	/**
	 * The rotation angle of the label in radians.
	 */
	@Name(ANGLE)
	double getAngle();

	/**
	 * Whether edge labels are automatically rotated according to the angle of the corresponding
	 * edge segment.
	 */
	@Name(AUTO_ROTATION)
	boolean getAutoRotation();

	/**
	 * Distance between the label box and the edge path.
	 */
	@Name(DISTANCE)
	double getDistance();

	/**
	 * Offset of the label box and the edge path relative to the default placement.
	 */
	@Name(OFFSET)
	double getOffset();

	/**
	 * The zero-based index of the segment beginning from the {@link #getSide()} side.
	 */
	@Name(SEGMENT_INDEX)
	int getSegmentIndex();

	/**
	 * The ratio at which to place the label at the segment.
	 * 
	 * <p>
	 * A ratio of 0.0 will place the label at the {@link #getSide()} side of the segment, a ratio of
	 * 1.0 at the opposite side.
	 * </p>
	 */
	@Name(SEGMENT_RATIO)
	@DoubleDefault(0.5)
	double getSegmentRatio();

}
