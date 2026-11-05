/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.model;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;

/**
 * Represents a drawable object within the Gantt chart.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class GanttObject {

	/**
	 * Class encapsulating the object to go to and the provider for the link.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class Goto {

		private final ResourceProvider _linkProvider;

		private final Object _object;

		Goto(ResourceProvider linkProvider, Object object) {
			_linkProvider = linkProvider;
			_object = object;
		}

		/**
		 * Creates the actual goto link.
		 */
		public String createGotoLink(DisplayContext context) {
			return _linkProvider.getLink(context, _object);
		}
	}

	private Object _businessObject;
	private String _name;		// mandatory
	private String _tooltip; 	// optional. HTML

	// Additional output for image map and goto. Coordinates represent drawing area without surrounding spaces and frames.
	private int _xMin;
	private int _yMin;
	private int _xMax;
	private int _yMax;

	private Goto _goto;

	/**
	 * Creates a new {@link GanttObject}.
	 */
	public GanttObject() {}

	/**
	 * Creates a new {@link GanttObject}.
	 */
	public GanttObject(int xmin, int ymin, int xmax, int ymax) {
		_xMin = xmin;
		_xMax = xmax;
		_yMin = ymin;
		_yMax = ymax;
	}

	/** The horizontal center of the drawing area of this object. */
	public int xMid() {
		return (_xMin + _xMax) / 2;
	}

	/** The vertical center of the drawing area of this object. */
	public int yMid() {
		return (_yMin + _yMax) / 2;
	}

	/** The width of the drawing area of this object. */
	public int getWidth() {
		return _xMax - _xMin;
	}

	/** The height of the drawing area of this object. */
	public int getHeight() {
		return _yMax - _yMin;
	}

	/** The business object represented by this object. */
	public Object getBusinessObject() {
		return _businessObject;
	}

	/** Setter for {@link #getBusinessObject()}. */
	public void setBusinessObject(Object businessObject) {
		_businessObject = businessObject;
	}

	/** The name displayed for this object. */
	public String getName() {
		return _name;
	}

	/** Setter for {@link #getName()}. */
	public void setName(String name) {
		_name = name;
	}

	/** The HTML tooltip of this object, or {@code null} for none. */
	public String getTooltip() {
		return _tooltip;
	}

	/** Setter for {@link #getTooltip()}. */
	public void setTooltip(String tooltip) {
		_tooltip = tooltip;
	}

	/**
	 * Sets the target of a goto from this object.
	 *
	 * @param linkProvider
	 *        The {@link ResourceProvider} creating the goto link for the given object.
	 * @param gotoObject
	 *        The object to go to.
	 *
	 * @see #newGoto(ResourceProvider, Object)
	 */
	public final void setGoto(ResourceProvider linkProvider, Object gotoObject) {
		setGoto(newGoto(linkProvider, gotoObject));
	}

	/**
	 * Creates a new {@link Goto}.
	 * 
	 * @param linkProvider
	 *        The {@link ResourceProvider} used to create goto link for the given object.
	 * @param object
	 *        The object to go to.
	 * @return A {@link Goto} encapsulating the goto link creation.
	 */
	public Goto newGoto(ResourceProvider linkProvider, Object object) {
		return new Goto(linkProvider, object);
	}

	/** Setter for {@link #getGoto()}. */
	public void setGoto(Goto gotoArg) {
		_goto = gotoArg;
	}

	/** The target of a goto from this object, or {@code null} if this object is not linked. */
	public Goto getGoto() {
		return _goto;
	}

	/** The left coordinate of the drawing area of this object. */
	public int getXMin() {
		return _xMin;
	}

	/** Setter for {@link #getXMin()}. */
	public void setXMin(int xmin) {
		_xMin = xmin;
	}

	/** The top coordinate of the drawing area of this object. */
	public int getYMin() {
		return _yMin;
	}

	/** Setter for {@link #getYMin()}. */
	public void setYMin(int ymin) {
		_yMin = ymin;
	}

	/** The right coordinate of the drawing area of this object. */
	public int getXMax() {
		return _xMax;
	}

	/** Setter for {@link #getXMax()}. */
	public void setXMax(int xmax) {
		_xMax = xmax;
	}

	/** The bottom coordinate of the drawing area of this object. */
	public int getYMax() {
		return _yMax;
	}

	/** Setter for {@link #getYMax()}. */
	public void setYMax(int ymax) {
		_yMax = ymax;
	}

}
