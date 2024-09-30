/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.Theme;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Renderer for images defined by a resource string.
 *
 * This string plus ".image" will then be taken to get the matching image using the {@link Theme}.
 * Moreover the image will become a tool tip which is the translation of the resource key.
 *
 * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
 */
public class ThemeBasedResourceImageRenderer implements Renderer<ResKey> {

    /** Suffix for getting the title of the image from the resources. */
    public static final String IMAGE_SUFFIX = ".image";

    @Override
	public void write(DisplayContext aContext, TagWriter aOut, ResKey aValue) throws IOException {
		ThemeImage image = ThemeImage.i18n(aValue.suffix(ThemeBasedResourceImageRenderer.IMAGE_SUFFIX));
		ResKey theText = aValue.optional();

		image.writeWithPlainTooltip(aContext, aOut, theText);
    }
}