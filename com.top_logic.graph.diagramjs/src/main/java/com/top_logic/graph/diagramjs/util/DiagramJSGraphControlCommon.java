/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.util;

/**
 * Code that is used both on the server in the control and on the client side when interacting with
 * the diagramjs control on the server.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface DiagramJSGraphControlCommon {

	/**
	 * Command ID to create connections on graphs.
	 */
	public static final String CREATE_CONNECTION_COMMAND = "createConnection";

	/**
	 * Command ID to create a class property.
	 */
	public static final String CREATE_CLASS_PROPERTY_COMMAND = "createClassProperty";

	/**
	 * Command ID to create a class.
	 */
	public static final String CREATE_CLASS_COMMAND = "createClass";

	/**
	 * Command ID to create an enumeration.
	 */
	public static final String CREATE_ENUMERATION_COMMAND = "createEnumeration";

	/**
	 * Command ID to delete graph parts.
	 */
	public static final String DELETE_GRAPH_PART_COMMAND = "deleteGraphPart";

	/**
	 * Command ID to go to the definition of a given element.
	 */
	public static final String GO_TO_DEFINITION_COMMAND = "gotoDefinition";

	/**
	 * Command ID to handle client diagram element visibility changes.
	 */
	public static final String ELEMENTS_VISIBILITY_COMMAND = "handleElementsVisibility";

}
