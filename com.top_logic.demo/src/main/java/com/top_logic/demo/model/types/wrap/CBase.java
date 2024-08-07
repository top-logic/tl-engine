/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.wrap;

/**
 * Basic interface for {@link #C_TYPE} business objects.
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.InterfaceGenerator}
 */
public interface CBase extends com.top_logic.demo.model.types.DemoTypesCAll, com.top_logic.demo.model.types.AChild, com.top_logic.demo.model.types.BChild, com.top_logic.demo.model.types.DemoTypesContainer, com.top_logic.demo.model.types.CChild {

	/**
	 * Name of type <code>C</code>
	 */
	String C_TYPE = "C";

	/**
	 * Getter for part {@link #CHILDREN_ATTR}.
	 */
	@Override
	@SuppressWarnings("unchecked")
	default java.util.List<? extends com.top_logic.demo.model.types.CChild> getChildren() {
		return (java.util.List<? extends com.top_logic.demo.model.types.CChild>) tValueByName(CHILDREN_ATTR);
	}

}
