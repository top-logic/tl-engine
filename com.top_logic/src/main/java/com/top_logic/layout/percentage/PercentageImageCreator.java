/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.percentage;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class PercentageImageCreator {

	public File createImageReturnFile(double percentage, AbstractPercentageImageInfo info) {
		BufferedImage bufferedImage = createBufferdImage(percentage, info);

        try {
			File file = ChartUtil.writeImageAsPng(bufferedImage);
			file.getName();

			return file;
		} catch (IOException e) {
			throw new TopLogicException(getClass(), "Couldn't create the image because of an io exception.", e);
		}
	}

	public String createImageReturnPath(double percentage, AbstractPercentageImageInfo info) {
		File imageFile = createImageReturnFile(percentage, info);

		return ChartUtil.getFilePath(imageFile);
	}

	protected BufferedImage createBufferdImage(double percentage, AbstractPercentageImageInfo info) {
		Shape shape = info.createShape(0, 0);
		Rectangle2D bounds = shape.getBounds2D();
		int space = info.getSpaceWidth();
		int width = (info.getShapeNumber() * ((int) bounds.getWidth())) + (info.getShapeNumber() * space);

		int height = (int )bounds.getHeight();
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bufferedImage.createGraphics();

		int fillIndex = (int) (info.getShapeNumber() * percentage);
		for (int i = 0; i < info.getShapeNumber(); i++) {
			int tmpX = (i * ((int) bounds.getWidth()) + (i * space));
			int tmpY = 0;
			Shape tmpShape = info.createShape(tmpX, tmpY);
			if (i < fillIndex) {
				g2.setPaint(transform(info.getShapeForeground(), tmpShape, tmpX, tmpY));
			} else {
				g2.setPaint(transform(info.getShapeBackground(), tmpShape, tmpX, tmpY));
			}

			g2.fill(tmpShape);
			g2.draw(tmpShape);

			Shape spaceShape = new Rectangle2D.Double(tmpX + bounds.getWidth(), tmpY, space, height);
			g2.setPaint(info.getSpacePaint());
			g2.fill(spaceShape);
			g2.draw(spaceShape);
		}

		g2.setPaint(Color.GRAY);
		g2.drawRect(0, 0, width - 1, height - 1);

		return bufferedImage;
	}

	private Paint transform(Paint paint, Shape shape, int posX, int posY) {
		if (paint instanceof GradientPaint) {
			GradientPaint shapePaint = (GradientPaint) paint;
			Rectangle shapeBounds = shape.getBounds();
			int xMiddle = (int) shapeBounds.getWidth() / 2;
			int yMiddle = (int) shapeBounds.getHeight()/ 2;

			return new GradientPaint(posX - xMiddle, posY + yMiddle, shapePaint.getColor1(), posX - xMiddle, posY, Color.WHITE);
		}

		return paint;
	}

}
