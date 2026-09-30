/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.control;

/**
 * Common interface of the client-side and server-side control displaying puzzle blocks.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface JSBlocksControlCommon {

	/**
	 * HTML data attribute encoding the block data as JSON string.
	 */
	public String TL_BLOCK_DATA = "data-blockdata";

	/**
	 * HTML data attribute encoding the block schema as JSON string.
	 */
	public String TL_BLOCK_SCHEMA = "data-blockschema";

	/**
	 * The control type for selecting the corresponding client-side implementation for the
	 * server-side rendered control structure.
	 */
	String BLOCKS_CONTROL_TYPE = "blocksControl";

}
