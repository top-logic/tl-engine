/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.text.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * Util methods for text rendering.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DiagramTextRenderingUtil implements DiagramTextRenderingConstants {

	private static final String DROID_SANS_FONT_PATH = "/webfonts/DroidSansMono.ttf";

	private static final String WRONG_FONT_FORMAT = "Specified font is in wrong format.";

	private static final Font DROID_SANS = createDroidSansFont();

	/**
	 * Text width for the default font.
	 */
	public static double getTextWidth(String text) {
		return getStringBounds(text).getWidth();
	}

	private static Font createDroidSansFont() {
		try {
			try (InputStream in = FileManager.getInstance().getStream(DROID_SANS_FONT_PATH)) {
				return Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(FONT_SIZE);
			}
		} catch (IOException exception) {
			throw new IOError(exception);
		} catch (FontFormatException exception) {
			throw new TopLogicException(ResKey.text(WRONG_FONT_FORMAT), exception);
		}
	}

	private static Rectangle2D getStringBounds(String text) {
		FontRenderContext renderContext = new FontRenderContext(null, false, true);

		return DROID_SANS.getStringBounds(text, renderContext);
	}

	/**
	 * Text height for the default font.
	 */
	public static double getTextHeight(String text) {
		return getStringBounds(text).getHeight();
	}
}
