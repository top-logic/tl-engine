/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.i18n.DefaultHtmlResKey;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.DefaultScriptDocumentation.DocumentationParameter;
import com.top_logic.util.error.TopLogicException;

/**
 * Reflection-based {@link GenericMethod TL-Script method} calling a static Java utility.
 */
public class TLScriptMethod extends GenericMethod {

	private final Method _java;

	private final Converter[] _conversions;

	private final boolean _sideEffectFree;

	private final boolean _canEvaluateAtCompileTime;

	/**
	 * Creates a {@link TLScriptMethod}.
	 */
	protected TLScriptMethod(String name, Method java, boolean sideEffectFree, boolean canEvaluateAtCompileTime,
			Converter[] conversions,
			SearchExpression[] arguments) {
		super(name, arguments);
		_java = java;
		_canEvaluateAtCompileTime = canEvaluateAtCompileTime;
		_conversions = conversions;
		_sideEffectFree = sideEffectFree;
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new TLScriptMethod(getName(), _java, _sideEffectFree, _canEvaluateAtCompileTime, _conversions,
			arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public boolean isSideEffectFree() {
		return _sideEffectFree;
	}

	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return _canEvaluateAtCompileTime;
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
	 * Builder for {@link TLScriptMethod}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<TLScriptMethod> {

		/** Separator between class name and method name. */
		public static final char METHOD_SEPARATOR = '#';

		private final Method _method;

		private final ArgumentDescriptor _descriptor;

		private final Converter[] _conversions;

		private final boolean _sideEffectFree;

		private final boolean _canEvaluateAtCompileTime;

		private final List<DocumentationParameter> _documentationParams = new ArrayList<>();

		/**
		 * Configuration options for {@link TLScriptMethod.Builder}.
		 */
		@TagName("java")
		public interface Config extends AbstractSimpleMethodBuilder.Config<Builder> {
			/**
			 * Qualified name of the static method to call in the form
			 * <code>fully.qualified.ClassName#methodName</code>
			 */
			@Mandatory
			String getMethod();
			
			/**
			 * Setter for {@link #getMethod()}.
			 */
			void setMethod(String value);
		}

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config config) {
			super(context, config);

			String method = config.getMethod();
			int sepIndex = method.indexOf(METHOD_SEPARATOR);
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
					DocumentationParameter docuParam = new DocumentationParameter(p.getName());
					docuParam.setDescription(parameterDescription(p));
					docuParam.setType(typeString(p.getParameterizedType()));
					{
						ValueConverter converter = converter(p);

						if (converter != null) {
							conversions.add(new Converter(n, converter));
						}

						if (p.getAnnotation(Mandatory.class) != null) {
							descriptor.mandatory(p.getName());
							docuParam.setMandatory();
						} else {
							Object defaultValue = defaultValue(p);
							descriptor.optional(p.getName(), () -> SearchExpressionFactory.literal(defaultValue));
							docuParam.setOptional(ToString.toString(defaultValue));
						}
					}
					_documentationParams.add(docuParam);
				}
				_descriptor = descriptor.build();
				_conversions = conversions.toArray(new Converter[0]);

				SideEffectFree sideEffectFree = _method.getAnnotation(SideEffectFree.class);
				if (sideEffectFree != null) {
					_sideEffectFree = true;
					_canEvaluateAtCompileTime = sideEffectFree.canEvaluateAtCompileTime();
				} else {
					_sideEffectFree = false;
					_canEvaluateAtCompileTime = false;
				}
			} catch (ClassNotFoundException | SecurityException | NoSuchMethodException ex) {
				throw new RuntimeException(ex);
			}
		}

		private ResKey key(Executable exectuable) {
			return ResKey.forClass(exectuable.getDeclaringClass())
				.suffix("." + exectuable.getName());
		}

		private HTMLFragment parameterDescription(Parameter p) {
			return new DefaultHtmlResKey(key(p.getDeclaringExecutable())
				.suffix(".param")
				.suffix("." + p.getName()).tooltipOptional());
		}

		private HTMLFragment methodDescription(Method m) {
			return new DefaultHtmlResKey(key(m).tooltipOptional());
		}

		private HTMLFragment returnDescription(Method m) {
			return new DefaultHtmlResKey(key(m).suffix(".return").optional());
		}

		private ResKey methodLabel(Method m) {
			return key(m);
		}

		private String typeString(Type type) {
			if (type instanceof Class<?> classType) {
				return classType.getSimpleName();
			} else if (type instanceof ParameterizedType paramType) {
				Type rawType = paramType.getRawType();
				if (rawType instanceof Class rawClassType) {
					if (rawClassType.isAssignableFrom(List.class)) {
						String typeString = typeString(List.class);
						String contentType = typeString(paramType.getActualTypeArguments()[0]);
						if (!"Object".equals(contentType)) {
							typeString += "<" + contentType + ">";
						}
						return typeString;
					}
					if (rawClassType.isAssignableFrom(Set.class)) {
						String typeString = typeString(Set.class);
						String contentType = typeString(paramType.getActualTypeArguments()[0]);
						if (!"Object".equals(contentType)) {
							typeString += "<" + contentType + ">";
						}
						return typeString;
					}
				}
				return type.toString();
			} else if(type instanceof WildcardType wildType) {
				Type[] upperBounds = wildType.getUpperBounds();
				if (upperBounds.length == 0) {
					return typeString(Object.class);
				} else {
					return typeString(upperBounds[0]);
				}
			} else {
				return type.toString();
			}
		}

		private Object defaultValue(Parameter p) {
			Class<?> type = p.getType();
			if (type == double.class) {
				DoubleDefault defAnnotation = p.getAnnotation(DoubleDefault.class);
				return Double.valueOf(defAnnotation == null ? 0 : defAnnotation.value());
			} else if (type == Double.class) {
				DoubleDefault defAnnotation = p.getAnnotation(DoubleDefault.class);
				return defAnnotation == null ? null : Double.valueOf(defAnnotation.value());
			} else if (type == float.class) {
				FloatDefault defAnnotation = p.getAnnotation(FloatDefault.class);
				return Float.valueOf(defAnnotation == null ? 0 : defAnnotation.value());
			} else if (type == Float.class) {
				FloatDefault defAnnotation = p.getAnnotation(FloatDefault.class);
				return defAnnotation == null ? null : Float.valueOf(defAnnotation.value());
			} else if (type == int.class) {
				IntDefault defAnnotation = p.getAnnotation(IntDefault.class);
				return Integer.valueOf(defAnnotation == null ? 0 : defAnnotation.value());
			} else if (type == Integer.class) {
				IntDefault defAnnotation = p.getAnnotation(IntDefault.class);
				return defAnnotation == null ? null : Integer.valueOf(defAnnotation.value());
			} else if (type == long.class) {
				LongDefault defAnnotation = p.getAnnotation(LongDefault.class);
				return Long.valueOf(defAnnotation == null ? 0 : defAnnotation.value());
			} else if (type == Long.class) {
				LongDefault defAnnotation = p.getAnnotation(LongDefault.class);
				return defAnnotation == null ? null : Long.valueOf(defAnnotation.value());
			} else if (type == boolean.class) {
				BooleanDefault defAnnotation = p.getAnnotation(BooleanDefault.class);
				return Boolean.valueOf(defAnnotation == null ? false : defAnnotation.value());
			} else if (type == Boolean.class) {
				BooleanDefault defAnnotation = p.getAnnotation(BooleanDefault.class);
				return defAnnotation == null ? null : Boolean.valueOf(defAnnotation.value());
			} else if (type == String.class) {
				StringDefault defAnnotation = p.getAnnotation(StringDefault.class);
				return defAnnotation == null ? null : defAnnotation.value();
			} else if (type.isEnum()) {
				return type.getEnumConstants()[0];
			} else {
				return null;
			}
		}

		private ValueConverter converter(Parameter param) throws NoSuchMethodException, SecurityException {
			ScriptConversion annotation = param.getAnnotation(ScriptConversion.class);
			if (annotation != null) {
				Class<? extends ValueConverter> implClass = annotation.value();
				try {
					ValueConverter result = ConfigUtil.getInstance(implClass);
					return result;
				} catch (ConfigurationException ex) {
					Logger.error("Cannot instantiate converter for: " + param, ex, TLScriptMethod.class);
				}
			}
			return defaultConverterForType(param, param.getParameterizedType());
		}

		private ValueConverter defaultConverterForType(Parameter param, Type type)
				throws NoSuchMethodException {
			if (type instanceof Class<?>) {
				return defaultConverterForClass(param, (Class<?>) type);
			} else if (type instanceof ParameterizedType paramType) { 
				Type rawType = paramType.getRawType();
				if (rawType instanceof Class<?> rawClass) {
					Type[] typeArgs = paramType.getActualTypeArguments();
					if (typeArgs.length == 0 ) {
						return defaultConverterForClass(param, rawClass);
					}
					if (rawClass.isAssignableFrom(List.class)) {
						ValueConverter innerConverter = defaultConverterForType(param, typeArgs[0]);
						if (innerConverter != null) {
							return input -> CollectionUtil.asList(input).stream().map(innerConverter::fromScript)
								.toList();
						} else {
							return CollectionUtil::asList;
						}
					}
					if (rawClass.isAssignableFrom(Set.class)) {
						ValueConverter innerConverter = defaultConverterForType(param, typeArgs[0]);
						if (innerConverter != null) {
							return input -> CollectionUtil.asSet(input).stream().map(innerConverter::fromScript)
								.collect(Collectors.toSet());
						} else {
							return CollectionUtil::asSet;
						}
					}
					return defaultConverterForClass(param, rawClass);
				} else {
					return null;
				}
			} else if (type instanceof WildcardType wildType) {
				// Use only one upper bound. It is quite complicated to convert correct for all
				// upper bounds.
				for (Type upperBound : wildType.getUpperBounds()) {
					ValueConverter innerConverter = defaultConverterForType(param, upperBound);
					if (innerConverter != null) {
						return innerConverter;
					}
				}
				return null;
			} else {
				return null;
			}
		}

		private ValueConverter defaultConverterForClass(Parameter param, Class<?> type)
				throws NoSuchMethodException, SecurityException {
			if (type == double.class) {
				return input -> input instanceof Double ? input : asNumber(param, input).doubleValue();
			} else if (type == Double.class) {
				return 
					input -> input instanceof Double ? input
						: input == null ? null : Double.valueOf(asNumber(param, input).doubleValue());
			} else if (type == float.class) {
				return 
				input -> input instanceof Float ? input : asNumber(param, input).floatValue();
			} else if (type == Float.class) {
				return 
					input -> input instanceof Float ? input
						: input == null ? null : Float.valueOf(asNumber(param, input).floatValue());
			} else if (type == int.class) {
				return 
				input -> input instanceof Integer ? input : asNumber(param, input).intValue();
			} else if (type == Integer.class) {
				return 
					input -> input instanceof Integer ? input
						: input == null ? null : Integer.valueOf(asNumber(param, input).intValue());
			} else if (type == long.class) {
				return 
				input -> input instanceof Long ? input : asNumber(param, input).longValue();
			} else if (type == Long.class) {
				return 
					input -> input instanceof Long ? input
						: input == null ? null : Long.valueOf(asNumber(param, input).longValue());
			} else if (type == boolean.class) {
				return 
					input -> input instanceof Boolean ? input : SearchExpression.asBoolean(input);
			} else if (type == Boolean.class) {
				return 
					input -> input instanceof Boolean ? input
						: input == null ? null : SearchExpression.asBoolean(input);
			} else if (type == String.class) {
				return 
					input -> input instanceof String ? 
						input : 
							(input == null ? null : ToString.toString(input));
			} else if (type.isEnum()) {
				Method valueOf = lookupValueOf(type);
				return input -> {
					if (input instanceof String) {
						try {
							return valueOf.invoke(null, input);
						} catch (InvocationTargetException | IllegalAccessException
								| IllegalArgumentException ex) {
							throw new RuntimeException(ex);
						}
					}
					return input;
				};
			} else if (type.isArray()) {
				Class<?> componentType = type.componentType();
				ValueConverter inner = defaultConverterForClass(param, componentType);

				return input -> {
					if (input instanceof Collection<?> coll) {
						Object result = Array.newInstance(componentType, coll.size());
						int index = 0;
						for (Object x : coll) {
							Array.set(result, index++, inner.fromScript(x));
						}
						return result;
					}
					return input;
				};
			} else if (type == Object.class) {
				// No conversion necessary.
				return null;
			} else if (type.isAssignableFrom(List.class)) {
				return CollectionUtil::asList;
			} else if (type.isAssignableFrom(Set.class)) {
				return CollectionUtil::asSet;
			} else {
				return input -> asType(param, type, input);
			}
		}

		private Object asType(Parameter param, Class<?> type, Object input) {
			if (input == null) {
				return null;
			}
			if (type.isInstance(input)) {
				return input;
			}
			throw new TopLogicException(I18NConstants.ERROR_WRONG_ARGUMENT__FUN_ARG_EXPECTED_VAL.fill(getName(),
				param.getName(), type.getSimpleName(), input));
		}

		private Number asNumber(Parameter param, Object input) {
			if (input instanceof Number number) {
				return number;
			} else {
				throw new TopLogicException(I18NConstants.ERROR_WRONG_ARGUMENT__FUN_ARG_EXPECTED_VAL.fill(getName(),
					param.getName(), "Number", input));
			}
		}

		private Method lookupValueOf(Class<?> type) throws NoSuchMethodException {
			try {
				return type.getMethod("valueOfProtocol", String.class);
			} catch (NoSuchMethodException ex) {
				return type.getMethod("valueOf", String.class);
			}
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return _descriptor;
		}

		@Override
		public TLScriptMethod build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new TLScriptMethod(getName(), _method, _sideEffectFree, _canEvaluateAtCompileTime, _conversions,
				args);
		}

		@Override
		public Object getId() {
			return _method;
		}

		/**
		 * Documentation for the TL-Script function.
		 */
		public HTMLFragment documentation() {
			return new DefaultScriptDocumentation(getName(), _documentationParams)
				.setLabel(methodLabel(_method))
				.setDescription(methodDescription(_method))
				.setReturnType(typeString(_method.getGenericReturnType()))
				.setReturnDescription(returnDescription(_method));
		}
	}

	@Override
	public Object getId() {
		return _java;
	}

	private static class Converter {
		private final int _index;

		private final ValueConverter _conversion;

		/**
		 * Creates a {@link TLScriptMethod.Converter}.
		 */
		public Converter(int index, ValueConverter converter) {
			_index = index;
			_conversion = converter;
		}

		public void convert(Object[] args) {
			args[_index] = _conversion.fromScript(args[_index]);
		}
	}
}
