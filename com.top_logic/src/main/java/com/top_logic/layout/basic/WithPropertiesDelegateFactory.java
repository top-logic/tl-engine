/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.ExpressionTemplate;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.WithPropertiesDelegate.Property;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;

/**
 * Factory for {@link WithPropertiesDelegate} implementations based on reflection.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class WithPropertiesDelegateFactory {

	private static final ConcurrentHashMap<Class<?>, Delegate> PROPERTY_DELEGATES =
		new ConcurrentHashMap<>();

	/**
	 * Creates a {@link WithPropertiesDelegate} for the given type.
	 */
	public static WithPropertiesDelegate lookup(Class<?> type) {
		return internalLookup(type);
	}

	private static Delegate internalLookup(Class<?> type) {
		// Note: Must not use computeIfAbsent(), because recursive calls might happen.
		Delegate cachedResult = PROPERTY_DELEGATES.get(type);
		if (cachedResult != null) {
			return cachedResult;
		}
		Delegate newDelegate = createDelegate(type);
		return MapUtil.putIfAbsent(PROPERTY_DELEGATES, type, newDelegate);
	}

	private static Delegate createDelegate(Class<?> type) {
		Class<?> superclass = type.getSuperclass();
	
		Delegate superDelegate = superclass == null ? Delegate.NONE : internalLookup(superclass);
		Delegate delegate = new Delegate(superDelegate);

		for (Class<?> intf : type.getInterfaces()) {
			delegate.addProperties(internalLookup(intf));
		}
	
		for (Method method : type.getDeclaredMethods()) {
			TemplateVariable annotation = method.getAnnotation(TemplateVariable.class);
			if (annotation != null) {
				if (!Modifier.isPublic(type.getModifiers())) {
					throw new IllegalArgumentException(
						"Only public classes can be used as reflective controls: " + type.getName());
				}
	
				if (!Modifier.isPublic(method.getModifiers())) {
					Logger.error(
						"Control property implementations must be public: " + method, WithProperties.class);
					continue;
				}
				if (Modifier.isStatic(method.getModifiers())) {
					Logger.error(
						"Control property implementations must not be static: " + method, WithProperties.class);
					continue;
				}
	
				Parameter[] parameters = method.getParameters();
				String propertyName = annotation.value();
				switch (parameters.length) {
					case 0:
						if (method.getReturnType() == void.class) {
							Logger.error(
								"Void method must not be used as control property implementation if the signatures"
									+ "parameter list is empty: " + method,
								WithProperties.class);
							continue;
						}
	
						delegate.addPropertyDelegate(propertyName, new Getter(propertyName, method));
						break;
					case 1: {
						Parameter first = parameters[0];
						Class<?> firstType = first.getType();
						if (firstType == TagWriter.class || firstType == Appendable.class) {
							delegate.addPropertyDelegate(propertyName,
								new ContextFreeRenderer(propertyName, method));
						} else {
							Logger.error(
								"Single-argument property render method must use TagWriter or Appendable as parameter type: "
										+ method,
								WithProperties.class);
							continue;
						}
						break;
					}
					case 2: {
						Parameter first = parameters[0];
						if (first.getType() != DisplayContext.class) {
							Logger.error(
								"Two-argument render method must use DisplayContext as first parameter: "
										+ method,
								WithProperties.class);
							continue;
						}
						Parameter second = parameters[1];
						if (second.getType() != TagWriter.class) {
							Logger.error(
								"Two-argument render method must use TagWriter as second parameter: "
										+ method,
								WithProperties.class);
							continue;
						}
						delegate.addPropertyDelegate(propertyName, new ContextRenderer(propertyName, method));
						break;
					}
					default:
						Logger.error(
							"Property render method with invalid signature, (DisplayContext, TagWriter), (TagWriter),"
								+ " (Appendable) or any empty parameter list with a non void return type is expected: "
								+ method,
							WithProperties.class);
						continue;
				}
			}
		}
		return delegate;
	}

	static final class Delegate implements WithPropertiesDelegate {

		public static final Delegate NONE = new Delegate();

		private final Map<String, PropertyDelegate> _propertyDelegates = new HashMap<>();

		/**
		 * Creates a {@link Delegate}.
		 */
		public Delegate() {
			super();
		}

		/**
		 * Creates a {@link Delegate}.
		 */
		public Delegate(Delegate superDelegate) {
			addProperties(superDelegate);
		}

		/**
		 * Adds all properties of the given {@link Delegate} to this {@link Delegate}.
		 */
		public void addProperties(Delegate other) {
			_propertyDelegates.putAll(other._propertyDelegates);
		}

		@Override
		public Object getPropertyValue(Object self, String propertyName) throws NoSuchPropertyException {
			return getPropertyDelegate(self, propertyName).getPropertyValue(self);
		}

		@Override
		public Collection<String> getAvailableProperties(Object self) {
			return Collections.unmodifiableCollection(_propertyDelegates.keySet());
		}

		@Override
		public Collection<? extends Property> getAvailablePropertyInstances() {
			return Collections.unmodifiableCollection(_propertyDelegates.values());
		}

		@Override
		public void renderProperty(DisplayContext context, TagWriter out, Object self, String propertyName)
				throws IOException {
			try {
				getPropertyDelegate(self, propertyName).renderProperty(context, out, self);
			} catch (NoSuchPropertyException ex) {
				throw WithProperties.reportError(self, propertyName, Optional.of(getAvailableProperties(self)));
			}
		}

		private PropertyDelegate getPropertyDelegate(Object self, String propertyName)
				throws NoSuchPropertyException {
			PropertyDelegate result = _propertyDelegates.get(propertyName);
			if (result == null) {
				throw new NoSuchPropertyException("No such property '" + propertyName + "' in '" + self + "'.");
			}
			return result;
		}

		public void addPropertyDelegate(String propertyName, PropertyDelegate delegate) {
			_propertyDelegates.put(propertyName, delegate);
		}
	}

	static abstract class PropertyDelegate implements Property {

		private final String _propertyName;

		/**
		 * Creates a {@link PropertyDelegate}.
		 */
		public PropertyDelegate(String propertyName) {
			_propertyName = propertyName;
		}

		@Override
		public String getPropertyName() {
			return _propertyName;
		}

		protected final ResKey getDocumentation(Class<?> modelType) {
			StringBuilder name = new StringBuilder();
			appendClassName(name, modelType);
			name.append(".");
			name.append(getPropertyName());
			return ResKey.internalCreate(name.toString());
		}

		protected void appendClassName(StringBuilder name, Class<?> modelType) {
			Class<?> declaringClass = modelType.getDeclaringClass();
			if (declaringClass == null) {
				name.append(modelType.getPackage().getName());
			} else {
				appendClassName(name, declaringClass);
			}
			name.append('.');
			name.append(modelType.getSimpleName());
		}
	}

	static final class Getter extends PropertyDelegate {

		private final Method _method;

		/**
		 * Creates a {@link Getter}.
		 */
		public Getter(String propertyName, Method method) {
			super(propertyName);
			_method = method;
		}

		@Override
		public Object getPropertyValue(Object self) {
			try {
				return _method.invoke(self);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new RuntimeException(
					"Error accessing control property '" + getPropertyName() + "' of '" + self + "'.", ex);
			}
		}

		@Override
		public void renderProperty(DisplayContext context, TagWriter out, Object self)
				throws IOException {
			ExpressionTemplate.renderValue(context, out, getPropertyValue(self));
		}

		@Override
		public ResKey getDocumentation() {
			return getDocumentation(_method.getDeclaringClass());
		}

	}

	static abstract class RenderedProperty extends PropertyDelegate {
		/**
		 * Creates a {@link RenderedProperty}.
		 */
		public RenderedProperty(String propertyName) {
			super(propertyName);
		}

		@Override
		public Object getPropertyValue(Object self) {
			throw new UnsupportedOperationException(
				"Property '" + getPropertyName() + "' of '" + self + "' must only be rendered directly.");
		}
	}

	static final class ContextFreeRenderer extends RenderedProperty {
		private final Method _method;

		/**
		 * Creates a {@link ContextFreeRenderer}.
		 */
		public ContextFreeRenderer(String propertyName, Method method) {
			super(propertyName);
			_method = method;
		}

		@Override
		public void renderProperty(DisplayContext context, TagWriter out, Object self)
				throws IOException {
			try {
				_method.invoke(self, out);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new RuntimeException(
					"Error invoking control property render method '" + _method + "' on '" + self + "'.", ex);
			}
		}

		@Override
		public ResKey getDocumentation() {
			return getDocumentation(_method.getDeclaringClass());
		}
	}

	static final class ContextRenderer extends RenderedProperty {
		private final Method _method;

		/**
		 * Creates a {@link ContextFreeRenderer}.
		 */
		public ContextRenderer(String propertyName, Method method) {
			super(propertyName);
			_method = method;
		}

		@Override
		public void renderProperty(DisplayContext context, TagWriter out, Object self)
				throws IOException {
			try {
				_method.invoke(self, context, out);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new RuntimeException(
					"Error invoking control property render method '" + _method + "' on '" + self + "'.", ex);
			}
		}

		@Override
		public ResKey getDocumentation() {
			return getDocumentation(_method.getDeclaringClass());
		}
	}
}