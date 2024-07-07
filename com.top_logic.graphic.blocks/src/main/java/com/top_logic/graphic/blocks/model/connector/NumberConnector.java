/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.connector;

import com.top_logic.graphic.blocks.model.block.BlockDimensions;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * {@link ConnectorType} with a rectangular notch.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NumberConnector extends ConnectorType {

	private static final double HEIGHT = BlockDimensions.CONNECTOR_MAX_HEIGHT;

	private static final double WIDTH = 2 * HEIGHT;

	private static final double OFFSET = (BlockDimensions.CONNECTOR_WIDTH - WIDTH) / 2.0;

	/**
	 * Singleton {@link NumberConnector} instance.
	 */
	public static final NumberConnector INSTANCE = new NumberConnector();

	private NumberConnector() {
		super(ConnectorTypes.NUMBER_KIND);
	}

	@Override
	public double getHeight() {
		return HEIGHT;
	}

	@Override
	public void outline(SvgWriter out, ConnectorKind kind) {
		boolean rtl = kind.isRightToLeft();
		double deltaW = kind.isFemale() ? BlockDimensions.MOUTH_SPACING_X : 0.0;

		out.lineToHorizontalRel(flip(rtl, OFFSET - deltaW));
		out.lineToVerticalRel(HEIGHT);
		out.lineToHorizontalRel(flip(rtl, WIDTH + 2 * deltaW));
		out.lineToVerticalRel(-HEIGHT);
		out.lineToHorizontalRel(flip(rtl, OFFSET - deltaW));
	}

}
