/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.graphic.flow.data.Box.Visitor;
import com.top_logic.graphic.flow.data.Widget;

/**
 * {@link Visitor} that creates an index of all observed objects pointing to their {@link Widget}
 * nodes.
 */
public class ObservedIndexCreator extends DescendingBoxVisitor<ObservedIndexCreator, Void> {

	private final Map<Object, List<Widget>> _index = new HashMap<>();

	private final Function<Widget, Collection<?>> _observed;

	/** 
	 * Creates a {@link ObservedIndexCreator}.
	 */
	public ObservedIndexCreator(Function<Widget, Collection<?>> observed) {
		_observed = observed;
	}

	/**
	 * The index created.
	 */
	public Map<Object, List<Widget>> getIndex() {
		return _index;
	}

	@Override
	protected ObservedIndexCreator visitBox(Widget self, Void arg) {
		for (Object x : _observed.apply(self)) {
			_index.computeIfAbsent(x, y -> new ArrayList<>()).add(self);
		}
		return super.visitBox(self, arg);
	}

	@Override
	protected ObservedIndexCreator none() {
		return this;
	}

	@Override
	public ObservedIndexCreator apply(ObservedIndexCreator a, ObservedIndexCreator b) {
		return a;
	}

}
