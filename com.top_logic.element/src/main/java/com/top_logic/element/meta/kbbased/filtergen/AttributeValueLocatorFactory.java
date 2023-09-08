/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.element.meta.expr.internal.OperationFactory;
import com.top_logic.element.meta.expr.parser.ExpressionParser;
import com.top_logic.element.meta.expr.parser.ParseException;
import com.top_logic.element.meta.expr.parser.TokenMgrError;

/**
 * Get QueryLocator for Wrapper based MetaAttributed
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class AttributeValueLocatorFactory extends ManagedClass {

	private final Map<String, Class<? extends AttributeValueLocator>> _classByName;

	private final ConcurrentHashMap<String, AttributeValueLocator> instanceByExpr =
		new ConcurrentHashMap<>();

	private final ConcurrentHashMap<AttributeValueLocator, String> exprByInstance =
		new ConcurrentHashMap<>();
	
	/**
	 * Configuration for {@link AttributeValueLocatorFactory}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<AttributeValueLocatorFactory> {
		/**
		 * Attribute value locators.
		 */
		@MapBinding()
		Map<String, Class<? extends AttributeValueLocator>> getLocators();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link AttributeValueLocatorFactory}.
	 */
	public AttributeValueLocatorFactory(InstantiationContext context, Config config) {
		super(context, config);

		_classByName = config.getLocators();
	}

	/**
	 * Get the AttributeValueLocator configured for the AttributeValueLocator config
	 * 
	 * @param aLocatorConfig
	 *        the AttributeValueLocator name in the config plus '('attribute path')'
	 * @return the AttributeValueLocator or <code>null</code> if none was configured or creation fails
	 */
    public static AttributeValueLocator configuredLocator(String aLocatorConfig) {
    	return getInstance().internalConfiguredLocator(aLocatorConfig);
    }

    protected AttributeValueLocator internalConfiguredLocator(String aLocatorConfig) {
        try {
        	int argStartIdx = aLocatorConfig.indexOf('(');
        	int argEndIdx   = aLocatorConfig.lastIndexOf(')');
        	
			String locatorName;
			String locatorArgument;
			if (argStartIdx < 0 && argEndIdx < 0) {
				locatorName = aLocatorConfig;
				locatorArgument = "";

				AttributeValueLocator locator = singletonLocator(locatorName);
				if (locator != null) {
					return locator;
				}
			} else {
				if (argStartIdx <= 0 || argEndIdx != (aLocatorConfig.length() - 1)) {
					throw new IllegalArgumentException("Locator config parse error: " + aLocatorConfig
						+ ". Must be in the form 'locator-name(config-string)'.");
				} else {
					locatorName = aLocatorConfig.substring(0, argStartIdx);
					locatorArgument = aLocatorConfig.substring(argStartIdx + 1, argEndIdx);
				}
			}

			return configuredLocator(locatorName, locatorArgument);
		} catch (Error ex) {
			throw ex;
		}
    }

	/**
	 * Resolves a potentially configured locator.
	 * 
	 * @param name
	 *        The locators name in the configuration.
	 * @param spec
	 *        An optional configuration argument. If <code>null</code> or empty, a no-arg locator
	 *        can be used.
	 * @return The configured locator.
	 */
	public AttributeValueLocator createConfiguredLocator(String name, String spec) {
		if (StringServices.isEmpty(spec)) {
			AttributeValueLocator locator = singletonLocator(name);
			if (locator != null) {
				return locator;
			}

			spec = "";
		}

		return configuredLocator(name, spec);
	}

	private AttributeValueLocator singletonLocator(String locatorName) {
		Class<? extends AttributeValueLocator> locatorClass = locatorClass(locatorName);
		try {
			return ConfigUtil.getInstance(locatorClass);
		} catch (ConfigurationException ex) {
			// Fall through, try to invoke legacy constructor with sub-configuration.
			return null;
		}
	}

	private AttributeValueLocator configuredLocator(String name, String spec) {
		try {
			Class<? extends AttributeValueLocator> theLocatorClass = locatorClass(name);
			Constructor<? extends AttributeValueLocator> theConstr;
			try {
				theConstr = theLocatorClass.getConstructor(new Class[] { String.class });
			} catch (NoSuchMethodException ex) {
				throw new ConfigurationError(
					"A locator with subconfiguration must provide a public constructor with String argument: "
						+ theLocatorClass.getName(), ex);
			} catch (SecurityException ex) {
				throw new ConfigurationError("Cannot access locator class: " + theLocatorClass.getName(), ex);
			}
			return theConstr.newInstance(new Object[] { spec });
		} catch (InstantiationException ex) {
			throw new ConfigurationError("Locator instantiation failed.", ex);
		} catch (IllegalAccessException ex) {
			throw new ConfigurationError("Locator class not accessible.", ex);
		} catch (InvocationTargetException ex) {
			throw new ConfigurationError("Locator instantiation failed.", ex);
		}
	}

	private Class<? extends AttributeValueLocator> locatorClass(String locatorName) {
		Class<? extends AttributeValueLocator> result = _classByName.get(locatorName);
		if (result == null) {
			throw new ConfigurationError("Locator '" + locatorName + "' is not registered.");
		}
		return result;
	}

	/**
	 * The expression source passed to {@link #getExpressionLocator(String)} that created the given
	 * locator.
	 */
    public static String getLocatorExpression(AttributeValueLocator locator) {
    	return getInstance().internalGetLocatorName(locator);
    }

    protected String internalGetLocatorName(AttributeValueLocator locator) {
		String result = exprByInstance.get(locator);
    	if (result == null) {
    		throw new IllegalArgumentException("Locator not from this factory: " + locator);
    	}
		return result;
    }
    
	/**
	 * Creates a generic expression-based {@link AttributeValueLocator}.
	 * 
	 * @see ExpressionParser
	 */
	public static AttributeValueLocator getExpressionLocator(String expressionSource) {
		return getInstance().lookupExpressionLocator(expressionSource);
	}

	/**
	 * Creates a generic expression-based {@link AttributeValueLocator}.
	 * 
	 * @see ExpressionParser
	 */
	public AttributeValueLocator lookupExpressionLocator(String expressionSource) {
		AttributeValueLocator result = instanceByExpr.get(expressionSource);
		if (result == null) {
			result = createExpressionLocator(expressionSource);
			result = MapUtil.putIfAbsent(instanceByExpr, expressionSource, result);
			exprByInstance.putIfAbsent(result, expressionSource);
		}
		return result;
	}

	private static AttributeValueLocator createExpressionLocator(String expressionSource) {
		try {
			ExpressionParser parser = new ExpressionParser(new StringReader(expressionSource));
			OperationFactory operations = new OperationFactory();
			parser.setOperationFactory(operations);
			PolymorphicConfiguration<? extends AttributeValueLocator> config = parser.expr();
			return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		} catch (TokenMgrError ex) {
			throw new ConfigurationError("Invalid expression syntax: " + expressionSource, ex);
		} catch (ParseException ex) {
			throw new ConfigurationError("Invalid expression syntax: " + expressionSource, ex);
		}
	}

	private static AttributeValueLocatorFactory getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * {@link BasicRuntimeModule} for {@link AttributeValueLocatorFactory}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Module extends TypedRuntimeModule<AttributeValueLocatorFactory> {

		/**
		 * Singleton {@link AttributeValueLocatorFactory.Module} instance.
		 */
		public static final Module INSTANCE = new AttributeValueLocatorFactory.Module();
		
		@Override
		public Class<AttributeValueLocatorFactory> getImplementation() {
			return AttributeValueLocatorFactory.class;
		}
	}

}