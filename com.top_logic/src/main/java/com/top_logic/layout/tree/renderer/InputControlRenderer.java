/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;

/**
 * {@link Renderer} that writes {@link FormMember}s with input and error controls and uses a
 * fallback renderer for other values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InputControlRenderer implements Renderer<FormField> {

	/**
	 * Singleton {@link InputControlRenderer} instance.
	 */
	public static final InputControlRenderer INSTANCE = new InputControlRenderer();

	private InputControlRenderer() {
		// Singleton constructor.
	}

	private ControlProvider _controlProvider = DefaultFormFieldControlProvider.INSTANCE;

	private Renderer<? super FormField> _fallbackRenderer = ResourceRenderer.INSTANCE;

	@Override
	public void write(DisplayContext context, TagWriter out, FormField value) throws IOException {
		HTMLFragment control = _controlProvider.createFragment(value);
		if (control == null) {
			_fallbackRenderer.write(context, out, value);
		} else {
			control.write(context, out);

			HTMLFragment error = _controlProvider.createFragment(value, FormTemplateConstants.STYLE_ERROR_VALUE);
			if (error != null) {
				error.write(context, out);
			}
		}
	}

}
