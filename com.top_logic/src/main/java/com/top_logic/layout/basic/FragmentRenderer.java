/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * {@link Renderer} that {@link HTMLFragment#write(DisplayContext, TagWriter) writes} a
 * {@link HTMLFragment}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FragmentRenderer implements Renderer<HTMLFragment> {

	/** Singleton {@link FragmentRenderer}. */
	public static final FragmentRenderer INSTANCE = new FragmentRenderer();
	
	private FragmentRenderer() {
		// Singleton constructor.
	}
	
	@Override
	public void write(DisplayContext context, TagWriter out, HTMLFragment value) throws IOException {
		if (value != null) {
			value.write(context, out);
		}
	}

}
