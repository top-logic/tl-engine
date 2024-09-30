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
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.label.ClassLabelProvider;
import com.top_logic.util.Resources;

/**
 * {@link LabelProvider} based on {@link TemplateExpression}s in resource files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ConfigLabelProvider extends AbstractResourceProvider {

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

		if (object instanceof ConfigurationItem) {
			ConfigurationItem config = (ConfigurationItem) object;
			ResourceTemplate template = configurationTemplate(config);
			return expand(template, config);
		}
		else if (object instanceof ConfiguredInstance) {
			ConfigurationItem config = ((ConfiguredInstance<?>) object).getConfig();
			ResourceTemplate template = implementationTemplate(object, config);
			return expand(template, config);
		} else if (object instanceof Class<?>) {
			return Resources.getInstance().getString(classKey(object));
		} else {
			return Resources.getInstance().getString(objectKey(object));
		}
	}

	@Override
	public String getTooltip(Object object) {
		if (object == null) {
			return null;
		}

		ResKey tooltip;
		if (object instanceof ConfigurationItem) {
			ConfigurationItem config = (ConfigurationItem) object;
			ResourceTemplate template = configurationTemplate(config);
			tooltip = templateTooltip(template);
		} else if (object instanceof ConfiguredInstance) {
			ConfigurationItem config = ((ConfiguredInstance<?>) object).getConfig();
			ResourceTemplate template = implementationTemplate(object, config);
			tooltip = templateTooltip(template);
		} else if (object instanceof Class<?>) {
			tooltip = classKey(object).tooltip();
		} else {
			tooltip = objectKey(object);
		}

		return Resources.getInstance().getStringOptional(tooltip);
	}

	private ResKey templateTooltip(ResourceTemplate template) {
		return ResKey.internalCreate(template.getName()).tooltip();
	}

	private ResourceTemplate configurationTemplate(ConfigurationItem config) {
		String implementationName;
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
		ResourceTemplate template = getTemplate(config, implementationName);
		return template;
	}

	private ResourceTemplate implementationTemplate(Object object, ConfigurationItem config) {
		String implementationName = object.getClass().getName();
		ResourceTemplate template = getTemplate(config, implementationName);
		return template;
	}

	private ResKey classKey(Object object) {
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
		return classKey;
	}

	private ResKey objectKey(Object object) {
		return ResKey.forClass(object.getClass());
	}

	private String expand(ResourceTemplate template, ConfigurationItem config) {
		TemplateExpression expression = template.getExpression();
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
			expression.visit(expander, new Eval.EvalContext(config, _variables));
		} catch (EvalException ex) {
			String message = "Error evaluating template '" + expression + "' for configuration '"
				+ config.descriptor().getConfigurationInterface().getName() + "': " + ex.getMessage();
			Logger.error(message, ex, ConfigLabelProvider.class);
			return "[" + message + "]";
		}
		return buffer.toString();
	}

	private ResourceTemplate getTemplate(ConfigurationItem config, String implementationName) {
		if (implementationName != null) {
			ResourceTemplate template = getTemplate(implementationName, true);
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
			ResourceTemplate template = getTemplateWithSuffix(interfaceName, true);
			if (template != null) {
				return template;
			}
		}
		return getTemplateWithoutSuffix(interfaceName, false);
	}

	private ResourceTemplate getTemplate(String baseName, boolean optional) {
		if (_suffix != null) {
			return getTemplateWithSuffix(baseName, optional);
		} else {
			return getTemplateWithoutSuffix(baseName, optional);
		}
	}

	private ResourceTemplate getTemplateWithSuffix(String baseName, boolean optional) {
		String name = baseName + _suffix;
		return ResourceTemplate.create(name, _scope.getTemplate(name, optional));
	}

	private ResourceTemplate getTemplateWithoutSuffix(String baseName, boolean optional) {
		return ResourceTemplate.create(baseName, _scope.getTemplate(baseName, optional));
	}

	private interface ResourceTemplate {

		String getName();

		TemplateExpression getExpression();

		public static ResourceTemplate create(String name, TemplateExpression expression) {
			if (expression == null) {
				return null;
			}
			return new ResourceTemplate() {
				@Override
				public String getName() {
					return name;
				}

				@Override
				public TemplateExpression getExpression() {
					return expression;
				}
			};
		}
	}
}