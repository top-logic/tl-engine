/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.script;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.script.MethodProperty;
import com.top_logic.basic.config.script.ScriptImplementationProvider;
import com.top_logic.basic.reflect.JavaTypeUtil;
import com.top_logic.layout.ScriptFunction0;
import com.top_logic.layout.ScriptFunction4;
import com.top_logic.layout.form.values.edit.ScriptImplementationLabels;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.form.ScriptFunction0Impl;
import com.top_logic.model.search.form.ScriptFunction1Impl;
import com.top_logic.model.search.form.ScriptFunction2Impl;
import com.top_logic.model.search.form.ScriptFunction3Impl;
import com.top_logic.model.search.form.ScriptFunction4Impl;
import com.top_logic.model.search.form.WithExpression;

/**
 * {@link ScriptImplementationProvider} implementing configured interfaces with TL-Script.
 *
 * <p>
 * A property of type {@link PolymorphicConfiguration PolymorphicConfiguration&lt;F&gt;} is
 * implemented by a {@link ScriptImplementation} with one TL-Script expression per method of
 * <code>F</code>, or a value for a method without parameters returning a plain value (see
 * {@link ScriptImplementationType}). Its configuration has the descriptor of the {@link ScriptImplementationType} of
 * <code>F</code> (with the type arguments of the property), and is written as element without
 * implementation class, one property per method:
 * </p>
 *
 * <pre>
 * &lt;builder&gt;
 *     &lt;get-model&gt;...&lt;/get-model&gt;
 *     &lt;supports-model&gt;...&lt;/supports-model&gt;
 * &lt;/builder&gt;
 * </pre>
 *
 * <p>
 * Every abstract method of <code>F</code> must be given, a default method may be given to override
 * its Java implementation. An interface whose abstract methods are overloaded or generic, or whose
 * methods have clashing property names, cannot be implemented by TL-Script. Overloaded and generic
 * default methods are not offered and keep their Java implementation, see
 * {@link ScriptImplementationType}.
 * </p>
 *
 * <p>
 * If <code>F</code> is a functional interface, the expression (or value) of its single abstract
 * method is also given as text: <code>filter="x -&gt; ..."</code> or
 * <code>&lt;filter&gt;x -&gt; ...&lt;/filter&gt;</code>.
 * Such a configuration is written back in this text form, as long as it implements no default
 * method.
 * </p>
 *
 * <p>
 * A property of type {@link ScriptFunction0} to {@link ScriptFunction4} is implemented by the
 * corresponding {@link ScriptFunction1Impl ScriptFunctionNImpl}, which is configured by its text
 * form or in element form with its {@link WithExpression#EXPRESSION expression} attribute.
 * </p>
 *
 * <p>
 * All script configurations remember the {@link WithImplementedType#getImplementedType()
 * implemented type} derived from the generic type of the property, e.g.
 * <code>Mapping&lt;Object, String&gt;</code>, to convert script results to the Java result types of
 * the implemented methods.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLScriptImplementationProvider implements ScriptImplementationProvider {

	/**
	 * The script configurations of the {@link ScriptFunction0} to {@link ScriptFunction4}
	 * properties.
	 */
	private static final List<ScriptFunctionType> SCRIPT_FUNCTIONS = List.of(
		new ScriptFunctionType(ScriptFunction0Impl.Config.class, ScriptFunction0Impl.class),
		new ScriptFunctionType(ScriptFunction1Impl.Config.class, ScriptFunction1Impl.class),
		new ScriptFunctionType(ScriptFunction2Impl.Config.class, ScriptFunction2Impl.class),
		new ScriptFunctionType(ScriptFunction3Impl.Config.class, ScriptFunction3Impl.class),
		new ScriptFunctionType(ScriptFunction4Impl.Config.class, ScriptFunction4Impl.class));

	/**
	 * Configuration interface of a script function together with the implementation class it is
	 * written for.
	 */
	private record ScriptFunctionType(Class<? extends WithExpression> configInterface, Class<?> implementationClass) {

		/**
		 * Whether the given configuration is exactly of this type, so that it is fully described
		 * by its expression.
		 */
		boolean isInstance(PolymorphicConfiguration<?> config) {
			return config.descriptor().getConfigurationInterface() == configInterface
				&& config.getImplementationClass() == implementationClass;
		}

	}

	@Override
	public boolean supports(PropertyDescriptor property) {
		return isGeneric(property) || scriptFunction(property.getType()) != null;
	}

	/**
	 * Whether the given property is implemented by a {@link ScriptImplementation}.
	 */
	private static boolean isGeneric(PropertyDescriptor property) {
		return property.getType() == PolymorphicConfiguration.class;
	}

	private static ScriptFunctionType scriptFunction(Class<?> propertyType) {
		for (ScriptFunctionType scriptFunction : SCRIPT_FUNCTIONS) {
			if (propertyType.isAssignableFrom(scriptFunction.configInterface())) {
				return scriptFunction;
			}
		}
		return null;
	}

	@Override
	public PolymorphicConfiguration<?> parse(PropertyDescriptor property, CharSequence text)
			throws ConfigurationException {
		if (isGeneric(property)) {
			ScriptImplementationType type = ScriptImplementationType.of(implementedType(property));
			MethodProperty method = type.getFunctionalMethod();
			if (method == null) {
				// The text form is only offered for functional interfaces.
				throw new IllegalStateException(
					"No functional interface: " + type.getImplementedInterface().getName());
			}
			PropertyDescriptor methodProperty = type.getProperty(method);
			Object value;
			if (type.isValue(method)) {
				value = methodProperty.getValueProvider().getValue(property.getPropertyName(), text);
			} else {
				value = ExprFormat.INSTANCE.getValue(property.getPropertyName(), text);
			}
			ScriptImplementation.Config<?> result = type.newConfig();
			result.update(methodProperty, value);
			return result;
		}
		Expr expression = ExprFormat.INSTANCE.getValue(property.getPropertyName(), text);
		WithExpression result = newScriptFunctionConfig(property);
		result.setExpression(expression);
		return (PolymorphicConfiguration<?>) result;
	}

	@Override
	public boolean isScript(PolymorphicConfiguration<?> config) {
		ScriptImplementationType type = ScriptImplementationType.forDescriptor(config.descriptor());
		if (type != null) {
			return config.getImplementationClass() == ScriptImplementation.class
				&& !config.valueSet(config.descriptor().getProperty(WithImplementedType.IMPLEMENTED_TYPE))
				&& type.isFunctional(config);
		}
		for (ScriptFunctionType scriptFunction : SCRIPT_FUNCTIONS) {
			if (scriptFunction.isInstance(config)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getSpecification(PolymorphicConfiguration<?> config) {
		ScriptImplementationType type = ScriptImplementationType.forDescriptor(config.descriptor());
		Expr expression;
		if (type != null) {
			MethodProperty method = type.getFunctionalMethod();
			PropertyDescriptor methodProperty = type.getProperty(method);
			Object value = config.value(methodProperty);
			if (type.isValue(method)) {
				@SuppressWarnings("unchecked")
				ConfigurationValueProvider<Object> format =
					(ConfigurationValueProvider<Object>) methodProperty.getValueProvider();
				return format.getSpecification(value);
			}
			expression = (Expr) value;
		} else {
			expression = ((WithExpression) config).getExpression();
		}
		return ExprFormat.INSTANCE.getSpecification(expression);
	}

	@Override
	public ConfigBuilder createScriptBuilder(PropertyDescriptor property) throws ConfigurationException {
		if (isGeneric(property)) {
			return TypedConfiguration.createConfigBuilder(
				ScriptImplementationType.of(implementedType(property)).getDescriptor());
		}
		ScriptFunctionType scriptFunction = scriptFunction(property.getType());
		if (scriptFunction == null) {
			return null;
		}
		ConfigBuilder result = TypedConfiguration.createConfigBuilder(scriptFunction.configInterface());
		result.update(result.descriptor().getProperty(WithImplementedType.IMPLEMENTED_TYPE),
			implementedType(property));
		return result;
	}

	/**
	 * The interface implemented by a script configured for the given property.
	 *
	 * @param property
	 *        A {@link #supports(PropertyDescriptor) supported} property.
	 * @return The type argument of {@link PolymorphicConfiguration} in the generic type of the
	 *         property, without wildcard and type variables, see
	 *         {@link WithImplementedType#getImplementedType()}.
	 */
	public static Type implementedType(PropertyDescriptor property) {
		Type type = JavaTypeUtil.resolve(property.getGenericType(),
			PolymorphicConfiguration.class.getTypeParameters()[0]);
		while (true) {
			if (type instanceof WildcardType wildcard) {
				// A property type with a wildcard: PolymorphicConfiguration<? extends F>.
				type = wildcard.getUpperBounds()[0];
			} else if (type instanceof TypeVariable<?> variable) {
				// A type variable, or the capture of a wildcard.
				type = variable.getBounds()[0];
			} else {
				break;
			}
		}
		type = JavaTypeUtil.eraseTypeVariables(type);
		if (!JavaTypeUtil.getRawClass(type).isInterface()) {
			return property.getInstanceType();
		}
		return type;
	}

	/**
	 * Creates the script configuration with a {@link #skeleton(Method) skeleton} for each method
	 * that must be implemented by a script: the expression of a script function, and the abstract
	 * methods of a {@link ScriptImplementationType} that are not implemented by a value. A default
	 * method keeps its Java implementation, until a script or value is entered for it.
	 */
	@Override
	public PolymorphicConfiguration<?> createScriptConfig(PropertyDescriptor property) throws ConfigurationException {
		PolymorphicConfiguration<?> result = newScriptConfig(property);
		ScriptImplementationType type = ScriptImplementationType.forDescriptor(result.descriptor());
		if (type != null) {
			for (MethodProperty method : type.getMethods()) {
				if (method.isAbstract() && !type.isValue(method)) {
					result.update(type.getProperty(method), skeleton(method.getMethod()));
				}
			}
		} else {
			PropertyDescriptor expression = result.descriptor().getProperty(WithExpression.EXPRESSION);
			Method method = getImplementedMethod(result, expression);
			if (method != null) {
				((WithExpression) result).setExpression(skeleton(method));
			}
		}
		return result;
	}

	/**
	 * The expression to start the implementation of the given method with: a function taking the
	 * arguments of the method and returning <code>null</code>, e.g.
	 * <code>context -&gt; attribute -&gt; null</code> for a method with the parameters
	 * <code>context</code> and <code>attribute</code>, and <code>null</code> for a method without
	 * arguments.
	 *
	 * <p>
	 * An argument is named like its parameter, see
	 * {@link ScriptImplementationLabels#argumentName(Method, int)}. A parameter name that is no
	 * valid TL-Script variable name is replaced with the generic name
	 * {@link ScriptImplementationLabels#ARGUMENT_PREFIX argN}.
	 * </p>
	 *
	 * @param method
	 *        The method to implement.
	 * @return The skeleton expression.
	 * @throws ConfigurationException
	 *         If the skeleton cannot be parsed, which does not happen.
	 */
	public static Expr skeleton(Method method) throws ConfigurationException {
		StringBuilder text = new StringBuilder();
		for (int n = 0, cnt = method.getParameterCount(); n < cnt; n++) {
			text.append(variableName(method, n)).append(" -> ");
		}
		text.append("null");
		return ExprFormat.INSTANCE.getValue(method.getName(), text);
	}

	private static String variableName(Method method, int index) {
		String name = ScriptImplementationLabels.argumentName(method, index);
		try {
			ExprFormat.INSTANCE.getValue(method.getName(), name + " -> null");
			return name;
		} catch (ConfigurationException | RuntimeException ex) {
			// Not a valid variable name in TL-Script, e.g. a keyword.
			return ScriptImplementationLabels.ARGUMENT_PREFIX + (index + 1);
		}
	}

	@Override
	public Method getImplementedMethod(ConfigurationItem script, PropertyDescriptor property) {
		ScriptImplementationType type = ScriptImplementationType.forDescriptor(script.descriptor());
		if (type != null) {
			for (MethodProperty method : type.getMethods()) {
				if (method.getPropertyName().equals(property.getPropertyName())) {
					return method.getMethod();
				}
			}
			return null;
		}
		if (script instanceof WithExpression && script instanceof WithImplementedType function
			&& WithExpression.EXPRESSION.equals(property.getPropertyName())) {
			Type implementedType = function.getImplementedType();
			if (implementedType == null) {
				return null;
			}
			List<Method> methods = JavaTypeUtil.getAbstractMethods(JavaTypeUtil.getRawClass(implementedType));
			return methods.size() == 1 ? methods.get(0) : null;
		}
		return null;
	}

	/**
	 * Creates an empty script configuration for the given property.
	 *
	 * @param property
	 *        A {@link #supports(PropertyDescriptor) supported} property.
	 * @return A script configuration for the given property without expressions: a configuration
	 *         of the {@link ScriptImplementationType} of the implemented interface for a
	 *         {@link PolymorphicConfiguration} property, or a {@link WithExpression} configuration
	 *         of a script function.
	 * @throws ConfigurationException
	 *         If the interface of the given property cannot be implemented by TL-Script.
	 */
	public static PolymorphicConfiguration<?> newScriptConfig(PropertyDescriptor property)
			throws ConfigurationException {
		if (isGeneric(property)) {
			return ScriptImplementationType.of(implementedType(property)).newConfig();
		}
		return (PolymorphicConfiguration<?>) newScriptFunctionConfig(property);
	}

	private static WithExpression newScriptFunctionConfig(PropertyDescriptor property) {
		ConfigurationItem result =
			TypedConfiguration.newConfigItem(scriptFunction(property.getType()).configInterface());
		((WithImplementedType) result).setImplementedType(implementedType(property));
		return (WithExpression) result;
	}

}
