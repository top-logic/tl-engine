/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import java.util.Map;

import com.top_logic.base.security.SecurityContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} to prevent deleting users with administrative rights.
 * 
 * <p>
 * This rule is required to prevent users that have no administrative rights (but delete access to
 * the users table) from deleting (the last) root user.
 * </p>
 */
public class DisallowRootDeleteRule implements ExecutabilityRule {

	public static final DisallowRootDeleteRule INSTANCE = new DisallowRootDeleteRule();

	/**
	 * Creates a new instance of this class.
	 */
	protected DisallowRootDeleteRule() {
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		{
			if (model instanceof Person && SecurityContext.isSuperUser(((Person) model).getUser())) {
				return ExecutableState.createDisabledState(I18NConstants.ERROR_CANNOT_DELETE_ADMIN_USER);
			}
		}
		return ExecutableState.EXECUTABLE;
	}

}
