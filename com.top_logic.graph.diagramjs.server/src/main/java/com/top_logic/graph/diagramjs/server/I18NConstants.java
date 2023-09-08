/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server;

import com.top_logic.basic.util.ResKey;
import com.top_logic.graph.diagramjs.server.commands.CreateClassCommand;
import com.top_logic.graph.diagramjs.server.commands.CreateConnectionGraphCommand;
import com.top_logic.graph.diagramjs.server.commands.DeleteGraphPartCommand;
import com.top_logic.graph.diagramjs.server.commands.GoToDefinitionCommand;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.model.TLClass;

/**
 * Internationalization constants for this package.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Label deletion message.
	 */
	public static ResKey DELETE_LABEL_MESSAGE;

	/**
	 * Node deletion message.
	 */
	public static ResKey DELETE_NODE_MESSAGE;

	/**
	 * Edge deletion message.
	 */
	public static ResKey DELETE_EDGE_MESSAGE;

	/**
	 * Type of the to deleted object is not supported.
	 */
	public static ResKey UNKNOWN_DELETION_TYPE_MESSAGE;

	/**
	 * Command to create a connection between {@link TLClass}es.
	 * 
	 * @see CreateConnectionGraphCommand
	 */
	public static ResKey CREATE_CONNECTION_COMMAND;

	/**
	 * Command to create a {@link TLClass}.
	 * 
	 * @see CreateClassCommand
	 */
	public static ResKey CREATE_CLASS_COMMAND;

	/**
	 * Command to delete a graph part.
	 * 
	 * @see DeleteGraphPartCommand
	 */
	public static ResKey DELETE_GRAPH_PART_COMMAND;

	/**
	 * Command to go to the definition of a graph part.
	 * 
	 * @see GoToDefinitionCommand
	 */
	public static ResKey GO_TO_DEFINITION_COMMAND;

	/**
	 * Delete protection is enabled.
	 * 
	 * @see DeleteGraphPartCommand
	 */
	public static ResKey GRAPH_PART_DELETE_PROTECTED_ENABLED;

	/**
	 * Command to create a class property.
	 * 
	 * @see CreateConnectionGraphCommand
	 */
	public static ResKey CREATE_CLASS_PROPERTY_COMMAND;

	/**
	 * Title for the dialog to create a reference connection.
	 */
	public static ResKey CREATE_REFERENCE_CONNECTION_TITLE;

	/**
	 * Title for the dialog to create an inheritance connection.
	 */
	public static ResKey CREATE_INHERITANCE_CONNECTION_TITLE;

	/**
	 * Title for the dialog to create a class property.
	 */
	public static ResKey CREATE_CLASS_PROPERTY_TITLE;

	/**
	 * Title for the dialog to create a class.
	 */
	public static ResKey CREATE_CLASS_TITLE;

	/**
	 * Title for the dialog to create an enumeration.
	 */
	public static ResKey CREATE_ENUMERATION_TITLE;

	/**
	 * @see CreateConnectionGraphCommand
	 */
	public static ResKey ERROR_INHERITANCE_ONLY_BETWEEN_CLASSES;

	/**
	 * @see DiagramJSGraphComponent#createInheritance(TLClass, TLClass)
	 */
	public static ResKey ERROR_NO_CYCLIC_INHERITANCE;

	static {
		initConstants(I18NConstants.class);
	}
}