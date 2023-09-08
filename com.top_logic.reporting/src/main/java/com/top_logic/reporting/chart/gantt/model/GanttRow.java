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

	public int getDepth() {
		return _depth;
	}

	public void setDepth(int depth) {
		_depth = depth;
	}

	public boolean isFirst() {
		return _isFirst;
	}

	public void setFirst(boolean isFirst) {
		_isFirst = isFirst;
	}

	public boolean isLast() {
		return _isLast;
	}

	public void setLast(boolean isLast) {
		_isLast = isLast;
	}

	public boolean isNavOnly() {
		return _isNavOnly;
	}

	public void setNavOnly(boolean isNavOnly) {
		_isNavOnly = isNavOnly;
	}

	public boolean isProgrammatic() {
		return _isProgrammatic;
	}

	public void setProgrammatic(boolean isProgrammatic) {
		_isProgrammatic = isProgrammatic;
	}

	public boolean isDisabled() {
		return _isDisabled;
	}

	public void setDisabled(boolean isDisabled) {
		_isDisabled = isDisabled;
	}

	public Color getNodeColor() {
		return _nodeColor;
	}

	public void setNodeColor(Color nodeColor) {
		_nodeColor = nodeColor;
	}

	public Font getNodeFont() {
		return _nodeFont;
	}

	public void setNodeFont(Font nodeFont) {
		_nodeFont = nodeFont;
	}

	public double getProgress() {
		return _progress;
	}

	public void setProgress(double progress) {
		_progress = progress;
	}

	public double getMinTime() {
		return _minTime;
	}

	public void setMinTime(double minTime) {
		_minTime = minTime;
	}

	public ThemeImage getNodeImagePath() {
		return _nodeImage;
	}

	public void setNodeImagePath(ThemeImage nodeImage) {
		_nodeImage = nodeImage;
	}

	public Date getStartDate() {
		return _startDate;
	}

	public Date setStartDate(Date startDate) {
		_startDate = startDate;
		return startDate;
	}

	public Date getEndDate() {
		return _endDate;
	}

	public void setEndDate(Date endDate) {
		_endDate = endDate;
	}

	public Map<String, String> getAttributeValues() {
		return _attributeValues;
	}

	public void setAttributeValues(Map<String, String> attributeValues) {
		_attributeValues = attributeValues;
	}

	public List<GanttNode> getNodes() {
		return _nodes;
	}

}
