/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model.connector;

import com.top_logic.graphic.blocks.model.block.BlockDimensions;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * {@link ConnectorType} with no notch.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class VoidConnector extends ConnectorType {

	/**
	 * Singleton {@link VoidConnector} instance.
	 */
	public static ConnectorType INSTANCE = new VoidConnector(ConnectorTypes.VOID_KIND);

	/** 
	 * Creates a {@link VoidConnector}.
	 */
	private VoidConnector(String name) {
		super(name);
	}

	@Override
	public double getHeight() {
		return 0;
	}

	@Override
	public void outline(SvgWriter out, ConnectorKind kind) {
		boolean rtl = kind.isRightToLeft();
		out.lineToHorizontalRel(flip(rtl, BlockDimensions.CONNECTOR_WIDTH));
	}

}