/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.View;

/**
 * {@link View} that displays an {@link HTMLFragment}.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HTMLFragmentView extends DefaultView {

	private final HTMLFragment _htmlFragment;

	/**
	 * Creates a new {@link HTMLFragmentView}.
	 * 
	 * @param htmlFragment
	 *        The rendered {@link HTMLFragment}.
	 */
	public HTMLFragmentView(HTMLFragment htmlFragment) {
		_htmlFragment = htmlFragment;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		_htmlFragment.write(context, out);
	}

}

