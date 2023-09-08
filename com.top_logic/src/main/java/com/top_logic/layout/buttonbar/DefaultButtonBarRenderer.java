/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.buttonbar;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractControlRenderer;

/**
 * Default renderer of a {@link ButtonBarControl}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DefaultButtonBarRenderer extends AbstractControlRenderer<ButtonBarControl> {

	private String _masterCssClass;

	/**
	 * Create a new {@link DefaultButtonBarRenderer}
	 */
	public DefaultButtonBarRenderer(String masterCssClass) {
		_masterCssClass = masterCssClass;
	}

	/**
	 * 
	 * Gets css class of {@link DefaultButtonBarRenderer}.
	 * 
	 * @return css class in form of a string.
	 */
	public String getMasterClass() {
		return _masterCssClass;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, ButtonBarControl value) throws IOException {
		Icons.BUTTON_BAR_TEMPLATE.get().write(context, out, value);
	}

}
