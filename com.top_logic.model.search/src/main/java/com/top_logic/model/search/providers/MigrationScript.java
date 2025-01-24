/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.knowledge.service.migration.StartupAction;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link StartupAction} written in TL-Script.
 */
public class MigrationScript extends AbstractConfiguredInstance<MigrationScript.Config> implements StartupAction {

	/**
	 * Configuration options for {@link MigrationScript}.
	 */
	@TagName("migration-script")
	public interface Config extends PolymorphicConfiguration<MigrationScript> {
		/**
		 * The TL-Script to execute after first application startup with a new software version.
		 */
		Expr getScript();
	}

	private final QueryExecutor _script;

	/**
	 * Creates a {@link MigrationScript} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MigrationScript(InstantiationContext context, Config config) {
		super(context, config);

		_script = QueryExecutor.compile(config.getScript());
	}

	@Override
	public void perform() {
		_script.execute();
	}

}
