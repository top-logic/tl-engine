/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.visit;

import com.top_logic.graphic.blocks.model.BlockModel;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.block.BlockList;
import com.top_logic.graphic.blocks.model.content.BlockContent;
import com.top_logic.graphic.blocks.model.content.mouth.Mouth;
import com.top_logic.graphic.blocks.model.content.row.BlockRow;
import com.top_logic.graphic.blocks.model.content.row.part.LabelDisplay;
import com.top_logic.graphic.blocks.model.content.row.part.RowPart;
import com.top_logic.graphic.blocks.model.content.row.part.SelectInput;
import com.top_logic.graphic.blocks.model.content.row.part.TextInput;

/**
 * {@link BlockVisitor} descending through the hierarchy of a {@link BlockModel}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DescendingBlockVisitor<R, A> implements BlockVisitor<R, A> {

	@Override
	public R visit(Block model, A arg) {
		for (BlockContent part : model.getParts()) {
			part.visit(this, arg);
		}
		return visitBlockModel(model, arg);
	}

	@Override
	public R visit(BlockList model, A arg) {
		for (Block part : model.contents()) {
			part.visit(this, arg);
		}
		return visitBlockModel(model, arg);
	}

	@Override
	public R visit(Mouth model, A arg) {
		for (Block part : model.contents()) {
			part.visit(this, arg);
		}
		return visitBlockModel(model, arg);
	}

	@Override
	public R visit(BlockRow model, A arg) {
		for (RowPart part : model.getContents()) {
			part.visit(this, arg);
		}
		return visitBlockModel(model, arg);
	}

	@Override
	public R visit(SelectInput model, A arg) {
		return visitBlockModel(model, arg);
	}

	@Override
	public R visit(TextInput model, A arg) {
		return visitBlockModel(model, arg);
	}

	@Override
	public R visit(LabelDisplay model, A arg) {
		return visitBlockModel(model, arg);
	}

	/**
	 * Produces the visit result for the given generic {@link BlockModel}.
	 * 
	 * <p>
	 * This method is called by default from all other visit methods after visiting the hierarchy
	 * below.
	 * </p>
	 * 
	 * @param model
	 *        The visited model object.
	 * @return The result of the visit.
	 */
	protected abstract R visitBlockModel(BlockModel model, A arg);

}
