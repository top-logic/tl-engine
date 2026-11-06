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
	 * Draws this node to the given x position. Returns the {@link BlockingInfo}s
	 * describing the used space. Therefore, the method should not add {@link BlockingInfo}s by
	 * itself to the {@link GanttChartCreatorFields}.
	 */
	public Collection<BlockingInfo> draw(ContentContext context, GanttChartCreatorFields cf,
			AbstractGanttChartCreator creator, int xPos, Map<String, Image> imageCache) {
		return getRenderer().draw(context, cf, creator, this, xPos, imageCache);
	}

	/** The color of this node, used if no image is set. */
	public Color getColor() {
		return _color;
	}

	/** Setter for {@link #getColor()}. */
	public void setColor(Color color) {
		_color = color;
	}

	/** The font of this node, or {@code null} to use the default font. */
	public Font getFont() {
		return _font;
	}

	/** Setter for {@link #getFont()}. */
	public void setFont(Font font) {
		_font = font;
	}

	/** The path of the image used to display this node, or {@code null} to draw a symbol. */
	public String getImagePath() {
		return _imagePath;
	}

	/**
	 * Sets the image used to display this node.
	 *
	 * @param image
	 *        The image to use. Only a plain image is supported; other images are ignored.
	 */
	public void setImagePath(ThemeImage image) {
		if (image != null) {
			image = image.resolve();
			if (image instanceof Img) {
				setImagePath(((Img) image).getImagePath());
			}
		}
	}

	/** Setter for {@link #getImagePath()}. */
	public void setImagePath(String imagePath) {
		_imagePath = imagePath;
	}

	/** Whether this node has date dependencies to objects that are not displayed. */
	public boolean isHasInvisibleDateDeps() {
		return _hasInvisibleDateDeps;
	}

	/** Setter for {@link #isHasInvisibleDateDeps()}. */
	public void setHasInvisibleDateDeps(boolean hasInvisibleDateDeps) {
		_hasInvisibleDateDeps = hasInvisibleDateDeps;
	}

	/** The other nodes this node depends on. */
	public List<GanttNode> getNodeDependencies() {
		return _nodeDependencies;
	}

	/** The events this node depends on. */
	public List<GanttEvent> getEventDependencies() {
		return _eventDependencies;
	}

	/** The row on which this node is displayed. */
	public GanttRow getRow() {
		return _row;
	}

	/** Setter for {@link #getRow()}. */
	public void setRow(GanttRow row) {
		_row = row;
	}

	/** Whether this node has milestone dependencies to objects that are not displayed. */
	public boolean isHasInvisibleMSDeps() {
		return _hasInvisibleMSDeps;
	}

	/** Setter for {@link #isHasInvisibleMSDeps()}. */
	public void setHasInvisibleMSDeps(boolean hasInvisibleMSDeps) {
		_hasInvisibleMSDeps = hasInvisibleMSDeps;
	}

}
