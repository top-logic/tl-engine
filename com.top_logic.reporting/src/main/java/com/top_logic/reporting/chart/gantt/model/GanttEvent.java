/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.model;

import java.awt.Color;
import java.util.Date;



/**
 * Represents an event represented as vertical line in the Gantt chart.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class GanttEvent extends GanttDate {

	/** optional. Falls back to foregroundColor. */
	private Color _lineColor;	

	/** optional. Falls back to dateLineWidth. */
	private int	 _lineWidth;

	// Additional output for image map and goto for the text of date lines within the footer.
	// Coordinates represent drawing area without surrounding spaces.
	private int _xminT;
	private int _yminT;
	private int _xmaxT;
	private int _ymaxT;


	/**
	 * Creates a new {@link GanttEvent}.
	 */
	public GanttEvent(Date date) {
		setDate(date);
	}


	public int xlengthT() {
		return _xmaxT - _xminT + 1;
	}

	public int ylengthT() {
		return _ymaxT - _yminT + 1;
	}

	public Color getLineColor() {
		return _lineColor;
	}


	public void setLineColor(Color lineColor) {
		_lineColor = lineColor;
	}


	public int getLineWidth() {
		return _lineWidth;
	}


	public void setLineWidth(int lineWidth) {
		_lineWidth = lineWidth;
	}


	public int getXminT() {
		return _xminT;
	}


	public void setXminT(int xminT) {
		_xminT = xminT;
	}


	public int getYminT() {
		return _yminT;
	}


	public void setYminT(int yminT) {
		_yminT = yminT;
	}


	public int getXmaxT() {
		return _xmaxT;
	}


	public void setXmaxT(int xmaxT) {
		_xmaxT = xmaxT;
	}


	public int getYmaxT() {
		return _ymaxT;
	}


	public void setYmaxT(int ymaxT) {
		_ymaxT = ymaxT;
	}

}
