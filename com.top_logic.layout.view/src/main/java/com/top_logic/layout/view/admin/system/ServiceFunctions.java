/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions about the application services (runtime modules): what a view administering
 * a service displays of it.
 *
 * <p>
 * A service is given by its module, e.g. the row of a {@link ServiceTable} a command was activated
 * for. Every function accepts no module at all and answers with nothing, so that a channel computed
 * from a module that is not yet known stays empty.
 * </p>
 *
 * <pre>
 * &lt;derived-channel name="changed"
 *   expr="serviceConfigurationChanged($module)"
 *   inputs="module, services"/&gt;
 * </pre>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #configuration(BasicRuntimeModule)} is called <code>serviceConfiguration</code>.
 *           The functions read the state of the services, which only an administrator may inspect
 *           from a script entered interactively.
 */
@ScriptPrefix(ServiceFunctions.PREFIX)
@AdminOnly
public class ServiceFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "service";

	/**
	 * The label of the given service, the configured label of its implementation class.
	 *
	 * @param module
	 *        The module of the service.
	 * @return The label, <code>null</code> without a module.
	 */
	@Label("Label of a service")
	public static String label(BasicRuntimeModule<?> module) {
		return module == null ? null : ServiceSnapshot.label(module);
	}

	/**
	 * The technical name of the given service, which identifies it, e.g. in the address of a page
	 * displaying it.
	 *
	 * @param module
	 *        The module of the service.
	 * @return The qualified name of the implementation class of the service, <code>null</code>
	 *         without a module.
	 *
	 * @see #byName(String)
	 */
	@Label("Name of a service")
	public static String name(BasicRuntimeModule<?> module) {
		return module == null ? null : module.getImplementation().getName();
	}

	/**
	 * The service of the given technical name.
	 *
	 * @param name
	 *        The name of the service, see {@link #name(BasicRuntimeModule)}.
	 * @return The module of the service, <code>null</code> if there is no service of that name.
	 */
	@Label("Service by its name")
	public static BasicRuntimeModule<?> byName(String name) {
		if (name == null) {
			return null;
		}
		return ModuleUtil.getAllModules().stream()
			.filter(module -> module.getImplementation().getName().equals(name))
			.findFirst()
			.orElse(null);
	}

	/**
	 * Whether the given service is running.
	 *
	 * @param module
	 *        The module of the service.
	 * @return Whether the service is started, <code>false</code> without a module.
	 */
	@Label("Whether a service is running")
	public static boolean active(BasicRuntimeModule<?> module) {
		return module != null && module.isActive();
	}

	/**
	 * An editable copy of the configuration of the given service.
	 *
	 * @param module
	 *        The module of the service.
	 * @return A copy of the configuration that becomes effective with the next start of the
	 *         service, <code>null</code> if the service has no configuration, and without a module.
	 *
	 * @see TLServiceUtils#getServiceConfiguration(BasicRuntimeModule)
	 */
	@Label("Configuration of a service")
	public static ServiceConfiguration<? extends ManagedClass> configuration(BasicRuntimeModule<?> module) {
		return module == null ? null : TLServiceUtils.getServiceConfiguration(module);
	}

	/**
	 * Whether the configuration of the given service may be changed in the application.
	 *
	 * @param module
	 *        The module of the service.
	 * @return Whether the service has a configuration and its implementation does not refuse a
	 *         configuration changed in the application, <code>false</code> without a module.
	 *
	 * @see TLServiceUtils#isInAppEditable(BasicRuntimeModule)
	 */
	@Label("Whether the configuration of a service can be edited")
	public static boolean configurationEditable(BasicRuntimeModule<?> module) {
		return module != null && TLServiceUtils.isInAppEditable(module)
			&& TLServiceUtils.hasServiceConfiguration(module);
	}

	/**
	 * Whether a configuration of the given service is stored in the application, replacing the
	 * configuration of the application's configuration files.
	 *
	 * @param module
	 *        The module of the service.
	 * @return Whether a stored configuration exists that a reset would remove, <code>false</code>
	 *         without a module.
	 *
	 * @see TLServiceUtils#hasInAppConfiguration(BasicRuntimeModule)
	 */
	@Label("Whether a configuration of a service is stored in the application")
	public static boolean configurationStored(BasicRuntimeModule<?> module) {
		return module != null && TLServiceUtils.hasInAppConfiguration(module);
	}

	/**
	 * Whether the configuration of the given service has changed since the service was started, so
	 * that it must be restarted to apply it.
	 *
	 * @param module
	 *        The module of the service.
	 * @return Whether the configuration has changed, <code>false</code> without a module.
	 *
	 * @see BasicRuntimeModule#isConfigurationChanged()
	 */
	@Label("Whether the configuration of a service has changed since its start")
	public static boolean configurationChanged(BasicRuntimeModule<?> module) {
		return module != null && module.isConfigurationChanged();
	}

}
