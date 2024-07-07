/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.connector;

import com.top_logic.graphic.blocks.model.block.BlockDimensions;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * {@link ConnectorType} with a spike outline.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BooleanConnector extends ConnectorType {

	private static final double RADIUS = BlockDimensions.CONNECTOR_MAX_HEIGHT;

	private static final double OFFSET = (BlockDimensions.CONNECTOR_WIDTH - 2 * RADIUS) / 2.0;

	/**
	 * Singleton {@link BooleanConnector} instance.
	 */
	public static final BooleanConnector INSTANCE = new BooleanConnector();

	private BooleanConnector() {
		super(ConnectorTypes.BOOLEAN_KIND);
	}

	@Override
	public double getHeight() {
		return RADIUS;
	}

	@Override
	public void outline(SvgWriter out, ConnectorKind kind) {
		boolean rtl = kind.isRightToLeft();
		out.lineToHorizontalRel(flip(rtl, OFFSET));
		out.lineToRel(flip(rtl, RADIUS), RADIUS);
		out.lineToRel(flip(rtl, RADIUS), -RADIUS);
		out.lineToHorizontalRel(flip(rtl, OFFSET));
	}

}
