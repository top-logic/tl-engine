/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model.block;

/**
 * Definition of CSS classes used for rendering {@link Block}s and their contents.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BlockCss {

	/**
	 * The bevel outline path of the {@link Block} drawn above the {@link #SHAPE_CLASS}.
	 */
	String BORDER_CLASS = "tlbBorder";

	/**
	 * The path filling the {@link Block}s area.
	 */
	String SHAPE_CLASS = "tlbShape";

	/**
	 * An active highlighted connector path.
	 */
	String CONNECTOR_CLASS = "tlbConnector";

	/**
	 * A label display in a {@link Block}.
	 */
	String LABEL_CSS = "tlbLabel";

	/**
	 * A text input in a {@link Block}.
	 */
	String INPUT_CLASS = "tlbInput";

	/**
	 * A select input in a {@link Block}.
	 */
	String SELECT_CSS = "tlbSelect";

}
