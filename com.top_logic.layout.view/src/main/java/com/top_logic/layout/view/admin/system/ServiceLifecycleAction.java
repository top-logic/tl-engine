/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.ArrayList;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ViewAction;

/**
 * {@link ViewAction} returning the current application services for display in a {@link ServiceTable},
 * and optionally starting, stopping or restarting the selected service first.
 *
 * <p>
 * The returned {@code List<BasicRuntimeModule>} is meant to be written to the channel feeding the
 * {@link ServiceTable}. The service to act on is read from the configured
 * {@link Config#getSelection() selection channel} (not the command input, which carries the running
 * state used to gate the command). Dependent services are started / stopped / restarted automatically.
 * </p>
 *
 * @implNote Enumeration uses {@link ModuleUtil#getAllModules()}; lifecycle delegates to
 *           {@link ModuleUtil#startUp(BasicRuntimeModule)}, {@link ModuleUtil#shutDown(BasicRuntimeModule)}
 *           and {@link ModuleUtil#restart(BasicRuntimeModule)}.
 */
public class ServiceLifecycleAction implements ViewAction {

	/**
	 * What a {@link ServiceLifecycleAction} does before returning the service list.
	 */
	public enum Mode {
		/** Just return the current service snapshot. */
		REFRESH,

		/** Start the selected service (and its dependencies). */
		START,

		/** Stop the selected service (and its dependents). */
		STOP,

		/** Restart the selected service (and its dependents). */
		RESTART;
	}

	/**
	 * Configuration for {@link ServiceLifecycleAction}.
	 *
	 * <p>
	 * App-specific action, referenced by {@code class=} in the services view rather than claiming a
	 * global {@code @TagName}.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<ServiceLifecycleAction> {

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(ServiceLifecycleAction.class)
		Class<? extends ServiceLifecycleAction> getImplementationClass();

		/**
		 * What the action does before returning the service list.
		 */
		@Name(MODE)
		@Mandatory
		Mode getMode();

		/**
		 * Name of the channel holding the service to act on; required for all modes but
		 * {@link Mode#REFRESH}.
		 */
		@Name(SELECTION)
		@Nullable
		String getSelection();
	}

	private final Mode _mode;

	private final String _selectionChannel;

	/**
	 * Creates a new {@link ServiceLifecycleAction} from configuration.
	 */
	@CalledByReflection
	public ServiceLifecycleAction(InstantiationContext context, Config config) {
		_mode = config.getMode();
		_selectionChannel = config.getSelection();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (_mode != Mode.REFRESH) {
			BasicRuntimeModule<?> module = selectedModule(context);
			if (module != null) {
				apply(module);
			}
		}
		return new ArrayList<>(ModuleUtil.getAllModules());
	}

	/**
	 * Performs the configured lifecycle operation on the given module, reporting any failure via the
	 * {@link InfoService}.
	 */
	private void apply(BasicRuntimeModule<?> module) {
		try {
			switch (_mode) {
				case START:
					ModuleUtil.INSTANCE.startUp(module);
					break;
				case STOP:
					ModuleUtil.INSTANCE.shutDown(module);
					break;
				case RESTART:
					ModuleUtil.INSTANCE.restart(module);
					break;
				case REFRESH:
					break;
			}
		} catch (ModuleException | RuntimeException ex) {
			InfoService.showError(
				I18NConstants.ERROR_SERVICE_LIFECYCLE__NAME.fill(ModuleUtil.INSTANCE.getModuleName(module)),
				ResKey.text(ex.getMessage()));
		}
	}

	/**
	 * The module held by the configured selection channel, or {@code null} when nothing is selected.
	 */
	private BasicRuntimeModule<?> selectedModule(ReactContext context) {
		if (_selectionChannel == null || !(context instanceof ViewContext viewContext)) {
			return null;
		}
		if (!viewContext.hasChannel(_selectionChannel)) {
			return null;
		}
		Object value = viewContext.resolveChannel(new ChannelRef(_selectionChannel)).get();
		return value instanceof BasicRuntimeModule<?> module ? module : null;
	}
}
