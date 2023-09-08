/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.layer;

import java.util.List;

import com.top_logic.basic.col.TupleFactory.Pair;

/**
 * General layer with alternating items. The type switches between each adjacent item forth and
 * back.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class AlternatingLayer<F, S> extends OrderedLayer<Pair<F, S>> {

	/**
	 * Creates an {@link AlternatingLayer}.
	 */
	public AlternatingLayer() {
		super();
	}

	/**
	 * Creates an {@link AlternatingLayer} for the given {@code items}.
	 */
	public AlternatingLayer(List<Pair<F, S>> items) {
		super(items);
	}
}
