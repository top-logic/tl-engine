/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.ThemeImage.Img;
import com.top_logic.reporting.chart.gantt.ui.AbstractGanttChartCreator;
import com.top_logic.reporting.chart.gantt.ui.BlockingInfo;
import com.top_logic.reporting.chart.gantt.ui.GanttChartCreatorFields;
import com.top_logic.reporting.chart.gantt.ui.GraphicsContext.ContentContext;
import com.top_logic.reporting.chart.gantt.ui.NodeDataRenderer;

/**
 * Represents a sub object on a {@link GanttRow} that might have dependencies to other objects.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class GanttNode extends GanttDate {

	private Color _color;		// optional. Used if no image is set
	private Font  _font;		// falls back to defaultFont
	private String _imagePath;	// used is set and showMSSymbol is set to true

	private boolean _hasInvisibleDateDeps;
	private boolean _hasInvisibleMSDeps;

	private final List<GanttNode> _nodeDependencies = new ArrayList<>(); // optional

	private final List<GanttEvent> _eventDependencies = new ArrayList<>(); // optional

	private GanttRow _row;


	/**
	 * Creates a {@link GanttNode}.
	 */
	public GanttNode(GanttRow node, Object businessObject) {
		_row = node;
		setBusinessObject(businessObject);
	}

	/**
	 * Gets optional offset used in rendering
	 */
	public int getOffset() {
		return 0;
	}

	/**
	 * Gets the renderer to use to draw this node data.
	 */
	protected NodeDataRenderer getRenderer() {
		return NodeDataRenderer.INSTANCE;
	}

	/**
	 * Draws the given NodeData to the given x position. Returns the {@link BlockingInfo}s
	 * describing the used space. Therefore, the method should not add {@link BlockingInfo}s by
	 * itself to the {@link GanttChartCreatorFields}.
	 */
	public Collection<BlockingInfo> draw(ContentContext context, GanttChartCreatorFields cf,
			AbstractGanttChartCreator creator, int xPos, Map<String, Image> imageCache) {
		return getRenderer().draw(context, cf, creator, this, xPos, imageCache);
	}

	public Color getColor() {
		return _color;
	}

	public void setColor(Color color) {
		_color = color;
	}

	public Font getFont() {
		return _font;
	}

	public void setFont(Font font) {
		_font = font;
	}

	public String getImagePath() {
		return _imagePath;
	}

	public void setImagePath(ThemeImage image) {
		if (image != null) {
			image = image.resolve();
			if (image instanceof Img) {
				setImagePath(((Img) image).getImagePath());
			}
		}
	}

	public void setImagePath(String imagePath) {
		_imagePath = imagePath;
	}

	public boolean isHasInvisibleDateDeps() {
		return _hasInvisibleDateDeps;
	}

	public void setHasInvisibleDateDeps(boolean hasInvisibleDateDeps) {
		_hasInvisibleDateDeps = hasInvisibleDateDeps;
	}

	public List<GanttNode> getNodeDependencies() {
		return _nodeDependencies;
	}

	public List<GanttEvent> getEventDependencies() {
		return _eventDependencies;
	}

	public GanttRow getRow() {
		return _row;
	}

	public void setRow(GanttRow row) {
		_row = row;
	}

	public boolean isHasInvisibleMSDeps() {
		return _hasInvisibleMSDeps;
	}

	public void setHasInvisibleMSDeps(boolean hasInvisibleMSDeps) {
		_hasInvisibleMSDeps = hasInvisibleMSDeps;
	}

}
