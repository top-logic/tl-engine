/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.InContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * The ExportQueueManager is responsible for starting and stopping
 * {@link ExportExecutor}s.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@ServiceDependencies({ ExportRegistryFactory.Module.class, PersonManager.Module.class })
public final class ExportQueueManager extends ConfiguredManagedClass<ExportQueueManager.Config> implements Reloadable {

	private static final String TECHNOLOGY = "technology";
	private static final String DURATION = "duration";

	private Map<String, ExportExecutor> executors;

	private final ExportRegistry _exportRegistry;

	/**
	 * Configuration for {@link ExportQueueManager}
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<ExportQueueManager> {
		/**
		 * Global flag to activate the ExportQueueManager.
		 */
		String ACTIVATED = "activated";

		/**
		 * true if its activated.
		 */
		@Name(ACTIVATED)
		boolean isActivated();

		/**
		 * Multiple queue managers.
		 */
		@MapBinding()
		Map<String, String> getQueueManagers();
	}

	/**
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        Configuration for {@link ExportQueueManager}.
	 */
	public ExportQueueManager(InstantiationContext context, Config config) {
		super(context, config);

		_exportRegistry = ExportRegistryFactory.getExportRegistry();
		this.createExecutorMap();
	}

	@Override
	public String getDescription() {
		return "Holds different queues for asyncronous exports";
	}

	@Override
	public String getName() {
		return "ExportQueueManager";
	}

	public static ExportQueueManager getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	@Override
	public boolean reload() {

		internalShutDown();

		this.createExecutorMap();

		internalStartUp();

		return true;
	}

	private void createExecutorMap() {

		this.executors = new HashMap<>();

		boolean activated = getConfig().isActivated();
		if (activated) {
			for (Entry<String, String> entry : getConfig().getQueueManagers().entrySet()) {
				String theKey = entry.getKey();
				String theVal = entry.getValue();

				// Empty configuration value will ignore an executor
				// This is needed if projects want to disable executors inherited by basic projects.
				if (StringServices.isEmpty(theVal)) {
					if (Logger.isDebugEnabled(ExportQueueManager.class)) {
						Logger.debug("Ignoreing export queue: " + theKey, ExportQueueManager.class);
					}
					continue;
				}

				Properties theInnerProps = StringServices.getAllSemicolonSeparatedValues(theVal);
				String theTech = theInnerProps.getProperty(TECHNOLOGY);
				String theDura = theInnerProps.getProperty(DURATION);

				if (StringServices.isEmpty(theTech) || StringServices.isEmpty(theDura)) {
					throw new ConfigurationError("Configuration value must contain 'technology:' and 'duration:'");
				}

				ExportExecutor theExecutor = new ExportExecutor(theKey, 5000, theTech, theDura, _exportRegistry);
				this.executors.put(theKey, theExecutor);
				if (Logger.isDebugEnabled(ExportQueueManager.class)) {
					Logger.debug("Registered export queue: " + theExecutor, ExportQueueManager.class);
				}
			}

			Logger.info("Registered " + this.executors.size() + " export queues.", ExportQueueManager.class);
		}
	}

	@Override
	public boolean usesXMLProperties() {
		return true;
	}

	@Override
	protected void shutDown() {
		ReloadableManager.getInstance().removeReloadable(this);
		
		internalShutDown();

		super.shutDown();
	}

	private void internalShutDown() {
		for (ExportExecutor theExecutor : this.executors.values()) {
			theExecutor.shutdown();
			if (Logger.isDebugEnabled(ExportQueueManager.class)) {
				Logger.debug("Shutdown export queue" + theExecutor, ExportQueueManager.class);
			}
		}
	}

	@Override
	protected void startUp() {
		super.startUp();

		internalStartUp();

		ReloadableManager.getInstance().addReloadable(this);
	}

	private void internalStartUp() {
		for (ExportExecutor theExecutor : this.executors.values()) {
			theExecutor.start();
			if (Logger.isDebugEnabled(ExportQueueManager.class)) {
				Logger.debug("Started export queue" + theExecutor, ExportQueueManager.class);
			}
		}
	}

	/**
	 * An {@link ExportExecutor} periodically looks out for {@link Export} in
	 * queued state and will execute such {@link Export}s in {@link ExportRun}s.
	 * 
	 * Each {@link ExportExecutor} runs as own {@link Thread} and in its own
	 * {@link TLContext}.
	 * 
	 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
	 */
	public static class ExportExecutor extends Thread implements InContext {

		private volatile boolean shouldStop;
		private final long interval;
		private final String technology;
		private final String duration;
		private final ExportRegistry registry;

		// Constructors

		public ExportExecutor(String aName, long interval, String anExportTechnologie, String anExportDuration, ExportRegistry exportRegistry) {
			super(aName);
			this.setDaemon(true);
			this.interval = interval;
			this.technology = anExportTechnologie;
			this.duration = anExportDuration;
			this.registry = exportRegistry;
		}

		@Override
		public String toString() {
			return this.getName() + ": " + this.technology + " " + this.duration;
		}

		@Override
		public void run() {
			while (!shouldStop && !this.isInterrupted()) {
				TLContext.inSystemContext(ExportQueueManager.class, this);
				try {
					waitInterval();
				} catch (InterruptedException ex) {
					Logger.warn(getName() + " interrupted while sleeping. Exiting...", ex, ExportExecutor.class);
					return;
				}
			}
		}

		private synchronized void waitInterval() throws InterruptedException {
			if (shouldStop) {
				// Must not wait when thread should stop
				return;
			}
			wait(this.interval);
		}

		private synchronized void notifyStop() {
			this.shouldStop = true;
			notifyAll();
		}

		@Override
		public void inContext() {
			Export theExport = this.lookupQueue();
			if (theExport != null) {
				TLSubSessionContext session = TLContextManager.getSubSession();
				do {
					this.processExport(session, theExport);
					if (shouldStop) {
						break;
					}
					theExport = this.lookupQueue();
				} while (theExport != null);
			}
		}

		public void shutdown() {
			notifyStop();
			try {
				waitForEnd(this);
			} catch (InterruptedException ex) {
				Logger.warn("Interrupted shut down thread '" + Thread.currentThread().getName()
					+ "' while waiting for thread '" + getName() + "' to end.", ex, ExportExecutor.class);
			}
		}

		private void waitForEnd(Thread t) throws InterruptedException {
			int waitTime = 5000;
			t.join(waitTime);
			if (t.isAlive()) {
				Logger.warn("Thread '" + t.getName() + "' does not end within " + waitTime + "ms.",
					ExportExecutor.class);
			}
		}

		private void processExport(TLSubSessionContext sessionContext, Export anExport) {
			String systemId = null;
			Person oldPerson = null;
			Person exportPerson = anExport.getPerson();
			boolean changePerson = exportPerson != null;
			if (changePerson) {
				oldPerson = sessionContext.getPerson();
				if (oldPerson == null) {
					systemId = sessionContext.getContextId();
				}
				sessionContext.setPerson(exportPerson);
			}
			try {
				inUserContext(anExport);
			} finally {
				if (changePerson) {
					sessionContext.setPerson(oldPerson);
					if (oldPerson == null) {
						sessionContext.setContextId(systemId);
					}
				}
			}
		}

		private void inUserContext(Export anExport) {
			try {
				new ExportRun(anExport).run();
			} catch (Throwable ex) {
				Logger.error("Execution of export " + anExport.getExportHandlerID() + "/" + anExport.getModel()
					+ " failed unexpected.", ex, ExportExecutor.class);
			}
		}

		private Export lookupQueue() {
			try {
				return this.registry.getNextRunningExport(this.getExportTechnology(), this.getExportDuration());
			} catch (Throwable ex) {
				Logger.error("Lookup of queue exports failed for " + this.toString(), ex, ExportExecutor.class);
			}
			return null;
		}

		public String getExportDuration() {
			return (this.duration);
		}

		public String getExportTechnology() {
			return (this.technology);
		}
	}
	
	/**
	 * Module for {@link ExportQueueManager}
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public static final class Module extends TypedRuntimeModule<ExportQueueManager> {

		/**
		 * Module instance for {@link ExportQueueManager}
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ExportQueueManager> getImplementation() {
			return ExportQueueManager.class;
		}
	}

}
