/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.graphic.blocks.server;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.TextMetricsImpl;

/**
 * {@link RenderContext} based on AWT {@link Font}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AWTContext implements RenderContext {

	private final Font _font;

	private final FontRenderContext _fontRenderContext;

	/**
	 * Creates a {@link AWTContext}.
	 */
	public AWTContext(float textSize) {
		_font = Font.decode("Arial").deriveFont(textSize);
		Graphics2D graphics = (Graphics2D) new BufferedImage(8, 8, BufferedImage.TYPE_4BYTE_ABGR).getGraphics();
		_fontRenderContext = graphics.getFontRenderContext();
	}

	@Override
	public TextMetricsImpl measure(String text) {
		LineMetrics metrics = _font.getLineMetrics(text, _fontRenderContext);
		Rectangle2D bounds = _font.getStringBounds(text, _fontRenderContext);

		return new TextMetricsImpl(
			bounds.getWidth(),
			bounds.getHeight(),
			metrics.getAscent());
	}

}
