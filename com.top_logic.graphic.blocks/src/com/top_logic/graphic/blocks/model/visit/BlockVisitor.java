/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.visit;

import com.top_logic.graphic.blocks.model.BlockModel;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.block.BlockList;
import com.top_logic.graphic.blocks.model.content.mouth.Mouth;
import com.top_logic.graphic.blocks.model.content.row.BlockRow;
import com.top_logic.graphic.blocks.model.content.row.part.LabelDisplay;
import com.top_logic.graphic.blocks.model.content.row.part.SelectInput;
import com.top_logic.graphic.blocks.model.content.row.part.TextInput;

/**
 * Visitor interface for the {@link BlockModel} hierarchy.
 * 
 * @see BlockModel#visit(BlockVisitor, Object)
 * 
 * @param <R>
 *        The result type of the visit.
 * @param <A>
 *        The argument type of the visit.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BlockVisitor<R, A> {

	/**
	 * Visit method for a {@link Block}.
	 */
	R visit(Block model, A arg);

	/**
	 * Visit method for a {@link BlockList}.
	 */
	R visit(BlockList model, A arg);

	/**
	 * Visit method for a {@link BlockRow}.
	 */
	R visit(BlockRow model, A arg);

	/**
	 * Visit method for a {@link SelectInput}.
	 */
	R visit(SelectInput model, A arg);

	/**
	 * Visit method for a {@link TextInput}.
	 */
	R visit(TextInput model, A arg);

	/**
	 * Visit method for a {@link LabelDisplay}.
	 */
	R visit(LabelDisplay model, A arg);

	/**
	 * Visit method for a {@link Mouth}.
	 */
	R visit(Mouth model, A arg);

}
