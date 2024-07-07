/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.connector;

import com.top_logic.graphic.blocks.model.block.BlockDimensions;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * {@link ConnectorType} with a trapezoid notch.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SequenceConnector extends ConnectorType {

	private static final double RAMP_X = 5;

	private static final double RAMP_Y = BlockDimensions.CONNECTOR_MAX_HEIGHT;

	/**
	 * Singleton {@link SequenceConnector} instance.
	 */
	public static final SequenceConnector INSTANCE = new SequenceConnector();

	private SequenceConnector() {
		super(ConnectorTypes.SEQUENCE_KIND);
	}

	@Override
	public double getHeight() {
		return RAMP_Y;
	}

	@Override
	public void outline(SvgWriter out, ConnectorKind kind) {
		boolean rtl = kind.isRightToLeft();
		out.lineToRel(flip(rtl, RAMP_X), RAMP_Y);
		out.lineToHorizontalRel(flip(rtl, BlockDimensions.CONNECTOR_WIDTH - 2 * RAMP_X));
		out.lineToRel(flip(rtl, RAMP_X), -RAMP_Y);
	}

}