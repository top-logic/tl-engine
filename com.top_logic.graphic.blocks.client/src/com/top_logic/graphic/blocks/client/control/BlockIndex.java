/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.client.control;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.graphic.blocks.model.BlockModel;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.visit.DescendingBlockVisitor;

/**
 * Index of {@link Block}s based on their {@link Block#getId()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BlockIndex extends DescendingBlockVisitor<Void, Void> {
	
	private final Map<String, Block> _index = new HashMap<>();

	@Override
	public Void visit(Block model, Void arg) {
		_index.put(model.getId(), model);
		return super.visit(model, arg);
	}

	@Override
	protected Void visitBlockModel(BlockModel model, Void arg) {
		return null;
	}

	/**
	 * Adds the given {@link BlockModel} hierarchy to this index.
	 */
	public void addAll(BlockModel model) {
		model.visit(this, null);
	}

	/**
	 * Retrieves the {@link Block} element with the given ID.
	 */
	public Block getModel(String id) {
		return _index.get(id);
	}

	/**
	 * All {@link Block}s currently indexed.
	 */
	public Collection<Block> getBlocks() {
		return _index.values();
	}

}
