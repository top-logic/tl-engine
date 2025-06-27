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
public class SelectableIndexCreator extends DescendingBoxVisitor<SelectableIndexCreator, Void> {

	private Map<Object, List<SelectableBox>> _index = new HashMap<>();

	/**
	 * The index created.
	 */
	public Map<Object, List<SelectableBox>> getIndex() {
		return _index;
	}

	@Override
	public SelectableIndexCreator visit(SelectableBox self, Void arg) {
		Object userObject = self.getUserObject();
		if (userObject != null) {
			_index.computeIfAbsent(userObject, x -> new ArrayList<>()).add(self);
		}
		return none();
	}

	@Override
	protected SelectableIndexCreator none() {
		return this;
	}

	@Override
	public SelectableIndexCreator apply(SelectableIndexCreator a, SelectableIndexCreator b) {
		return a;
	}

}
