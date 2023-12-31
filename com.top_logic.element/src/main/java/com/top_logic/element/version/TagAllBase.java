/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.version;

/**
 * Basic interface for {@link #TAG_ALL_TYPE} business objects.
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.InterfaceGenerator}
 */
public interface TagAllBase extends com.top_logic.model.TLNamed {

	/**
	 * Name of type <code>Tag.all</code>
	 */
	String TAG_ALL_TYPE = "Tag.all";

	/**
	 * Part <code>date</code> of <code>Tag.all</code>
	 * 
	 * <p>
	 * Declared as <code>tl.core:Date</code> in configuration.
	 * </p>
	 */
	String DATE_ATTR = "date";

	/**
	 * Part <code>name</code> of <code>Tag.all</code>
	 * 
	 * <p>
	 * Declared as <code>tl.core:String</code> in configuration.
	 * </p>
	 */
	String NAME_ATTR = "name";

	/**
	 * Part <code>taggedObj</code> of <code>Tag.all</code>
	 * 
	 * <p>
	 * Declared as <code>tl.model:TLObject</code> in configuration.
	 * </p>
	 */
	String TAGGED_OBJ_ATTR = "taggedObj";

	/**
	 * Getter for part {@link #DATE_ATTR}.
	 */
	default java.util.Date getDate() {
		return (java.util.Date) tValueByName(DATE_ATTR);
	}

	/**
	 * Getter for part {@link #TAGGED_OBJ_ATTR}.
	 */
	default com.top_logic.model.TLObject getTaggedObj() {
		return (com.top_logic.model.TLObject) tValueByName(TAGGED_OBJ_ATTR);
	}

	/**
	 * Setter for part {@link #TAGGED_OBJ_ATTR}.
	 */
	default void setTaggedObj(com.top_logic.model.TLObject newValue) {
		tUpdateByName(TAGGED_OBJ_ATTR, newValue);
	}

}
