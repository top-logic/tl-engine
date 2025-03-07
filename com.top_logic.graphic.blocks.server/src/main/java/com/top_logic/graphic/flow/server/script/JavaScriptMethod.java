/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.script;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptorBuilder;

/**
 * Reflection-based {@link GenericMethod} calling static Java utilities.
 */
public class JavaScriptMethod extends GenericMethod {

	private final Method _java;

	private final Converter[] _conversions;

	/**
	 * Creates a {@link JavaScriptMethod}.
	 */
	protected JavaScriptMethod(String name, Method java, Converter[] conversions,
			SearchExpression[] arguments) {
		super(name, arguments);
		_java = java;
		_conversions = conversions;
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new JavaScriptMethod(getName(), _java, _conversions, arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		for (Converter conversion : _conversions) {
			conversion.convert(arguments);
		}
		try {
			return _java.invoke(null, arguments);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Builder for {@link JavaScriptMethod}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<JavaScriptMethod> {

		private final Method _method;

		private final ArgumentDescriptor _descriptor;

		private final Converter[] _conversions;

		/**
		 * Configuration options for {@link JavaScriptMethod.Builder}.
		 */
		@TagName("java")
		public interface Config extends AbstractSimpleMethodBuilder.Config<Builder> {
			/**
			 * Qualified name of the static method to call in the form
			 * <code>fully.qualified.ClassName#methodName</code>
			 */
			@Mandatory
			String getMethod();
		}

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config config) {
			super(context, config);

			String method = config.getMethod();
			int sepIndex = method.indexOf('#');
			String methodName = method.substring(sepIndex + 1);
			String className = method.substring(0, sepIndex);

			try {
				Class<?> implClass = Class.forName(className);
				_method = Arrays.stream(implClass.getDeclaredMethods())
					.filter(m -> m.getName().equals(methodName)).findAny().get();

				ArgumentDescriptorBuilder descriptor = ArgumentDescriptor.builder();
				Parameter[] parameters = _method.getParameters();
				List<Converter> conversions = new ArrayList<>();
				for (int n = 0; n < parameters.length; n++) {
					Parameter p = parameters[n];
					if (p.getAnnotation(Mandatory.class) != null) {
						descriptor.mandatory(p.getName());
					} else {
						Object defaultValue;
						Class<?> type = p.getType();
						if (type == double.class) {
							DoubleDefault defAnnotation = p.getAnnotation(DoubleDefault.class);
							defaultValue = Double.valueOf(defAnnotation == null ? 0 : defAnnotation.value());
							conversions.add(new Converter(n,
								input -> input instanceof Double ? input : ((Number) input).doubleValue()));
						} else if (type == Double.class) {
							DoubleDefault defAnnotation = p.getAnnotation(DoubleDefault.class);
							defaultValue = defAnnotation == null ? null : Double.valueOf(defAnnotation.value());
							conversions.add(new Converter(n,
								input -> input instanceof Double ? input
									: input == null ? null : Double.valueOf(((Number) input).doubleValue())));
						} else if (type == float.class) {
							FloatDefault defAnnotation = p.getAnnotation(FloatDefault.class);
							defaultValue = Float.valueOf(defAnnotation == null ? 0 : defAnnotation.value());
							conversions.add(new Converter(n,
								input -> input instanceof Float ? input : ((Number) input).floatValue()));
						} else if (type == Float.class) {
							FloatDefault defAnnotation = p.getAnnotation(FloatDefault.class);
							defaultValue = defAnnotation == null ? null : Float.valueOf(defAnnotation.value());
							conversions.add(new Converter(n,
								input -> input instanceof Float ? input
									: input == null ? null : Float.valueOf(((Number) input).floatValue())));
						} else if (type == int.class) {
							IntDefault defAnnotation = p.getAnnotation(IntDefault.class);
							defaultValue = Integer.valueOf(defAnnotation == null ? 0 : defAnnotation.value());
							conversions.add(new Converter(n,
								input -> input instanceof Integer ? input : ((Number) input).intValue()));
						} else if (type == Integer.class) {
							IntDefault defAnnotation = p.getAnnotation(IntDefault.class);
							defaultValue = defAnnotation == null ? null : Integer.valueOf(defAnnotation.value());
							conversions.add(new Converter(n,
								input -> input instanceof Integer ? input
									: input == null ? null : Integer.valueOf(((Number) input).intValue())));
						} else if (type == long.class) {
							LongDefault defAnnotation = p.getAnnotation(LongDefault.class);
							defaultValue = Long.valueOf(defAnnotation == null ? 0 : defAnnotation.value());
							conversions.add(new Converter(n,
								input -> input instanceof Long ? input : ((Number) input).longValue()));
						} else if (type == Long.class) {
							LongDefault defAnnotation = p.getAnnotation(LongDefault.class);
							defaultValue = defAnnotation == null ? null : Long.valueOf(defAnnotation.value());
							conversions.add(new Converter(n,
								input -> input instanceof Long ? input
									: input == null ? null : Long.valueOf(((Number) input).longValue())));
						} else if (type == boolean.class) {
							BooleanDefault defAnnotation = p.getAnnotation(BooleanDefault.class);
							defaultValue = Boolean.valueOf(defAnnotation == null ? false : defAnnotation.value());
							conversions.add(new Converter(n,
								input -> input instanceof Boolean ? input : SearchExpression.asBoolean(input)));
						} else if (type == Boolean.class) {
							BooleanDefault defAnnotation = p.getAnnotation(BooleanDefault.class);
							defaultValue = defAnnotation == null ? null : Boolean.valueOf(defAnnotation.value());
							conversions.add(new Converter(n,
								input -> input instanceof Boolean ? input
									: input == null ? null : SearchExpression.asBoolean(input)));
						} else if (type.isEnum()) {
							defaultValue = type.getEnumConstants()[0];
							Method valueOf = type.getMethod("valueOf", String.class);
							conversions.add(new Converter(n, input -> {
								if (input instanceof String) {
									try {
										return valueOf.invoke(null, input);
									} catch (InvocationTargetException | IllegalAccessException
											| IllegalArgumentException ex) {
										throw new RuntimeException(ex);
									}
								}
								return input;
							}));
						} else {
							defaultValue = null;
						}
						descriptor.optional(p.getName(), () -> SearchExpressionFactory.literal(defaultValue));
					}
				}
				_descriptor = descriptor.build();
				_conversions = conversions.toArray(new Converter[0]);
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return _descriptor;
		}

		@Override
		public JavaScriptMethod build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new JavaScriptMethod(getName(), _method, _conversions, args);
		}
	}

	private static class Converter {
		private final int _index;

		private final Function<Object, Object> _conversion;

		/**
		 * Creates a {@link JavaScriptMethod.Converter}.
		 */
		public Converter(int index, Function<Object, Object> conversion) {
			_index = index;
			_conversion = conversion;
		}

		public void convert(Object[] args) {
			args[_index] = _conversion.apply(args[_index]);
		}
	}
}
