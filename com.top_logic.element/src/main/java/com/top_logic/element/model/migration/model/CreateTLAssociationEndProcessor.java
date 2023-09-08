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
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLAssociationEnd;

/**
 * {@link MigrationProcessor} creating {@link TLAssociationEnd}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLAssociationEndProcessor
		extends AbstractEndAspectProcessor<CreateTLAssociationEndProcessor.Config> {

	/**
	 * Configuration options of {@link CreateTLAssociationEndProcessor}.
	 */
	@TagName("create-association-end")
	public interface Config extends AbstractEndAspectProcessor.Config<CreateTLAssociationEndProcessor> {

		/**
		 * Target type of the {@link TLAssociationEnd}.
		 * 
		 * @see PartConfig#getTypeSpec()
		 */
		@Mandatory
		@Name(PartConfig.TYPE_SPEC)
		QualifiedTypeName getType();


	}

	/**
	 * Creates a {@link CreateTLAssociationEndProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTLAssociationEndProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Creating association end migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedPartName partName = getConfig().getName();
		QualifiedTypeName targetType = getConfig().getType();
		Util.createTLAssociationEnd(connection, partName, targetType,
			getConfig().isMandatory(), getConfig().isComposite(), getConfig().isAggregate(),
			getConfig().isMultiple(), getConfig().isBag(),
			getConfig().isOrdered(),
			getConfig().canNavigate(), getConfig());

		log.info("Created part " + Util.qualifiedName(partName));
	}

}
