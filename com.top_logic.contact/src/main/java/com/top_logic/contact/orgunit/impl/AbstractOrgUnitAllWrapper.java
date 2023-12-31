/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.orgunit.impl;


import com.top_logic.contact.orgunit.OrgUnitAll;
import com.top_logic.element.structured.wrap.AttributedStructuredElementWrapper;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * Implementation base class for wrapper classes of <code>OrgUnit.all</code> business objects.
 * 
 * <p>
 * Note: this is generated code. Do not modify. Instead, create subclass and register this class as wrapper implementation.
 * </p>
 * 
 * @author Automatically generated by <code>com.top_logic.element.binding.gen.ImplementationGenerator</code>
 */
public class AbstractOrgUnitAllWrapper extends AttributedStructuredElementWrapper implements OrgUnitAll {

	/**
	 * Default constructor.
	 */
	public AbstractOrgUnitAllWrapper(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public String getName() {
		return (String) getValue(ATTRIBUTE_NAME);
	}

	@Override
	public void setName(String newValue) {
		setValue(ATTRIBUTE_NAME, newValue);
	}

}
