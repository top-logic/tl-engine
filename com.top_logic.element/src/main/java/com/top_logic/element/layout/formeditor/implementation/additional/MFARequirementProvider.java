/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.layout.formeditor.implementation.additional;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.element.layout.formeditor.definition.FieldDefinition;
import com.top_logic.element.layout.formeditor.implementation.FieldDefinitionTemplateProvider;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Whether multi-factor authentication is required for the edited {@link Person}.
 * 
 * @see Person#getMFARequirement()
 */
@InApp
@Label("MFA requirement")
public class MFARequirementProvider extends AbstractIsAdminProvider {

	private final FieldDefinitionTemplateProvider _delegate;

	/**
	 * Creates a new {@link MFARequirementProvider}.
	 */
	public MFARequirementProvider() {
		FieldDefinition definition = TypedConfiguration.newConfigItem(FieldDefinition.class);
		definition.setAttribute(Person.MFA_REQUIREMENT_ATTR);
		definition.setTypeSpec(Person.PERSON_TYPE);
		_delegate = TypedConfigUtil.createInstance(definition);
	}

	@Override
	protected FieldDefinitionTemplateProvider getDelegate() {
		return _delegate;
	}

}

