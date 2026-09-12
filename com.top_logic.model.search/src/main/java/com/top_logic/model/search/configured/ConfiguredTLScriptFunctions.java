/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceExtensionPoint;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.treexf.TreeMaterializer.Factory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.values.edit.annotation.CollapseEntries;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.MethodResolver;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.search.expr.interpreter.UpdateSecurityVisitor;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.expr.trace.TracingAccessRewriter;
import com.top_logic.model.search.expr.visit.Copy;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link ConfiguredManagedClass} holding additional TL-Script functions that can be defined within
 * the application.
 * 
 * @param <C>
 *        Type of the configuration.
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceExtensionPoint(SearchBuilder.Module.class)
@Label("Configured TL-Script functions")
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
		@Key(ConfiguredScript.Config.NAME_ATTRIBUTE)
		@CollapseEntries
		Map<String, ConfiguredScript.Config> getScripts();

	}

	private Map<String, ConfiguredScript> _builders = Collections.emptyMap();

	private Map<String, QueryExecutor> _executors = Collections.emptyMap();

	/**
	 * Lazily filled map holding the security-disabled variant of the configured scripts.
	 *
	 * <p>
	 * A separate {@link QueryExecutor} is needed (instead of switching security off on the shared
	 * {@link #_executors secured executor}) because the compiled expression tree is shared by all
	 * callers and threads and its {@link com.top_logic.model.search.WithSecurityCheck security flag}
	 * must therefore not be mutated per call.
	 * </p>
	 */
	private final ConcurrentHashMap<String, QueryExecutor> _executorsNoSecurity = new ConcurrentHashMap<>();

	/**
	 * Map holding the tracing {@link SearchExpression} for the configured scripts.
	 * 
	 * <p>
	 * This map is filled lazy, because creating tracing search needs the {@link TLModel} and it may
	 * be that this service is started before the {@link ModelService}.
	 * </p>
	 */
	private final ConcurrentHashMap<String, SearchExpression> _tracingSearches = new ConcurrentHashMap<>();

	/**
	 * Like {@link #_tracingSearches}, but for the security-disabled variant of the scripts.
	 *
	 * @see #getTracingExecutor(String, boolean)
	 */
	private final ConcurrentHashMap<String, SearchExpression> _tracingSearchesNoSecurity = new ConcurrentHashMap<>();

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
		Map<String, QueryExecutor> executors = new HashMap<>();
		for (Entry<String, ConfiguredScript> builder : _builders.entrySet()) {
			executors.put(builder.getKey(), createExecutor(builder.getKey(), builder.getValue()));
		}
		_executors = executors;
	}

	private QueryExecutor createExecutor(String scriptName, ConfiguredScript script) {
		try {
			return script.createExecutor();
		} catch (RuntimeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_RESOLVING_SCRIPT__NAME.fill(scriptName), ex);
		}
	}

	private SearchExpression createTracingExecutor(String scriptName, QueryExecutor origExecutor) {
		try {
			TLModel model = tlModel();
			SearchExpression origSearch = origExecutor.getSearch();
			
			/* Copy original search to avoid changing original executor. */
			SearchExpression searchCopy = origSearch.visit(Copy.INSTANCE, null);
			SearchExpression tracingSearch = searchCopy.visit(TracingAccessRewriter.INSTANCE, null);
			return QueryExecutor.resolve(model, tracingSearch);
		} catch (RuntimeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_RESOLVING_SCRIPT__NAME.fill(scriptName), ex);
		}
	}

	private TLModel tlModel() {
		return ModelService.getApplicationModel();
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
		_executors.clear();
		_executorsNoSecurity.clear();
		_tracingSearches.clear();
		_tracingSearchesNoSecurity.clear();
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
		ConfiguredScript script = _builders.get(functionName);
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
			throw new UncheckedIOException(ex);
		}
		return Optional.of(sw.toString());
	}

	/**
	 * Determines the {@link QueryExecutor} for the configured function.
	 *
	 * @param usesSecurity
	 *        Whether the executor must apply the current user's access rights. The unsecured variant
	 *        is created lazily.
	 * @throws TopLogicException
	 *         iff there is no executor for the given name.
	 */
	QueryExecutor getExecutor(String scriptName, boolean usesSecurity) {
		if (usesSecurity) {
			QueryExecutor queryExecutor = _executors.get(scriptName);
			if (queryExecutor == null) {
				throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_SCRIPT__NAME.fill(scriptName));
			}
			return queryExecutor;
		}
		return getExecutorWithoutSecurity(scriptName);
	}

	private QueryExecutor getExecutorWithoutSecurity(String scriptName) {
		QueryExecutor existing = _executorsNoSecurity.get(scriptName);
		if (existing != null) {
			return existing;
		}
		ConfiguredScript script = _builders.get(scriptName);
		if (script == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_SCRIPT__NAME.fill(scriptName));
		}
		/* Compile a separate executor and switch security off on it. The shared secured executor
		 * must not be modified, since its expression tree is used concurrently. */
		QueryExecutor executor = createExecutor(scriptName, script);
		executor.disableSecurity();
		return MapUtil.putIfAbsent(_executorsNoSecurity, scriptName, executor);
	}

	/**
	 * Determines the tracing {@link SearchExpression} for the configured function.
	 *
	 * <p>
	 * Both variants are created lazily from the secured executor. For the security-disabled variant,
	 * security is switched off on the resolved tracing search afterwards.
	 * </p>
	 *
	 * @param usesSecurity
	 *        Whether the secured or the security-disabled variant must be traced.
	 * @throws TopLogicException
	 *         iff there is no executor for the given name.
	 */
	SearchExpression getTracingExecutor(String scriptName, boolean usesSecurity) {
		ConcurrentHashMap<String, SearchExpression> cache =
			usesSecurity ? _tracingSearches : _tracingSearchesNoSecurity;
		SearchExpression tracingSearch = cache.get(scriptName);
		if (tracingSearch != null) {
			return tracingSearch;
		}
		// Build the tracing search from the secured executor. For the unsecured variant, switch
		// security off explicitly on the resolved tracing search (consistent with the unsecured
		// executor and PathByExpression) instead of relying on the flag propagating through copy
		// and resolve.
		tracingSearch = createTracingExecutor(scriptName, getExecutor(scriptName, true));
		if (!usesSecurity) {
			UpdateSecurityVisitor.disableSecurity(tracingSearch);
		}
		return MapUtil.putIfAbsent(cache, scriptName, tracingSearch);
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
				return Optional.empty();
			}
			return getImplementationInstance().getDocumentation(context, functionName);
		}

	}

}
