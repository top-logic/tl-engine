/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.graphic.flow.data.Box.Visitor;
import com.top_logic.graphic.flow.data.SelectableBox;

/**
 * {@link Visitor} that creates an index of all {@link SelectableBox} nodes by their user objects.
 */
public class IndexCreator extends DescendingBoxVisitor<IndexCreator, Void> {

	private Map<Object, List<SelectableBox>> _index = new HashMap<>();

	/**
	 * The index created.
	 */
	public Map<Object, List<SelectableBox>> getIndex() {
		return _index;
	}

	@Override
	public IndexCreator visit(SelectableBox self, Void arg) {
		_index.computeIfAbsent(self.getUserObject(), x -> new ArrayList<>()).add(self);
		return none();
	}

	@Override
	protected IndexCreator none() {
		return this;
	}

	@Override
	public IndexCreator apply(IndexCreator a, IndexCreator b) {
		return a;
	}

}
