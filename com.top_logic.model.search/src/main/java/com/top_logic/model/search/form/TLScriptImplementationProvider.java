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
 * implemented by a {@link ScriptImplementation}. A property of type {@link ScriptFunction0} to
 * {@link ScriptFunction4} is implemented by the corresponding {@link ScriptFunction1Impl
 * ScriptFunctionNImpl}, which is also configured in element form with its
 * {@link WithExpression#EXPRESSION expression} attribute. Both remember the
 * {@link WithImplementedType#getImplementedType() implemented type} derived from the generic type
 * of the property, e.g. <code>Mapping&lt;Object, String&gt;</code>, to convert script results to
 * the Java result type of the implemented method.
 * </p>
 *
 * <p>
 * The script text is a TL-Script expression, which is executed with the arguments of the method
 * call. A script configuration is written back as its expression only.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLScriptImplementationProvider implements ScriptImplementationProvider {

	/**
	 * The script configurations of the {@link ScriptFunction0} to {@link ScriptFunction4}
	 * properties.
	 */
	private static final List<ScriptConfigType> SCRIPT_FUNCTIONS = List.of(
		new ScriptConfigType(ScriptFunction0Impl.Config.class, ScriptFunction0Impl.class),
		new ScriptConfigType(ScriptFunction1Impl.Config.class, ScriptFunction1Impl.class),
		new ScriptConfigType(ScriptFunction2Impl.Config.class, ScriptFunction2Impl.class),
		new ScriptConfigType(ScriptFunction3Impl.Config.class, ScriptFunction3Impl.class),
		new ScriptConfigType(ScriptFunction4Impl.Config.class, ScriptFunction4Impl.class));

	/**
	 * The script configuration of plain {@link PolymorphicConfiguration} properties.
	 */
	private static final ScriptConfigType GENERIC =
		new ScriptConfigType(ScriptImplementation.Config.class, ScriptImplementation.class);

	/**
	 * Configuration interface of a script implementation together with the implementation class it
	 * is written for.
	 */
	private record ScriptConfigType(Class<? extends WithExpression> configInterface, Class<?> implementationClass) {

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
		return scriptConfigType(property) != null;
	}

	/**
	 * The script configuration created for the given property.
	 *
	 * @return The script configuration type whose configurations are instances of the property
	 *         type, or <code>null</code>, if there is none.
	 */
	private static ScriptConfigType scriptConfigType(PropertyDescriptor property) {
		Class<?> type = property.getType();
		if (type == PolymorphicConfiguration.class) {
			return GENERIC;
		}
		return scriptFunction(type);
	}

	private static ScriptConfigType scriptFunction(Class<?> propertyType) {
		for (ScriptConfigType scriptFunction : SCRIPT_FUNCTIONS) {
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
		WithExpression result = newScriptConfig(property);
		result.setExpression(expression);
		return (PolymorphicConfiguration<?>) result;
	}

	@Override
	public boolean isScript(PolymorphicConfiguration<?> config) {
		if (GENERIC.isInstance(config)) {
			return true;
		}
		for (ScriptConfigType scriptFunction : SCRIPT_FUNCTIONS) {
			if (scriptFunction.isInstance(config)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getSpecification(PolymorphicConfiguration<?> config) {
		return ExprFormat.INSTANCE.getSpecification(((WithExpression) config).getExpression());
	}

	@Override
	public ConfigBuilder createScriptBuilder(PropertyDescriptor property) throws ConfigurationException {
		ScriptConfigType scriptFunction = scriptFunction(property.getType());
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
	 * Creates an empty script configuration for the given property.
	 *
	 * @param property
	 *        A {@link #supports(PropertyDescriptor) supported} property.
	 * @return A script configuration with {@link WithImplementedType#getImplementedType()} set for
	 *         the given property, whose expression is still to be set.
	 */
	public static WithExpression newScriptConfig(PropertyDescriptor property) {
		ConfigurationItem result = TypedConfiguration.newConfigItem(scriptConfigType(property).configInterface());
		((WithImplementedType) result).setImplementedType(implementedType(property));
		return (WithExpression) result;
	}

}
