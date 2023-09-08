/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import com.top_logic.gui.ThemeFactory;
import com.top_logic.reporting.chart.gantt.model.GanttNode;
import com.top_logic.reporting.chart.gantt.ui.GraphicsContext.ContentContext;

/**
 * Renderer for {@link GanttNode}s.
 *
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class NodeDataRenderer {

	/** Default instance of this class. */
	public static final NodeDataRenderer INSTANCE = new NodeDataRenderer();

	/**
	 * Draws the given NodeData to the given x position. Returns the {@link BlockingInfo}s
	 * describing the used space. Therefore, the method should not add {@link BlockingInfo}s by
	 * itself to the {@link GanttChartCreatorFields}.
	 */
	public Collection<BlockingInfo> draw(ContentContext context, GanttChartCreatorFields cf,
			AbstractGanttChartCreator creator, GanttNode nodeData, int xPos, Map<String, Image> imageCache) {
		if (cf.showMilestoneIcons()) {
			return drawNodeDataAsSymbol(context, cf, creator, nodeData, xPos, imageCache);
		}
		else {
			return drawNodeDataAsRectangle(context, cf, creator, nodeData, xPos);
		}
	}

	/**
	 * Draws the given NodeData (milestone) as symbol.
	 */
	protected Collection<BlockingInfo> drawNodeDataAsSymbol(ContentContext context, GanttChartCreatorFields cf,
			AbstractGanttChartCreator creator, GanttNode nodeData, int xPos, Map<String, Image> imageCache) {
		Image image = getImage(nodeData, imageCache);
		int width = image.getWidth(null);
		if (width <= 0) {
			width = 16;
		}
		int height = image.getHeight(null);
		if (height <= 0) {
			height = 16;
		}
		nodeData.setXMin(xPos - width / 2);
		nodeData.setXMax(nodeData.getXMin() + width - 1); // index of last pixel of the image, therefore "- 1"
		nodeData.setYMin(nodeData.getRow().getYMin());
		nodeData.setYMin(nodeData.getRow().yMid() - height / 2);
		nodeData.setYMax(nodeData.getYMin() + height - 1); // index of last pixel of the image, therefore "- 1"
		Collection<BlockingInfo> infos = new HashSet<>();
		if (cf.showReportLines()) {
			infos.addAll(drawReportLine(context, cf, creator, nodeData));
		}
		context.content().drawImage(image, nodeData.getXMin(), nodeData.yMid() - (height / 2) + 1, null);
		infos.add(new BlockingInfo(nodeData));
		return infos;
	}

	/**
	 * Draws the given NodeData (milestone) as rectangle with name.
	 */
	protected Collection<BlockingInfo> drawNodeDataAsRectangle(ContentContext context, GanttChartCreatorFields cf,
			AbstractGanttChartCreator creator, GanttNode nodeData, int xPos) {
		if (xPos >= 0) {
			Collection<BlockingInfo> infos = new HashSet<>();
			context.content().setFont(creator.getFont());

			FontMetrics fontMetrics = creator.fontMetrics();
			int defaultFontHeight = creator.fontHeight();
			int verticalTextSpace = creator.getChartConfig().getVerticalTextSpace();
			Color foregroundColor = creator.getChartConfig().getForegroundColor();

			String msName = nodeData.getName();
			msName += nodeData.isHasInvisibleMSDeps() ? " M" : "";
			msName += nodeData.isHasInvisibleDateDeps() ? " D" : "";
			int stringWidth = fontMetrics.stringWidth(msName);

			int textPos = xPos - stringWidth / 2;
			if (textPos - 3 < cf.getTreeWidth()) {
				textPos = cf.getTreeWidth() + 3;
			}

			int x = textPos - 2;
			int y = nodeData.getRow().getYMin() - 2;
			nodeData.setXMin(x);
			nodeData.setXMax(x + stringWidth + 3);
			nodeData.setYMin(y);
			nodeData.setYMax(nodeData.getRow().getYMax() + 2);
			if (cf.showReportLines()) {
				infos.addAll(drawReportLine(context, cf, creator, nodeData));
			}

			// Draw surrounding box
			context.content().setColor(foregroundColor);
			context.content().drawRect(x, y, stringWidth + 4, defaultFontHeight + 3);

			// Draw and fill inner box
			Color boxColor = nodeData.getColor() != null ? nodeData.getColor() : creator.getChartConfig().getBackgroundColor();
			context.content().setColor(boxColor);
			context.content().fill(new Rectangle(x + 1, y + 1, stringWidth + 3, defaultFontHeight + 2));
			// Draw lines at correct date
			context.content().setColor(foregroundColor);
			context.content().drawLine(xPos, nodeData.getRow().getYMin() - verticalTextSpace, xPos, y);
			context.content().drawLine(xPos, nodeData.getRow().getYMax() + verticalTextSpace, xPos,
				nodeData.getRow().getYMax() + 2);

			// Draw text
			context.content().setColor(foregroundColor);
			context.content().drawString(msName, textPos, nodeData.getRow().getYMax() - fontMetrics.getDescent());

			infos.add(new BlockingInfo(nodeData));
			return infos;
		}
		return Collections.emptySet();
	}

	/**
	 * Draws the report line for the given milestone.
	 */
	protected Collection<BlockingInfo> drawReportLine(ContentContext context, GanttChartCreatorFields cf, AbstractGanttChartCreator creator, GanttNode nodeData) {
		return Collections.emptySet();
	}

	/**
	 * Gets the image for the given node data.
	 */
	protected Image getImage(GanttNode nodeData, Map<String, Image> imageCache) {
		String imagePath = nodeData.getImagePath();
		Image image = imageCache.get(imagePath);
		if (image == null) {
			image = ThemeFactory.getTheme().getImageByPath(imagePath);
			imageCache.put(imagePath, image);
		}
		return image;
	}

}
