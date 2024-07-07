/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model;

import com.top_logic.graphic.blocks.json.JsonSerializable;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.block.BlockList;
import com.top_logic.graphic.blocks.model.visit.BlockVisitor;

/**
 * Common interface for the block model hierarchy.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BlockModel extends Identified, JsonSerializable<BlockSchema> {

	/**
	 * @see #getId()
	 */
	void setId(String value);

	/**
	 * The container owning this {@link BlockModel}.
	 */
	BlockModel getOwner();

	/**
	 * The top-level group of {@link Block}s this instance is part of.
	 */
	default BlockList top() {
		return getOwner().top();
	}

	/**
	 * Visit method for visiting this model object.
	 *
	 * @see BlockVisitor
	 */
	<R, A> R visit(BlockVisitor<R, A> v, A arg);

}
