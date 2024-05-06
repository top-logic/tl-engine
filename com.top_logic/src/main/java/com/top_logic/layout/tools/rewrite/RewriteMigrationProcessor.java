/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.rewrite;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;

/**
 * {@link MigrationProcessor} applying a configured {@link DocumentRewrite} operation.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class RewriteMigrationProcessor<C extends RewriteMigrationProcessor.Config<?>>
		extends AbstractConfiguredInstance<C> implements MigrationProcessor {

	private DocumentRewrite _rewriter;

	private Util _util;

	/**
	 * Configuration options for {@link RewriteMigrationProcessor}.
	 */
	public interface Config<I extends RewriteMigrationProcessor<?>> extends PolymorphicConfiguration<I> {

		/**
		 * The {@link DocumentRewrite} implementation to be applied.
		 */
		@Name("rewriter")
		PolymorphicConfiguration<? extends DocumentRewrite> getRewriter();

	}

	/**
	 * Creates a {@link RewriteMigrationProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public RewriteMigrationProcessor(InstantiationContext context, C config) {
		super(context, config);

		_rewriter = context.getInstance(config.getRewriter());
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		_rewriter.init(log);
		_util = context.getSQLUtils();

		doMigration(log, connection, _rewriter);
	}

	/**
	 * Migration utilities.
	 */
	public Util util() {
		return _util;
	}

	/**
	 * Executes the migration with the given {@link DocumentRewrite}.
	 */
	protected abstract void doMigration(Log log, PooledConnection connection, DocumentRewrite rewriter);

}
