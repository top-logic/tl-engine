/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.layer;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * General layer where the items are unordered.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class UnorderedLayer<I> extends AbstractLayer<I> {

	/**
	 * Creates an {@link UnorderedLayer}.
	 */
	public UnorderedLayer() {
		super(new LinkedHashSet<>());
	}

	/**
	 * Creates an {@link UnorderedLayer} for the given {@code items}.
	 */
	public UnorderedLayer(Collection<I> items) {
		super(new LinkedHashSet<>(items));
	}

	/**
	 * Creates an {@link UnorderedLayer} for the given {@link OrderedLayer}.
	 */
	public UnorderedLayer(OrderedLayer<I> layer) {
		super(new LinkedHashSet<>(layer.getAll()));
	}

}
