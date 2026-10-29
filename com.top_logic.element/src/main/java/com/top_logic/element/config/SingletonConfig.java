/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.DynamicModelService.Config.ModuleSetting;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
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
	 * {@link AbstractModelPartMapping} that produces {@link TLModule}-local type names.
	 */
	class LocalTypeMapping extends AbstractModelPartMapping<String> {

		private String _moduleName;

		/**
		 * Creates a {@link SingletonConfig.LocalTypeMapping}.
		 */
		public LocalTypeMapping(DeclarativeFormOptions options) {
			_moduleName = moduleName(options);
		}

		/**
		 * The name of the module the edited singleton is defined in.
		 * 
		 * <p>
		 * In the model editor, the singleton is defined in the {@link ModuleConfig} being the form
		 * model. In the configuration of the {@link DynamicModelService} (e.g. in the service
		 * editor), the singleton is defined in a {@link ModuleSetting} somewhere in the edited
		 * configuration, so the module is searched in the enclosing configurations.
		 * </p>
		 */
		private static String moduleName(DeclarativeFormOptions options) {
			for (FormContainer container = options.getContainer(); container != null; container =
				container.getParent()) {
				Object model = EditorFactory.getModel(container);
				if (model instanceof ModuleConfig module) {
					return module.getName();
				}
				if (model instanceof ModuleSetting setting) {
					return setting.getName();
				}
			}
			ConfigurationItem formModel = options.get(DeclarativeFormBuilder.FORM_MODEL);
			if (formModel instanceof ModuleConfig module) {
				return module.getName();
			}
			return null;
		}

		@Override
		protected TLModelPart resolveName(String name) throws ConfigurationException {
			if (name.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR) < 0) {
				if (_moduleName == null) {
					throw new ConfigurationException(I18NConstants.ERROR_UNKNOWN_SINGLETON_MODULE__TYPE.fill(name),
						TYPE_SPEC, name);
				}
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