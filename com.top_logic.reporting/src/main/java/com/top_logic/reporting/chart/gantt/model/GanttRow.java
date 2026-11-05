/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.model;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.basic.ThemeImage;


/**
 * Represents a row object in the Gantt chart.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class GanttRow extends GanttObject {

	private int 	_depth;				// tree node depth. mandatory
	private boolean	_isFirst;			// is this the first node in the child list?
	private boolean	_isLast;			// is this the last node in the child list?
	private boolean	_isNavOnly;			// is this node for navigation only (no MSs and dates)
	private boolean _isProgrammatic;	// should the node (complete) be drawn in a disabled / finished color
	private boolean _isDisabled;		// should the node (complete) be drawn in a disabled / finished color

	private Color _nodeColor;	// falls back to defaultColor
	private Font  _nodeFont;	// falls back to foregroundColor

	private double _progress;	// percentage
	private double _minTime;	// percentage

	private ThemeImage _nodeImage; // optional. Used only if defined and showNodeSymbol is set to
									// true

	private Date _startDate;
	private Date _endDate;

	private Map<String, String> 	_attributeValues;

	private final List<GanttNode> _nodes = new ArrayList<>();

	/**
	 * Creates a {@link GanttRow}.
	 */
	public GanttRow(Object businessObject) {
		setBusinessObject(businessObject);
	}

	/**
	 * Adjusts the start and end of the given node.
	 */
	public void adjustNodeDates(Date date) {
		if (_startDate == null || date.before(_startDate)) {
			_startDate = date;
		}
		if (_endDate == null || date.after(_endDate)) {
			_endDate = date;
		}
	}

	/** The depth of this row in the displayed tree. */
	public int getDepth() {
		return _depth;
	}

	/** Setter for {@link #getDepth()}. */
	public void setDepth(int depth) {
		_depth = depth;
	}

	/** Whether this row is the first child of its parent. */
	public boolean isFirst() {
		return _isFirst;
	}

	/** Setter for {@link #isFirst()}. */
	public void setFirst(boolean isFirst) {
		_isFirst = isFirst;
	}

	/** Whether this row is the last child of its parent. */
	public boolean isLast() {
		return _isLast;
	}

	/** Setter for {@link #isLast()}. */
	public void setLast(boolean isLast) {
		_isLast = isLast;
	}

	/** Whether this row is for navigation only and displays no milestones and dates. */
	public boolean isNavOnly() {
		return _isNavOnly;
	}

	/** Setter for {@link #isNavOnly()}. */
	public void setNavOnly(boolean isNavOnly) {
		_isNavOnly = isNavOnly;
	}

	/** Whether this row is drawn in a disabled color. */
	public boolean isProgrammatic() {
		return _isProgrammatic;
	}

	/** Setter for {@link #isProgrammatic()}. */
	public void setProgrammatic(boolean isProgrammatic) {
		_isProgrammatic = isProgrammatic;
	}

	/** Whether this row is drawn in a disabled (finished) color. */
	public boolean isDisabled() {
		return _isDisabled;
	}

	/** Setter for {@link #isDisabled()}. */
	public void setDisabled(boolean isDisabled) {
		_isDisabled = isDisabled;
	}

	/** The color of this row, or {@code null} to use the default color. */
	public Color getNodeColor() {
		return _nodeColor;
	}

	/** Setter for {@link #getNodeColor()}. */
	public void setNodeColor(Color nodeColor) {
		_nodeColor = nodeColor;
	}

	/** The font of this row, or {@code null} to use the default font. */
	public Font getNodeFont() {
		return _nodeFont;
	}

	/** Setter for {@link #getNodeFont()}. */
	public void setNodeFont(Font nodeFont) {
		_nodeFont = nodeFont;
	}

	/** The progress of this row in percent. */
	public double getProgress() {
		return _progress;
	}

	/** Setter for {@link #getProgress()}. */
	public void setProgress(double progress) {
		_progress = progress;
	}

	/** The minimum time progress of this row in percent. */
	public double getMinTime() {
		return _minTime;
	}

	/** Setter for {@link #getMinTime()}. */
	public void setMinTime(double minTime) {
		_minTime = minTime;
	}

	/** The symbol displayed for this row, or {@code null} for none. */
	public ThemeImage getNodeImagePath() {
		return _nodeImage;
	}

	/** Setter for {@link #getNodeImagePath()}. */
	public void setNodeImagePath(ThemeImage nodeImage) {
		_nodeImage = nodeImage;
	}

	/** The start date of this row. */
	public Date getStartDate() {
		return _startDate;
	}

	/**
	 * Setter for {@link #getStartDate()}.
	 *
	 * @return The given start date.
	 */
	public Date setStartDate(Date startDate) {
		_startDate = startDate;
		return startDate;
	}

	/** The end date of this row. */
	public Date getEndDate() {
		return _endDate;
	}

	/** Setter for {@link #getEndDate()}. */
	public void setEndDate(Date endDate) {
		_endDate = endDate;
	}

	/** The values of the additional columns displayed for this row, indexed by column name. */
	public Map<String, String> getAttributeValues() {
		return _attributeValues;
	}

	/** Setter for {@link #getAttributeValues()}. */
	public void setAttributeValues(Map<String, String> attributeValues) {
		_attributeValues = attributeValues;
	}

	/** The {@link GanttNode}s displayed on this row. */
	public List<GanttNode> getNodes() {
		return _nodes;
	}

}
