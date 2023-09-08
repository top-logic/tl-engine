/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModule;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Assignment of {@link #getRoles()} to a {@link #getGroup()} on the declaring context element.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RoleAssignment extends ConfigurationItem {

	/** Property name of {@link #getRoles()}. */
	String ROLE = "role";

	/** Property name of {@link #getGroup()}. */
	String GROUP = "group";

	/**
	 * The group to which the {@link #getRoles()} are assigned on the context instance.
	 */
	@Key(GROUP)
	@Options(fun = AllGlobalGroups.class, mapping = GroupNameOptionMapping.class)
	String getGroup();

	/**
	 * The assigned role(s).
	 * 
	 * <p>
	 * Members of the {@link #getGroup()} get these roles on the annotated context object.
	 * </p>
	 * 
	 * @see SingletonConfig#getRoleAssignments()
	 */
	@Name(ROLE)
	@Format(CommaSeparatedStrings.class)
	@Options(fun = AllRolesInContext.class, mapping = RoleNameOptionMapping.class)
	List<String> getRoles();

	/**
	 * Lookup function for {@link Group#getAllGlobalGroups()}.
	 */
	class AllGlobalGroups extends Function0<List<Group>> {
		@Override
		public List<Group> apply() {
			return Group.getAllGlobalGroups();
		}
	}

	/**
	 * {@link OptionMapping} allowing to store {@link Group}s by their {@link Group#getName() name}.
	 */
	class GroupNameOptionMapping implements OptionMapping {
		@Override
		public Object toSelection(Object option) {
			if (option == null) {
				return null;
			}
			return ((Group) option).getName();
		}
	}

	/**
	 * Function to lookup all {@link BoundedRole}s defined on the model of the context component.
	 */
	class AllRolesInContext extends Function0<List<BoundedRole>> {

		private TLModule _context;

		/**
		 * Creates a {@link RoleAssignment.AllRolesInContext}.
		 */
		public AllRolesInContext(DeclarativeFormOptions options) {
			LayoutComponent component = options.get(DeclarativeFormBuilder.COMPONENT);
			if (component != null) {
				Object model = component.getModel();
				if (model instanceof TLModule) {
					_context = (TLModule) model;
				}
			}
		}

		@Override
		public List<BoundedRole> apply() {
			Collection<BoundedRole> allRoles =
				_context == null ? BoundedRole.getAllGlobalRoles() : BoundedRole.getDefinedRoles(_context);
			ArrayList<BoundedRole> result = new ArrayList<>(allRoles);
			Collections.sort(result, (r1, r2) -> r1.getName().compareTo(r2.getName()));
			return result;
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