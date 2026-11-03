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


	/** The width of the text area of this event in the footer. */
	public int xlengthT() {
		return _xmaxT - _xminT + 1;
	}

	/** The height of the text area of this event in the footer. */
	public int ylengthT() {
		return _ymaxT - _yminT + 1;
	}

	/** The color of the event line, or {@code null} to use the foreground color. */
	public Color getLineColor() {
		return _lineColor;
	}


	/** Setter for {@link #getLineColor()}. */
	public void setLineColor(Color lineColor) {
		_lineColor = lineColor;
	}


	/** The width of the event line, or {@code 0} to use the default date line width. */
	public int getLineWidth() {
		return _lineWidth;
	}


	/** Setter for {@link #getLineWidth()}. */
	public void setLineWidth(int lineWidth) {
		_lineWidth = lineWidth;
	}


	/** The left coordinate of the text area of this event in the footer. */
	public int getXminT() {
		return _xminT;
	}


	/** Setter for {@link #getXminT()}. */
	public void setXminT(int xminT) {
		_xminT = xminT;
	}


	/** The top coordinate of the text area of this event in the footer. */
	public int getYminT() {
		return _yminT;
	}


	/** Setter for {@link #getYminT()}. */
	public void setYminT(int yminT) {
		_yminT = yminT;
	}


	/** The right coordinate of the text area of this event in the footer. */
	public int getXmaxT() {
		return _xmaxT;
	}


	/** Setter for {@link #getXmaxT()}. */
	public void setXmaxT(int xmaxT) {
		_xmaxT = xmaxT;
	}


	/** The bottom coordinate of the text area of this event in the footer. */
	public int getYmaxT() {
		return _ymaxT;
	}


	/** Setter for {@link #getYmaxT()}. */
	public void setYmaxT(int ymaxT) {
		_ymaxT = ymaxT;
	}

}
