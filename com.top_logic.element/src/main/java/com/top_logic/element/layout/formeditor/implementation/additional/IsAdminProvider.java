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
 * Whether the edited {@link Person} is an technical administrator.
 * 
 * @see Person#isAdmin()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
@Label("Is administrator")
public class IsAdminProvider extends AbstractIsAdminProvider {

	private final FieldDefinitionTemplateProvider _delegate;

	/**
	 * Creates a new {@link IsAdminProvider}.
	 */
	public IsAdminProvider() {
		FieldDefinition definition = TypedConfiguration.newConfigItem(FieldDefinition.class);
		definition.setAttribute(Person.ADMIN_ATTR);
		definition.setTypeSpec(Person.PERSON_TYPE);
		_delegate = TypedConfigUtil.createInstance(definition);
	}

	@Override
	protected FieldDefinitionTemplateProvider getDelegate() {
		return _delegate;
	}

}

