/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.PartConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLReference;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;

/**
 * {@link MigrationProcessor} creates a new {@link TLReference}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLReferenceProcessor extends AbstractEndAspectProcessor<CreateTLReferenceProcessor.Config> {

	/**
	 * Configuration options of {@link CreateTLReferenceProcessor}.
	 */
	@TagName("create-reference")
	public interface Config extends AbstractEndAspectProcessor.Config<CreateTLReferenceProcessor> {

		/**
		 * Qualified name of the target type.
		 * 
		 * @see PartConfig#getTypeSpec()
		 */
		@Mandatory
		@Name(PartConfig.TYPE_SPEC)
		QualifiedTypeName getType();

	}

	private Util _util;

	/**
	 * Creates a {@link CreateTLReferenceProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTLReferenceProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.get(Util.PROPERTY);
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Creating part migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedPartName partName = getConfig().getName();
		QualifiedTypeName targetType = getConfig().getType();
		_util.createTLReference(connection,
			partName, targetType, getConfig().isMandatory(),
			getConfig().isComposite(), getConfig().isAggregate(),
			getConfig().isMultiple(), getConfig().isBag(), getConfig().isOrdered(),
			getConfig().canNavigate(),
			getConfig());

		log.info("Created reference " + _util.qualifiedName(partName));
	}

}
