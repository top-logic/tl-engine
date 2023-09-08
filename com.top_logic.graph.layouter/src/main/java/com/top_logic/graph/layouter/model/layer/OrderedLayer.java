/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.layer;

import java.util.LinkedList;
import java.util.List;

/**
 * General layer where the items are ordered.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class OrderedLayer<I> extends AbstractLayer<I> {

	/**
	 * Creates an {@link OrderedLayer}.
	 */
	public OrderedLayer() {
		super(new LinkedList<>());
	}

	/**
	 * Creates an {@link OrderedLayer} for the given ordered {@code items}.
	 */
	public OrderedLayer(List<I> items) {
		super(new LinkedList<>(items));
	}

	/**
	 * Creates an {@link OrderedLayer} for the given unordered {@code items}. It orders as it comes.
	 */
	public OrderedLayer(UnorderedLayer<I> layer) {
		super(new LinkedList<>(layer.getAll()));
	}

	@Override
	public List<I> getAll() {
		return (List<I>) super.getAll();
	}
}
