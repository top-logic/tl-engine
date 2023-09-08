/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

/**
 * Create input fields for MetaAttributes in a meta group.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class GroupedMetaInputCellTag extends MetaInputCellTag {

	private String name;

	/**
	 * Returns the name of the input field.
	 * 
	 * @return Name of the input field.
	 */
	public String getName() {
		return (this.name);
	}

	/**
	 * Sets the name for the input field by the given name and initializes the tag.
	 * 
	 * @param aName
	 *        Name of the input field.
	 */
	public void setName(String aName) {
		this.name = aName;

		GroupedMetaInputTag.initAttributeUpdate(this, aName);
	}

}
