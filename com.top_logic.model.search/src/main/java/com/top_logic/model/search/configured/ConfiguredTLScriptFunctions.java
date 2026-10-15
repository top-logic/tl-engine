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
import java.util.function.Function;
import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.ServiceExtensionPoint;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.treexf.TreeMaterializer.Factory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.values.edit.annotation.CollapseEntries;
import com.top_logic.model.search.expr.config.MethodResolver;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ConfiguredManagedClass} holding additional TL-Script functions that can be defined within
 * the application.
 * 
 * @param <C>
 *        Type of the configuration.
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	CommandGroupRegistry.Module.class
})
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
	 * Calls the configured function.
	 *
	 * <p>
	 * A secured call of a {@link PrivilegedScript} is checked once: if the current user is
	 * {@link PrivilegedScript#isAllowed(Object[]) allowed}, the function is executed without
	 * security and its result is filtered for the Discover right, since the calling script
	 * continues secured; otherwise the {@link PrivilegedScript#denied(Object[]) denied result} is
	 * returned. Every other call executes the function with the given security setting.
	 * </p>
	 *
	 * <p>
	 * The plain route ({@link QueryExecutorMethod}) and the tracing route ({@link TracingScriptMethod},
	 * e.g. a computation in a form) are checked alike. In the tracing route, the implementation is
	 * evaluated in the context of the caller and therefore sees the overlays of its evaluation
	 * context. The security object is computed in a fresh context without these overlays, and the
	 * check is made on the persistent object (see {@link PrivilegedScript#isAllowed(Object[])}).
	 * An overlay passed as argument, however, is the argument itself: a security object expression
	 * navigating from it reads its not yet stored values.
	 * </p>
	 *
	 * @param scriptName
	 *        The name of the function.
	 * @param usesSecurity
	 *        Whether the calling expression is evaluated with security.
	 * @param arguments
	 *        The arguments of the call.
	 * @param execution
	 *        Executes the function with the given security setting.
	 * @return The result of the call.
	 */
	public Object call(String scriptName, boolean usesSecurity, Object[] arguments,
			Function<Boolean, Object> execution) {
		if (usesSecurity && _builders.get(scriptName) instanceof PrivilegedScript privileged) {
			if (!privileged.isAllowed(arguments)) {
				return privileged.denied(arguments);
			}
			return SearchExpression.filterSecurity(execution.apply(Boolean.FALSE));
		}
		return execution.apply(Boolean.valueOf(usesSecurity));
	}

	/**
	 * Whether the current user may call the configured function with the given arguments.
	 *
	 * @return <code>true</code> for a function that is no {@link PrivilegedScript}.
	 * @throws TopLogicException
	 *         iff there is no function with the given name.
	 */
	public boolean isCallAllowed(String scriptName, Object[] arguments) {
		ConfiguredScript script = _builders.get(scriptName);
		if (script == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_SCRIPT__NAME.fill(scriptName));
		}
		return !(script instanceof PrivilegedScript privileged) || privileged.isAllowed(arguments);
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
