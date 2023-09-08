/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.ui;

import com.top_logic.reporting.chart.gantt.model.GanttObject;


/**
 * Holds information about the used space of a object or a dependency line for collision
 * computation.
 *
 * @author <a href="mailto:cbr@top-logic.com">Christian Braun</a>
 */
public class BlockingInfo {

	private int _x1;
	private int _y1;
	private int _x2;
	private int _y2;
	private boolean _isHoriz;
	private boolean _isVert;
	private GanttObject _source;
	private GanttObject _dest;


	/**
	 * Creates a new {@link BlockingInfo} with the given Parameters.
	 */
	public BlockingInfo(GanttObject object) {
		this(object.getXMin(), object.getYMin(), object.getXMax(), object.getYMax(), true, true, object, object);
	}

	/**
	 * Creates a new {@link BlockingInfo} with the given Parameters.
	 */
	public BlockingInfo(int x1, int y1, int x2, int y2, GanttObject object) {
		this(x1, y1, x2, y2, true, true, object, object);
	}

	/**
	 * Creates a new {@link BlockingInfo} with the given Parameters.
	 */
	public BlockingInfo(int x1, int y1, int x2, int y2, boolean isHoriz, boolean isVert, GanttObject source, GanttObject dest) {
		_x1 = x1;
		_x2 = x2;
		if (_x1 > _x2) { // Sort x asc
			_x2 = x1;
			_x1 = x2;
		}

		_y1 = y1;
		_y2 = y2;
		if (_y1 > _y2) { // Sort y asc
			_y2 = y1;
			_y1 = y2;
		}

		_isHoriz = isHoriz;
		_isVert  = isVert;
		_source  = source;
		_dest    = dest;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BlockingInfo)) {
			return false;
		}
		BlockingInfo b = (BlockingInfo) obj;
		return _x1 == b._x1 && _x2 == b._x2 && _y1 == b._y1 && _y2 == b._y2 && _isHoriz == b._isHoriz && _isVert == b._isVert
			&& _source == b._source && _dest == b._dest;
	}

	@Override
	public int hashCode() {
		return _x1 + _y1 + _x2 + _y2 + ((_source != null) ? _source.hashCode() : 0) + (_dest != null ? _dest.hashCode() : 0) +
			(_isHoriz ? 1 : 0) + (_isVert ? 1 : 0);
	}

	/**
	 * Gets the width of this {@link BlockingInfo}.
	 */
	public int getWidth() {
		return _x2 - _x1;
	}

	/**
	 * Gets the height of this {@link BlockingInfo}.
	 */
	public int getHeight() {
		return _y2 - _y1;
	}

	public int getX1() {
		return _x1;
	}

	public void setX1(int x1) {
		_x1 = x1;
	}

	public int getY1() {
		return _y1;
	}

	public void setY1(int y1) {
		_y1 = y1;
	}

	public int getX2() {
		return _x2;
	}

	public void setX2(int x2) {
		_x2 = x2;
	}

	public int getY2() {
		return _y2;
	}

	public void setY2(int y2) {
		_y2 = y2;
	}

	public boolean isHoriz() {
		return _isHoriz;
	}

	public void setHoriz(boolean isHoriz) {
		_isHoriz = isHoriz;
	}

	public boolean isVert() {
		return _isVert;
	}

	public void setVert(boolean isVert) {
		_isVert = isVert;
	}

	public GanttObject getSource() {
		return _source;
	}

	public void setSource(GanttObject source) {
		_source = source;
	}

	public GanttObject getDest() {
		return _dest;
	}

	public void setDest(GanttObject dest) {
		_dest = dest;
	}

}
