/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.connector;

import com.top_logic.graphic.blocks.model.block.BlockDimensions;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * {@link ConnectorType} with a round notch.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ValueConnector extends ConnectorType {

	private static final double RADIUS = BlockDimensions.CONNECTOR_MAX_HEIGHT;

	private static final double OFFSET = (BlockDimensions.CONNECTOR_WIDTH - 2 * RADIUS) / 2.0;

	/**
	 * Singleton {@link ValueConnector} instance.
	 */
	public static final ValueConnector INSTANCE = new ValueConnector();

	private ValueConnector() {
		super(ConnectorTypes.VALUE_KIND);
	}

	@Override
	public double getHeight() {
		return RADIUS;
	}

	@Override
	public void outline(SvgWriter out, ConnectorKind kind) {
		boolean rtl = kind.isRightToLeft();
		double deltaW = kind.isFemale() ? 1.0 : 0.0;
		out.lineToHorizontalRel(flip(rtl, OFFSET - deltaW));
		out.roundedCorner(!rtl, flip(rtl, RADIUS + deltaW), RADIUS);
		out.roundedCorner(!rtl, flip(rtl, RADIUS + deltaW), -RADIUS);
		out.lineToHorizontalRel(flip(rtl, OFFSET - deltaW));
	}

}
