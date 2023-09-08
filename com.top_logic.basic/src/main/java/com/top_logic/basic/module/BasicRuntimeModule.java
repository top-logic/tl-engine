/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.annotation.Label;

/**
 * Abstract super class for services in <i>TopLogic</i> systems.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Service module")
public abstract class BasicRuntimeModule<M extends ManagedClass> {

	/** Empty {@link BasicRuntimeModule} array. */
	public static final BasicRuntimeModule<?>[] NO_MODULES = new BasicRuntimeModule<?>[0];

	private M impl;

	private List<ServiceDependency<M>> _dynamicDependencies = new CopyOnWriteArrayList<>();

	/**
	 * Convenience constant for the implementation of {@link #getDependencies()}
	 * in {@link RuntimeModule}s without dependencies.
	 */
	public static final Collection<Class<? extends BasicRuntimeModule<?>>> NO_DEPENDENCIES = Collections.emptyList();

	/**
	 * All {@link RuntimeModule}s that must be {@link #startUp() started} before this
	 * {@link RuntimeModule} can be started.
	 * 
	 * @see #getExtendedService()
	 */
	public abstract Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies();

	/**
	 * The class of the service for which this service is an add on.
	 * 
	 * <p>
	 * If the extended service (call is <code>S</code>) is started this service is also started. In
	 * contrast to {@link #getDependencies()}, no other service depending on <code>S</code> (except
	 * for other services extending <code>S</code>) is started before this module is started.
	 * </p>
	 * 
	 * @return The class of the extended service. May be <code>null</code>.
	 * 
	 * @see #getDependencies()
	 */
	public Class<? extends BasicRuntimeModule<?>> getExtendedService() {
		return null;
	}

	/**
	 * The implementation class that is represented by this
	 * {@link RuntimeModule} instance.
	 */
	public abstract Class<M> getImplementation();

	/**
	 * creates a new instance of the described service.
	 * 
	 * @return the new service which will be returned by
	 *         {@link #getImplementationInstance()}. never <code>null</code>
	 * 
	 * @throws ModuleException
	 *         iff creating the service failed for serveral reasons
	 */
	protected abstract M newImplementationInstance() throws ModuleException;

	/**
	 * Whether this {@link RuntimeModule} has been successfully
	 * {@link #startUp() started}.
	 */
	public boolean isActive() {
		return impl != null;
	}

	/**
	 * The currently active instance of this service.
	 */
	public M getImplementationInstance() {
		if (!isActive()) {
			throw ModuleUtil.invalidStateNotStarted(getImplementation());
		}

		return (impl);
	}

	/**
	 * Adds a {@link ServiceDependency} callback that is informed when the service represented by
	 * this {@link BasicRuntimeModule} starts and stops.
	 * 
	 * <p>
	 * Note: If this service is already active when {@link #addServiceDependency(ServiceDependency)}
	 * is called the callback {@link ServiceDependency#onConnect(ManagedClass)} is triggered
	 * immediately. Therefore, no additional check for {@link #isActive()} is necessary.
	 * </p>
	 *
	 * @param dependency
	 *        The dependency to add. {@link ServiceDependency#onConnect(ManagedClass)} is called
	 *        immediately, if {@link #isActive()}.
	 * 
	 * @see #removeServiceDependency(ServiceDependency)
	 */
	public void addServiceDependency(ServiceDependency<M> dependency) {
		_dynamicDependencies.add(dependency);

		if (isActive()) {
			dependency.onConnect(impl);
		}
	}

	/**
	 * Removes a {@link ServiceDependency} callback form this service.
	 * 
	 * <p>
	 * Note: If this service is still active when
	 * {@link #removeServiceDependency(ServiceDependency)} is called the callback
	 * {@link ServiceDependency#onDisconnect(ManagedClass)} is triggered immediately. Therefore, no
	 * additional check for {@link #isActive()} is necessary.
	 * </p>
	 *
	 * @param dependency
	 *        The dependency to remove. {@link ServiceDependency#onDisconnect(ManagedClass)} is
	 *        called immediately, if {@link #isActive()}.
	 * 
	 * @see #addServiceDependency(ServiceDependency)
	 */
	public void removeServiceDependency(ServiceDependency<M> dependency) {
		if (isActive()) {
			dependency.onDisconnect(impl);
		}

		_dynamicDependencies.remove(dependency);
	}

	void startUp() throws ModuleException {
		if (isActive()) {
			throw ModuleUtil.invalidStateAlreadyStarted(getImplementation());
		}
		Logger.info("Starting '" + getImplementation().getName() + "' described by module '" + getClass().getName() + "'", BasicRuntimeModule.class);

		startUp(newImplementationInstance());
		notifyStartupListeners();

		Logger.info("Service '" + getImplementation().getName() + "' successfully started.", BasicRuntimeModule.class);
	}

	private void notifyStartupListeners() {
		for (ServiceDependency<M> l : _dynamicDependencies) {
			try {
				l.onConnect(impl);
			} catch (Throwable ex) {
				Logger.error("Failed to inform service startup listener: " + l, ex, BasicRuntimeModule.class);
			}
		}
	}

	/**
	 * Starts the given service instance.
	 *
	 * @param newInstance
	 *        The new instance of the service to start.
	 * @throws ModuleException
	 *         If the service {@link ManagedClass#startUp() startup} fails for any reason.
	 */
	protected void startUp(M newInstance) throws ModuleException {
		impl = newInstance;
		try {
			startUpImplementation(newInstance);
		} catch (ModuleException ex) {
			// mark module as not started.
			impl = null;
			throw ex;
		}
	}

	/**
	 * Starts given instance of the described service.
	 * 
	 * @param newInstance
	 *        The service to start.
	 * @throws ModuleException
	 *         iff the new instance could not be started.
	 */
	protected void startUpImplementation(M newInstance) throws ModuleException {
		try {
			newInstance.doStart();
		} catch (Exception ex) {
			throw new ModuleException("Unable to start implementation '" + newInstance + "'.", ex, getImplementation());
		}
	}

	void shutDown() {
		M oldInstance = impl;
		if (!isActive()) {
			throw ModuleUtil.invalidStateNotStarted(getImplementation());
		}

		Logger.info("Shutting down '" + getImplementation().getName() + "' described by module '" + getClass().getName() + "'",
				BasicRuntimeModule.class);

		notifyShutdownListeners(oldInstance);
		try {
			shutDownImplementation(oldInstance);
			impl = null;
			Logger.info("Service '" + getImplementation().getName() + "' successfully shut down .",
				BasicRuntimeModule.class);
		} catch (Exception ex) {
			Logger.error("Unable to stop service " + getImplementation().getName(), ex, BasicRuntimeModule.class);
		}
	}

	private void notifyShutdownListeners(M oldInstance) {
		for (ServiceDependency<M> l : _dynamicDependencies) {
			try {
				l.onDisconnect(oldInstance);
			} catch (Throwable ex) {
				Logger.error("Failed to inform service shutdown listener: " + l, ex, BasicRuntimeModule.class);
			}
		}
	}

	/**
	 * Stops given instance of the described service.
	 * 
	 * @param oldInstance
	 *        The service to stop.
	 */
	protected void shutDownImplementation(M oldInstance) {
		oldInstance.doStop();
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getClass().getName());
		result.append('[');
		addToString(result);
		result.append(']');
		return result.toString();
	}

	/**
	 * Appends additional parts of the debug information of this service to the given builder.
	 */
	protected void addToString(StringBuilder result) {
		String implName = getImplementation() == null ? null : getImplementation().getName();
		result.append("impl:'").append(implName).append('\'');
	}
	
}
