/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;

/**
 * {@link Renderer} for {@link StructuredText}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HTMLRenderer implements Renderer<StructuredText> {

	/**
	 * Singleton {@link HTMLRenderer} instance.
	 */
	public static final HTMLRenderer INSTANCE = new HTMLRenderer();

	private HTMLRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out, StructuredText value) throws IOException {
		if (value == null) {
			return;
		}
		HiddenField field = FormFactory.newHiddenField("display", value);
		field.setImmutable(true);
		new StructuredTextControl(field).write(context, out);
	}

}
