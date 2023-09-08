/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.ObjectFlag;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.ResourcesModule;

/**
 * Utilities for managing the live-cycle of {@link RuntimeModule}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModuleUtil {

	/**
	 * Startup context for {@link BasicRuntimeModule} startups. 
	 * 
	 * <p>
	 * Keeps track about dependencies being started along with requested modules.
	 * </p>
	 */
	public static abstract class ModuleContext implements AutoCloseable {

		abstract void started(BasicRuntimeModule<?> module);

		/**
		 * Shuts down all started modules with all its dependencies being started in this context
		 * (as requested by startups in this context).
		 * 
		 * <p>
		 * Does not shut down modules that are dependencies of requested modules that were started
		 * outside of this context.
		 * </p>
		 */
		@Override
		public final void close() {
			closeContext();
		}

		abstract void closeContext();

	}

	class GlobalContext extends ModuleContext {
		private final ModuleContext _outer;
	
		private final List<BasicRuntimeModule<?>> _started = new ArrayList<>();
	
		GlobalContext() {
			_outer = this;
		}
	
		protected GlobalContext(ModuleContext outer) {
			_outer = outer;
		}
	
		@Override
		void started(BasicRuntimeModule<?> module) {
			_started.add(module);
		}
	
		@Override
		void closeContext() {
			dropContext(this, _outer);
			shutDown();
		}
	
		private void shutDown() {
			for (BasicRuntimeModule<?> module : _started) {
				ModuleUtil.INSTANCE.shutDown(module);
			}
			_started.clear();
		}
	
	}

	class InnerContext extends GlobalContext {
	
		private boolean _closed;
	
		public InnerContext(ModuleContext outer) {
			super(outer);
		}
	
		@Override
		void closeContext() {
			if (_closed) {
				return;
			}
			_closed = true;
	
			super.closeContext();
		}
	
	}

	private static final ModuleContext NONE = new ModuleContext() {
	
		@Override
		void started(BasicRuntimeModule<?> module) {
			// Ignore.
		}
	
		@Override
		void closeContext() {
			throw new UnsupportedOperationException();
		}
	
	};

	/**
	 * {@link ModuleContext} that does no bookkeeping.
	 */
	private ModuleContext _currentContext = new GlobalContext();

	/**
	 * Part of the error message of the exception which is thrown when some
	 * module is accessed which is not yet started.
	 */
	public static final String MODULE_NOT_STARTED = " module not started.";

	/**
	 * singleton instance of this class.
	 */
	public static final ModuleUtil INSTANCE = new ModuleUtil(); 

	/**
	 * The {@link #dependentModules} is a cache for the dependents of
	 * {@link BasicRuntimeModule}. It is expected that the direct dependents are
	 * constant for all module but those which depends on {@link XMLProperties}.
	 * 
	 * The value for a {@link BasicRuntimeModule} <tt>x</tt> is a
	 * {@link Collection} of {@link BasicRuntimeModule} which contains exactly
	 * the modules which declare <tt>x</tt> as direct dependency.
	 * 
	 * The value of <code>null</code> is a {@link Collection} which contains
	 * exactly the {@link BasicRuntimeModule}s which have no dependencies.
	 */
	private HashMap<BasicRuntimeModule<?>, Collection<BasicRuntimeModule<?>>> dependentModules = new HashMap<>();

	/**
	 * The set of {@link BasicRuntimeModule} which were currently started.
	 */
	private Set<BasicRuntimeModule<?>> activeModules = new HashSet<>();
	
	private static Map<Class<? extends BasicRuntimeModule<?>>, BasicRuntimeModule<?>> _moduleByClass =
		new ConcurrentHashMap<>();

	private ModuleUtil() {
		// singleton constructor (is also called via reflection in test)
	}
	
	/**
	 * Creates a new {@link ModuleContext} context.
	 */
	public static ModuleContext beginContext() {
		return INSTANCE.begin();
	}

	/**
	 * Creates a new {@link ModuleContext} context.
	 */
	public ModuleContext begin() {
		InnerContext result = new InnerContext(_currentContext);
		_currentContext = result;
		return result;
	}

	void dropContext(ModuleContext current, ModuleContext outer) {
		if (_currentContext != current) {
			throw new IllegalStateException("Context is not the innermost open context.");
		}
		_currentContext = outer;
	}

	/**
	 * Starts all configured modules and their dependencies.
	 * 
	 * @throws IllegalStateException
	 *         When {@link ModuleSystem} not started.
	 * @throws IllegalArgumentException
	 *         If there is a cyclic dependency.
	 * @throws ModuleException
	 *         if startup of some dependent of the given {@link RuntimeModule} failed
	 */
	public void startConfiguredModules() throws IllegalArgumentException, ModuleException {
		ModuleSystem moduleSystem = ModuleSystem.Module.INSTANCE.getImplementationInstance();
		Set<BasicRuntimeModule<?>> modules = moduleSystem.configuredServices();
		for (BasicRuntimeModule<?> module : modules) {
			startUp(module);
		}
	}

	/**
	 * Start the given {@link RuntimeModule} with all its dependencies.
	 * 
	 * @param module
	 *        The {@link RuntimeModule} to start.
	 * 
	 * @throws IllegalArgumentException
	 *         If there is a cyclic dependency.
	 * @throws ModuleException
	 *         if startup of some dependent of the given {@link RuntimeModule}
	 *         failed
	 * 
	 * @see #beginContext()
	 */
	public void startUp(BasicRuntimeModule<?> module) throws IllegalArgumentException, ModuleException {
		if (module.isActive()) {
			return;
		}
		startModulesAndAdd(module, null);
	}

	/**
	 * starts the given module expecting that all dependencies are already
	 * started
	 * 
	 * @param module
	 *        the module to start
	 * 
	 * @throws ModuleException
	 *         iff the given module throws some in
	 *         {@link BasicRuntimeModule#startUp()}
	 */
	void start(BasicRuntimeModule<?> module) throws ModuleException {
		if (module.isActive()) {
			// Nothing to do: already started
			return;
		}

		module.startUp();
		markStarted(module);
	}

	/**
	 * Marks the given service module explicitly as started.
	 *
	 * @param module
	 *        The explicitly started module.
	 */
	@FrameworkInternal
	public void markStarted(BasicRuntimeModule<?> module) {
		activeModules.add(module);
		_currentContext.started(module);

		// Add module to direct dependents map
		Collection<? extends Class<? extends BasicRuntimeModule<?>>> dependencies = directDependencies(module);
		Class<? extends BasicRuntimeModule<?>> extendedService = module.getExtendedService();
		if (dependencies.isEmpty() && extendedService == null) {
			addDependent(null, module);
		} else {
			for (Class<? extends BasicRuntimeModule<?>> dependency : dependencies) {
				try {
					addDependent(moduleByClass(dependency), module);
				} catch (ConfigurationException ex) {
					throw new ConfigurationError("Can not get singleton from class " + dependency.getName(), ex);
				}
			}
			if (extendedService != null) {
				try {
					addDependent(moduleByClass(extendedService), module);
				} catch (ConfigurationException ex) {
					throw new ConfigurationError("Can not get singleton from class " + extendedService.getName(), ex);
				}
			}
		}
	}

	private void addDependent(BasicRuntimeModule<?> dependency, BasicRuntimeModule<?> dependent) {
		Collection<BasicRuntimeModule<?>> directDependents = dependentModules.get(dependency);
		if (directDependents == null) {
			directDependents = new HashSet<>();
			directDependents.add(dependent);
			dependentModules.put(dependency, directDependents);
		} else {
			directDependents.add(dependent);
		}
	}

	/**
	 * Stops the given module and starts it again.
	 * 
	 * If some dependent module of the given one is started, it is also shutdown and restarted.
	 * 
	 * @param module
	 *        the module to restart. must not be <code>null</code>
	 * 
	 * @throws RestartException
	 *         when during the restart some {@link ModuleException} occurs
	 */
	public void restart(BasicRuntimeModule<?> module) throws RestartException {
		restart(module, null);
	}

	/**
	 * Stops the given module, executes the given callback and starts the given
	 * module again.
	 * 
	 * If some dependent module of the given one is started, it is also shutdown
	 * before the callback is executed, and will also restarted after callback
	 * execution.
	 * 
	 * @param module
	 *        the module to restart. must not be <code>null</code>
	 * @param callback
	 *        a callback to run after stopping and before starting the module.
	 *        May be <code>null</code> if no additional action is needed.
	 * 
	 * @throws RestartException
	 *         when during the restart some {@link ModuleException} occurs
	 */
	public void restart(BasicRuntimeModule<?> module, Runnable callback) throws RestartException {
		if (!module.isActive()) {
			throw new IllegalStateException("The given Module is currently not started so restart is not possible.");
		}

		ModuleContext before = _currentContext;
		_currentContext = NONE;
		try {
			internalRestart(module, callback);
		} finally {
			_currentContext = before;
		}
	}

	private void internalRestart(BasicRuntimeModule<?> module, Runnable callback) throws RestartException {
		Set<BasicRuntimeModule<?>> startedDependents = new HashSet<>();
		/*
		 * store currently started top level modules (i.e. the modules which
		 * have no dependents) to restart them. It is enough to consider such
		 * modules as all models which are dependants of a top level module are
		 * restarted automatically. Moreover it is necessary to recompute
		 * dependencies as it is possible that callback changes them.
		 */
		addStartedDependents(module, startedDependents);

		shutDown(module);
		try {
			if (callback != null) {
				callback.run();
			}
			if (isXMLProperties(module)) {
				// it is necessary to have XML properties when restart
				// dependents, as some dependents need the XMLProperties to
				// compute dependencies.
				startXMLProperties();
			}
			for (BasicRuntimeModule<?> dependent : startedDependents) {
				startUp(dependent);
			}
		} catch (Throwable ex) {
			throw new RestartException("Unable to restart '" + module + "'", ex, module.getImplementation(), startedDependents);
		}
	}
	
	/**
	 * Returns a subset of the given <code>modules</code> such that no two
	 * elements in the returned set are dependent of each other, and each
	 * element in the given <code>modules</code> is a dependency (or the same)
	 * of one element in the returned set. 
	 * 
	 * @param modules the modules to find an independent subset. must not be <code>null</code>
	 */
	public Set<BasicRuntimeModule<?>> getIndependant(Collection<? extends BasicRuntimeModule<?>> modules) {
		HashSet<BasicRuntimeModule<?>> noDependents = new HashSet<>(modules);
		HashSet<BasicRuntimeModule<?>> independents = new HashSet<>();
		for (BasicRuntimeModule<?> moduleToRestart: modules) {
			if (noDependents.contains(moduleToRestart)) {
				removeDependents(moduleToRestart, independents, noDependents);
				independents.add(moduleToRestart);
			}
		}
		return independents;
		
	}

	private void removeDependents(BasicRuntimeModule<?> moduleToRestart, Collection<?>... colls) {
		for (Collection<?> independan: colls) {
			independan.remove(moduleToRestart);
		}
		for (BasicRuntimeModule<?> dependent : getDirectDependents(moduleToRestart)) {
			removeDependents(dependent, colls);
		}
	}

	/**
	 * Make it compile under JDK 1.5.0_22 under Unix. Compiler seems to have a bug deciding about comparable types.
	 * 
	 * <pre>
	 * ModuleUtil.java:158: incomparable types: com.top_logic.basic.module.BasicRuntimeModule<capture of ?> and com.top_logic.basic.XMLProperties.Module
     * if (module == XMLProperties.Module.INSTANCE) {
     *                                    ^
     * </pre>
	 */
	@SuppressWarnings("cast")
	private boolean isXMLProperties(BasicRuntimeModule<?> module) {
		return module == (Object) XMLProperties.Module.INSTANCE;
	}

	@SuppressWarnings("cast")
	boolean isThreadContextModule(BasicRuntimeModule<?> module) {
		return module == (Object) getThreadContextModule();
	}

	private BasicRuntimeModule<?> getThreadContextModule() {
		return ThreadContextManager.Module.INSTANCE;
	}

	/**
	 * Adds to the given {@link Collection} <code>startedDependents</code> some
	 * started dependent of the given {@link BasicRuntimeModule}
	 * <code>dependency</code>. It is ensured that after restarting all modules
	 * in the Collection, all currently started modules which have
	 * <code>dependency</code> as (heritable) dependant, are started again.
	 * 
	 * If the given module is not started nothing happens.
	 * 
	 * @param dependency
	 *        module to determine the started module <code>x</code> which have
	 *        <code>dependency</code> as (heritable) dependant for adding
	 *        <code>x</code> to the given {@link Collection}. must not be
	 *        <code>null</code>.
	 * @param startedDependents
	 *        collection of {@link BasicRuntimeModule} to add started dependents
	 *        of the given <code>dependency</code>. must not be
	 *        <code>null</code>.
	 * 
	 * @return true iff the given {@link Collection} has changed.
	 */
	private boolean addStartedDependents(BasicRuntimeModule<?> dependency, Collection<BasicRuntimeModule<?>> startedDependents) {
		if (!dependency.isActive()) {
			return false;
		}
		Collection<BasicRuntimeModule<?>> dependents = dependentModules.get(dependency);
		if (dependents == null) {
			return startedDependents.add(dependency);
		} else {
			boolean someDependentStarted = false;
			for (BasicRuntimeModule<?> dependent : dependents) {
				someDependentStarted |= addStartedDependents(dependent, startedDependents);
			}
			if (!someDependentStarted) {
				return startedDependents.add(dependency);
			}
			return true;
		}

	}

	/**
	 * Stop the given {@link RuntimeModule} with all modules that depend on the
	 * given module.
	 * 
	 * @param module
	 *        The {@link RuntimeModule} to stop.
	 */
	public void shutDown(BasicRuntimeModule<?> module) {
		if (!module.isActive()) {
			// Nothing to do: already terminated. it is supposed that all
			// dependents are also terminated
			return;
		}
		LinkedHashSet<BasicRuntimeModule<?>> dependents = new LinkedHashSet<>();
		addDependentModules(module, dependents);

		shutDown(dependents.iterator());
	}

	/**
	 * Shuts all given modules down.
	 * 
	 * @param modules
	 *        Modules to shut down. Dependent modules must appear before their dependencies.
	 */
	private void shutDown(final Iterator<BasicRuntimeModule<?>> modules) {
		if (threadContextManagerActive()) {
			stopInThreadContext(modules);
		}
		// stop modules without threadContext
		while (modules.hasNext()) {
			BasicRuntimeModule<?> module = modules.next();
			if (!module.isActive()) {
				continue;
			}
			ModuleUtil.this.stop(module);
		}
	}

	/**
	 * Stops all modules in the given list with {@link InteractionContext} until the
	 * {@link ThreadContextManager} is reached,i.e. if the {@link ThreadContextManager} is contained
	 * in the modules, it is the last module that is stopped.
	 * 
	 * <p>
	 * It is expected that the {@link ThreadContextManager} is active when this method is called.
	 * </p>
	 */
	private void stopInThreadContext(final Iterator<BasicRuntimeModule<?>> modules) {
		final ObjectFlag<BasicRuntimeModule<?>> threadContextModule = new ObjectFlag<>(null);
		InContext job = new InContext() {
			
			@Override
			public void inContext() {
				while (modules.hasNext()) {
					BasicRuntimeModule<?> module = modules.next();
					if (!module.isActive()) {
						continue;
					}
					if (ModuleUtil.this.isThreadContextModule(module)) {
						// can not stop thread context module in thread context
						threadContextModule.set(module);
						break;
					}
					ModuleUtil.this.stop(module);
				}
			}
		};
		ThreadContextManager.inSystemInteraction(ModuleUtil.class, job);
		if (threadContextModule.get() != null) {
			// stop thread context module
			stop(threadContextModule.get());
		}
	}

	/**
	 * Adds the given module and all dependencies to the given collection.
	 * 
	 * <p>
	 * Dependent module are inserted before their dependencies.
	 * </p>
	 */
	private void addDependentModules(BasicRuntimeModule<?> module, Set<BasicRuntimeModule<?>> dependents) {
		if (dependents.contains(module)) {
			return;
		}
		Collection<BasicRuntimeModule<?>> directDependents = dependentModules.get(module);
		if (directDependents != null) {
			for (BasicRuntimeModule<?> dependent : directDependents) {
				addDependentModules(dependent, dependents);
			}
		}

		dependents.add(module);
	}

	/**
	 * Terminates the given module. It is supposed that all dependents are
	 * already terminated.
	 * 
	 * @param module
	 *        the module to {@link BasicRuntimeModule#shutDown()}
	 */
	void stop(BasicRuntimeModule<?> module) {
		assert module.isActive(): "Given module " + module + " should be stopped but is not active";
		if (module.isActive()) {
			assert activeModules.contains(module) : "Given module " + module + " is active but was not started by ModuleUtil";
			module.shutDown();
			activeModules.remove(module);
		}

		if (isXMLProperties(module)) {
			removeDependents(module);
		}

	}

	private void removeDependents(BasicRuntimeModule<?> module) {
		Collection<BasicRuntimeModule<?>> dependents = dependentModules.get(module);
		if (dependents == null) {
			return;
		}
		for (BasicRuntimeModule<?> dependent : dependents) {
			removeDependents(dependent);
		}
		dependentModules.remove(module);
	}

	public void shutDownAll() {
		Collection<BasicRuntimeModule<?>> noDependenciesModules = dependentModules.get(null);
		if (noDependenciesModules != null) {
			for (BasicRuntimeModule<?> module : noDependenciesModules) {
				shutDown(module);
			}
		}
	}

	/**
	 * Starts the given service and executes the given {@link Computation}.
	 * After running the computation the started modules are shut down.
	 * 
	 * 
	 * @param neededModule
	 *        the service which is needed in the given {@link Computation}
	 * @param computation
	 *        the {@link Computation} which needs the given service
	 * 
	 * @return the return value of the given computation
	 * 
	 * @throws ModuleRuntimeException
	 *         iff the given Module or some dependency could not be started
	 */
	public <T, E1 extends Throwable, E2 extends Throwable> T inModuleContext(BasicRuntimeModule<?> neededModule,
			ComputationEx2<T, E1, E2> computation) throws ModuleRuntimeException, E1, E2 {
		final T result;
		if (neededModule == null || neededModule.isActive()) {
			result = computation.run();
		} else {
			List<BasicRuntimeModule<?>> startedModules = new ArrayList<>();

			startModuleAndDependencies(neededModule, startedModules);

			assert activeModules.containsAll(startedModules) : "some modules has been started up, but are not active: started:" + startedModules
					+ " active:" + activeModules;

			ArrayList<BasicRuntimeModule<?>> activeBeforeComputation = new ArrayList<>(activeModules);
			try {
				result = computation.run();
			} finally {
				final HashSet<BasicRuntimeModule<?>> activeAfterComputation = new HashSet<>(activeModules);
				activeAfterComputation.removeAll(activeBeforeComputation);
				final HashSet<BasicRuntimeModule<?>> byComputationStarted = activeAfterComputation;
				startedModules.removeAll(byComputationStarted);

				shutdownStartedModules(startedModules);
			}
		}
		return result;
	}

	private void shutdownStartedModules(List<? extends BasicRuntimeModule<?>> startedModules) {
		for (int index = startedModules.size() - 1; index >= 0; index--) {
			shutDown(startedModules.get(index));
		}
	}

	public <T, E1 extends Throwable, E2 extends Throwable> T inModuleContext(ComputationEx2<T, E1, E2> computation,
			BasicRuntimeModule<?>... neededModules) throws ModuleRuntimeException, E1, E2 {
		if (neededModules.length == 0) {
			return computation.run();
		}

		List<BasicRuntimeModule<?>> startedModules = new ArrayList<>();

		for (BasicRuntimeModule<?> currentModule : neededModules) {
			if (currentModule.isActive()) {
				continue;
			}
			startModuleAndDependencies(currentModule, startedModules);
		}

		try {
			return computation.run();
		} finally {
			shutdownStartedModules(startedModules);
		}
	}

	/**
	 * Starts the given module and its dependencies (if not already active). Started modules are
	 * added to given list if not <code>null</code>.
	 */
	private void startModuleAndDependencies(BasicRuntimeModule<?> module, List<BasicRuntimeModule<?>> startedModules) {
		try {
			startModulesAndAdd(module, startedModules);
		} catch (ModuleException ex) {
			Logger.error("Unable to start module for " + ex.getFailedService().getSimpleName(), ex, ModuleUtil.class);
			shutdownStartedModules(startedModules);
			throw new ModuleRuntimeException(ex);
		}
	}

	/**
	 * Starts the given module and its dependencies (if not already active). Started modules are
	 * added to given list if not <code>null</code>.
	 * 
	 * @param startedModules
	 *        List to add started modules to. May be <code>null</code>, is no tracking is needed.
	 */
	public void startModulesAndAdd(BasicRuntimeModule<?> module, List<BasicRuntimeModule<?>> startedModules)
			throws ModuleException {
		Iterable<? extends BasicRuntimeModule<?>> dependencies2 = getDependencies(module);
		final Iterator<? extends BasicRuntimeModule<?>> dependencies = dependencies2.iterator();
		if (threadContextManagerActive()) {
			// It is possible to have threadContext, so install one.
			startInThreadContext(dependencies, startedModules);
		} else {
			while (dependencies.hasNext()) {
				BasicRuntimeModule<?> dependency = dependencies.next();
				
				startModuleAndAdd(dependency, startedModules);
				
				if (isThreadContextModule(dependency)) {
					// Now ThreadContextManager available.
					if (!dependencies.hasNext()) {
						// The module actually started is the thread context module itself.
						break;
					}
					startInThreadContext(dependencies, startedModules);
				}
			}
		}
	}

	private void startInThreadContext(final Iterator<? extends BasicRuntimeModule<?>> dependencies,
			final List<BasicRuntimeModule<?>> startedModules) throws ModuleException {
		Computation<ModuleException> job = new Computation<>() {

			@Override
			public ModuleException run() {
				try {
					while (dependencies.hasNext()) {
						ModuleUtil.this.startModuleAndAdd(dependencies.next(), startedModules);
					}
					return null;
				} catch (ModuleException ex) {
					return ex;
				}
			}

		};
		ModuleException problem = ThreadContextManager.inSystemInteraction(ModuleUtil.class, job);
		if (problem != null) {
			throw problem;
		}
	}

	private boolean threadContextManagerActive() {
		return getThreadContextModule().isActive();
	}

	void startModuleAndAdd(BasicRuntimeModule<?> module, List<BasicRuntimeModule<?>> startedModules)
			throws ModuleException {
		if (module.isActive()) {
			return;
		}
		start(module);
		if (startedModules != null) {
			startedModules.add(module);
		}
	}

	/**
	 * Resolve all dependencies of a given {@link RuntimeModule}.
	 * 
	 * @param module
	 *        The {@link RuntimeModule} to resolve dependencies for.
	 * @return {@link Iterable} of all dependencies of the given module and the
	 *         given module itself in topological order (dependencies first).
	 * 
	 * @throws IllegalArgumentException
	 *         If there is a cyclic dependency.
	 */
	public Iterable<? extends BasicRuntimeModule<?>> getDependencies(BasicRuntimeModule<?> module) throws IllegalArgumentException {
		// Build transitive dependencies:
		Map<BasicRuntimeModule<?>, BasicRuntimeModule<?>> pending = new HashMap<>();
		Collection<BasicRuntimeModule<?>> dependencies = new LinkedHashSet<>();

		Map<BasicRuntimeModule<?>, List<BasicRuntimeModule<?>>> extensionMap = getExtensionMap(module);
		try {
			addDependencies(pending, dependencies, module, extensionMap);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Unablet to get dependencies.", ex);
		}
		return dependencies;
	}

	private Map<BasicRuntimeModule<?>, List<BasicRuntimeModule<?>>> getExtensionMap(BasicRuntimeModule<?> module) {
		Map<BasicRuntimeModule<?>, List<BasicRuntimeModule<?>>> extensionMap;
		if (ModuleSystem.Module.INSTANCE.isActive()) {
			ModuleSystem moduleSystem = ModuleSystem.Module.INSTANCE.getImplementationInstance();
			extensionMap = moduleSystem.extensionMap();
			if (!moduleSystem.configuredServices().contains(module)) {
				extensionMap = addExtensions(Collections.singletonList(module), new HashMap<>(extensionMap));
			}
		} else {
			extensionMap = createExtensions(Collections.singletonList(module));
		}
		return extensionMap;
	}

	Map<BasicRuntimeModule<?>, List<BasicRuntimeModule<?>>> createExtensions(
			Collection<? extends BasicRuntimeModule<?>> modules) {
		Map<BasicRuntimeModule<?>, List<BasicRuntimeModule<?>>> extensionMap = new HashMap<>();
		addExtensions(modules, extensionMap);
		return extensionMap;
	}

	Map<BasicRuntimeModule<?>, List<BasicRuntimeModule<?>>> addExtensions(
			Collection<? extends BasicRuntimeModule<?>> modules,
			Map<BasicRuntimeModule<?>, List<BasicRuntimeModule<?>>> extensionMap) {
		Map<BasicRuntimeModule<?>, BasicRuntimeModule<?>> pending = new HashMap<>();
		Set<BasicRuntimeModule<?>> processed = new HashSet<>();
		try {
			for (BasicRuntimeModule<?> module : modules) {
				addExtensionMappings(pending, processed, null, module, extensionMap);
			}
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Unable to fill extension map", ex);
		}
		return extensionMap;
	}

	/**
	 * Determines the extensions for the modules in the dependency graph.
	 * 
	 * @param pending
	 *        The {@link Map#keySet()} contains the {@link BasicRuntimeModule} that are currently,
	 *        but not completely processed.
	 * @param processed
	 *        The {@link BasicRuntimeModule} that have been processed.
	 * @param module
	 *        The module which has the <code>dependency</code> as dependent class. May be
	 *        <code>null</code>.
	 * @param dependency
	 *        The service to add as extension service to the <code>extensionMap</code> for the
	 *        extended service.
	 * @param extensionMap
	 *        Mapping from a module to all modules declaring the module as
	 *        {@link BasicRuntimeModule#getExtendedService()}
	 */
	private void addExtensionMappings(Map<BasicRuntimeModule<?>, BasicRuntimeModule<?>> pending,
			Set<BasicRuntimeModule<?>> processed, BasicRuntimeModule<?> module,
			BasicRuntimeModule<?> dependency, Map<BasicRuntimeModule<?>, List<BasicRuntimeModule<?>>> extensionMap)
			throws IllegalArgumentException, ConfigurationException {

		if (pending.keySet().contains(dependency)) {
			// cyclic dependency
			throw failCyclicDependency(pending, module, dependency);
		}

		if (processed.contains(dependency)) {
			// module already processed.
			return;
		}
		
		// Add problematic dependency to allow creating cycle information.
		pending.put(dependency, module);

		Class<? extends BasicRuntimeModule<?>> extendedClass = dependency.getExtendedService();
		if (extendedClass != null) {
			BasicRuntimeModule<?> extended = moduleByClass(extendedClass);
			List<BasicRuntimeModule<?>> extensions = extensionMap.get(extended);
			if (extensions == null) {
				extensions = new ArrayList<>(2);
				extensionMap.put(extended, extensions);
			}
			extensions.add(dependency);
		}

		processed.add(dependency);

		for (Class<? extends BasicRuntimeModule<?>> dependency1 : directDependencies(dependency)) {
			BasicRuntimeModule<?> dependentModule = moduleByClass(dependency1);
			addExtensionMappings(pending, processed, dependency, dependentModule, extensionMap);
		}

		pending.remove(dependency);
	}

	private IllegalArgumentException failCyclicDependency(Map<BasicRuntimeModule<?>, BasicRuntimeModule<?>> pending,
			BasicRuntimeModule<?> module, BasicRuntimeModule<?> directDependency) {
		StringBuilder msg = new StringBuilder("Cyclic module dependency: ");
		msg.append(directDependency.getImplementation().getName());

		BasicRuntimeModule<?> dependent = module;
		while (dependent != directDependency) {
			msg.append(" <- ");
			msg.append(dependent.getImplementation().getName());

			dependent = pending.get(dependent);
		}

		// Closing edge of cycle.
		msg.append(" <- ");
		msg.append(dependent.getImplementation().getName());

		throw new IllegalArgumentException(msg.toString());
	}

	/**
	 * Determines the dependencies of the given <code>module</code>. For each dependent and the
	 * extended service it calls
	 * {@link #addDependencies(Map, Collection, BasicRuntimeModule, Map)} with that dependent.
	 * 
	 * <p>
	 * Then the given module is added to <code>allDependencies</code>.
	 * </p>
	 * 
	 * <p>
	 * After all the extensions of the module are added.
	 * </p>
	 * 
	 * @param pending
	 *        The {@link Map#keySet()} contains the {@link BasicRuntimeModule} that are currently,
	 *        but not completely processed.
	 * @param allDependencies
	 *        All dependencies. At the end the given module will be added to.
	 * @param module
	 *        the {@link BasicRuntimeModule} whose dependents should be checked
	 * @param extensionMap
	 *        Mapping of the {@link BasicRuntimeModule} to all {@link BasicRuntimeModule}s that are
	 *        {@link BasicRuntimeModule#getExtendedService() extensions}.
	 */
	private void addDependencies(Map<BasicRuntimeModule<?>, BasicRuntimeModule<?>> pending,
			Collection<BasicRuntimeModule<?>> allDependencies, BasicRuntimeModule<?> module,
			Map<BasicRuntimeModule<?>, List<BasicRuntimeModule<?>>> extensionMap) throws ConfigurationException {

		if (allDependencies.contains(module) || module.isActive()) {
			// module already processed.
			return;
		}

		// store module to know currently processed modules
		pending.put(module, module);

		for (Class<? extends BasicRuntimeModule<?>> dependencyClass : directDependencies(module)) {
			BasicRuntimeModule<?> dependency = moduleByClass(dependencyClass);
			addDependencies(pending, allDependencies, dependency, extensionMap);
		}

		Class<? extends BasicRuntimeModule<?>> extendedService = module.getExtendedService();
		if (extendedService != null) {
			// ensure that also extended modules are started.
			BasicRuntimeModule<?> extended = moduleByClass(extendedService);
			addDependencies(pending, allDependencies, extended, extensionMap);
		}

		/* After adding all depending modules to the collection of modules, add the given module. */
		allDependencies.add(module);

		pending.remove(module);

		List<BasicRuntimeModule<?>> extensions = extensionMap.get(module);
		if (extensions != null) {
			for (BasicRuntimeModule<?> extension : extensions) {
				if (pending.containsKey(extension)) {
					/* Extension currently processed. */
					continue;
				}
				/* ensure that the extensions of the module are started directly after the module
				 * itself. */
				addDependencies(pending, allDependencies, extension, extensionMap);
			}
		}
	}

	/**
	 * Returns the singleton {@link BasicRuntimeModule} instance for the given class.
	 * 
	 * @throws ConfigurationException
	 *         iff given class is not a singleton class.
	 */
	public static BasicRuntimeModule<?> moduleByClass(Class<? extends BasicRuntimeModule<?>> moduleClass)
			throws ConfigurationException {
		BasicRuntimeModule<?> module = _moduleByClass.get(moduleClass);
		if (module != null) {
			return module;
		}
		module = ConfigUtil.getSingleton(moduleClass);
		_moduleByClass.put(moduleClass, module);
		return module;
	}

	private Collection<? extends Class<? extends BasicRuntimeModule<?>>> directDependencies(
			BasicRuntimeModule<?> module) {
		Collection<? extends Class<? extends BasicRuntimeModule<?>>> dependencies = module.getDependencies();
		if (dependencies == null) {
			assert false : "Module '" + module + "' declares 'null' as dependencies. Use constant in "
				+ BasicRuntimeModule.class;
			dependencies = Collections.emptyList();
		}
		return dependencies;
	}

	/**
	 * Throw an {@link IllegalStateException} that reports that the given module
	 * was already started.
	 * 
	 * @param moduleClass
	 *        The {@link Class} that implements the module for which the problem
	 *        is reported.
	 * @return The {@link IllegalStateException} is declared to be returned to
	 *         allow using this method as argument to the <code>throw</code>
	 *         keyword to convince the Java compiler that control flow does not
	 *         skip the call of this method.
	 */
	static IllegalStateException invalidStateAlreadyStarted(Class<?> moduleClass) throws IllegalStateException {
		throw new IllegalStateException(moduleClass.getName() + " module already started.");
	}

	/**
	 * Throw an {@link IllegalStateException} that reports that the given module
	 * was not yet started.
	 * 
	 * @param moduleClass
	 *        The {@link Class} that implements the module for which the problem
	 *        is reported.
	 * @return The {@link IllegalStateException} is declared to be returned to
	 *         allow using this method as argument to the <code>throw</code>
	 *         keyword to convince the Java compiler that control flow does not
	 *         skip the call of this method.
	 */
	static IllegalStateException invalidStateNotStarted(Class<?> moduleClass) throws IllegalStateException {
		String msg = moduleClass.getName() + MODULE_NOT_STARTED;
		IllegalStateException ex = new IllegalStateException(msg);
		Logger.error(msg, ex, ModuleUtil.class);
		throw ex;
	}

	public Collection<? extends BasicRuntimeModule<?>> getActiveModules() {
		return Collections.unmodifiableCollection(activeModules);
	}

	/**
	 * Starts the {@link XMLProperties} in the current configuration.
	 * <p>
	 * <b>This method is just for internals and must not be used by the
	 * application.</b>
	 * </p>
	 * 
	 * @throws ModuleException
	 *         iff startup failed.
	 */
	public void startXMLProperties() throws ModuleException {
		// start AliasManager as AliasManager is essentially an inner
		// service which is needed
		startUp(XMLProperties.Module.INSTANCE);
		startUp(AliasManager.Module.INSTANCE);
		startUp(ApplicationConfig.Module.INSTANCE);
		startUp(ResourcesModule.Module.INSTANCE);
		startUp(ModuleSystem.Module.INSTANCE);
	}

	/**
	 * Returns a set of all (inherited) dependents of the given
	 * {@link BasicRuntimeModule module} which are currently started.
	 * 
	 * @param module
	 *        may be <code>null</code>. In this case the returned {@link Collection} contains
	 *        all currently started modules.
	 */
	public Collection<BasicRuntimeModule<?>> getAllDependents(BasicRuntimeModule<?> module) {
		return getAllDependents(module, true);
	}

	/**
	 * Returns a set of all (inherited) dependents of the given {@link BasicRuntimeModule module}
	 * which are currently started.
	 * 
	 * @param module
	 *        may be <code>null</code>. In this case the returned {@link Collection} contains all
	 *        currently started modules.
	 * 
	 * @param withRootModule
	 *        True if the root module should be included in the resulting set of dependent modules.
	 */
	public Collection<BasicRuntimeModule<?>> getAllDependents(BasicRuntimeModule<?> module, boolean withRootModule) {
		Set<BasicRuntimeModule<?>> allDependents = new HashSet<>();
		if (withRootModule && module != null) {
			allDependents.add(module);
		}
		resolveAllStartedDependents(allDependents, module);
		return allDependents;
	}

	private void resolveAllStartedDependents(Collection<BasicRuntimeModule<?>> allDependents, BasicRuntimeModule<?> module) {
		final Collection<? extends BasicRuntimeModule<?>> directDependents = dependentModules.get(module);
		if (directDependents == null) {
			return;
		}
		for (BasicRuntimeModule<?> dependent : directDependents) {
			if (!dependent.isActive()) {
				continue;
			}
			if (allDependents.add(dependent)) {
				resolveAllStartedDependents(allDependents, dependent);
			}
		}
	}

	/**
	 * Returns the direct dependents of the given module which are currently
	 * started.
	 * 
	 * @param module
	 *        may be <code>null</code>. In that case the returned
	 *        {@link Collection} contains the module which declare no
	 *        dependencies.
	 */
	public Collection<? extends BasicRuntimeModule<?>> getDirectDependents(BasicRuntimeModule<?> module) {
		final Collection<BasicRuntimeModule<?>> dependents = dependentModules.get(module);
		if (dependents == null) {
			return Collections.emptyList();
		}
		return CollectionUtil.intersection(dependents, activeModules);
	}

	/**
	 * Tries to start the module corresponding to the given class. It is
	 * expected that the given class has exactly one `public static` inner class
	 * which is a subclass of {@link BasicRuntimeModule} and which is a
	 * singleton.
	 * 
	 * @param serviceClass
	 *        the service to start. must not be <code>null</code>
	 * @throws IllegalArgumentException
	 *         if one of the assumptions fails or the module has cyclic
	 *         references
	 * 
	 * @return <code>true</code> iff the module is currently not started
	 * 
	 * @throws ModuleException
	 *         if starting the module fails
	 */
	public boolean startModule(Class<? extends ManagedClass> serviceClass) throws IllegalArgumentException, ModuleException {
		final Class<?>[] classes = serviceClass.getDeclaredClasses();
		final int numberInnerClasses = classes.length;
		if (numberInnerClasses == 0) {
			throw new IllegalArgumentException("Expected there is a inner subclass of '" + BasicRuntimeModule.class.getName() + "' in given class '" + serviceClass.getName() + "'");
		}
		Class<? extends BasicRuntimeModule<?>> moduleClass = null;
		int index = 0;
		for (; index < numberInnerClasses; index++) {
			final Class<?> currentClass = classes[index];
			if (BasicRuntimeModule.class.isAssignableFrom(currentClass)) {
				@SuppressWarnings("unchecked")
				Class<? extends BasicRuntimeModule<?>> currentClass2 =
					(Class<? extends BasicRuntimeModule<?>>) currentClass;
				moduleClass = currentClass2;
				break;
			}
		}
		if (moduleClass == null) {
			throw new IllegalArgumentException("Expected there is a inner subclass of '" + BasicRuntimeModule.class.getName() + "' in given class '" + serviceClass.getName() + "'");
		} else {
			for (index++; index < numberInnerClasses; index++) {
				final Class<?> currentClass = classes[index];
				if (BasicRuntimeModule.class.isAssignableFrom(currentClass)) {
					throw new IllegalArgumentException("Expected there is only inner subclass of '" + BasicRuntimeModule.class.getName() + "' in given class '" + serviceClass.getName() + "', but there are at least '" + moduleClass.getName() + "' and '" + currentClass.getName() + "'");
				}
			}
		}
		
		try {

			final Object singleton = ConfigUtil.getSingleton(moduleClass);
			final BasicRuntimeModule<?> module = BasicRuntimeModule.class.cast(singleton);
			if (!module.isActive()) {
				startUp(module);
				return true;
			}
			return false;
		} catch (ConfigurationException ex) {
			throw new IllegalArgumentException("Unable to fetch singleton instance of inner module class '" + moduleClass.getName() + "'.", ex);
		}
	}

	/**
	 * Searches the {@link BasicRuntimeModule} class for the given class.
	 * 
	 * If the given class is an extension of {@link BasicRuntimeModule} it is
	 * returned. Otherwise the first inner class which is an extension of a
	 * {@link BasicRuntimeModule} is returned.
	 * 
	 * @throws ConfigurationException
	 *         iff it is not possible to determine a unique module class.
	 */
	public static Class<? extends BasicRuntimeModule<?>> getModuleClass(Class<?> clazz) throws ConfigurationException {
		if (BasicRuntimeModule.class.isAssignableFrom(clazz)) {
			return (Class<? extends BasicRuntimeModule<?>>) clazz;
		} else {
			Class<?>[] innerClasses = clazz.getClasses();
			Class<? extends BasicRuntimeModule<?>> moduleClass = null;
			for (Class<?> potentialModule : innerClasses) {
				if (BasicRuntimeModule.class.isAssignableFrom(potentialModule)) {
					if (moduleClass != null) {
						throw new ConfigurationException(clazz.getName() + " contains at least two inner "
								+ BasicRuntimeModule.class.getName() + ": " + moduleClass.getName() + ", "
								+ potentialModule.getName());
					}
					moduleClass = (Class<? extends BasicRuntimeModule<?>>) potentialModule;
				}
			}
			if (moduleClass == null) {
				throw new ConfigurationException(clazz.getName() + " does not contain any inner "
						+ BasicRuntimeModule.class.getName() + ".");
			}
			return moduleClass;
		}
	}
	
	/**
	 * Returns the {@link BasicRuntimeModule} for the given class. It is
	 * expected that the given class itself or an inner class is an extension of
	 * {@link BasicRuntimeModule}.
	 * 
	 * @return never <code>null</code>
	 * 
	 * @throws ConfigurationException
	 *         iff it is not possible to determine the module class or the
	 *         singleton instance an not be given.
	 */
	public static BasicRuntimeModule<?> getModule(Class<?> clazz) throws ConfigurationException {
		Class<? extends BasicRuntimeModule<?>> moduleClass = getModuleClass(clazz);
		if (moduleClass == null) {
			throw new ConfigurationException("No ModuleClass found for " + clazz.getName());
		}
		return moduleByClass(moduleClass);
	}

	/**
	 * All {@link BasicRuntimeModule}s in a TopLogic application.
	 */
	public static Collection<BasicRuntimeModule<?>> getAllModules() {
		return getAllModuleClasses().stream().map(moduleClass -> {
			try {
				return getModule(moduleClass);
			} catch (ConfigurationException exception) {
				throw new ConfigurationError(exception);
			}
		}).collect(Collectors.toSet());
	}

	/**
	 * Name of the implementation of the given module.
	 */
	public String getModuleName(BasicRuntimeModule<?> module) {
		return module.getImplementation().getSimpleName();
	}

	private static Collection<Class<?>> getAllModuleClasses() {
		return TypeIndex.getInstance().getSpecializations(BasicRuntimeModule.class, true, false, false);
	}
	
}
