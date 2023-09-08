/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.config.HTMLTemplateFragmentFormat;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;
import com.top_logic.layout.template.WithProperties;

/**
 * Template for rendering an arbitrary object to HTML.
 */
@Label("HTML Template")
@RenderWholeLine
@Format(HTMLTemplateFragmentFormat.class)
@ControlProvider(MultiLineText.class)
public interface HTMLTemplateFragment extends Renderer<WithProperties> {

	/**
	 * Writes an object with the given properties to the given writer.
	 *
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param properties
	 *        Access to the properties of the rendered object. The template may access those
	 *        properties for embedding them into the created output.
	 * @throws IOException
	 *         If writing fails.
	 */
	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException;

}
