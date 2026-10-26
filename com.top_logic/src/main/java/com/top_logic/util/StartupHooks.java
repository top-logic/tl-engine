/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.RuntimeCategory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.migration.StartupAction;
import com.top_logic.util.model.ModelService;

/**
 * Executes the configured {@link StartupAction}s after all services of the application are
 * started.
 *
 * <p>
 * A startup hook is the place for actions that set up data on startup and may require any service
 * of the application, e.g. imports of configuration files. Such an action cannot be a service
 * itself, since the services it requires cannot be declared as its dependencies.
 * </p>
 *
 * <p>
 * The hooks run after the startup actions of the data migration and after the startup transaction
 * is committed. Each hook runs in its own transaction, in the order of their configuration.
 * </p>
 */
@ServiceDependencies({
	PersistencyLayer.Module.class,
	ModelService.Module.class,
})
@Label("Startup hooks")
@ServiceCategory.Of(RuntimeCategory.class)
public class StartupHooks extends ManagedClass {

	/**
	 * Configuration of the {@link StartupHooks}.
	 */
	public interface Config extends ServiceConfiguration<StartupHooks> {

		/** Configuration name for {@link #getHooks()}. */
		String HOOKS = "hooks";

		/**
		 * The hooks to execute after all services are started, in execution order.
		 */
		@Name(HOOKS)
		@Key(HookConfig.ID)
		@EntryTag(HookConfig.TAG_NAME)
		List<HookConfig> getHooks();

	}

	/**
	 * Configuration of a single startup hook.
	 */
	public interface HookConfig extends ConfigurationItem {

		/** Tag name of a {@link HookConfig} in a list. */
		String TAG_NAME = "hook";

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/** Configuration name for {@link #getOnError()}. */
		String ON_ERROR = "on-error";

		/** Configuration name for {@link #getAction()}. */
		String ACTION = "action";

		/**
		 * Unique name of the hook, used to reference the hook in configuration overlays.
		 */
		@Name(ID)
		@Mandatory
		String getId();

		/**
		 * What happens, when the hook fails.
		 */
		@Name(ON_ERROR)
		OnError getOnError();

		/**
		 * The action to execute.
		 */
		@Name(ACTION)
		@Mandatory
		PolymorphicConfiguration<? extends StartupAction> getAction();

	}

	/**
	 * Reaction to a failure of a startup hook.
	 */
	public enum OnError implements ExternallyNamed {

		/**
		 * The startup of the application is aborted.
		 */
		FAIL("fail"),

		/**
		 * The failure is logged as error and the remaining hooks are executed.
		 */
		LOG_ERROR("log-error"),

		/**
		 * The failure is logged as warning and the remaining hooks are executed.
		 */
		LOG_WARN("log-warn");

		private final String _externalName;

		private OnError(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

	}

	private final List<HookConfig> _hooks;

	/**
	 * Creates a {@link StartupHooks} from configuration.
	 */
	public StartupHooks(InstantiationContext context, Config config) {
		super(context, config);
		_hooks = config.getHooks();
	}

	/**
	 * Executes all configured hooks.
	 *
	 * <p>
	 * Must be called after all services of the application are started, outside of any
	 * transaction.
	 * </p>
	 */
	public void runHooks() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		for (HookConfig hook : _hooks) {
			Logger.info("Performing startup hook: " + hook.getId(), StartupHooks.class);
			try (Transaction tx = kb.beginTransaction(I18NConstants.PERFORMED_STARTUP_HOOK__ID.fill(hook.getId()))) {
				StartupAction action = TypedConfigUtil.createInstance(hook.getAction());
				action.perform();
				tx.commit();
			} catch (RuntimeException ex) {
				String message = "Startup hook '" + hook.getId() + "' failed.";
				switch (hook.getOnError()) {
					case FAIL:
						throw ex;
					case LOG_ERROR:
						Logger.error(message, ex, StartupHooks.class);
						break;
					case LOG_WARN:
						Logger.warn(message, ex, StartupHooks.class);
						break;
				}
			}
		}
	}

	/**
	 * Singleton holder for the {@link StartupHooks}.
	 */
	public static final class Module extends TypedRuntimeModule<StartupHooks> {

		/**
		 * Singleton {@link StartupHooks.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<StartupHooks> getImplementation() {
			return StartupHooks.class;
		}
	}

}
