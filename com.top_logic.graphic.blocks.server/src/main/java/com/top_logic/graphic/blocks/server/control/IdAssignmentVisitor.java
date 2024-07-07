/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.server.control;

import com.top_logic.graphic.blocks.model.BlockModel;
import com.top_logic.graphic.blocks.model.visit.DescendingBlockVisitor;
import com.top_logic.layout.FrameScope;

/**
 * {@link DescendingBlockVisitor} assigning IDs to {@link BlockModel}s taken from a
 * {@link FrameScope}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class IdAssignmentVisitor extends DescendingBlockVisitor<Void, FrameScope> {

	/**
	 * Singleton {@link IdAssignmentVisitor} instance.
	 */
	public static final IdAssignmentVisitor INSTANCE = new IdAssignmentVisitor();

	private IdAssignmentVisitor() {
		// Singleton constructor.
	}

	@Override
	protected Void visitBlockModel(BlockModel model, FrameScope arg) {
		model.setId(arg.createNewID());
		return null;
	}
}