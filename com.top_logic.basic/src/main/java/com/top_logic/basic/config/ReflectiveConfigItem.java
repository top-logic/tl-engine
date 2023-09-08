/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.Log;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.container.ConfigPartUtilInternal;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.config.equal.EqualityByValue;
import com.top_logic.basic.reflect.DefaultMethodInvoker;

/**
 * Abstract {@link ConfigurationItem} which may be used to instanciate it via a
 * {@link Proxy}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class ReflectiveConfigItem implements InvocationHandler, ConfigurationItem {

	private interface MethodHandler {
		/**
		 * Implementation of a {@link ConfigurationItem} core method.
		 * @param impl
		 *        The implementation object implementing the reflection access.
		 * @param args
		 *        The method arguments.
		 * 
		 * @return The method result.
		 * @throws Throwable
		 *         If the implementation throws any exception.
		 */
		public Object invoke(ReflectiveConfigItem impl, Object[] args) throws Throwable;
	}

	/**
	 * Reflection name of the {@link Object#hashCode()} method.
	 */
	protected static final String HASH_CODE_METHOD_NAME = "hashCode";
	
	/**
	 * Reflection name of the {@link Object#equals(Object)} method.
	 */
	protected static final String EQUALS_METHOD_NAME = "equals";
	
	/**
	 * Reflection name of the {@link Object#toString()} method.
	 */
	protected static final String TO_STRING_METHOD_NAME = "toString";
	
	/**
	 * Reflection name of the {@link Annotation#annotationType()} method.
	 */
	protected static final String ANNOTATION_TYPE_METHOD_NAME = "annotationType";

	/**
	 * Reflection name of the {@link ConfigurationItem#valueSet(PropertyDescriptor)} method.
	 */
	protected static final String VALUE_SET_METHOD_NAME = "valueSet";
	
	/**
	 * Reflection name of the
	 * {@link ConfigurationItem#value(PropertyDescriptor)} method.
	 */
	protected static final String VALUE_METHOD_NAME = "value";
	
	/**
	 * Reflection name of the
	 * {@link ConfigurationItem#update(PropertyDescriptor, Object)} method.
	 */
	protected static final String UPDATE_METHOD_NAME = "update";

	/**
	 * Reflection name of the {@link ConfigurationItem#reset(PropertyDescriptor)} method.
	 */
	protected static final String RESET_METHOD_NAME = "reset";

	/**
	 * Reflection name of the
	 * {@link ConfigurationItem#addConfigurationListener(PropertyDescriptor, ConfigurationListener)}
	 * method.
	 */
	protected static final String ADD_CONFIGURATION_LISTENER_METHOD_NAME = "addConfigurationListener";

	/**
	 * Reflection name of the
	 * {@link ConfigurationItem#removeConfigurationListener(PropertyDescriptor, ConfigurationListener)}
	 * method.
	 */
	protected static final String REMOVE_CONFIGURATION_LISTENER_METHOD_NAME = "removeConfigurationListener";

	/**
	 * @see ConfigurationItem#check(Log)
	 */
	protected static final String CHECK_METHOD_NAME = "check";

	/**
	 * Marker method that prevents implementations outside the defining package.
	 */
	protected static final String UNIMPLEMENTABLE_METHOD_NAME = "unimplementable";

	/**
	 * Reflection name of the {@link ConfigurationItem#descriptor()} method.
	 */
	protected static final String DESCRIPTOR_METHOD_NAME = "descriptor";

	/**
	 * Reflection name of the {@link ConfigurationItem#location()} method.
	 */
	protected static final String LOCALTION_METHOD_NAME = "location";
	
	/**
	 * The {@link ConfigurationItem#descriptor()} method.
	 */
	protected static final Method DESCRIPTOR_METHOD;
	static {
		DESCRIPTOR_METHOD = getMethod(ConfigurationItem.class, DESCRIPTOR_METHOD_NAME);
	}

	/**
	 * The {@link ConfigurationItem#location()} method.
	 */
	protected static final Method LOCATION_METHOD;
	static {
		LOCATION_METHOD = getMethod(ConfigurationItem.class, LOCALTION_METHOD_NAME);
	}

	/**
	 * The {@link ConfigurationItem#value(PropertyDescriptor)} method.
	 */
	protected static final Method VALUE_METHOD;
	static {
		VALUE_METHOD = getMethod(ConfigurationItem.class, VALUE_METHOD_NAME, PropertyDescriptor.class);
	}

	/**
	 * The {@link ConfigurationItem#valueSet(PropertyDescriptor)} method.
	 */
	protected static final Method VALUE_SET_METHOD;
	static {
		VALUE_SET_METHOD = getMethod(ConfigurationItem.class, VALUE_SET_METHOD_NAME, PropertyDescriptor.class);
	}

	/**
	 * The {@link ConfigurationItem#update(PropertyDescriptor, Object)} method.
	 */
	protected static final Method UPDATE_METHOD;
	static {
		UPDATE_METHOD = getMethod(ConfigurationItem.class, UPDATE_METHOD_NAME, PropertyDescriptor.class, Object.class);
	}

	/**
	 * The {@link ConfigurationItem#reset(PropertyDescriptor)} method.
	 */
	protected static final Method RESET_METHOD;
	static {
		RESET_METHOD = getMethod(ConfigurationItem.class, RESET_METHOD_NAME, PropertyDescriptor.class);
	}

	/**
	 * The
	 * {@link ConfigurationItem#addConfigurationListener(PropertyDescriptor, ConfigurationListener)}
	 * method.
	 */
	protected static final Method ADD_CONFIGURATION_LISTENER_METHOD;
	static {
		ADD_CONFIGURATION_LISTENER_METHOD =
			getMethod(ConfigurationItem.class, ADD_CONFIGURATION_LISTENER_METHOD_NAME, PropertyDescriptor.class,
				ConfigurationListener.class);
	}

	/**
	 * The
	 * {@link ConfigurationItem#removeConfigurationListener(PropertyDescriptor, ConfigurationListener)}
	 * method.
	 */
	protected static final Method REMOVE_CONFIGURATION_LISTENER_METHOD;
	static {
		REMOVE_CONFIGURATION_LISTENER_METHOD =
			getMethod(ConfigurationItem.class, REMOVE_CONFIGURATION_LISTENER_METHOD_NAME, PropertyDescriptor.class,
				ConfigurationListener.class);
	}

	/**
	 * The {@link ConfigurationItem#check(Log)} method.
	 */
	protected static final Method CHECK_METHOD;

	static {
		CHECK_METHOD = getMethod(ConfigurationItem.class, CHECK_METHOD_NAME, Log.class);
	}

	/**
	 * The {@link ConfigurationItem#unimplementable()} method.
	 */
	protected static final Method UNIMPLEMENTABLE_METHOD;
	static {
		UNIMPLEMENTABLE_METHOD = getMethod(ConfigurationItem.class, UNIMPLEMENTABLE_METHOD_NAME);
	}
	
	/**
	 * The {@link Object#equals(Object)} method.
	 */
	protected static final Method EQUALS_METHOD;
	static {
		EQUALS_METHOD = getMethod(Object.class, EQUALS_METHOD_NAME, Object.class);
	}

	/**
	 * The {@link Object#hashCode()} method.
	 */
	protected static final Method HASH_CODE_METHOD;
	static {
		HASH_CODE_METHOD = getMethod(Object.class, HASH_CODE_METHOD_NAME);
	}

	/**
	 * The {@link Object#toString()} method.
	 */
	protected static final Method TO_STRING_METHOD;
	static {
		TO_STRING_METHOD = getMethod(Object.class, TO_STRING_METHOD_NAME);
	}

	/**
	 * The {@link Object#hashCode()} method.
	 */
	protected static final Method ANNOTATION_TYPE_METHOD;
	static {
		ANNOTATION_TYPE_METHOD = getMethod(Annotation.class, ANNOTATION_TYPE_METHOD_NAME);
	}

	protected static Method getMethod(Class<?> type, String name, Class<?>... signature) {
		try {
			return type.getMethod(name, signature);
		} catch (SecurityException e) {
			throw new AssertionError(e);
		} catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		}
	}

	private static final Map<Method, MethodHandler> INTERNAL_METHODS = new MapBuilder<Method, MethodHandler>()
			.put(DESCRIPTOR_METHOD, new MethodHandler() {
				@Override
				public Object invoke(ReflectiveConfigItem impl, Object[] args) {
					return impl.descriptor();
				}
			}).put(LOCATION_METHOD, new MethodHandler() {
				@Override
				public Object invoke(ReflectiveConfigItem impl, Object[] args) {
					return impl.location();
				}
			}).put(VALUE_METHOD, new MethodHandler() {
				@Override
				public Object invoke(ReflectiveConfigItem impl, Object[] args) {
					return impl.value((PropertyDescriptor) args[0]);
				}
			}).put(VALUE_SET_METHOD, new MethodHandler() {
				@Override
				public Object invoke(ReflectiveConfigItem impl, Object[] args) {
					return Boolean.valueOf(impl.valueSet((PropertyDescriptor) args[0]));
				}
			}).put(UPDATE_METHOD, new MethodHandler() {
				@Override
				public Object invoke(ReflectiveConfigItem impl, Object[] args) {
				return impl.update((PropertyDescriptor) args[0], args[1]);
				}
			}).put(RESET_METHOD, new MethodHandler() {
				@Override
				public Object invoke(ReflectiveConfigItem impl, Object[] args) {
					impl.reset((PropertyDescriptor) args[0]);
					return null;
				}
			}).put(ADD_CONFIGURATION_LISTENER_METHOD, new MethodHandler() {
				@Override
				public Object invoke(ReflectiveConfigItem impl, Object[] args) {
					return impl.addConfigurationListener((PropertyDescriptor) args[0], (ConfigurationListener)args[1]);
				}
			}).put(REMOVE_CONFIGURATION_LISTENER_METHOD, new MethodHandler() {
				@Override
				public Object invoke(ReflectiveConfigItem impl, Object[] args) {
					return impl.removeConfigurationListener((PropertyDescriptor) args[0], (ConfigurationListener)args[1]);
				}
			}).put(CHECK_METHOD, new MethodHandler() {
				@Override
				public Object invoke(ReflectiveConfigItem impl, Object[] args) {
					impl.check((Log) args[0]);
					return null;
				}
			}).put(UNIMPLEMENTABLE_METHOD, new MethodHandler() {
				@Override
				public Object invoke(ReflectiveConfigItem impl, Object[] args) {
					return impl.unimplementable();
				}
			}).put(EQUALS_METHOD, new MethodHandler() {
				@Override
				public Boolean invoke(ReflectiveConfigItem impl, Object[] args) {
					Object other = args[0];
					if (other == null) {
						return false;
					}
					// impl.equals(other) == false // Reason: 'impl' is the ReflectiveConfigItem,
					// but 'other' is a Proxy, which is not equal to anything.
					// And calling 'impl.getInterface().equals(other)' causes an infinite recursion,
					// as it is exactly what has been called and should be answered here.
					return impl.getInterface() == other;
				}

			}).put(HASH_CODE_METHOD, new MethodHandler() {
				@Override
				public Integer invoke(ReflectiveConfigItem impl, Object[] args) {
					return impl.hashCode();
				}
			}).put(TO_STRING_METHOD, new MethodHandler() {
				@Override
				public Object invoke(ReflectiveConfigItem impl, Object[] args) {
					try {
						return TypedConfiguration.toString(impl);
					} catch (Exception ex) {
						return impl.descriptor().getConfigurationInterface().getName() + "(" + "/*"
								+ "error getting properties: " + ex + "*/" + ")";
					}
				}
		}).put(ANNOTATION_TYPE_METHOD, new MethodHandler() {
			@Override
			public Object invoke(ReflectiveConfigItem impl, Object[] args) {
				return impl.descriptor().getConfigurationInterface();
			}
			}).put(ConfigPartUtilInternal.UPDATE_CONTAINER_METHOD, new MethodHandler() {
				@Override
				public Object invoke(ReflectiveConfigItem impl, Object[] args) {
					impl.updateContainer((ConfigurationItem) args[0]);
					return null;
				}
			}).put(ConfigPartUtilInternal.CONTAINER_METHOD, new MethodHandler() {
				@Override
				public Object invoke(ReflectiveConfigItem impl, Object[] args) {
					return impl.container();
				}
			}).toMap();

	private static final Map<Method, MethodHandler> INTERNAL_METHODS_EQUALITY_BY_VALUE;
	static {
		// Override equals and hasCode methods.
		Map<Method, MethodHandler> tmp = new HashMap<>();
		tmp.putAll(INTERNAL_METHODS);
		tmp.put(EQUALS_METHOD, new MethodHandler() {

			@Override
			public Object invoke(ReflectiveConfigItem impl, Object[] args) throws Throwable {
				Object other = args[0];
				if (!(other instanceof ConfigurationItem)) {
					return false;
				}

				ConfigurationItem left = impl.getInterface();
				ConfigurationItem right = (ConfigurationItem) args[0];
				return ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(left, right);
			}
		});
		tmp.put(HASH_CODE_METHOD, new MethodHandler() {
			@Override
			public Integer invoke(ReflectiveConfigItem impl, Object[] args) {
				return ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(impl.getInterface());
			}
		});
		INTERNAL_METHODS_EQUALITY_BY_VALUE = tmp;
	}

	/**
	 * The wrapper for accessing this data item in a type-safe way.
	 */
	private ConfigurationItem _intf = this;

	private Map<Method, MethodHandler> _internalMethods = Collections.emptyMap();

	ConfigurationItem getInterface() {
		return _intf;
	}

	void initInterface(ConfigurationItem intf) {
		_intf = intf;
		if (intf instanceof EqualityByValue) {
			_internalMethods = INTERNAL_METHODS_EQUALITY_BY_VALUE;
		} else {
			_internalMethods = INTERNAL_METHODS;
		}
	}

	/**
	 * Implements the method {@link ConfigPart#container()}, when this is a subclass of
	 * {@link ConfigPart}.
	 */
	abstract ConfigurationItem container();

	/**
	 * Implements the method <code>ConfigPartInternal.updateContainer(ConfigurationItem)</code>,
	 * when this is a subclass of {@link ConfigPart}.
	 * <p>
	 * <code>ConfigPartInternal.updateContainer(ConfigurationItem)</code> is invisible here and
	 * accessed only via the {@link ConfigPartUtilInternal}.
	 * </p>
	 */
	abstract void updateContainer(ConfigurationItem container);

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		assert _intf != this : "Interface not set. Reflection methods accessing the interfaces would leads to StackOverflowError.";
		MethodImplementation methodImplementation = descriptorImpl().getMethodImplementations().get(method);
		if (methodImplementation != null) {
			try {
				return methodImplementation.invoke(this, method, args);
			} catch (IllegalAccessException e) {
				throw (AssertionError) new AssertionError("Could not invoke generic method.").initCause(e);
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}

		MethodHandler handler = _internalMethods.get(method);
		if (handler != null) {
			return handler.invoke(this, args);
		}

		if (method.isDefault()) {
			return DefaultMethodInvoker.INSTANCE.invoke(proxy, method, args);
		}
		if (Modifier.isStatic(method.getModifiers())) {
			return method.invoke(proxy, args);
		}

		throw new AssertionError("No generic implementation of method '" + method + "' for type '"
			+ descriptor().getConfigurationInterface().getName() + "'.");
	}

	protected AbstractConfigurationDescriptor descriptorImpl() {
		ConfigurationDescriptor descriptor = descriptor();
		if (descriptor instanceof AbstractConfigurationDescriptor) {
			return (AbstractConfigurationDescriptor) descriptor;
		}
		throw new ClassCastException(
			"Reflection methods can not be called on descriptor " + descriptor + " of type" + descriptor.getClass());
	}

	@Override
	public final Class<?> getConfigurationInterface() {
		return descriptor().getConfigurationInterface();
	}

	@Override
	public final Unimplementable unimplementable() {
		return null;
	}

	@Override
	public String toString() {
		if (getInterface() != this) {
			return getClass().getSimpleName() + "(" + getInterface() + ")";
		} else {
			return super.toString();
		}
	}

}
