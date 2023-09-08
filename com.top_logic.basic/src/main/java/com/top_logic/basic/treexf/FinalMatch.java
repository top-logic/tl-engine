/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

/**
 * Wrapper for a {@link Match} that prevents any further modification.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FinalMatch implements Match {

	private final Match _impl;

	/**
	 * Creates a {@link FinalMatch}.
	 * 
	 * @param impl
	 *        The wrapped {@link Match}.
	 */
	public FinalMatch(Match impl) {
		_impl = impl;
	}

	@Override
	public void bind(Capture capture, Node node) {
		throw new UnsupportedOperationException("Unmodifiable.");
	}

	@Override
	public Node getBinding(Capture capture) {
		return _impl.getBinding(capture);
	}

	@Override
	public void clear() {
		// Ignore.
	}

	@Override
	public String getValue(NewIdReplacement expr) {
		return _impl.getValue(expr);
	}

}
