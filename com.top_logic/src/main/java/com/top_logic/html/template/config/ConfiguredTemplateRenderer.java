/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.config;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.control.ButtonControl;

/**
 * {@link Renderer} for {@link ConfiguredTemplate}s displaying a button that shows the template
 * source in a pop-up.
 */
public class ConfiguredTemplateRenderer implements Renderer<ConfiguredTemplate> {

	@Override
	public void write(DisplayContext context, TagWriter out, ConfiguredTemplate value) throws IOException {
		new ButtonControl(new DisplayTemplateCodeCommand(value)).write(context, out);
	}

}
