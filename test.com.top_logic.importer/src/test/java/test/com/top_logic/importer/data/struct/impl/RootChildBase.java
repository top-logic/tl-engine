/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.importer.data.struct.impl;

/**
 * Basic interface for {@link #ROOT_CHILD_TYPE} business objects.
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.InterfaceGenerator}
 */
public interface RootChildBase extends com.top_logic.element.structured.StructuredElement {

	/**
	 * Name of type <code>RootChild</code>
	 */
	String ROOT_CHILD_TYPE = "RootChild";

	/**
	 * Getter for part {@link #PARENT_ATTR}.
	 */
	@Override
	default test.com.top_logic.importer.data.struct.Root getParent() {
		return (test.com.top_logic.importer.data.struct.Root) tValueByName(PARENT_ATTR);
	}

}
