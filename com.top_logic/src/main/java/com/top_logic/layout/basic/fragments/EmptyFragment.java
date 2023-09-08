/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.fragments;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * The empty {@link HTMLFragment} rendering nothing.
 * 
 * @see EmptyFragment
 * @see TextFragment
 * @see MessageFragment
 * @see RenderedFragment
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EmptyFragment implements HTMLFragment {

	/**
	 * Singleton {@link EmptyFragment} instance.
	 */
	static final EmptyFragment INSTANCE = new EmptyFragment();

	private EmptyFragment() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		// No contents.
	}

	/**
	 * @see Fragments#empty()
	 */
	static EmptyFragment getInstance() {
		return INSTANCE;
	}

}
