/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.importer.data.struct.impl;

/**
 * Basic interface for {@link #TEST_STRUCT_SUB2_TYPE} business objects.
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.InterfaceGenerator}
 */
public interface TestStructSub2Base extends test.com.top_logic.importer.data.struct.TestStructAll {

	/**
	 * Name of type <code>testStruct.Sub2</code>
	 */
	String TEST_STRUCT_SUB2_TYPE = "testStruct.Sub2";

	/**
	 * Part <code>lead</code> of <code>testStruct.Sub2</code>
	 * 
	 * <p>
	 * Declared as <code>Contacts:Contact.Person</code> in configuration.
	 * </p>
	 */
	String LEAD_ATTR = "lead";

	/**
	 * Part <code>size</code> of <code>testStruct.Sub2</code>
	 * 
	 * <p>
	 * Declared as <code>tl.core:Long</code> in configuration.
	 * </p>
	 */
	String SIZE_ATTR = "size";

	/**
	 * Getter for part {@link #LEAD_ATTR}.
	 */
	default com.top_logic.model.TLObject getLead() {
		return (com.top_logic.model.TLObject) tValueByName(LEAD_ATTR);
	}

	/**
	 * Setter for part {@link #LEAD_ATTR}.
	 */
	default void setLead(com.top_logic.model.TLObject newValue) {
		tUpdateByName(LEAD_ATTR, newValue);
	}

	/**
	 * Getter for part {@link #SIZE_ATTR}.
	 */
	default Long getSize() {
		return (Long) tValueByName(SIZE_ATTR);
	}

	/**
	 * Setter for part {@link #SIZE_ATTR}.
	 */
	default void setSize(Long newValue) {
		tUpdateByName(SIZE_ATTR, newValue);
	}

}
