/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.v5;

/**
 * Naming convention for a <i>TopLogic</i> 5 model import.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ModelNamingConvention {

	/**
	 * The name of the type synthesized for a meta object type.
	 */
	String getMetaObjectTypeName(String name);

	/**
	 * The name of the type synthesized for a structure node type.
	 */
	String getNodeTypeName(String nodeName);

	/**
	 * The name of the type synthesized for a common super type for all nodes of a structure.
	 */
	String getStructureTypeName(String structureName);

	/**
	 * The name of the type synthesized for a meta element type.
	 */
	String getElementTypeName(String elementTypeName);

	/**
	 * The name of the source end of associations created for object valued meta attributes.
	 */
	String getReferenceSourceName(String elementTypeName);

	/**
	 * The name an enumeration type for a fast list.
	 */
	String getEnumName(String fastListName);

	/**
	 * The name of a classifier of an enumeration.
	 */
	String getClassifierName(String fastListName, String listElementName);

}