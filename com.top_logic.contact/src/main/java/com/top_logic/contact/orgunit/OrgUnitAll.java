/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.orgunit;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.element.structured.StructuredElement;

/**
 * Interface for {@link OrgUnitAllConstants#ME_ORG_UNIT_ALL} business objects.
 * 
 * @author Automatically generated by <code>com.top_logic.element.binding.gen.InterfaceGenerator</code>
 */
public interface OrgUnitAll extends Wrapper, StructuredElement, OrgUnitAllConstants {

	/** Getter for attribute {@link #ATTRIBUTE_NAME}. */
	@Override
	String getName();

	/** Setter for attribute {@link #ATTRIBUTE_NAME}. */
	@Override
	void setName(String newValue);

}
