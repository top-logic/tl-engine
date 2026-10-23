/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.label.I18NClassNameProvider;

/**
 * The application services (runtime modules) and their status, as read at one point in time - the
 * value a table of the services displays.
 *
 * <p>
 * The modules themselves are singletons whose state changes in place: starting a service, storing
 * its configuration or restarting it leaves the module the same object. A snapshot therefore stands
 * for the reading, not for the modules: it records the {@link #status(BasicRuntimeModule) status}
 * of each module when it was taken, and two snapshots are never equal, even over the same modules,
 * so that writing a new one to a channel always tells its listeners to read the services again.
 * </p>
 */
public final class ServiceSnapshot {

	/**
	 * The status of a service, as a {@link ServiceSnapshot} records it.
	 */
	public enum Status {

		/** The service is running with its stored configuration. */
		ACTIVE(I18NConstants.SERVICE_STATE_ACTIVE),

		/**
		 * The service is running, but its stored configuration has changed since it was started, so
		 * that it must be restarted to apply it.
		 */
		RESTART_REQUIRED(I18NConstants.SERVICE_STATE_RESTART_REQUIRED),

		/** The service is stopped. */
		INACTIVE(I18NConstants.SERVICE_STATE_INACTIVE);

		private final ResKey _label;

		Status(ResKey label) {
			_label = label;
		}

		/**
		 * What the user reads for this status.
		 */
		public ResKey getLabel() {
			return _label;
		}

		/**
		 * The current status of the given module.
		 */
		public static Status of(BasicRuntimeModule<?> module) {
			if (!module.isActive()) {
				return INACTIVE;
			}
			return module.isConfigurationChanged() ? RESTART_REQUIRED : ACTIVE;
		}
	}

	private final List<BasicRuntimeModule<?>> _modules;

	private final Map<BasicRuntimeModule<?>, Status> _status;

	private ServiceSnapshot(List<BasicRuntimeModule<?>> modules) {
		_modules = modules;
		_status = new IdentityHashMap<>();
		for (BasicRuntimeModule<?> module : modules) {
			_status.put(module, Status.of(module));
		}
	}

	/**
	 * A snapshot of all modules of the application, sorted by their {@link #label(BasicRuntimeModule)
	 * label}.
	 */
	public static ServiceSnapshot current() {
		List<BasicRuntimeModule<?>> modules = new ArrayList<>(ModuleUtil.getAllModules());
		modules.sort(Comparator.comparing(ServiceSnapshot::label));
		return new ServiceSnapshot(List.copyOf(modules));
	}

	/**
	 * The modules of this snapshot, sorted by their {@link #label(BasicRuntimeModule) label}.
	 */
	public List<BasicRuntimeModule<?>> getModules() {
		return _modules;
	}

	/**
	 * The status of the given module when this snapshot was taken.
	 *
	 * @return The recorded status; the current one for a module this snapshot does not hold.
	 */
	public Status status(BasicRuntimeModule<?> module) {
		Status result = _status.get(module);
		return result == null ? Status.of(module) : result;
	}

	/**
	 * The label of the given module: the configured label of its implementation class.
	 */
	public static String label(BasicRuntimeModule<?> module) {
		return I18NClassNameProvider.INSTANCE.getLabel(module.getImplementation());
	}

	@Override
	public String toString() {
		return "ServiceSnapshot(" + _modules.size() + " modules)";
	}

}
