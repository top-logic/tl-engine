/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.model;


import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.model.util.TLModelUtil;

/**
 * Utility class for creating legacy type references.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TL5Types {

	public static final String NODE_PROTOCOL = "node";

	public static final String CLASS_PROTOCOL = "me";

	public static final String ENUM_PROTOCOL = "enum";

	/**
	 * Creates a {@link FastList} type reference.
	 */
	public static String listTypeSpec(String listType) {
		return ENUM_PROTOCOL + TLModelUtil.QUALIFIED_NAME_SEPARATOR + listType;
	}

	/**
	 * Creates a meta element type reference.
	 */
	public static String meTypeSpec(String meType) {
		return CLASS_PROTOCOL + TLModelUtil.QUALIFIED_NAME_SEPARATOR + meType;
	}

	/**
	 * Creates a structure node type reference.
	 */
	public static String nodeTypeSpec(String structureType, String nodeType) {
		return NODE_PROTOCOL + TLModelUtil.QUALIFIED_NAME_SEPARATOR + structureType
			+ TLModelUtil.QUALIFIED_NAME_SEPARATOR + nodeType;
	}

}
