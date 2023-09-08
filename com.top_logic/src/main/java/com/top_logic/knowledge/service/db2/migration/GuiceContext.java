/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.InstantiationContextAdaptor;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.knowledge.service.db2.migration.rewriters.ProvidesAPI;

/**
 * {@link InstantiationContext} that enables injection using Guice.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GuiceContext extends InstantiationContextAdaptor implements Module {

	private Set<Object> _instances = new HashSet<>();

	private Map<Object, Object> _bindings = new HashMap<>();

	/**
	 * Creates a new {@link GuiceContext}.
	 */
	public GuiceContext(InstantiationContext context) {
		this(context, context);
	}

	/**
	 * Creates a new {@link GuiceContext}.
	 */
	public GuiceContext(Log log, InstantiationContext context) {
		super(log, context);
	}

	@Override
	public <T> T getInstance(InstantiationContext self, PolymorphicConfiguration<T> configuration) {
		if (configuration == null) {
			return null;
		}
		T instance = super.getInstance(self, configuration);
		boolean isNew = _instances.add(instance);
		if (isNew) {
			ProvidesAPI providesAnnotation = instance.getClass().getAnnotation(ProvidesAPI.class);
			if (providesAnnotation != null) {
				internalBind(providesAnnotation.value(), instance);
			}
		}
		return instance;
	}

	@Override
	public void configure(Binder binder) {
		for (Entry<Object, Object> entry : _bindings.entrySet()) {
			Object key = entry.getKey();

			if (key instanceof Class<?>) {
				Class<?> type = (Class<?>) key;
				@SuppressWarnings({ "unchecked", "rawtypes" })
				AnnotatedBindingBuilder<Object> bind = binder.bind((Class) type);
				bind.toInstance(entry.getValue());
			}
			else if (key instanceof Name) {
				Name name = (Name) key;
				@SuppressWarnings({ "unchecked", "rawtypes" })
				LinkedBindingBuilder<Object> bind =
					binder.bind((Class) name.getType()).annotatedWith(Names.named(name.getName()));
				bind.toInstance(entry.getValue());
			}

		}
	}

	/**
	 * Binds the given implementation for the given API.
	 * 
	 * <p>
	 * This method allows to define implementations for API's that were not created by this
	 * {@link GuiceContext}.
	 * </p>
	 */
	public <T> void bind(Class<T> api, T impl) {
		internalBind(api, impl);
	}

	/**
	 * Binds the given implementation to variables annotated with the given {@link Named name}.
	 * 
	 * <p>
	 * This method allows to define implementations for API's that were not created by this
	 * {@link GuiceContext}.
	 * </p>
	 */
	public void bind(Class<?> type, String name, Object impl) {
		_bindings.put(new Name(type, name), impl);
	}

	private static class Name {

		private final Class<?> _type;

		private final String _name;

		public Name(Class<?> type, String name) {
			_type = type;
			_name = name;
		}

		public Class<?> getType() {
			return _type;
		}

		public String getName() {
			return _name;
		}
	}

	private void internalBind(Class<?> api, Object impl) {
		Object clash = _bindings.put(api, impl);
		if (clash != null) {
			error("Duplicate binding for API '" + api.getName() + "': " + impl
				+ " vs. " + clash);
		}
	}

	/**
	 * Injects all necessary members to instances that where created by this context.
	 */
	public void inject() {
		Injector injector = Guice.createInjector(this);
		for (Object instance : _instances) {
			injector.injectMembers(instance);
		}
	}

}
