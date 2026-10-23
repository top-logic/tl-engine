/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.layout.provider.label.I18NClassNameProvider;

/**
 * The application services (runtime modules) as read at one point in time, the value a
 * {@link ServiceTable} displays.
 *
 * <p>
 * The modules themselves are singletons whose state changes in place: starting a service, storing
 * its configuration or restarting it leaves the module the same object. A snapshot therefore stands
 * for the reading, not for the modules: two snapshots are never equal, even over the same modules,
 * so that writing a new one to a channel always tells its listeners to read the services' state
 * again.
 * </p>
 */
public final class ServiceSnapshot {

	private final List<BasicRuntimeModule<?>> _modules;

	private ServiceSnapshot(List<BasicRuntimeModule<?>> modules) {
		_modules = modules;
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
