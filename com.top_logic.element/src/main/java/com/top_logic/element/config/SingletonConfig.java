/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.util.Collection;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.security.TLRoleDefinitions;
import com.top_logic.model.config.AbstractModelPartMapping;
import com.top_logic.model.config.TypeRef;
import com.top_logic.model.util.AllClasses;
import com.top_logic.model.util.TLModelUtil;

/**
 * Definition of a well-known named object (e.g. a structure root element).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SingletonConfig extends TypeRef {

	/** Property name of {@link #getName()}. */
	String NAME = "name";

	/** Property name of {@link #getRoleAssignments()}. */
	String ROLE_ASSIGNMENTS = "role-assignments";

	/**
	 * Name of the singleton object.
	 * 
	 * <p>
	 * This special object gets a qualified model name such as an enumeration classifier. The
	 * qualified name of a singleton is <code>`my.module#SINGLETON_NAME`</code>. This qualified name
	 * can be used in TL-Script to access the object by name. The default singleton name in a module
	 * is <code>ROOT</code>, but any other name will do. Typically, singletons are used to
	 * distinguish the root node of a tree structure.
	 * </p>
	 * 
	 * @see PartConfig#getName()
	 */
	@Name(NAME)
	@RegexpConstraint(value = "[a-zA-Z_][a-zA-Z_0-9]*")
	@StringDefault(TLModule.DEFAULT_SINGLETON_NAME)
	String getName();

	/**
	 * @see #getName()
	 */
	void setName(String value);

	@Override
	@Options(fun = AllClasses.class, mapping = LocalTypeMapping.class)
	@Mandatory
	String getTypeSpec();

	/**
	 * Roles assigned to group members on this instance.
	 * 
	 * @implNote The role assignments are not offered for configuration in the UI, since they are
	 *           edited in a specialized role assignment UI.
	 * 
	 * @see TLRoleDefinitions#getRoles()
	 */
	@Hidden
	@Key(RoleAssignment.GROUP)
	@Name(ROLE_ASSIGNMENTS)
	Collection<RoleAssignment> getRoleAssignments();

	/**
	 * {@link AbstractModelPartMapping} that produces {@link TLModule}-local type names.
	 */
	class LocalTypeMapping extends AbstractModelPartMapping<String> {

		private String _moduleName;

		/**
		 * Creates a {@link SingletonConfig.LocalTypeMapping}.
		 */
		public LocalTypeMapping(DeclarativeFormOptions options) {
			ModuleConfig module = (ModuleConfig) options.get(DeclarativeFormBuilder.FORM_MODEL);
			_moduleName = module.getName();
		}

		@Override
		protected TLModelPart resolveName(String name) throws ConfigurationException {
			if (name.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR) < 0) {
				TLModule module = TLModelUtil.findModule(_moduleName);
				if (name.indexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR) < 0) {
					return TLModelUtil.findType(module, name);
				} else {
					return TLModelUtil.findPart(module, name);
				}
			} else {
				return (TLModelPart) TLModelUtil.resolveQualifiedName(name);
			}
		}

		@Override
		protected String buildName(TLModelPart option) {
			String result = TLModelUtil.qualifiedName(option);
			int moduleSep = result.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
			if (moduleSep >= 0 && result.substring(0, moduleSep).equals(_moduleName)) {
				// Localize name.
				return result.substring(moduleSep + 1);
			} else {
				return result;
			}
		}
	}

}