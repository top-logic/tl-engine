/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.fragments;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * {@link HTMLFragment} displaying the concatenation of other {@link HTMLFragment}s
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConcatenatedFragment implements HTMLFragment {

	private final HTMLFragment[] _fragments;

	/**
	 * Creates a {@link ConcatenatedFragment}.
	 * 
	 * @param fragments
	 *        See {@link #getFragments()}.
	 * 
	 * @see Fragments#concat(HTMLFragment...) Use the factory method!
	 */
	ConcatenatedFragment(HTMLFragment... fragments) {
		_fragments = fragments;
	}

	/**
	 * The parts being concatenated.
	 */
	public final HTMLFragment[] getFragments() {
		return _fragments;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		for (HTMLFragment fragment : getFragments()) {
			fragment.write(context, out);
		}
	}

}