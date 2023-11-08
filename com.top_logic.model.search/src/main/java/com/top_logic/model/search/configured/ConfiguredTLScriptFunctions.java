/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceExtensionPoint;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.treexf.TreeMaterializer.Factory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.search.expr.config.MethodResolver;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ConfiguredManagedClass} holding additional TL-Script functions that can be defined within
 * the application.
 * 
 * @param <C>
 *        Type of the configuration.
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceExtensionPoint(SearchBuilder.Module.class)
public class ConfiguredTLScriptFunctions<C extends ConfiguredTLScriptFunctions.Config<?>>
		extends ConfiguredManagedClass<C> implements MethodResolver {

	/**
	 * Configuration for a {@link ConfiguredTLScriptFunctions}.
	 * 
	 * @param <I>
	 *        Type of the concrete implementation class.
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends ConfiguredTLScriptFunctions<?>> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration name for the value of {@link #getScripts()}.
		 */
		String SCRIPTS = "scripts";

		/**
		 * Scripts that are available for the application.
		 * 
		 * <p>
		 * Each script define the name under which it can be executed. The names must be pairwise
		 * different.
		 * </p>
		 */
		@Name(SCRIPTS)
		@Key(ConfiguredMethodBuilder.Config.NAME_ATTRIBUTE)
		Map<String, ConfiguredMethodBuilder.Config<? extends ConfiguredMethodBuilder<?>>> getScripts();

	}

	private Map<String, ConfiguredMethodBuilder<?>> _builders = Collections.emptyMap();

	private Map<String, Factory> _factories = Collections.emptyMap();

	/**
	 * Creates a {@link ConfiguredTLScriptFunctions}.
	 */
	public ConfiguredTLScriptFunctions(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		_builders = TypedConfigUtil.createInstanceMap(getConfig().getScripts());
		initFactoriesFromMethodBuilders();
		for (Entry<String, ConfiguredMethodBuilder<?>> builder : _builders.entrySet()) {
			resolveExternals(builder);
		}
	}

	private void resolveExternals(Entry<String, ConfiguredMethodBuilder<?>> builder) {
		try {
			builder.getValue().resolveExternalRelations();
		} catch (RuntimeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_RESOLVING_SCRIPT__NAME.fill(builder.getKey()), ex);
		}
	}

	private void initFactoriesFromMethodBuilders() {
		_factories = _builders.entrySet()
			.stream()
			.collect(Collectors.toMap(Entry::getKey, e -> new SearchBuilder.BuilderFactory(e.getValue())));
	}

	@Override
	protected void shutDown() {
		_builders.clear();
		_factories.clear();
		super.shutDown();
	}

	@Override
	public Factory getFactory(Object type) {
		return _factories.get(type);
	}

	@Override
	public MethodBuilder<?> getMethodBuilder(String functionName) {
		return _builders.get(functionName);
	}

	@Override
	public Collection<String> getMethodNames() {
		return _builders.keySet();
	}

	@Override
	public Optional<String> getDocumentation(DisplayContext context, String functionName) {
		ConfiguredMethodBuilder<?> script = _builders.get(functionName);
		if (script == null) {
			return Optional.empty();
		}
		HTMLFragment documentation = script.documentation();
		if (documentation == null) {
			return Optional.empty();
		}
		StringWriter sw = new StringWriter();
		try (TagWriter out = new TagWriter(sw)) {
			documentation.write(context, out);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return Optional.of(sw.toString());
	}

	/**
	 * {@link TypedRuntimeModule} of the {@link ConfiguredTLScriptFunctions}.
	 */
	public static final class Module extends TypedRuntimeModule<ConfiguredTLScriptFunctions<?>>
			implements MethodResolver {

		/**
		 * Singleton module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<ConfiguredTLScriptFunctions<?>> getImplementation() {
			return cast(ConfiguredTLScriptFunctions.class);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Class<ConfiguredTLScriptFunctions<?>> cast(Class implClass) {
			return implClass;
		}

		@Override
		public Collection<String> getMethodNames() {
			if (!isActive()) {
				return Collections.emptyList();
			}
			return getImplementationInstance().getMethodNames();
		}

		@Override
		public Factory getFactory(Object type) {
			if (!isActive()) {
				return null;
			}
			return getImplementationInstance().getFactory(type);
		}

		@Override
		public MethodBuilder<?> getMethodBuilder(String functionName) {
			if (!isActive()) {
				return null;
			}
			return getImplementationInstance().getMethodBuilder(functionName);
		}

		@Override
		public Optional<String> getDocumentation(DisplayContext context, String functionName) {
			if (!isActive()) {
				return null;
			}
			return getImplementationInstance().getDocumentation(context, functionName);
		}

	}

}
