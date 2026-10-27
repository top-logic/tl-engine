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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.MethodProperty;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.reflect.JavaTypeUtil;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.ConversionSite;
import com.top_logic.model.search.expr.config.operations.ScriptConversions;
import com.top_logic.model.search.expr.config.operations.ValueConverter;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Implementation of a Java interface by TL-Script expressions, one per method.
 *
 * <p>
 * The configuration of a script implementation of an interface <code>F</code> has the descriptor
 * of the {@link ScriptImplementationType} of <code>F</code>: one property per method of
 * <code>F</code>, an {@link Expr expression} or, for a method without parameters returning a plain
 * value, a value. The instance created from such a configuration is a dynamic proxy implementing
 * <code>F</code> and {@link ConfiguredInstance}. A call to a method of <code>F</code> executes the
 * expression of the method with the call arguments (the elements of a variable argument array are
 * passed as separate arguments). The script result is converted to the return type of the method,
 * resolved in the context of the {@link Config#getImplementedType() implemented type}, see
 * {@link #resultConverter(Type, Method)}. A method implemented by a value returns the configured
 * value. A default method of <code>F</code> without expression or value executes its Java
 * implementation.
 * </p>
 *
 * <p>
 * The arguments are passed to the script unfiltered, they are values the calling application code
 * provides. Like for any {@link QueryExecutor}, the script result is filtered for objects the
 * current user must not discover.
 * </p>
 *
 * <p>
 * This class is the option that a configuration editor offers for implementing a property in
 * TL-Script, see {@link com.top_logic.layout.form.values.ScriptImplOptionMapping}. Its label is a
 * resource of the module's resource bundle <code>modelSearchMessages</code>, since the class is
 * instantiated through a factory method, not a configuration constructor.
 * </p>
 *
 * @see TLScriptImplementationProvider
 * @see ScriptImplementationType
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ScriptImplementation {

	/**
	 * Base configuration of a {@link ScriptImplementation}.
	 *
	 * <p>
	 * A script configuration has the descriptor of a {@link ScriptImplementationType}, which
	 * extends this interface with the method properties of the implemented interface.
	 * </p>
	 *
	 * @param <T>
	 *        The implemented interface.
	 */
	public interface Config<T> extends PolymorphicConfiguration<T>, WithImplementedType {

		@Override
		@ClassDefault(ScriptImplementation.class)
		Class<? extends T> getImplementationClass();

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
	 *        The configuration of the script, a configuration of a {@link ScriptImplementationType}.
	 * @return A dynamic proxy implementing the configured interface.
	 */
	@CalledByReflection
	public static Object newInstance(InstantiationContext context, Config<?> config) {
		ScriptImplementationType type = ScriptImplementationType.forDescriptor(config.descriptor());
		if (type == null) {
			throw new IllegalArgumentException("Not the configuration of a script implementation: " + config);
		}
		Type implementedType = config.getImplementedType();
		if (implementedType == null) {
			implementedType = type.getImplementedType();
		}
		Map<List<Object>, MethodImplementation> implementations = new LinkedHashMap<>();
		for (MethodProperty method : type.getMethods()) {
			PropertyDescriptor property = type.getProperty(method);
			if (type.isValue(method)) {
				if (!config.valueSet(property)) {
					continue;
				}
				Object value = config.value(property);
				implementations.put(method.getSignature(),
					new MethodImplementation(method, specification(property, value), null, null, value));
				continue;
			}
			Expr expression = (Expr) config.value(property);
			if (expression == null) {
				continue;
			}
			implementations.put(method.getSignature(),
				new MethodImplementation(method, ExprFormat.INSTANCE.getSpecification(expression),
					QueryExecutor.compile(expression), resultConverter(implementedType, method.getMethod()), null));
		}
		Class<?> implementedInterface = type.getImplementedInterface();
		Handler handler = new Handler(config, implementedInterface, implementations);
		return Proxy.newProxyInstance(classLoader(implementedInterface), new Class<?>[] {
			implementedInterface, ConfiguredInstance.class }, handler);
	}

	/**
	 * The text form of the value of the given property.
	 */
	private static String specification(PropertyDescriptor property, Object value) {
		@SuppressWarnings("unchecked")
		ConfigurationValueProvider<Object> format = (ConfigurationValueProvider<Object>) property.getValueProvider();
		return format == null ? String.valueOf(value) : format.getSpecification(value);
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
	 * The script or value implementing a method.
	 *
	 * @param method
	 *        The implemented method.
	 * @param specification
	 *        The text form of the script or value.
	 * @param query
	 *        The compiled script, <code>null</code> for a method implemented by a value.
	 * @param resultConverter
	 *        The conversion of the script result.
	 * @param value
	 *        The value returned by a method implemented by a value.
	 */
	private record MethodImplementation(MethodProperty method, String specification, QueryExecutor query,
			ValueConverter resultConverter, Object value) {

		Object invoke(Method called, Object[] args) {
			if (query == null) {
				return value;
			}
			Object result = query.execute(scriptArguments(called, args));
			if (called.getReturnType() == void.class) {
				return null;
			}
			return convertResult(resultConverter, result);
		}

	}

	/**
	 * {@link InvocationHandler} of a script implementation.
	 */
	private static final class Handler implements InvocationHandler {

		/**
		 * Marker for a method without script in {@link #_byMethod}.
		 */
		private static final MethodImplementation NONE = new MethodImplementation(null, null, null, null, null);

		private final Config<?> _config;

		private final Class<?> _implementedInterface;

		private final Map<List<Object>, MethodImplementation> _bySignature;

		private final Map<Method, MethodImplementation> _byMethod = new ConcurrentHashMap<>();

		Handler(Config<?> config, Class<?> implementedInterface,
				Map<List<Object>, MethodImplementation> bySignature) {
			_config = config;
			_implementedInterface = implementedInterface;
			_bySignature = bySignature;
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
			MethodImplementation implementation = _byMethod.computeIfAbsent(method, this::lookup);
			if (implementation != NONE) {
				return implementation.invoke(method, args);
			}
			if (method.isDefault()) {
				return InvocationHandler.invokeDefault(proxy, method, args);
			}
			throw new UnsupportedOperationException("No script implements: " + method);
		}

		private MethodImplementation lookup(Method method) {
			MethodImplementation result =
				_bySignature.get(JavaTypeUtil.getResolvedSignature(_implementedInterface, method));
			return result == null ? NONE : result;
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
			StringBuilder result = new StringBuilder();
			result.append("TL-Script ");
			result.append(_implementedInterface.getName());
			result.append("(");
			boolean first = true;
			for (MethodImplementation implementation : _bySignature.values()) {
				if (!first) {
					result.append(", ");
				}
				first = false;
				if (_bySignature.size() > 1) {
					result.append(implementation.method().getPropertyName());
					result.append(": ");
				}
				result.append(implementation.specification());
			}
			result.append(")");
			return result.toString();
		}
	}

}
