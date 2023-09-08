/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link Renderer} rendering a {@link String} as URL.
 * 
 * @see URLControlProvider
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class URLRenderer implements Renderer<String> {

	private final ControlProvider _cp = new URLControlProvider();

	@Override
	public void write(DisplayContext context, TagWriter out, String value) throws IOException {
		if (value != null) {
			// Simple way to render a URL-String as an input field.
			StringField tmp = FormFactory.newStringField("tmp", value, FormFactory.IMMUTABLE);
			_cp.createControl(tmp, null).write(context, out);
		}
	}

}

