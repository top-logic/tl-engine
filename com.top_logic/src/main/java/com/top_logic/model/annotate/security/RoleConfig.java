/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.security;

import java.util.Collection;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.GenericFunction;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Definition of a role.
 * 
 * @see TLRoleDefinitions#getRoles()
 * @see BoundedRole
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RoleConfig extends NamedConfigMandatory {

	/**
	 * Pattern describing a valid role name.
	 */
	String ROLE_NAME_PATTERN = "[a-zA-z][a-zA-z0-9_\\.]*";

	/**
	 * The name of the declared role.
	 */
	@Override
	@RegexpConstraint(ROLE_NAME_PATTERN)
	@Options(fun = AllRoles.class, mapping = RoleNameOptionMapping.class)
	String getName();

	/**
	 * {@link GenericFunction} retrieving all {@link BoundedRole}s.
	 */
	class AllRoles extends Function0<Collection<BoundedRole>> {

		@Override
		public Collection<BoundedRole> apply() {
			return BoundedRole.getAll();
		}

	}

	/**
	 * {@link OptionMapping} allowing to store {@link BoundRole}s identified by their
	 * {@link BoundedRole#getName() name}.
	 */
	class RoleNameOptionMapping implements OptionMapping {
		@Override
		public Object toSelection(Object option) {
			if (option == null) {
				return null;
			}
			return ((BoundRole) option).getName();
		}
	}

}
