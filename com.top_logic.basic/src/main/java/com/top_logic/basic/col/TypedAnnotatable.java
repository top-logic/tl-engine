/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * Container of typed values for {@link Property properties}.
 * 
 * @see TypedAnnotationContainer Default implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TypedAnnotatable {

	/**
	 * Creates a {@link Property} with <code>null</code> default.
	 * 
	 * @param type
	 *        The value type of the property.
	 * @param name
	 *        The name of the property for debugging.
	 * 
	 * @return The new {@link Property}.
	 * 
	 * @see #property(Class, String, Object)
	 */
	public static <T> Property<T> property(Class<T> type, String name) {
		return property(type, name, (T) null);
	}

	/**
	 * Creates a property with the given default value.
	 * 
	 * @param type
	 *        The value type of the property.
	 * @param name
	 *        The name of the property for debugging.
	 * @param defaultValue
	 *        The property value used, if the property is not explicitly set.
	 * 
	 * @return The new {@link Property}.
	 */
	public static <T> Property<T> property(Class<T> type, String name, T defaultValue) {
		return new Property<>(type, name, defaultValue);
	}

	/**
	 * Creates a property with the given default value computation algorithm.
	 * 
	 * @param type
	 *        The value type of the property.
	 * @param name
	 *        The name of the property for debugging.
	 * @param getDefault
	 *        The algorithm creating the value, if the property is not explicitly set.
	 * 
	 * @return The new {@link Property}.
	 */
	public static <T> Property<T> propertyDynamic(Class<T> type, String name,
			Function<TypedAnnotatable, ? extends T> getDefault) {
		return new Property.Dynamic<>(type, name, getDefault);
	}

	/**
	 * Creates a {@link List}-valued {@link Property} with {@link Collections#EMPTY_LIST} as default
	 * value.
	 * 
	 * @see #property(Class, String, Object)
	 * @see #mkList(Property)
	 */
	@SuppressWarnings("unchecked")
	public static <T, L extends List<? extends T>> Property<L> propertyList(String name) {
		return propertyRaw(List.class, name, Collections.emptyList());
	}

	/**
	 * Creates a {@link Set}-valued {@link Property} with {@link Collections#EMPTY_SET} as default
	 * value.
	 * 
	 * @see #property(Class, String, Object)
	 * @see #mkSet(Property)
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends Set<? extends T>> Property<S> propertySet(String name) {
		return propertyRaw(Set.class, name, Collections.emptySet());
	}

	/**
	 * Creates a {@link Map}-valued {@link Property} with {@link Collections#EMPTY_MAP} as default
	 * value.
	 * 
	 * @see #property(Class, String, Object)
	 * @see #mkMap(Property)
	 */
	@SuppressWarnings("unchecked")
	public static <K, V, M extends Map<? extends K, ? extends V>> Property<M> propertyMap(String name) {
		return propertyRaw(Map.class, name, Collections.emptyMap());
	}

	/**
	 * Creates a {@link Property} with raw type.
	 * 
	 * <p>
	 * Using this API is required, if the property type is a parameterized type, since a class
	 * literal is of raw type.
	 * </p>
	 * 
	 * @see #property(Class, String, Object)
	 */
	@SuppressWarnings("rawtypes")
	public static <T> Property propertyRaw(Class<T> type, String name, T defaultValue) {
		return property(type, name, defaultValue);
	}

	/**
	 * Creates a {@link Property} with raw type.
	 * 
	 * <p>
	 * Using this API is required, if the property type is a parameterized type, since a class
	 * literal is of raw type.
	 * </p>
	 * 
	 * @see #property(Class, String)
	 */
	@SuppressWarnings("rawtypes")
	public static Property propertyRaw(Class<?> type, String name) {
		return property(type, name);
	}

	/**
	 * Property of a {@link TypedAnnotatable}.
	 * 
	 * <p>
	 * Like a {@link NamedConstant} with additional type information.
	 * </p>
	 * 
	 * <p>
	 * Use factory methods like {@link TypedAnnotatable#property(Class, String)} for instantiation.
	 * </p>
	 */
	public class Property<T> {
		private final Class<T> _type;

		private final String _name;

		private final T _defaultValue;

		/** Constant as representation for <code>null</code> in the values map. */
		static final NamedConstant NULL = new NamedConstant("<null>");

		/**
		 * Creates a {@link Property}.
		 * 
		 * @param type
		 *        See {@link #getType()}.
		 * @param name
		 *        See {@link #getName()}.
		 * @param defaultValue
		 *        See {@link #getDefaultValue(TypedAnnotatable)}.
		 */
		Property(Class<T> type, String name, T defaultValue) {
			_type = type;
			_name = name;
			_defaultValue = defaultValue;
		}

		/**
		 * {@link Property} that computes its default value dynamically based on the owning object.
		 */
		static class Dynamic<T> extends Property<T> {
			private final Function<TypedAnnotatable, ? extends T> _getDefault;

			public Dynamic(Class<T> type, String name, Function<TypedAnnotatable, ? extends T> getDefault) {
				super(type, name, null);
				_getDefault = getDefault;
			}

			@Override
			public T getDefaultValue(TypedAnnotatable self) {
				return _getDefault.apply(self);
			}
		}

		/**
		 * The type of the value that can be {@link TypedAnnotatable#set(Property, Object) assigned}
		 * to this property.
		 */
		public Class<T> getType() {
			return _type;
		}

		/**
		 * The name of this property.
		 */
		public String getName() {
			return _name;
		}

		/**
		 * The value to use, if the property is not {@link TypedAnnotatable#set(Property, Object)
		 * set}.
		 * 
		 * @param self
		 *        The object from which the property was read.
		 */
		public T getDefaultValue(TypedAnnotatable self) {
			return _defaultValue;
		}

		@Override
		public String toString() {
			return getName() + " : " + getType().getSimpleName();
		}

		/**
		 * Converts the raw value to the application value for returning from
		 * {@link TypedAnnotatable#get(Property)}.
		 * 
		 * @param self
		 *        The {@link TypedAnnotatable} from which the property is requested.
		 * 
		 * @see #internalize(Object)
		 */
		@FrameworkInternal
		public T externalize(TypedAnnotatable self, Object value) {
			if (value == null) {
				return getDefaultValue(self);
			}
		
			if (value == Property.NULL) {
				return null;
			}
		
			@SuppressWarnings("unchecked")
			T regularValue = (T) value;
		
			return regularValue;
		}

		/**
		 * Converts the application value for this property to an internal representation.
		 */
		@FrameworkInternal
		public Object internalize(T externalValue) {
			if (externalValue == null) {
				return Property.NULL;
			} else {
				return getType().cast(externalValue);
			}
		}
	}

	/**
	 * Retrieves the value of the given {@link Property}.
	 * 
	 * @param property
	 *        The {@link Property} to retrieve the value for.
	 */
	<T> T get(Property<T> property);

	/**
	 * Assigns the given value to the given property in the context of this {@link TypedAnnotatable}
	 * .
	 * 
	 * @param property
	 *        The {@link Property} to assign a value to.
	 * @param value
	 *        The new value for the given property.
	 * @return The value {@link #get(Property)} would have returned just before the call.
	 */
	<T> T set(Property<T> property, T value);

	/**
	 * Whether the given property has an explicit value assigned.
	 * 
	 * @param property
	 *        The {@link Property} to check value assignment for.
	 * @return Whether {@link #set(Property, Object)} has been called for the given property (after
	 *         a potential call to {@link #reset(Property)}).
	 */
	boolean isSet(Property<?> property);

	/**
	 * If the specified property is not already associated with a value associates it with the given
	 * value and returns {@code null}, else returns the current value.
	 *
	 * @implSpec The default implementation is equivalent to :
	 *           <pre>
	 * {@code
	 *  if (isSet(property)) {
	 *   return get(property);
	 *  }
	 *  set(property, value);
	 *  return null;
	 * }
	 *           </pre>
	 * 
	 * @param property
	 *        {@link Property} with which the specified value is to be associated.
	 * @param value
	 *        Value to be associated with the specified {@link Property}.
	 * 
	 * @return the previous value associated with the specified property, or {@code null} if there
	 *         was no mapping for the key. (A {@code null} return can also indicate that the
	 *         {@link TypedAnnotatable} previously associated {@code null} with the property.)
	 *         Assigns the given value for the property iff there is not already a value assigned.
	 */
	default <T> T setIfAbsent(Property<T> property, T value) {
		if (isSet(property)) {
			return get(property);
		}
		set(property, value);
		return null;
	}

	/**
	 * Resets the given property's {@link #get(Property) value} to its
	 * {@link Property#getDefaultValue(TypedAnnotatable) default value}.
	 * 
	 * @param property
	 *        The {@link Property} which is reset.
	 * @return The last value of the property (value of {@link #get(Property)} before the call).
	 */
	<T> T reset(Property<T> property);

	/**
	 * Retrieves the value of the given {@link List}-valued {@link Property}.
	 * 
	 * <p>
	 * If the {@link Property} does not exist, it is allocated with an empty mutable list.
	 * </p>
	 * 
	 * @param property
	 *        The {@link Property} to retrieve the value for.
	 * @return See {@link #get(Property)}.
	 * 
	 * @see #propertyList(String)
	 */
	default <T> List<T> mkList(Property<List<T>> property) {
		List<T> result = get(property);
		if (result == Collections.EMPTY_LIST) {
			result = new ArrayList<>();
			set(property, result);
		}
		return result;
	}

	/**
	 * Retrieves the value of the given {@link Set}-valued {@link Property}.
	 * 
	 * <p>
	 * If the {@link Property} does not exist, it is allocated with an empty mutable set.
	 * </p>
	 * 
	 * @param property
	 *        The {@link Property} to retrieve the value for.
	 * @return See {@link #get(Property)}.
	 * 
	 * @see #propertySet(String)
	 */
	default <T> Set<T> mkSet(Property<Set<T>> property) {
		Set<T> result = get(property);
		if (result == Collections.EMPTY_SET) {
			result = new HashSet<>();
			set(property, result);
		}
		return result;
	}

	/**
	 * Retrieves the value of the given {@link Map}-valued {@link Property}.
	 * 
	 * <p>
	 * If the {@link Property} does not exist, it is allocated with an empty mutable map.
	 * </p>
	 * 
	 * @param property
	 *        The {@link Property} to retrieve the value for.
	 * @return See {@link #get(Property)}.
	 * 
	 * @see #propertyMap(String)
	 */
	default <K, V> Map<K, V> mkMap(Property<Map<K, V>> property) {
		Map<K, V> result = get(property);
		if (result == Collections.EMPTY_MAP) {
			result = new HashMap<>();
			set(property, result);
		}
		return result;
	}

}
