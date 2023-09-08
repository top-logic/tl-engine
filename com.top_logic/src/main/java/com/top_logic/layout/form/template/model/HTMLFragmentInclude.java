/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.io.IOException;
import java.util.Objects;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.LiteralTemplate;
import com.top_logic.layout.DisplayContext;

/**
 * Template rendering a given {@link HTMLFragment}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HTMLFragmentInclude implements Template, LiteralTemplate {

	private final HTMLFragment _fragment;

	HTMLFragmentInclude(HTMLFragment fragment) {
		_fragment = Objects.requireNonNull(fragment);
	}

	/**
	 * This method returns the {@link HTMLFragment} to render.
	 */
	public HTMLFragment getFragment() {
		return _fragment;
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out) throws IOException {
		getFragment().write(displayContext, out);
	}
}

