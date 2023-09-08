/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.util.Computation;

/**
 * Container for all configured {@link DBSetupAction}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DBSetupActions {

	/**
	 * Configuration for {@link DBSetupActions}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<DBSetupActions> {

		/**
		 * All actions that take part in the setup process.
		 */
		List<PolymorphicConfiguration<DBSetupAction>> getActions();

	}

	/**
	 * Returns a {@link DBSetupActions} for executing all configured {@link DBSetupAction}s
	 */
	public static DBSetupActions newInstance() {
		return newInstance(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY);
	}

	/**
	 * Returns a {@link DBSetupActions} for executing all configured {@link DBSetupAction}s
	 */
	public static DBSetupActions newInstance(InstantiationContext context) {
		Config config = ApplicationConfig.getInstance().getConfig(DBSetupActions.Config.class);
		return context.getInstance(config);
	}

	final List<DBSetupAction> _actions;

	/**
	 * Creates a new {@link DBSetupActions} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DBSetupActions}.
	 */
	public DBSetupActions(InstantiationContext context, Config config) {
		_actions = TypedConfiguration.getInstanceList(context, config.getActions());
	}

	/**
	 * Create all tables using the configured {@link DBSetupAction}.
	 * 
	 * @see DBSetupAction#doCreateTables(CreateTablesContext)
	 */
	public void createTables(final CreateTablesContext context) throws Exception {
		Exception problem = ModuleUtil.INSTANCE.inModuleContext(new Computation<Exception>() {

			@Override
			public Exception run() {
				try {
					for (DBSetupAction action : DBSetupActions.this._actions) {
						action.doCreateTables(context);
					}
					return null;
				} catch (Exception ex) {
					return ex;
				}
			}
		}, getDependencies().toArray(BasicRuntimeModule.NO_MODULES));
		if (problem != null) {
			throw problem;
		}
	}

	/**
	 * Drops all tables using the configured {@link DBSetupAction}.
	 * 
	 * @see DBSetupAction#doDropTables(DropTablesContext, PrintWriter)
	 */
	public void dropTables(final DropTablesContext context, final PrintWriter out) throws Exception {
		Exception problem = ModuleUtil.INSTANCE.inModuleContext(new Computation<Exception>() {

			@Override
			public Exception run() {
				try {
					for (DBSetupAction action : DBSetupActions.this._actions) {
						action.doDropTables(context, out);
					}
					return null;
				} catch (Exception ex) {
					return ex;
				}
			}
		}, getDependencies().toArray(BasicRuntimeModule.NO_MODULES));
		if (problem != null) {
			throw problem;
		}
	}

	/**
	 * Returns the {@link BasicRuntimeModule} which are needed for creation or drop of tables.
	 */
	public Collection<? extends BasicRuntimeModule<?>> getDependencies() {
		Collection<? extends BasicRuntimeModule<?>> firstActionDependency = null;
		Collection<BasicRuntimeModule<?>> dependency = null;

		for (DBSetupAction action : _actions) {
			Collection<? extends BasicRuntimeModule<?>> actionDependency = action.getDependencies();
			if (actionDependency.isEmpty()) {
				continue;
			}
			if (firstActionDependency == null) {
				firstActionDependency = actionDependency;
				continue;
			}
			if (dependency == null) {
				dependency = new HashSet<>(firstActionDependency);
			}
			dependency.addAll(actionDependency);
		}
		if (dependency != null) {
			return dependency;
		}
		if (firstActionDependency != null) {
			return firstActionDependency;
		}
		return Collections.emptySet();
	}

}

