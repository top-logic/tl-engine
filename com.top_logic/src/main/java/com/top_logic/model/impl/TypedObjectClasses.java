/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.MultipleParentClassLoader;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.binding.DynamicBinding;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;

/**
 * Classes for transient objects and form buffers that implement the generated Java interfaces of
 * their model type.
 *
 * <p>
 * A type with a generated Java interface (see {@link TLModelNamingConvention#interfaceName(TLType)})
 * is represented by persistent objects that implement this interface. A transient object or a form
 * buffer of that type is created from a fixed base class (e.g. {@link TransientTLObjectImpl}) that
 * knows nothing about the type. To offer the typed access of the generated interface on such
 * objects as well, {@link #classFor(Class, TLStructuredType)} delivers a subclass of the base class
 * that additionally implements the generated interfaces of the type and all of its
 * generalizations. This class is generated at runtime and cached.
 * </p>
 *
 * <p>
 * The default methods of the generated interfaces work through {@link TLObject#tValue} and
 * {@link TLObject#tUpdate} on the object itself, i.e. they read and write the values of the
 * transient object or buffer.
 * </p>
 *
 * <p>
 * A generated interface may extend framework interfaces (e.g. {@link Wrapper} and {@link TLScope})
 * whose methods do not work on an object without persistent storage. The generated class
 * implements these methods by fixed rules:
 * </p>
 * <ul>
 * <li>{@link Wrapper#tHandle()} narrows the result of the {@link TLObject#tHandle() handle} of the
 * base class to {@link KnowledgeObject}. An object without persistent counterpart fails with an
 * {@link UnsupportedOperationException} (see {@link TLObject#tHandle()}).</li>
 * <li>{@link Wrapper#getValue(String)} and {@link Wrapper#setValue(String, Object)} read and write
 * the value of the object itself through {@link TLObject#tValueByName(String)} and
 * {@link TLObject#tUpdateByName(String, Object)}, see {@link Methods}.</li>
 * <li>The methods of {@link TLScope} deliver the values of the persistent counterpart of the object,
 * and empty values for an object without one, see {@link Methods}.</li>
 * </ul>
 *
 * <p>
 * A generated interface with an abstract method that neither the base class, nor a default method,
 * nor one of these rules implements is not added to the generated class.
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
	 * Name of the method {@link Wrapper#tHandle()}.
	 */
	private static final String T_HANDLE = "tHandle";

	/**
	 * Implementations of the generated classes for methods of framework interfaces, indexed by the
	 * signature of the implemented method.
	 *
	 * <p>
	 * An implementation is created for the base class of the generated class.
	 * </p>
	 */
	private static final Map<Signature, Function<Class<?>, Implementation>> RULES = createRules();

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

	private static Map<Signature, Function<Class<?>, Implementation>> createRules() {
		Map<Signature, Function<Class<?>, Implementation>> result = new HashMap<>();
		result.put(Signature.of(method(Wrapper.class, T_HANDLE)), TypedObjectClasses::narrowedHandle);
		for (Method staticMethod : Methods.class.getDeclaredMethods()) {
			int modifiers = staticMethod.getModifiers();
			if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
				continue;
			}
			Class<?>[] parameterTypes = staticMethod.getParameterTypes();
			Signature signature = new Signature(staticMethod.getName(),
				List.of(parameterTypes).subList(1, parameterTypes.length), staticMethod.getReturnType());
			result.put(signature, base -> MethodCall.invoke(staticMethod).withThis().withAllArguments());
		}
		return result;
	}

	/**
	 * Implementation of {@link Wrapper#tHandle()} calling the handle method of the given base class
	 * and narrowing its result to {@link KnowledgeObject}.
	 */
	private static Implementation narrowedHandle(Class<?> base) {
		return MethodCall.invoke(method(base, T_HANDLE)).onSuper()
			.withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC);
	}

	private static Method method(Class<?> type, String name, Class<?>... parameterTypes) {
		try {
			return type.getMethod(name, parameterTypes);
		} catch (NoSuchMethodException ex) {
			throw new IllegalArgumentException("No method '" + name + "' in '" + type.getName() + "'.", ex);
		}
	}

	/**
	 * Creates a new instance of the class {@link #classFor(Class, TLStructuredType)} delivers.
	 *
	 * @param base
	 *        The base class of the object.
	 * @param type
	 *        The model type of the object, <code>null</code> for an object without type.
	 * @param parameterTypes
	 *        The parameter types of the constructor of the base class to call. A non-public
	 *        constructor of the base class must be accessible from this class.
	 * @param arguments
	 *        The arguments to pass to the constructor.
	 * @return The new object, an instance of the base class and of all generated interfaces of
	 *         the given type and its generalizations that the generated class can implement.
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
	 *        The model type of the object, <code>null</code> for an object without type.
	 * @return The given base class itself, if there is no type, or if neither the type nor any of
	 *         its generalizations has a generated Java interface that the base class does not
	 *         implement already and that a subclass can implement, otherwise a subclass of the base
	 *         class implementing all these interfaces.
	 */
	@SuppressWarnings("unchecked")
	public static <B extends TLObject> Class<? extends B> classFor(Class<B> base, TLStructuredType type) {
		if (type == null) {
			return base;
		}
		Set<Class<?>> interfaces = interfaces(base, type);
		if (interfaces.isEmpty()) {
			return base;
		}
		return (Class<? extends B>) CLASSES.computeIfAbsent(new Key(base, Set.copyOf(interfaces)),
			TypedObjectClasses::create);
	}

	/**
	 * The generated interfaces of the given type and its generalizations that the base class does
	 * not implement and that a subclass of the base class can implement, without interfaces that
	 * one of the others extends.
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
			if (intf != null && !intf.isAssignableFrom(base) && !candidates.contains(intf)
				&& implementable(base, intf)) {
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
	 * Whether every abstract method of the given interface is implemented by the given base class,
	 * by a default method of the interface, or by a rule of the generated class.
	 */
	private static boolean implementable(Class<?> base, Class<?> intf) {
		Set<Signature> concrete = new HashSet<>();
		addConcrete(concrete, base.getMethods());
		addConcrete(concrete, intf.getMethods());
		for (Method method : intf.getMethods()) {
			if (!Modifier.isAbstract(method.getModifiers())) {
				continue;
			}
			Signature signature = Signature.of(method);
			if (!concrete.contains(signature) && !RULES.containsKey(signature)) {
				return false;
			}
		}
		return true;
	}

	private static void addConcrete(Set<Signature> result, Method[] methods) {
		for (Method method : methods) {
			int modifiers = method.getModifiers();
			if (!Modifier.isAbstract(modifiers) && !Modifier.isStatic(modifiers)) {
				result.add(Signature.of(method));
			}
		}
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

		DynamicType.Builder<?> builder = new ByteBuddy()
			.with(new NamingStrategy.SuffixingRandom(CLASS_NAME_SUFFIX))
			.subclass(base, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
			.implement(interfaces);
		for (Signature signature : rulesFor(base, interfaces)) {
			// Defined explicitly with the signature of the interface method: Matching the inherited
			// method would find the method of the base class for a method with a narrowed return
			// type (e.g. tHandle()).
			builder = builder
				.defineMethod(signature.name(), signature.returnType(), Visibility.PUBLIC)
				.withParameters(signature.parameterTypes())
				.intercept(RULES.get(signature).apply(base));
		}
		return builder
			.make()
			.load(parent, ClassLoadingStrategy.Default.WRAPPER)
			.getLoaded();
	}

	/**
	 * The signatures of the {@link #RULES} that the generated class implements: methods of the
	 * given interfaces that no class of the base class hierarchy implements.
	 */
	private static List<Signature> rulesFor(Class<?> base, List<Class<?>> interfaces) {
		Set<Signature> implementedByClass = new HashSet<>();
		for (Method method : base.getMethods()) {
			if (!method.getDeclaringClass().isInterface() && !Modifier.isAbstract(method.getModifiers())) {
				implementedByClass.add(Signature.of(method));
			}
		}
		Set<Signature> result = new LinkedHashSet<>();
		for (Class<?> intf : interfaces) {
			for (Method method : intf.getMethods()) {
				Signature signature = Signature.of(method);
				if (RULES.containsKey(signature) && !implementedByClass.contains(signature)) {
					result.add(signature);
				}
			}
		}
		return new ArrayList<>(result);
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

	/**
	 * Signature of a method as seen by the virtual machine.
	 *
	 * @param name
	 *        The method name.
	 * @param parameterTypes
	 *        The parameter types.
	 * @param returnType
	 *        The return type.
	 */
	private record Signature(String name, List<Class<?>> parameterTypes, Class<?> returnType) {

		static Signature of(Method method) {
			return new Signature(method.getName(), List.of(method.getParameterTypes()), method.getReturnType());
		}

	}

	/**
	 * Implementations of framework interface methods in classes generated by
	 * {@link TypedObjectClasses}.
	 *
	 * <p>
	 * Each method implements the interface method of the same name. Its first parameter is the
	 * object the interface method is called on, followed by the parameters of the interface method.
	 * </p>
	 *
	 * <p>
	 * The persistent counterpart of an object that is not {@link TLObject#tTransient() transient}
	 * is the persistent object with its {@link TLObject#tId() identity}, e.g. the edited object of
	 * a form buffer. A transient object has none.
	 * </p>
	 */
	@FrameworkInternal
	public static final class Methods {

		private Methods() {
			// Static methods only.
		}

		/**
		 * Implementation of {@link ValueProvider#getValue(String)} reading the value of the object
		 * itself.
		 *
		 * @see TLObject#tValueByName(String)
		 */
		public static Object getValue(TLObject self, String attributeName) {
			return self.tValueByName(attributeName);
		}

		/**
		 * Implementation of {@link ValueProvider#setValue(String, Object)} writing the value of the
		 * object itself.
		 *
		 * @see TLObject#tUpdateByName(String, Object)
		 */
		public static void setValue(TLObject self, String attributeName, Object value) {
			self.tUpdateByName(attributeName, value);
		}

		/**
		 * Implementation of {@link TLScope#getClasses()} delivering the classes of the
		 * persistent counterpart.
		 */
		public static Collection<TLClass> getClasses(TLObject self) {
			TLScope scope = persistentScope(self);
			return scope == null ? List.of() : scope.getClasses();
		}

		/**
		 * Implementation of {@link TLScope#getAssociations()} delivering the associations of the
		 * persistent counterpart.
		 */
		public static Collection<TLAssociation> getAssociations(TLObject self) {
			TLScope scope = persistentScope(self);
			return scope == null ? List.of() : scope.getAssociations();
		}

		/**
		 * Implementation of {@link TLScope#getEnumerations()} delivering the enumerations of the
		 * persistent counterpart.
		 */
		public static Collection<TLEnumeration> getEnumerations(TLObject self) {
			TLScope scope = persistentScope(self);
			return scope == null ? List.of() : scope.getEnumerations();
		}

		/**
		 * Implementation of {@link TLScope#getDatatypes()} delivering the data types of the
		 * persistent counterpart.
		 */
		public static Collection<TLPrimitive> getDatatypes(TLObject self) {
			TLScope scope = persistentScope(self);
			return scope == null ? List.of() : scope.getDatatypes();
		}

		/**
		 * Implementation of {@link TLScope#getTypes()} delivering the types of the
		 * persistent counterpart.
		 */
		public static Collection<TLType> getTypes(TLObject self) {
			TLScope scope = persistentScope(self);
			return scope == null ? List.of() : scope.getTypes();
		}

		/**
		 * Implementation of {@link TLScope#getType(String)} looking up the type in the
		 * persistent counterpart.
		 */
		public static TLType getType(TLObject self, String name) {
			TLScope scope = persistentScope(self);
			return scope == null ? null : scope.getType(name);
		}

		/**
		 * The persistent object the given object stands for as {@link TLScope}.
		 *
		 * @return The persistent object with the {@link TLObject#tId() identity} of the given
		 *         object, or <code>null</code> if the given object is {@link TLObject#tTransient()
		 *         transient} or the persistent object is no {@link TLScope}.
		 */
		private static TLScope persistentScope(TLObject self) {
			if (self.tTransient()) {
				return null;
			}
			TLObject persistent = self.tHandle().getWrapper();
			return persistent instanceof TLScope scope ? scope : null;
		}

	}

}
