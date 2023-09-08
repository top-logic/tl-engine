/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

/**
 * Special {@link ExprCapture} that performs an additional transformation on the matched
 * {@link Node}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TransformCapture extends ExprCapture {

	private final TreeTransformer _matcher;

	/**
	 * Creates a {@link TransformCapture}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param transformations
	 *        {@link Transformation}s to apply on a matched {@link Node} during
	 *        {@link Node#expand(Match) expansion}.
	 */
	public TransformCapture(String name, Transformation... transformations) {
		super(name);

		_matcher = new TreeTransformer(transformations);
	}

	@Override
	public Node expand(Match match) {
		Node binding = match.getBinding(this);

		return _matcher.transform(new FinalMatch(match), binding);
	}

}
