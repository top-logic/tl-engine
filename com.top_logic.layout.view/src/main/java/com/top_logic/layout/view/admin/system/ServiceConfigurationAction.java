/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.WithResultActions;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} storing, discarding or removing the configuration of a service stored in the
 * application, returning the current application services afterwards.
 *
 * <p>
 * The service is the module held by the configured {@link Config#getModule() module channel}. A
 * changed configuration becomes effective with the next start of the service; until then, its module
 * {@link BasicRuntimeModule#isConfigurationChanged() reports a changed configuration}. The returned
 * {@link ServiceSnapshot} is meant to be written to the channel feeding a table of the services,
 * which then shows the changed state.
 * </p>
 *
 * @see TLServiceUtils#storeServiceConfiguration(BasicRuntimeModule, ServiceConfiguration)
 * @see TLServiceUtils#resetServiceConfiguration(BasicRuntimeModule)
 * @see TLServiceUtils#discardConfigurationChanges(BasicRuntimeModule)
 */
public class ServiceConfigurationAction implements ViewAction {

	/**
	 * What a {@link ServiceConfigurationAction} does with the configuration of the service.
	 */
	public enum Mode {
		/**
		 * Store the configuration given as input of the action in the application.
		 *
		 * @see TLServiceUtils#storeServiceConfiguration(BasicRuntimeModule, ServiceConfiguration)
		 */
		SAVE,

		/**
		 * Remove the configuration stored in the application, so that the configuration of the
		 * application's configuration files becomes effective again.
		 *
		 * @see TLServiceUtils#resetServiceConfiguration(BasicRuntimeModule)
		 */
		RESET,

		/**
		 * Discard the changes of the configuration stored in the application made since the service
		 * was started, so that the stored configuration is the one the service is running with.
		 *
		 * @see TLServiceUtils#discardConfigurationChanges(BasicRuntimeModule)
		 */
		DISCARD;
	}

	/**
	 * Configuration for {@link ServiceConfigurationAction}.
	 *
	 * <p>
	 * App-specific action, referenced by {@code class=} in the services view rather than claiming a
	 * global {@code @TagName}.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<ServiceConfigurationAction>, WithResultActions {

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		/** Configuration name for {@link #getModule()}. */
		String MODULE = "module";

		@Override
		@ClassDefault(ServiceConfigurationAction.class)
		Class<? extends ServiceConfigurationAction> getImplementationClass();

		/**
		 * What the action does with the configuration of the service.
		 */
		@Name(MODE)
		@Mandatory
		Mode getMode();

		/**
		 * Name of the channel holding the module of the service whose configuration is changed.
		 */
		@Name(MODULE)
		@Mandatory
		String getModule();
	}

	private final Mode _mode;

	private final String _moduleChannel;

	/**
	 * Creates a new {@link ServiceConfigurationAction} from configuration.
	 */
	@CalledByReflection
	public ServiceConfigurationAction(InstantiationContext context, Config config) {
		_mode = config.getMode();
		_moduleChannel = config.getModule();
	}

	/**
	 * Changes the configuration of the service.
	 *
	 * @param input
	 *        For {@link Mode#SAVE}, the {@link ServiceConfiguration} to store, e.g. the working copy
	 *        a {@code <config-form>} hands to its save actions; ignored for the other modes.
	 * @throws TopLogicException
	 *         When the configuration cannot be stored, e.g. because it violates its constraints.
	 */
	@Override
	public Object execute(ReactContext context, Object input) {
		BasicRuntimeModule<?> module = ServiceLifecycleAction.selectedModule(context, _moduleChannel);
		if (module == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SERVICE);
		}
		switch (_mode) {
			case SAVE:
				if (!(input instanceof ServiceConfiguration<?> config)) {
					throw new TopLogicException(I18NConstants.ERROR_NO_SERVICE_CONFIGURATION);
				}
				TLServiceUtils.storeServiceConfiguration(module, config);
				ViewMessages.info(context, I18NConstants.SERVICE_CONFIGURATION_SAVED);
				break;
			case RESET:
				if (TLServiceUtils.resetServiceConfiguration(module)) {
					ViewMessages.info(context, I18NConstants.SERVICE_CONFIGURATION_RESET);
				}
				break;
			case DISCARD:
				if (TLServiceUtils.discardConfigurationChanges(module)) {
					ViewMessages.info(context, I18NConstants.SERVICE_CONFIGURATION_DISCARDED);
				}
				break;
		}
		return ServiceSnapshot.current();
	}

	/**
	 * Tells the user that the configuration has been changed.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
