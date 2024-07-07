/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.content.row.part;

import com.top_logic.graphic.blocks.model.BlockShape;
import com.top_logic.graphic.blocks.model.content.row.BlockRow;
import com.top_logic.graphic.blocks.svg.TextMetrics;

/**
 * Part of a {@link BlockRow} rendered horizontally aligned to a common
 * {@link #getTargetBaseLine()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RowPart extends BlockShape {

	/**
	 * Description of the outline of thie {@link RowPart}.
	 */
	TextMetrics getMetrics();

	/**
	 * Shot-cut for {@link #getMetrics()}.{@link TextMetrics#getWidth() getWidth()}.
	 */
	default double getWidth() {
		return getMetrics().getWidth();
	}

	/**
	 * Shot-cut for {@link #getMetrics()}.{@link TextMetrics#getHeight() getHeight()}.
	 */
	@Override
	default double getHeight() {
		return getMetrics().getHeight();
	}

	/**
	 * Shot-cut for {@link #getMetrics()}.{@link TextMetrics#getBaseLine() getBaseLine()}.
	 */
	default double getBaseLine() {
		return getMetrics().getBaseLine();
	}

	/**
	 * The common base-line of all {@link RowPart}s in the {@link #getOwner() owning}
	 * {@link BlockRow}.
	 */
	double getTargetBaseLine();

	/**
	 * @see #getTargetBaseLine()
	 */
	void setTargetBaseLine(double baseLine);

}
