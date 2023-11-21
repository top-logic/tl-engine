/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.event;

/**
 * General diagramjs event names.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface GeneralEventNames extends CommonEventNames {

	/**
	 * DiagramJS {@link Event} name for clicking an element.
	 */
	public static final String ELEMENT_CLICKED_EVENT = "element" + EVENT_NAME_SEPARATOR + "click";

	/**
	 * DiagramJS {@link Event} name for double clicking an element.
	 */
	public static final String ELEMENT_DBCLICKED_EVENT = "element" + EVENT_NAME_SEPARATOR + "dblclick";

	/**
	 * DiagramJS {@link Event} name for mousedown an element.
	 */
	public static final String ELEMENT_MOUSEDOWN_EVENT = "element" + EVENT_NAME_SEPARATOR + "mousedown";

	/**
	 * DiagramJS {@link Event} name for initialization of the diagram.
	 */
	public static final String DIAGRAM_INIT_EVENT = "diagram" + EVENT_NAME_SEPARATOR + "init";

	/**
	 * DiagramJS {@link Event} name for destroying the diagram.
	 */
	public static final String DIAGRAM_DESTROY_EVENT = "diagram" + EVENT_NAME_SEPARATOR + "destroy";

	/**
	 * DiagramJS {@link Event} name for changing the selection of an element.
	 */
	public static final String SELECTION_CHANGED_EVENT = "selection" + EVENT_NAME_SEPARATOR + "changed";

	/**
	 * DiagramJS {@link Event} name for changing an element.
	 */
	public static final String ELEMENT_CHANGED_EVENT = "element" + EVENT_NAME_SEPARATOR + "changed";

	/**
	 * DiagramJS {@link Event} name for changing a shape.
	 * 
	 */
	public static final String SHAPE_CHANGED_EVENT = "shape" + EVENT_NAME_SEPARATOR + "changed";

	/**
	 * DiagramJS {@link Event} name for changing a connection.
	 */
	public static final String CONNECTION_CHANGED_EVENT = "connection" + EVENT_NAME_SEPARATOR + "changed";

	/**
	 * DiagramJS {@link Event} name for adding a shape.
	 */
	public static final String SHAPE_ADDED_EVENT = "shape" + EVENT_NAME_SEPARATOR + "added";

	/**
	 * DiagramJS {@link Event} name for adding a connection.
	 */
	public static final String CONNECTION_ADDED_EVENT = "connection" + EVENT_NAME_SEPARATOR + "added";

	/**
	 * DiagramJS {@link Event} name for hovering an element.
	 */
	public static final String ELEMENT_HOVER_EVENT = "element" + EVENT_NAME_SEPARATOR + "hover";

	/**
	 * DiagramJS {@link Event} name for hovering a shape.
	 */
	public static final String SHAPE_HOVER_EVENT = "shape" + EVENT_NAME_SEPARATOR + "hover";

	/**
	 * DiagramJS {@link Event} name for hovering a connection.
	 */
	public static final String CONNECTION_HOVER_EVENT = "connection" + EVENT_NAME_SEPARATOR + "hover";

	/**
	 * DiagramJS {@link Event} name for creating a class property.
	 */
	public static final String CREATE_CLASS_PROPERTY_EVENT = "create.class.property";

	/**
	 * DiagramJS {@link Event} name for creating a class.
	 */
	public static final String CREATE_CLASS_EVENT = "create.class";

	/**
	 * DiagramJS {@link Event} name for creating an enumeration.
	 */
	public static final String CREATE_ENUMERATION_EVENT = "create.enumeration";

	/**
	 * DiagramJS {@link Event} name for deleting elements.
	 */
	public static final String ELEMENTS_DELETE_EVENT = "elements.delete";

	/**
	 * DiagramJS {@link Event} name for moving elements.
	 */
	public static final String ELEMENTS_MOVE_EVENT = "elements.move";

	/**
	 * DiagramJS {@link Event} name when diagram elements have changed their visibility.
	 */
	public static final String ELEMENTS_VISIBILITY_EVENT = "elements.visibility";

	/**
	 * DiagramJS {@link Event} name for updating waypoints of a connection.
	 */
	public static final String UPDATET_WAYPOINTS_EVENT = "connection.updateWaypoints";

	/**
	 * DiagramJS {@link Event} name for resizing a shape.
	 */
	public static final String SHAPE_RESIZE_EVENT = "shape.resize";

	/**
	 * DiagramJS {@link Event} name for layouting a connection.
	 */
	public static final String CONNECTION_LAYOUT_EVENT = "connection.layout";

	/**
	 * DiagramJS {@link Event} name for deleting element.
	 */
	public static final String DELETE_ELEMENT_EVENT = "delete.element";

	/**
	 * DiagramJS {@link Event} name for a go to the definition of an element.
	 */
	public static final String ELEMENT_GOTO_EVENT = "element.goto";
}
