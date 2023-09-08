/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.template.CachedScope;
import com.top_logic.basic.config.template.Eval;
import com.top_logic.basic.config.template.Eval.EvalException;
import com.top_logic.basic.config.template.Expand;
import com.top_logic.basic.config.template.StringOutput;
import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.basic.config.template.TemplateScope;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.label.ClassLabelProvider;
import com.top_logic.util.Resources;

/**
 * {@link LabelProvider} based on {@link TemplateExpression}s in resource files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ConfigLabelProvider implements LabelProvider {

	private final TemplateScope _scope;

	private final String _suffix;

	private final Map<String, ? extends Object> _variables;

	/**
	 * Creates a {@link ConfigLabelProvider} that that looks up templates from the {@link Resources}
	 * of the current user.
	 * 
	 * <p>
	 * <b>Note:</b> This instance must not be shared among sessions, since it cannot be accessed
	 * multi-threaded, caches parsed templates and is bound to the language of the currently
	 * logged-in user.
	 * </p>
	 */
	public ConfigLabelProvider() {
		this(null);
	}

	/**
	 * Creates a {@link ConfigLabelProvider} that that looks up templates from the {@link Resources}
	 * of the current user.
	 * 
	 * <p>
	 * <b>Note:</b> This instance must not be shared among sessions, since it cannot be accessed
	 * multi-threaded, caches parsed templates and is bound to the language of the currently
	 * logged-in user.
	 * </p>
	 * 
	 * @param suffix
	 *        The suffix that is appended to the {@link ConfigurationItem} name to build a template
	 *        resource key. <code>null</code> means that no suffix is used.
	 */
	public ConfigLabelProvider(String suffix) {
		this(suffix, new CachedScope(new ResourceScope()), Collections.<String, Object> emptyMap());
	}

	/**
	 * Creates a {@link ConfigLabelProvider} that uses a custom {@link TemplateScope} for looking up
	 * templates.
	 * 
	 * @param suffix
	 *        The suffix that is appended to the {@link ConfigurationItem} name to build a template
	 *        resource key. <code>null</code> means that no suffix is used.
	 * @param scope
	 *        Registry of {@link TemplateExpression}s by name.
	 * @param variables
	 *        Global variable bindings that can be used from templates.
	 */
	public ConfigLabelProvider(String suffix, TemplateScope scope, Map<String, ? extends Object> variables) {
		_suffix = StringServices.nonEmpty(suffix);
		_scope = scope;
		_variables = variables;
	}

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		}

		String implementationName;
		ConfigurationItem config;
		if (object instanceof ConfigurationItem) {
			config = (ConfigurationItem) object;
			if (config instanceof PolymorphicConfiguration<?>) {
				Class<?> implementationClass = ((PolymorphicConfiguration<?>) config).getImplementationClass();
				if (implementationClass != null) {
					implementationName = implementationClass.getName();
				} else {
					implementationName = null;
				}
			} else {
				implementationName = null;
			}
		}
		else if (object instanceof ConfiguredInstance) {
			config = ((ConfiguredInstance<?>) object).getConfig();
			implementationName = object.getClass().getName();
		}
		else if (object instanceof Class<?>) {
			Class<?> clazz = (Class<?>) object;
			ResKey classKey = ResKey.forClass(clazz);
			if (_suffix != null) {
				// Workaround for classKey.suffix(_suffix) not being possible due to the constraint
				// that suffix must start with '.' character.
				ResKey suffixKey = ResKey.internalCreate(classKey.getKey() + _suffix);
				classKey = ResKey.fallback(suffixKey, classKey);
			}
			
			// Last resort: The simple class name - this makes UIs usable, even if not all classes
			// are internationalized.
			classKey = ResKey.fallback(classKey, ResKey.text(ClassLabelProvider.simpleName(clazz)));

			return Resources.getInstance().getString(classKey);
		}
		else {
			return Resources.getInstance().getString(ResKey.forClass(object.getClass()));
		}

		TemplateExpression template = getTemplate(config, implementationName);

		return expand(template, config);
	}

	private String expand(TemplateExpression template, ConfigurationItem config) {
		StringOutput buffer = new StringOutput() {
			@Override
			protected String toString(Object value) {
				if (value instanceof ConfigurationItem) {
					return getLabel(value);
				}
				if (value instanceof ConfiguredInstance<?>) {
					return getLabel(((ConfiguredInstance<?>) value).getConfig());
				}
				return MetaLabelProvider.INSTANCE.getLabel(value);
			}
		};
		Expand expander = new Expand(_scope, buffer);
		try {
			template.visit(expander, new Eval.EvalContext(config, _variables));
		} catch (EvalException ex) {
			String message = "Error evaluating template '" + template + "' for configuration '"
				+ config.descriptor().getConfigurationInterface().getName() + "': " + ex.getMessage();
			Logger.error(message, ex, ConfigLabelProvider.class);
			return "[" + message + "]";
		}
		return buffer.toString();
	}

	private TemplateExpression getTemplate(ConfigurationItem config, String implementationName) {
		if (implementationName != null) {
			TemplateExpression template = getTemplate(implementationName, true);
			if (template != null) {
				return template;
			}

			if (_suffix != null) {
				template = getTemplateWithoutSuffix(implementationName, true);
				if (template != null) {
					return template;
				}
			}
		}

		String interfaceName = config.descriptor().getConfigurationInterface().getName();
		if (_suffix != null) {
			TemplateExpression template = getTemplateWithSuffix(interfaceName, true);
			if (template != null) {
				return template;
			}
		}
		return getTemplateWithoutSuffix(interfaceName, false);
	}

	private TemplateExpression getTemplate(String baseName, boolean optional) {
		if (_suffix != null) {
			return getTemplateWithSuffix(baseName, optional);
		} else {
			return getTemplateWithoutSuffix(baseName, optional);
		}
	}

	private TemplateExpression getTemplateWithSuffix(String baseName, boolean optional) {
		return _scope.getTemplate(baseName + _suffix, optional);
	}

	private TemplateExpression getTemplateWithoutSuffix(String baseName, boolean optional) {
		return _scope.getTemplate(baseName, optional);
	}
}