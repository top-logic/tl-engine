/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.form;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.reflect.JavaTypeUtil;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.ConversionSite;
import com.top_logic.model.search.expr.config.operations.ScriptConversions;
import com.top_logic.model.search.expr.config.operations.ValueConverter;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Implementation of a Java functional interface by a TL-Script expression.
 *
 * <p>
 * The instance created from a {@link Config} is a dynamic proxy implementing the
 * {@link Config#getImplementedType() implemented interface} and {@link ConfiguredInstance}. A call
 * to the abstract method of the interface executes the {@link Config#getExpression() expression}
 * with the call arguments (the elements of a variable argument array are passed as separate
 * arguments). The script result is converted to the return type of the method, resolved in the
 * context of the implemented type, see {@link #resultConverter(Type, Method)}. Default methods of
 * the interface execute their Java implementation.
 * </p>
 *
 * <p>
 * The arguments are passed to the script unfiltered, they are values the calling application code
 * provides. Like for any {@link QueryExecutor}, the script result is filtered for objects the
 * current user must not discover.
 * </p>
 *
 * @see TLScriptImplementationProvider
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("TL-Script implementation")
public final class ScriptImplementation {

	/**
	 * Configuration of a {@link ScriptImplementation}.
	 *
	 * @param <T>
	 *        The implemented interface.
	 */
	public interface Config<T> extends PolymorphicConfiguration<T>, WithExpression, WithImplementedType {

		@Override
		@ClassDefault(ScriptImplementation.class)
		Class<? extends T> getImplementationClass();

		/**
		 * The functional interface implemented by the {@link #getExpression() expression}.
		 */
		@Override
		@Mandatory
		Type getImplementedType();

	}

	private ScriptImplementation() {
		// Factory only.
	}

	/**
	 * Creates the implementation of the {@link Config#getImplementedType() implemented
	 * interface}.
	 *
	 * @param context
	 *        The context of the instantiation.
	 * @param config
	 *        The configuration of the script.
	 * @return A dynamic proxy implementing the configured interface.
	 */
	@CalledByReflection
	public static Object newInstance(InstantiationContext context, Config<?> config) {
		Type implementedType = config.getImplementedType();
		Class<?> implementedInterface = JavaTypeUtil.getRawClass(implementedType);
		if (!implementedInterface.isInterface()) {
			throw new IllegalArgumentException(
				"Script implementation requires an interface, got: " + implementedType.getTypeName());
		}
		List<Method> methods = JavaTypeUtil.getAbstractMethods(implementedInterface);
		if (methods.size() != 1) {
			throw new IllegalArgumentException("Script implementation requires a functional interface, '"
				+ implementedInterface.getName() + "' has abstract methods: " + methods);
		}
		Method method = methods.get(0);
		Handler handler = new Handler(config, implementedInterface, QueryExecutor.compile(config.getExpression()),
			resultConverter(implementedType, method));
		return Proxy.newProxyInstance(classLoader(implementedInterface), new Class<?>[] {
			implementedInterface, ConfiguredInstance.class }, handler);
	}

	/**
	 * The class loader that defines the proxy class, it must see the implemented interface and
	 * {@link ConfiguredInstance}.
	 */
	private static ClassLoader classLoader(Class<?> implementedInterface) {
		ClassLoader loader = implementedInterface.getClassLoader();
		if (loader != null) {
			try {
				if (Class.forName(ConfiguredInstance.class.getName(), false, loader) == ConfiguredInstance.class) {
					return loader;
				}
			} catch (ClassNotFoundException ex) {
				// Not visible, use own class loader.
			}
		}
		return ScriptImplementation.class.getClassLoader();
	}

	/**
	 * The conversion of the script result to the return type of a method implemented by a
	 * script.
	 *
	 * @param implementedType
	 *        The implemented interface, possibly parameterized. May be <code>null</code>, then the
	 *        return type of the method is resolved to the bounds of its type variables.
	 * @param method
	 *        The method of the implemented interface (or one of its super interfaces) whose result
	 *        is converted. A {@link com.top_logic.model.search.expr.config.operations.ScriptConversion}
	 *        annotation of the method takes precedence over the conversion for its return type.
	 * @return The converter of the script result, or <code>null</code>, if the result needs no
	 *         conversion or the method returns nothing.
	 *
	 * @see ScriptConversions#fromScript(Type, java.lang.reflect.AnnotatedElement, ConversionSite)
	 */
	public static ValueConverter resultConverter(Type implementedType, Method method) {
		Class<?> returnType = method.getReturnType();
		if (returnType == void.class) {
			return null;
		}
		Type resultType = method.getGenericReturnType();
		Class<?> implementedInterface = method.getDeclaringClass();
		if (implementedType != null) {
			resultType = JavaTypeUtil.resolve(implementedType, resultType);
			implementedInterface = JavaTypeUtil.getRawClass(implementedType);
		}
		return ScriptConversions.fromScript(resultType, method,
			new ConversionSite.MethodResult(implementedInterface, method));
	}

	/**
	 * Converts a script result with a converter of {@link #resultConverter(Type, Method)}.
	 *
	 * @param converter
	 *        The converter, may be <code>null</code>.
	 * @param result
	 *        The script result.
	 * @return The converted value.
	 */
	public static Object convertResult(ValueConverter converter, Object result) {
		return converter == null ? result : converter.fromScript(result);
	}

	/**
	 * Converts the arguments of a Java call to the arguments passed to the script.
	 *
	 * @param method
	 *        The called method.
	 * @param args
	 *        The arguments of the call, <code>null</code> for no arguments.
	 * @return The script arguments.
	 */
	static Object[] scriptArguments(Method method, Object[] args) {
		if (args == null) {
			return new Object[0];
		}
		Object[] arguments = args;
		if (method.isVarArgs() && args.length > 0 && args[args.length - 1] instanceof Object[] varArgs) {
			arguments = new Object[args.length - 1 + varArgs.length];
			System.arraycopy(args, 0, arguments, 0, args.length - 1);
			System.arraycopy(varArgs, 0, arguments, args.length - 1, varArgs.length);
		}
		Object[] result = new Object[arguments.length];
		for (int n = 0; n < arguments.length; n++) {
			result[n] = ScriptConversions.toScript(arguments[n], false);
		}
		return result;
	}

	/**
	 * {@link InvocationHandler} of a script implementation.
	 */
	private static final class Handler implements InvocationHandler {

		private final Config<?> _config;

		private final Class<?> _implementedInterface;

		private final QueryExecutor _query;

		private final ValueConverter _resultConverter;

		Handler(Config<?> config, Class<?> implementedInterface, QueryExecutor query,
				ValueConverter resultConverter) {
			_config = config;
			_implementedInterface = implementedInterface;
			_query = query;
			_resultConverter = resultConverter;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Class<?> declaringClass = method.getDeclaringClass();
			if (declaringClass == Object.class) {
				return invokeObjectMethod(proxy, method, args);
			}
			if (declaringClass == ConfiguredInstance.class) {
				return _config;
			}
			if (method.isDefault()) {
				return InvocationHandler.invokeDefault(proxy, method, args);
			}
			Object result = _query.execute(scriptArguments(method, args));
			if (method.getReturnType() == void.class) {
				return null;
			}
			return convertResult(_resultConverter, result);
		}

		private Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
			switch (method.getName()) {
				case "equals":
					return Boolean.valueOf(proxy == args[0]);
				case "hashCode":
					return Integer.valueOf(System.identityHashCode(proxy));
				case "toString":
					return toString();
				default:
					throw new UnsupportedOperationException("Unexpected method: " + method);
			}
		}

		@Override
		public String toString() {
			Expr expression = _config.getExpression();
			return "TL-Script " + _implementedInterface.getName() + "("
				+ ExprFormat.INSTANCE.getSpecification(expression) + ")";
		}
	}

}
