/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * {@link Renderer}for a {@link ThemeImage}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ThemeImageRenderer implements Renderer<ThemeImage> {

	/**
	 * Singleton {@link ThemeImageRenderer} instance.
	 */
	public static final ThemeImageRenderer INSTANCE = new ThemeImageRenderer();

	@Override
	public void write(DisplayContext context, TagWriter out, ThemeImage value) throws IOException {
		ThemeUtil.writeThemeImage(context, out, value, ResKey.text(value.toEncodedForm()));
	}

}
