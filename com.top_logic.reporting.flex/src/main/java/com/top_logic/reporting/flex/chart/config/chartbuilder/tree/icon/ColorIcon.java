/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.tree.icon;

import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.ui.GradientPaintTransformer;
import org.jfree.chart.ui.StandardGradientPaintTransformer;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.reporting.flex.chart.config.color.HexEncodedPaint;

/**
 * Implementation of {@link AbstractIconProvider} that creates colored shapes as icons.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class ColorIcon extends AbstractIconProvider {

	private static final GradientPaintTransformer PAINT_TRANSFORMER = new StandardGradientPaintTransformer();

	/**
	 * Config-interface for {@link ColorIcon}.
	 */
	@TagName("color")
	public interface Config extends AbstractIconProvider.Config {

		@Override
		@ClassDefault(ColorIcon.class)
		public Class<? extends AbstractIconProvider> getImplementationClass();

		/**
		 * the color of the icon
		 */
		@Format(HexEncodedPaint.class)
		public Paint getColor();

	}

	private Image _image;

	/**
	 * Config-Constructor for {@link ThemePathIcon}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public ColorIcon(InstantiationContext context, Config config) {
		_image = getImage(config.getColor());
	}

	@Override
	public Image getImage() {
		return _image;
	}

	private Image getImage(Paint aColor) {
		int width = 17;
		int height = 17;

		// We can't use the image icon because the constructors use the
		// default toolkit that makes trouble with vista. The color schema
		// is reset under vista that is really ugly. We need a higher java
		// version as 1.4.2.
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();

		Rectangle2D.Double shape = new Rectangle2D.Double(0, 0, width, height);

		Paint paint = aColor;
		if (aColor instanceof GradientPaint) {
			paint = PAINT_TRANSFORMER.transform((GradientPaint) aColor, shape);
		}

		g2.setPaint(paint);
		g2.fill(shape);
		g2.draw(shape);
		g2.drawImage(image, width, height, null);

		return image;
	}

}