/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.content.row.part;

import com.top_logic.graphic.blocks.model.AbstractBlockModel;
import com.top_logic.graphic.blocks.model.content.row.BlockRow;
import com.top_logic.graphic.blocks.svg.RenderContext;

/**
 * Common base class for all {@link RowPart}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractRowPart<T extends RowPartType> extends AbstractBlockModel implements RowPart {

	private final BlockRow _owner;

	private T _type;

	private double _baseLine;

	private double _offsetX;

	private double _offsetY;

	/**
	 * Creates a {@link AbstractRowPart}.
	 */
	public AbstractRowPart(BlockRow owner, T type) {
		_owner = owner;
		_type = type;
	}


	@Override
	public BlockRow getOwner() {
		return _owner;
	}

	/**
	 * The {@link RowPartType} of this {@link RowPart}.
	 */
	public T getType() {
		return _type;
	}

	@Override
	public void updateDimensions(RenderContext context, double offsetX, double offsetY) {
		_offsetX = offsetX;
		_offsetY = offsetY;
	}

	/**
	 * The X offset of this {@link RowPart} to the left border of the content area of the
	 * {@link #getOwner() owning} {@link BlockRow}.
	 */
	public double getOffsetX() {
		return _offsetX;
	}

	/**
	 * The Y offset of this {@link RowPart} to the left border of the content area of the
	 * {@link #getOwner() owning} {@link BlockRow}.
	 */
	public double getOffsetY() {
		return _offsetY;
	}

	@Override
	public double getTargetBaseLine() {
		return _baseLine;
	}

	@Override
	public void setTargetBaseLine(double baseLine) {
		_baseLine = baseLine;
	}
}
