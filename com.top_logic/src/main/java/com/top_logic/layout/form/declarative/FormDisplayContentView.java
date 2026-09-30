/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.declarative;

import java.io.IOException;

import jakarta.servlet.ServletException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.structure.InlineContentView;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link InlineContentView} displaying a {@link DirectFormDisplay}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormDisplayContentView extends InlineContentView {

	private final DirectFormDisplay _view;

	/**
	 * Creates a new {@link FormDisplayContentView}.
	 * 
	 * @param businessComponent
	 *        See {@link #getComponent()}.
	 * @param view
	 *        The {@link DirectFormDisplay} to render.
	 */
	public FormDisplayContentView(LayoutComponent businessComponent, DirectFormDisplay view) {
		super(businessComponent);
		_view = view;
	}

	@Override
	protected void renderComponent(DisplayContext context, TagWriter out) throws IOException, ServletException {
		LayoutComponent component = getComponent();
		FormHandler formHandler = (FormHandler) component;
		Object model = component.getModel();
		ResKey noModelKey = component.noModelKey();
		_view.createView(formHandler, model, noModelKey).write(context, out);
	}
}
