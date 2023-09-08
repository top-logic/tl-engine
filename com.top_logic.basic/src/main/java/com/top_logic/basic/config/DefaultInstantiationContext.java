/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Supplier;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.equal.EqualityRedirect;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.equal.ConfigEquality;

/**
 * Default {@link InstantiationContext} for instantiating configured objects
 * through a constructor adhering to the {@link DefaultConfigConstructorScheme}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultInstantiationContext extends AbstractInstantiationContext {

	private static final Object NULL = new NamedConstant("null");
	
	private final Map<EqualityRedirect<? super PolymorphicConfiguration<?>>, Object> sharedInstances =
		new HashMap<>();

	private Map<Ref, Object> _referencesById = new HashMap<>();

	private int _depth = 0;

	private int _level;

	/**
	 * Creates a new {@link DefaultInstantiationContext} using the {@link LogProtocol}.
	 * 
	 * @param logSource
	 *        See {@link LogProtocol#LogProtocol(Class)}.
	 */
	public DefaultInstantiationContext(Class<?> logSource) {
		this(new LogProtocol(logSource));
	}

	/**
	 * Creates a new {@link DefaultInstantiationContext} using the given
	 * {@link Protocol} for error reporting.
	 */
	public DefaultInstantiationContext(Log protocol) {
		super(protocol);
	}
	
	@Override
	protected <T> T lookupOrCreate(InstantiationContext self, PolymorphicConfiguration<T> configuration, Class<?> implementationClass)
			throws ConfigurationException {
		Factory factory = DefaultConfigConstructorScheme.getFactory(implementationClass);
		if (factory.isSharedInstance()) {
			synchronized (sharedInstances) {
				EqualityRedirect<PolymorphicConfiguration<?>> configWithEquality =
					wrapConfigWithEquality(configuration);

				@SuppressWarnings("unchecked")
				T sharedInstance = (T) sharedInstances.get(configWithEquality);
				if (sharedInstance != null) {
					return unwrapNull(sharedInstance);
				} else {
					// Fall through and allocate new instance.
				}
				
				T result = null;
				try {
					result = create(self, configuration, factory);
				} catch (ConfigurationException ex) {
					error("Configured singleton lookup or instantiation failed.", ex);
				} finally {
					sharedInstances.put(configWithEquality, wrapNull(result));
				}
				return result;
			}
		} else {
			return create(self, configuration, factory);
		}
	}

	private <T> T unwrapNull(T sharedInstance) {
		return sharedInstance != NULL ? sharedInstance : null;
	}

	private <T> Object wrapNull(T result) {
		return result != null ? result : NULL;
	}

	private <T> T create(InstantiationContext self, PolymorphicConfiguration<T> configuration, Factory factory)
			throws ConfigurationException {
		T ref;
		int createLevel;

		_level++;
		try {
			createLevel = _level;
			ref = invokeFactory(self, configuration, factory);
		} finally {
			_level--;
		}

		PropertyDescriptor idProperty = configuration.descriptor().getIdProperty();
		if (idProperty != null) {
			Class<?> idScope = configuration.descriptor().getIdScope();
			OuterRef inner = (OuterRef) _referencesById.remove(new OuterRef(idScope, createLevel, null));
			if (inner != null) {
				OuterRef outer = inner.resolve(createLevel, ref);
				if (outer != null) {
					// Remaining unresolved reference.
					_referencesById.put(outer, outer);
				}
			}

			Object id = configuration.value(idProperty);
			if (id != null) {
				fillReferenceValue(self, new IdRef(idScope, id), ref);
			}
		}

		checkReferenceResolution(self);

		return ref;
	}

	/**
	 * Fills the {@link ReferenceResolver} stored under the given id with the given value.
	 * 
	 * @param self
	 *        The outer {@link InstantiationContext} instance.
	 * @param id
	 *        The {@link IdRef} for the requested value.
	 * @param value
	 *        The requested value.
	 */
	protected <T> void fillReferenceValue(InstantiationContext self, IdRef id, T value) {
		Object oldValue = _referencesById.put(id, wrapValue(value));
		if (oldValue != null) {
			Object oldRef = unwrapValue(oldValue);
			if (oldRef instanceof ReferenceResolver<?>) {
				@SuppressWarnings("unchecked")
				ReferenceResolver<T> resolver = (ReferenceResolver<T>) oldRef;
				resolver.setReference(value);
			} else {
				self.error("Duplicate ID '" + id.id() + "': " + oldRef + " vs. " + value);
			}
		}
	}

	private <T> T invokeFactory(InstantiationContext self, PolymorphicConfiguration<T> configuration, Factory factory)
			throws ConfigurationException {
		incDepth();
		try {
			@SuppressWarnings("unchecked")
			T result = (T) factory.createInstance(self, configuration);
			return result;
		} finally {
			decDepth(self);
		}
	}

	@Override
	public <T> T deferredReferenceCheck(InstantiationContext self, Supplier<T> r) {
		incDepth();
		try {
			return r.get();
		} finally {
			decDepth(self);
			checkReferenceResolution(self);
		}
	}

	private void incDepth() {
		_depth++;
	}

	private void decDepth(InstantiationContext self) {
		_depth--;
	}

	private void checkReferenceResolution(InstantiationContext self) {
		if (_depth > 0) {
			return;
		}
		for (Entry<Ref, Object> entry : _referencesById.entrySet()) {
			if (entry.getValue() instanceof ReferenceResolver<?>) {
				self.error("Unresolved reference '" + entry.getKey() + "': " + entry.getValue());
			}
		}
		_referencesById.clear();
	}

	private static final class CombinedResolver<T> implements ReferenceResolver<Object> {
		private final ReferenceResolver<? super T> _resolver1;

		private final ReferenceResolver<? super T> _resolver2;

		public CombinedResolver(ReferenceResolver<? super T> ref, ReferenceResolver<? super T> setter) {
			_resolver1 = ref;
			_resolver2 = setter;
		}

		@Override
		public void setReference(Object value) {
			@SuppressWarnings("unchecked")
			T castValue = (T) value;
			_resolver1.setReference(castValue);
			_resolver2.setReference(castValue);
		}

		@Override
		public String toString() {
			return _resolver1 + ", " + _resolver2;
		}
	}

	/**
	 * General configuration reference.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	protected static class Ref {

		/**
		 * Creates a new configuration reference.
		 */
		public Ref() {
			super();
		}
	}

	/**
	 * ID of an object to resolve for a {@link ReferenceResolver}.
	 * 
	 * @see ReferenceResolver
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	protected static final class IdRef extends Ref {

		private final Class<?> _scope;

		private final Object _id;

		/**
		 * Creates a new {@link IdRef}.
		 * 
		 * @param scope
		 *        See {@link #scope()}.
		 * @param id
		 *        See {@link #id()}.
		 */
		public IdRef(Class<?> scope, Object id) {
			_scope = Objects.requireNonNull(scope);
			_id = Objects.requireNonNull(id);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + scope().hashCode();
			result = prime * result + id().hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			IdRef other = (IdRef) obj;
			if (!scope().equals(other.scope()))
				return false;
			if (!id().equals(other.id()))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return scope().getSimpleName() + ":" + id();
		}

		/**
		 * The scope in which {@link #id()} is an unique identifier.
		 */
		public Class<?> scope() {
			return _scope;
		}

		/**
		 * The unique identifier of the requested object.
		 */
		public Object id() {
			return _id;
		}

	}

	private static class OuterRef extends Ref {

		private final Class<?> _scope;

		private int _level;

		private ReferenceResolver<Object> _setter;

		private OuterRef _outer;

		@SuppressWarnings("unchecked")
		public OuterRef(Class<?> scope, int level, ReferenceResolver<?> setter) {
			_scope = scope;
			_level = level;
			_setter = (ReferenceResolver<Object>) setter;
		}

		public OuterRef resolve(int createLevel, Object ref) {
			if (createLevel < _level) {
				_setter.setReference(ref);
				if (_outer != null) {
					return _outer.resolve(createLevel, ref);
				} else {
					return null;
				}
			} else {
				// Not resolved.
				return this;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_scope == null) ? 0 : _scope.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OuterRef other = (OuterRef) obj;
			if (_scope == null) {
				if (other._scope != null)
					return false;
			} else if (!_scope.equals(other._scope))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "outer(" +_level +", "+ _scope.getSimpleName() + ")";
		}

		public void setOuter(OuterRef outer) {
			_outer = outer;
		}
	}

	private static class Value {

		private final Object _value;

		public Value(Object value) {
			_value = value;
		}

		Object getValue() {
			return _value;
		}

	}

	private EqualityRedirect<PolymorphicConfiguration<?>> wrapConfigWithEquality(
			PolymorphicConfiguration<?> configuration) {

		return new EqualityRedirect<>(configuration,
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED);
	}

	@Override
	public <T> void resolveReference(Object id, Class<T> scope, final ReferenceResolver<T> setter) {
		if (id == null) {
			// Null is no valid ID. This is a convenience for not caring for unassigned IDs in
			// application code by assigning `null` to `null` IDs.
			setter.setReference(null);
			return;
		}

		if (id == OUTER) {
			OuterRef inner = new OuterRef(scope, _level, setter);

			OuterRef outer = (OuterRef) _referencesById.get(inner);
			if (outer != null) {
				inner.setOuter(outer);
			}
			_referencesById.put(inner, inner);
		} else {
			IdRef key = new IdRef(scope, id);

			final Object value = _referencesById.get(key);
			if (value == null) {
				// Reference not yet available, keep resolver.
				_referencesById.put(key, setter);
			} else if (value instanceof ReferenceResolver<?>) {
				// Another resolver already waiting, combine resolvers.
				@SuppressWarnings("unchecked")
				ReferenceResolver<? super T> previousSetter = (ReferenceResolver<? super T>) value;
				_referencesById.put(key, new CombinedResolver<>(previousSetter, setter));
			} else {
				// Reference already present, directly resolve.
				@SuppressWarnings("unchecked")
				T ref = (T) unwrapValue(value);
				setter.setReference(ref);
			}
		}
	}

	private Object wrapValue(Object value) {
		return wrapResolver(value, wrapNull(value));
	}

	private Object wrapResolver(Object value, Object result) {
		if (result instanceof ReferenceResolver<?>) {
			return new Value(value);
		} else {
			return result;
		}
	}

	private Object unwrapValue(Object ref) {
		return unwrapNull(unwrapResolver(ref));
	}

	private Object unwrapResolver(Object ref) {
		if (ref instanceof Value) {
			return ((Value) ref).getValue();
		} else {
			return ref;
		}
	}

	/**
	 * Returns all known {@link ReferenceResolver}.
	 */
	protected final Map<Ref, Object> getReferenceResolvers() {
		return _referencesById;
	}

}
