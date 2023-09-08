/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.top_logic.base.chart.ImageComponent;
import com.top_logic.base.chart.ImageData;
import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.DisplayContext;

/**
 * Creates a demo chart, containing a rectangle and an ellipsis.
 * 
 * @author <a href=mailto:sts}@top-logic.com>Stefan Steinert</a>
 */
public class DemoChartProducer implements ImageComponent {

	private int minWidth;
	private int maxWidth;
	private int minHeight;
	private int maxHeight;

	private final int _id;

	/**
	 * Create a new {@link DemoChartProducer}
	 */
	public DemoChartProducer(int minWidth, int maxWidth, int minHeight, int maxHeight) {
		this(minWidth, maxWidth, minHeight, maxHeight, 0);
	}

	/**
	 * Creates a {@link DemoChartProducer}.
	 */
	public DemoChartProducer(int minWidth, int maxWidth, int minHeight, int maxHeight, int id) {
		this.minWidth = minWidth;
		this.maxWidth = maxWidth;
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
		_id = id;
	}

	public int getId() {
		return _id;
	}

	@Override
	public void prepareImage(DisplayContext context, String imageId, Dimension dimension) throws IOException {
		// Do nothing
	}

	@Override
	public ImageData createImage(DisplayContext context, String imageId, String imageType, Dimension dimension)
			throws IOException {
		int width = getWidth(dimension);
		int height = getHeight(dimension);
		BufferedImage image = createBuffer(width, height);
		drawImage(image);
		return ChartUtil.encode(false, image);
	}

	private int getWidth(Dimension aDimension) {
		return inRange(minWidth, maxWidth, aDimension.width);
	}

	private int getHeight(Dimension aDimension) {
		return inRange(minHeight, maxHeight, aDimension.height);
	}

	private int inRange(int min, int max, int value) {
		return Math.max(min, Math.min(max, value));
	}

	private BufferedImage createBuffer(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	private void drawImage(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();

		Graphics2D g = image.createGraphics();
		draw(g, width, height);
	}

	/**
	 * Actually performs the rendering to the {@link Graphics2D} context.
	 */
	protected void draw(Graphics2D g, int width, int height) {
		g.setColor(Color.red);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(5));
		int r = 2;
		g.drawRect(r, r, width - 3 * r, height - 3 * r);

		int b = 20;
		if (width > 2 * b && height > 2 * b) {
			g.drawOval(b, b, width - 2 * b, height - 2 * b);
		}

		g.setColor(Color.black);

		if (_id > 0) {
			String idString = Integer.toString(_id);
			Rectangle2D bounds = g.getFontMetrics().getStringBounds(idString, g);
			
			int x = (int) ((width + bounds.getWidth()) / 2);
			int y = (int) ((height + bounds.getHeight()) / 2);
			g.drawString(idString, x, y);
		}
	}

	@Override
	public HTMLFragment getImageMap(String imageId, String mapName, Dimension dimension)
			throws IOException {
		return null;
	}
}