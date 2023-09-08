/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;

/**
 * A {@link ServiceStarter} for some {@link BasicRuntimeModule} can be used to
 * start the given service using {@link #startService()}. After having used that
 * module the module can be shut down using {@link #stopService()}.
 * 
 * The advantage using a {@link ServiceStarter} instead of using
 * {@link ModuleUtil} directly is, that not just the given module is shut down
 * after usagem, but also all dependencies of it which had been started when
 * starting the service.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ServiceStarter {

	/**
	 * @see #getServiceToStart()
	 */
	private final BasicRuntimeModule<?> _module;
	/**
	 * contains all {@link BasicRuntimeModule} which are started during
	 * {@link #startService()}
	 */
	private final List<BasicRuntimeModule<?>> _startedModules = new ArrayList<>();

	/**
	 * contains all started {@link BasicRuntimeModule} which are started after
	 * starting the {@link #_module}.
	 */
	private ArrayList<BasicRuntimeModule<?>> _formerlyStartedModules;

	/**
	 * @see #hasStartedService()
	 */
	private boolean _serviceStarted = false;

	public ServiceStarter(BasicRuntimeModule<?> module) {
		this._module = module;
	}

	/**
	 * Starts the service with all dependencies.
	 * 
	 * @throws Exception
	 *         iff the service or some dependenvy throws some.
	 */
	public void startService() throws Exception {
		if (_module.isActive()) {
			return;
		}
		try {
			ModuleUtil.INSTANCE.startModulesAndAdd(_module, _startedModules);
		} catch (Exception ex) {
			shutdownStartedModules();
			throw ex;
		}
		_formerlyStartedModules = new ArrayList<>(ModuleUtil.INSTANCE.getActiveModules());
		_serviceStarted = true;
	}

	/**
	 * Stops the service and all services which has also been started by
	 * {@link #startService()}
	 * 
	 * @return true iff the service has started by this {@link ServiceStarter}
	 * 
	 * @throws IllegalStateException
	 *         iff someone else has started additional services after
	 *         {@link #startService()} but has not shut them down.
	 */
	public boolean stopService() throws IllegalStateException {
		if (!_serviceStarted) {
			// startService has not been completed normally, e.g. the module to
			// start was already active or some exception occurred. In that case
			// nothing is to do.
			return false;
		}
		shutdownStartedModules();

		HashSet<BasicRuntimeModule<?>> startedInTheMeantime = new HashSet<>(ModuleUtil.INSTANCE.getActiveModules());
		startedInTheMeantime.removeAll(_formerlyStartedModules);
		_formerlyStartedModules = null;
		_serviceStarted = false;
		if (!startedInTheMeantime.isEmpty()) {
			throw new IllegalStateException("Between starting and stopping '" + _module + "', following services were started but not shut down: "
					+ startedInTheMeantime);
		}
		return true;
	}

	/**
	 * the service to start by this {@link ServiceStarter}.
	 */
	public BasicRuntimeModule<?> getServiceToStart() {
		return _module;
	}

	/**
	 * whether the service {@link #getServiceToStart()} has been started
	 *         by this {@link ServiceStarter} and {@link #startService()}
	 *         completes normally.
	 */
	public boolean hasStartedService() {
		return _serviceStarted;
	}

	private void shutdownStartedModules() {
		for (int index = _startedModules.size() - 1; index >= 0; index--) {
			ModuleUtil.INSTANCE.shutDown(_startedModules.get(index));
		}
		_startedModules.clear();
	}

	@Override
	public String toString() {
		return ServiceStarter.class.getName() + "[service:" + _module + "]";
	}

}
