/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.MultipleParentClassLoader;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import com.top_logic.knowledge.wrap.binding.DynamicBinding;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;

/**
 * Classes for transient objects and form buffers that implement the generated Java interfaces of
 * their model type.
 *
 * <p>
 * A type with a generated Java interface (see {@link TLModelNamingConvention#interfaceName(TLType)}) is represented by persistent objects that implement this interface.
 * A transient object or a form buffer of that type is created from a fixed base class (e.g.
 * {@link TransientTLObjectImpl}) that knows nothing about the type. To offer the typed access of the
 * generated interface on such objects as well, {@link #classFor(Class, TLStructuredType)} delivers a
 * subclass of the base class that additionally implements the generated interfaces of the type and
 * all of its generalizations. This class is generated at runtime and cached.
 * </p>
 *
 * <p>
 * The default methods of the generated interfaces work through {@link TLObject#tValue} and
 * {@link TLObject#tUpdate} on the object itself, i.e. they read and write the values of the
 * transient object or buffer. An abstract method that a generated interface inherits from a
 * framework interface and that the base class does not implement throws an
 * {@link UnsupportedOperationException} naming the type of the object and the method.
 * </p>
 *
 * <p>
 * The generated class passes all constructors of the base class through as public constructors.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class TypedObjectClasses {

	/**
	 * Suffix of the names of the generated classes.
	 */
	private static final String CLASS_NAME_SUFFIX = "Typed";

	/**
	 * Implementation of the abstract methods of the generated classes.
	 */
	private static final InvocationHandler UNSUPPORTED = TypedObjectClasses::unsupported;

	/**
	 * Generated classes by base class and implemented interfaces.
	 *
	 * <p>
	 * The key is not the model type, so that a model change at runtime leaves no stale entries.
	 * </p>
	 */
	private static final Map<Key, Class<?>> CLASSES = new ConcurrentHashMap<>();

	/**
	 * Resolved generated interfaces by their fully qualified names, empty if no such interface
	 * exists.
	 */
	private static final Map<String, Optional<Class<?>>> INTERFACES = new ConcurrentHashMap<>();

	private TypedObjectClasses() {
		// Utility class.
	}

	/**
	 * Creates a new instance of the class {@link #classFor(Class, TLStructuredType)} delivers.
	 *
	 * @param base
	 *        The base class of the object.
	 * @param type
	 *        The model type of the object.
	 * @param parameterTypes
	 *        The parameter types of the constructor of the base class to call. A non-public
	 *        constructor of the base class must be accessible from this class.
	 * @param arguments
	 *        The arguments to pass to the constructor.
	 * @return The new object, an instance of the base class and of all generated interfaces of
	 *         the given type and its generalizations.
	 */
	public static <B extends TLObject> B newInstance(Class<B> base, TLStructuredType type,
			Class<?>[] parameterTypes, Object... arguments) {
		Class<? extends B> implementationClass = classFor(base, type);
		try {
			Constructor<? extends B> constructor = implementationClass.getDeclaredConstructor(parameterTypes);
			return constructor.newInstance(arguments);
		} catch (InvocationTargetException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof RuntimeException runtimeException) {
				throw runtimeException;
			}
			if (cause instanceof Error error) {
				throw error;
			}
			throw new RuntimeException("Creating an instance of '" + implementationClass.getName() + "' failed.",
				cause);
		} catch (ReflectiveOperationException ex) {
			throw new IllegalArgumentException(
				"Cannot instantiate '" + implementationClass.getName() + "' with constructor parameters "
					+ List.of(parameterTypes) + ".",
				ex);
		}
	}

	/**
	 * The class of a transient object or form buffer of the given type.
	 *
	 * @param base
	 *        The class implementing the object.
	 * @param type
	 *        The model type of the object.
	 * @return The given base class itself, if neither the type nor any of its generalizations has
	 *         a generated Java interface that the base class does not implement already, otherwise
	 *         a subclass of the base class implementing all these interfaces.
	 */
	@SuppressWarnings("unchecked")
	public static <B extends TLObject> Class<? extends B> classFor(Class<B> base, TLStructuredType type) {
		Set<Class<?>> interfaces = interfaces(base, type);
		if (interfaces.isEmpty()) {
			return base;
		}
		return (Class<? extends B>) CLASSES.computeIfAbsent(new Key(base, Set.copyOf(interfaces)),
			TypedObjectClasses::create);
	}

	/**
	 * The generated interfaces of the given type and its generalizations that the base class does
	 * not implement, without interfaces that one of the others extends.
	 */
	private static Set<Class<?>> interfaces(Class<?> base, TLStructuredType type) {
		Collection<? extends TLStructuredType> types;
		if (type instanceof TLClass clazz) {
			types = TLModelUtil.getReflexiveTransitiveGeneralizations(clazz);
		} else {
			types = List.of(type);
		}

		List<Class<?>> candidates = new ArrayList<>();
		for (TLStructuredType generalization : types) {
			Class<?> intf = generatedInterface(generalization);
			if (intf != null && !intf.isAssignableFrom(base) && !candidates.contains(intf)) {
				candidates.add(intf);
			}
		}

		Set<Class<?>> result = new LinkedHashSet<>();
		for (Class<?> candidate : candidates) {
			if (!extendedByOther(candidate, candidates)) {
				result.add(candidate);
			}
		}
		return result;
	}

	private static boolean extendedByOther(Class<?> candidate, List<Class<?>> candidates) {
		for (Class<?> other : candidates) {
			if (other != candidate && candidate.isAssignableFrom(other)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The generated Java interface of the given type, or <code>null</code> if it has none.
	 */
	private static Class<?> generatedInterface(TLStructuredType type) {
		String interfaceName = TLModelNamingConvention.interfaceName(type);
		if (interfaceName == null) {
			return null;
		}
		return INTERFACES.computeIfAbsent(interfaceName, TypedObjectClasses::resolveInterface).orElse(null);
	}

	/**
	 * Resolves the interface with the given name.
	 *
	 * <p>
	 * The interface is resolved with the class loader of the engine, the same way
	 * {@link DynamicBinding} resolves the implementation classes of persistent objects. In a web
	 * application, this is the class loader of the application that sees the engine and the
	 * application classes.
	 * </p>
	 */
	private static Optional<Class<?>> resolveInterface(String interfaceName) {
		Class<?> result;
		try {
			result = Class.forName(interfaceName, false, TypedObjectClasses.class.getClassLoader());
		} catch (ClassNotFoundException | LinkageError ex) {
			return Optional.empty();
		}
		if (!result.isInterface() || !TLObject.class.isAssignableFrom(result)) {
			return Optional.empty();
		}
		return Optional.of(result);
	}

	/**
	 * Generates the subclass of the given base class implementing the given interfaces.
	 *
	 * <p>
	 * The class is defined in a new class loader whose parents are the class loaders of the base
	 * class and of all interfaces. So the generated class sees its base class and its interfaces,
	 * even if they come from different class loaders (e.g. the engine and an application module),
	 * and it can be unloaded independently of them.
	 * </p>
	 */
	private static Class<?> create(Key key) {
		Class<?> base = key.base();
		List<Class<?>> interfaces = new ArrayList<>(key.interfaces());
		interfaces.sort(Comparator.comparing(Class::getName));

		ClassLoader parent = new MultipleParentClassLoader.Builder()
			.appendMostSpecific(base)
			.appendMostSpecific(interfaces)
			.build();

		return new ByteBuddy()
			.with(new NamingStrategy.SuffixingRandom(CLASS_NAME_SUFFIX))
			.subclass(base, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
			.implement(interfaces)
			.method(ElementMatchers.isAbstract())
			.intercept(InvocationHandlerAdapter.of(UNSUPPORTED))
			.make()
			.load(parent, ClassLoadingStrategy.Default.WRAPPER)
			.getLoaded();
	}

	/**
	 * Implementation of an abstract method that a generated interface inherits from a framework
	 * interface and that the base class does not implement.
	 */
	private static Object unsupported(Object self, Method method, Object[] arguments) {
		throw new UnsupportedOperationException("Method '" + method.getDeclaringClass().getName() + "."
			+ method.getName() + "()' is not supported by '" + self.getClass().getSuperclass().getName()
			+ "' objects of type '" + TLModelUtil.qualifiedName(((TLObject) self).tType()) + "'.");
	}

	/**
	 * Key of a generated class.
	 *
	 * @param base
	 *        The base class.
	 * @param interfaces
	 *        The additionally implemented interfaces.
	 */
	private record Key(Class<?> base, Set<Class<?>> interfaces) {
		// Pure value.
	}

}
