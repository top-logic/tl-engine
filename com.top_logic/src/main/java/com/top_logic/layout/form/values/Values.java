/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.PropertyValue;
import com.top_logic.basic.config.misc.PropertyValueImpl;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.util.Utils;

/**
 * Utility for constructing meshes of interdependent {@link Value}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Values {

	private static final class Literal<T> extends AbstractValue<T> {
		private final T _literal;

		public Literal(T literal) {
			_literal = literal;
		}

		@Override
		public T get() {
			return _literal;
		}

		@Override
		public ListenerBinding addListener(Listener listener) {
			return ListenerBinding.NONE;
		}
	}

	private static abstract class CachedValue<T> extends ObservableValue<T> {

		private boolean _hasValue;

		private T _value;

		CachedValue() {
			super();
		}

		@Override
		public T get() {
			return internalGet();
		}

		protected T internalGet() {
			return _value;
		}

		protected final boolean internalHasValue() {
			return _hasValue;
		}

		protected void internalSetValue(T newValue) {
			_value = newValue;
			_hasValue = true;
		}

		protected void internalReset() {
			_value = null;
			_hasValue = false;
		}

		protected void internalUpdateValue(T newValue) {
			if (!Utils.equals(newValue, internalGet())) {
				internalSetValue(newValue);
				handleChanged();
			}
		}

	}

	/**
	 * {@link Value} that can directly be modified.
	 */
	static class ModifiableCachedValue<T> extends CachedValue<T> implements ModifiableValue<T> {

		ModifiableCachedValue() {
		}

		@Override
		public void set(T newValue) {
			internalUpdateValue(newValue);
		}

	}

	public static class DeferredValue<T> extends CachedValue<T> {

		private final Value<? extends T> _input;

		private boolean _online;

		private T _cache;

		DeferredValue(Value<? extends T> input) {
			_input = input;

			_input.addListener(new Listener() {
				@Override
				public void handleChange(Value<?> sender) {
					_cache = _input.get();
					if (_online) {
						internalUpdateValue(_cache);
					}
				}
			});
		}

		public void setOnline(boolean online) {
			if (online != _online) {
				_online = online;

				if (online) {
					internalUpdateValue(_cache);
				}
			}
		}

	}

	public static final class LazyValue<T> extends CachedValue<T> {
		private final Provider<T> _provider;

		private LazyValue(Provider<T> provider) {
			_provider = provider;
		}

		@Override
		public T get() {
			ensureValue();
			return super.get();
		}

		private void ensureValue() {
			if (!internalHasValue()) {
				internalSetValue(_provider.get());
			}
		}

		public void update() {
			if (!internalHasValue()) {
				return;
			}

			internalUpdateValue(_provider.get());
		}
	}

	private static final class PathStep<T> extends CachedValue<T> {
	
		private final Value<? extends ConfigurationItem> _base;
	
		private final PropertyDescriptor _property;
	
		ConfigurationItem _model;

		/**
		 * The property of the runtime type of the {@link #_model}. The field {@link #_property} is
		 * only the property of the static type.
		 */
		PropertyDescriptor _dynamicProperty;

		protected ConfigurationListener _onModelChange = new ConfigurationListener() {
			@Override
			public void onChange(ConfigurationChange change) {
				updateValue();
				handleChanged();
			}
		};
	
		private Listener _onBaseChange = new Listener() {
			@Override
			public void handleChange(Value<?> sender) {
				_dynamicProperty = null;
				if (_model != null) {
					_model.removeConfigurationListener(getProperty(), _onModelChange);
					_model = null;
				}
	
				internalReset();
				handleChanged();
			}
		};
	
		private PathStep(Value<? extends ConfigurationItem> base, PropertyDescriptor property) {
			_base = base;
			_property = property;
	
			_base.addListener(_onBaseChange);
		}
	
		@Override
		public T get() {
			if (!internalHasValue()) {
				_model = _base.get();
				if (_model != null) {
					updateValue();
					_model.addConfigurationListener(getProperty(), _onModelChange);
				} else {
					internalSetValue(null);
				}
			}
			return super.get();
		}
	
		void updateValue() {
			boolean hasValue = _model.valueSet(getProperty()) || !getProperty().isMandatory();
			T newValue = hasValue ? (T) _model.value(getProperty()) : null;
			internalSetValue(newValue);
		}

		PropertyDescriptor getProperty() {
			if (_model == null) {
				return _property;
			}
			if (_dynamicProperty == null) {
				_dynamicProperty = _model.descriptor().getProperty(_property.getPropertyName());
			}
			return _dynamicProperty;
		}

	}

	private static class ConfigurationItemValue<T extends ConfigurationItem> extends AbstractValue<T> {
	
		private final T _model;

		private final PropertyDescriptor[] _properties;
	
		public ConfigurationItemValue(T model, PropertyDescriptor[] properties) {
			_model = model;
			_properties = properties;
		}
	
		@Override
		public T get() {
			return _model;
		}
	
		@Override
		public ListenerBinding addListener(final Listener listener) {
			ConfigurationListener adapter = new ConfigurationListener() {
				@Override
				public void onChange(ConfigurationChange change) {
					listener.handleChange(ConfigurationItemValue.this);
				}
			};

			if (_properties == null || _properties.length == 0) {
				_model.addConfigurationListener(null, adapter);
				return () -> _model.removeConfigurationListener(null, adapter);
			} else {
				for (PropertyDescriptor property : _properties) {
					_model.addConfigurationListener(property, adapter);
				}
				return () -> {
					for (PropertyDescriptor property : _properties) {
						_model.removeConfigurationListener(property, adapter);
					}
				};
			}
		}
	}

	private static final class IfElse<T> implements Mapping<Boolean, T> {
		private final Value<T> _v2;
	
		private final Value<T> _v1;
	
		IfElse(Value<T> v2, Value<T> v1) {
			_v2 = v2;
			_v1 = v1;
		}
	
		@Override
		public T map(Boolean input) {
			if (input != null && input.booleanValue()) {
				return _v1.get();
			} else {
				return _v2.get();
			}
		}
	}

	private static final class IfElseLiteral<T> implements Mapping<Boolean, T> {
		private final T _v2;

		private final T _v1;

		IfElseLiteral(T v2, T v1) {
			_v2 = v2;
			_v1 = v1;
		}

		@Override
		public T map(Boolean input) {
			if (input != null && input.booleanValue()) {
				return _v1;
			} else {
				return _v2;
			}
		}
	}

	private static final Mapping<? super Object, ? extends Boolean> IS_NULL = new Mapping<>() {
		@Override
		public Boolean map(Object input) {
			return input == null;
		}
	};

	private static final Mapping<? super Object, ? extends Boolean> NOT_NULL = new Mapping<>() {
		@Override
		public Boolean map(Object input) {
			return input != null;
		}
	};

	private static final Mapping<Object, ? extends Boolean> IS_EMPTY =
		new Mapping<>() {
			@Override
			public Boolean map(Object input) {
				return input == null || (input instanceof Collection<?> && ((Collection<?>) input).isEmpty());
			}
		};

	private static final Mapping<Object, ? extends Boolean> NOT_EMPTY =
		new Mapping<>() {
			@Override
			public Boolean map(Object input) {
				return !(input == null || (input instanceof Collection<?> && ((Collection<?>) input).isEmpty()));
			}
		};

	private static final Mapping<? super Iterable<?>, Object> FIRST =
		new Mapping<>() {
			@Override
			public Object map(Iterable<?> input) {
				if (input == null) {
					return null;
				}
				Iterator<?> iterator = input.iterator();
				if (iterator.hasNext()) {
					return iterator.next();
				}
				return null;
			}
		};

	/**
	 * Computed {@link Value} that is derived by a one-argument {@link Mapping function} from
	 * another value.
	 * 
	 * @param arg
	 *        The argument {@link Value}.
	 * @param mapping
	 *        The function transforming the argument value into the result.
	 * @return The result {@link Value}.
	 */
	public static <A, T> Value<T> map(final Value<? extends A> arg, final Mapping<? super A, ? extends T> mapping) {
		return new MappedValue<A, T>(arg, mapping);
	}

	/**
	 * Computed {@link Value} that is derived by a {@link Function2 two-argument function} from two
	 * other values.
	 * 
	 * @param arg1
	 *        The first argument {@link Value}.
	 * @param arg2
	 *        The second argument {@link Value}.
	 * @param function
	 *        The function transforming the argument values into the result.
	 * @return The result {@link Value}.
	 */
	public static <A1, A2, T> Value<T> map(Value<? extends A1> arg1, Value<? extends A2> arg2,
			final Function2<? super A1, ? super A2, ? extends T> function) {
		return new MappedValue2<A1, A2, T>(arg1, arg2, function);
	}

	/**
	 * Computed {@link Value} that is derived by a {@link Function3 three-argument function} from
	 * three other values.
	 * 
	 * @param arg1
	 *        The first argument {@link Value}.
	 * @param arg2
	 *        The second argument {@link Value}.
	 * @param arg3
	 *        The third argument {@link Value}.
	 * @param function
	 *        The function transforming the argument values into the result.
	 * @return The result {@link Value}.
	 */
	public static <A1, A2, A3, T> Value<T> map(Value<? extends A1> arg1, Value<? extends A2> arg2,
			Value<? extends A3> arg3, final Function3<? super A1, ? super A2, ? super A3, ? extends T> function) {
		return new MappedValue3<A1, A2, A3, T>(arg1, arg2, arg3, function);
	}

	public static void onChange(Listener listener, Value<?>... values) {
		for (Value<?> value : values) {
			value.addListener(listener);
		}
	}

	public static <T> Value<T> literal(final T literal) {
		return new Literal<>(literal);
	}

	public static <T extends ConfigurationItem> Value<T> item(T model, String... properties) {
		return new ConfigurationItemValue<>(model, resolve(model, properties));
	}

	private static PropertyDescriptor[] resolve(ConfigurationItem model, String... propertyNames) {
		if (propertyNames == null || propertyNames.length == 0) {
			return null;
		} else {
			ConfigurationDescriptor descriptor = model.descriptor();

			PropertyDescriptor[] result = new PropertyDescriptor[propertyNames.length];
			for (int n = 0, cnt = propertyNames.length; n < cnt; n++) {
				result[n] = descriptor.getProperty(propertyNames[n]);
			}
			return result;
		}
	}

	public static <T> Value<T> configurationValue(ConfigurationItem model, String... propertyNames) {
		Value<?> result;
		if (propertyNames == null || propertyNames.length == 0) {
			result = literal(model);
		} else {
			ConfigurationDescriptor descriptor = model.descriptor();
			PropertyDescriptor property = descriptor.getProperty(propertyNames[0]);
			result = configurationValue(model, property);

			for (int n = 1, cnt = propertyNames.length; n < cnt; n++) {
				descriptor = TypedConfiguration.getConfigurationDescriptor(property.getType());
				String propertyName = propertyNames[n];
				property = descriptor.getProperty(propertyName);

				result = new PathStep<>((Value) result, property);
			}
		}

		return (Value<T>) result;
	}

	public static <T> ConfigurationValue<T> configurationValue(ConfigurationItem model, String propertyName) {
		return configurationValue(model, model.descriptor().getProperty(propertyName));
	}

	public static <T> ConfigurationValue<T> configurationValue(ValueModel valueModel) {
		return configurationValue(valueModel.getModel(), valueModel.getProperty());
	}

	public static <T> ConfigurationValue<T> configurationValue(ConfigurationItem model, PropertyDescriptor property) {
		return new ConfigurationValue<>(stablePropertyValue(model, property));
	}

	/**
	 * Creates a {@link PropertyValue} accessing the given property of the given model.
	 * 
	 * <p>
	 * When accessing collection properties, the result is stabilized in a way that changes to the
	 * collection value do not affect the given model element.
	 * </p>
	 * 
	 * @param model
	 *        The model element to access.
	 * @param property
	 *        The property to retrieve its value.
	 * @return An accessor for the given properties value.
	 */
	public static PropertyValue stablePropertyValue(ConfigurationItem model, PropertyDescriptor property) {
		switch (property.kind()) {
			case ARRAY:
				return new PropertyValueImpl(model, property) {
					@Override
					public Object get() {
						return PropertyDescriptorImpl.arrayAsList(super.get());
					}
				};
			case LIST:
				return new PropertyValueImpl(model, property) {
					@Override
					public Object get() {
						return new ArrayList<>((Collection<?>) super.get());
					}
				};
			case MAP:
				return new PropertyValueImpl(model, property) {
					@Override
					public Object get() {
						return new HashMap<>((Map<?, ?>) super.get());
					}
				};
			default:
				return new PropertyValueImpl(model, property);
		}
	}

	public static <S, T> ListenerBinding linkStorage(final ModifiableValue<S> editor, final ModifiableValue<T> storage,
			final Mapping<? super S, ? extends T> converter) {
		// Initialization.
		initStorage(editor, storage, converter);

		return editor.addListener(new Listener() {
			@Override
			public void handleChange(Value<?> sender) {
				storage.set(converter.map(editor.get()));
			}
		});
	}

	private static <T, S> void initStorage(final ModifiableValue<S> editor, final ModifiableValue<T> storage,
			final Mapping<? super S, ? extends T> converter) {
		/* The value of the field must not be transferred unconditionally to the configuration
		 * element: The field is created from the ConfiugrationItem, so the value should be the
		 * same. If the value of the field is explicitly copied to the configuration element, then
		 * the value for the property is marked as "valueSet". In this case, no value
		 * initializations (executed later) are applied. */
		T editorValue = converter.map(editor.get());
		T storageValue = storage.get();
		if (Utils.equals(editorValue, storageValue)) {
			return;
		}
		if (isFuzzyEmpty(editorValue) && isFuzzyEmpty(storageValue)) {
			/* Treat values that are seen as empty as equal: This is necessary because normalization
			 * processes may switch null to empty string (when the field displays strings) or empty
			 * collections (when the field displays collections). */
			return;
		}
		/* Value has changed. This may happen when the value set from the item to the field changes
		 * the value. */
		storage.set(editorValue);
	}

	private static boolean isFuzzyEmpty(Object val) {
		if (val == null) {
			return true;
		}
		if (val instanceof CharSequence) {
			return ((CharSequence) val).length() == 0;
		}
		if (val instanceof Collection<?>) {
			return ((Collection<?>) val).isEmpty();
		}
		if (val instanceof Map<?, ?>) {
			return ((Map<?, ?>) val).isEmpty();
		}
		if (val.getClass().isArray()) {
			return Array.getLength(val) == 0;
		}
		return false;
	}

	public static <T> LazyValue<T> lazy(Provider<T> provider) {
		return new LazyValue<>(provider);
	}

	public static <T> ModifiableValue<T> modifiable() {
		return new ModifiableCachedValue<>();
	}

	public static <T> DeferredValue<T> defer(Value<? extends T> input) {
		return new DeferredValue<>(input);
	}

	public static <T> Value<Collection<T>> setFilter(Value<? extends Collection<T>> input,
			final Filter<? super T> elementFilter) {
		return filter(input, new Filter<Collection<T>>() {
			@Override
			public boolean accept(Collection<T> anObject) {
				for (T element : anObject) {
					if (!elementFilter.accept(element)) {
						return false;
					}
				}
				return true;
			}
		}, Collections.<T> emptySet());
	}

	public static <T> Value<T> filter(Value<? extends Object> input, final Class<? extends T> type) {
		@SuppressWarnings("unchecked")
		Value<T> result = (Value<T>) filter(input, FilterFactory.createClassFilter(type));
		return result;
	}

	public static <T> Value<T> filter(Value<? extends T> input, final Filter<? super T> filter) {
		return filter(input, filter, null);
	}

	public static <T> Value<List<T>> list(Value<? extends Collection<? extends T>> input) {
		return list(input, ComparableComparator.INSTANCE);
	}

	public static <T> Value<List<T>> list(Value<? extends Collection<? extends T>> collection,
			final Comparator<? super T> comparator) {
		return map(collection, new Mapping<Collection<? extends T>, List<T>>() {
			@Override
			public List<T> map(Collection<? extends T> input) {
				ArrayList<T> result = new ArrayList<>(input);
				Collections.sort(result, comparator);
				return result;
			}
		});
	}

	public static <T> Value<Set<T>> singleton(Value<? extends T> input) {
		return map(input, new Mapping<T, Set<T>>() {
			@Override
			public Set<T> map(T input) {
				return CollectionUtilShared.singletonOrEmptySet(input);
			}
		});
	}

	public static <T> Value<T> filter(Value<? extends T> input, final Filter<? super T> filter, final T defaultValue) {
		return map(input, new Mapping<T, T>() {
			@Override
			public T map(T input) {
				if (filter.accept(input)) {
					return input;
				} else {
					return defaultValue;
				}
			}
		});
	}

	public static Value<Boolean> eq(final Value<?> value1, final Value<?> value2) {
		return map(value1, value2, new Function2<Object, Object, Boolean>() {
			@Override
			public Boolean eval(Object arg1, Object arg2) {
				return Utils.equals(arg1, arg2);
			}
		});
	}

	public static Value<Boolean> not(final Value<Boolean> value) {
		return map(value, new Mapping<Boolean, Boolean>() {
			@Override
			public Boolean map(Boolean arg) {
				return !Utils.isTrue(arg);
			}
		});
	}

	public static Value<Boolean> and(Value<Boolean> v1, Value<Boolean> v2) {
		return map(v1, v2, new Function2<Boolean, Boolean, Boolean>() {
			@Override
			public Boolean eval(Boolean arg1, Boolean arg2) {
				return Utils.isTrue(arg1) && Utils.isTrue(arg2);
			}
		});
	}

	public static Value<Boolean> or(Value<Boolean> v1, Value<Boolean> v2) {
		return map(v1, v2, new Function2<Boolean, Boolean, Boolean>() {
			@Override
			public Boolean eval(Boolean arg1, Boolean arg2) {
				return Utils.isTrue(arg1) || Utils.isTrue(arg2);
			}
		});
	}

	public static Value<Boolean> isNull(final Value<?> value) {
		return map(value, IS_NULL);
	}

	public static Value<Boolean> notNull(final Value<?> value) {
		return map(value, NOT_NULL);
	}

	public static Value<Boolean> isEmpty(final Value<?> value) {
		return map(value, IS_EMPTY);
	}

	public static Value<Boolean> notEmpty(final Value<?> value) {
		return map(value, NOT_EMPTY);
	}

	public static <T> Value<T> first(final Value<? extends Iterable<? extends T>> value) {
		return (Value<T>) map(value, FIRST);
	}

	public static <T> Value<T> ifElse(Value<Boolean> empty, final Value<T> v1, final Value<T> v2) {
		return map(empty, new IfElse<>(v2, v1));
	}

	public static <T> Value<T> ifElse(Value<Boolean> empty, final T v1, T v2) {
		return map(empty, new IfElseLiteral<>(v2, v1));
	}

}
