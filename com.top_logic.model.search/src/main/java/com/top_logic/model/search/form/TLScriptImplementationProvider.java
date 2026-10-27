/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.form;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.MethodProperty;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.ScriptImplementationProvider;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.JavaTypeUtil;
import com.top_logic.layout.ScriptFunction0;
import com.top_logic.layout.ScriptFunction4;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link ScriptImplementationProvider} implementing configured interfaces with TL-Script.
 *
 * <p>
 * A property of type {@link PolymorphicConfiguration PolymorphicConfiguration&lt;F&gt;} is
 * implemented by a {@link ScriptImplementation} with one TL-Script expression per method of
 * <code>F</code>. Its configuration has the descriptor of the {@link ScriptImplementationType} of
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
 * If <code>F</code> is a functional interface, the expression of its single abstract method is also
 * given as text: <code>filter="x -&gt; ..."</code> or <code>&lt;filter&gt;x -&gt; ...&lt;/filter&gt;</code>.
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
		Expr expression = ExprFormat.INSTANCE.getValue(property.getPropertyName(), text);
		if (isGeneric(property)) {
			ScriptImplementationType type = ScriptImplementationType.of(implementedType(property));
			MethodProperty method = type.getFunctionalMethod();
			if (method == null) {
				// The text form is only offered for functional interfaces.
				throw new IllegalStateException(
					"No functional interface: " + type.getImplementedInterface().getName());
			}
			ScriptImplementation.Config<?> result = type.newConfig();
			result.update(type.getProperty(method), expression);
			return result;
		}
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
			expression = (Expr) config.value(type.getProperty(type.getFunctionalMethod()));
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

	@Override
	public PolymorphicConfiguration<?> createScriptConfig(PropertyDescriptor property) throws ConfigurationException {
		return newScriptConfig(property);
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
