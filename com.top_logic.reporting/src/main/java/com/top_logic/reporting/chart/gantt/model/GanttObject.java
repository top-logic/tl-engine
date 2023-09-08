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

	public int xMid() {
		return (_xMin + _xMax) / 2;
	}

	public int yMid() {
		return (_yMin + _yMax) / 2;
	}

	public int getWidth() {
		return _xMax - _xMin;
	}

	public int getHeight() {
		return _yMax - _yMin;
	}

	public Object getBusinessObject() {
		return _businessObject;
	}

	public void setBusinessObject(Object businessObject) {
		_businessObject = businessObject;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getTooltip() {
		return _tooltip;
	}

	public void setTooltip(String tooltip) {
		_tooltip = tooltip;
	}

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

	public void setGoto(Goto gotoArg) {
		_goto = gotoArg;
	}

	public Goto getGoto() {
		return _goto;
	}

	public int getXMin() {
		return _xMin;
	}

	public void setXMin(int xmin) {
		_xMin = xmin;
	}

	public int getYMin() {
		return _yMin;
	}

	public void setYMin(int ymin) {
		_yMin = ymin;
	}

	public int getXMax() {
		return _xMax;
	}

	public void setXMax(int xmax) {
		_xMax = xmax;
	}

	public int getYMax() {
		return _yMax;
	}

	public void setYMax(int ymax) {
		_yMax = ymax;
	}

}
