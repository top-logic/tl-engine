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
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLReference;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedPartName;

/**
 * {@link MigrationProcessor} to create an inverse {@link TLReference}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateInverseTLReferenceProcessor
		extends AbstractEndAspectProcessor<CreateInverseTLReferenceProcessor.Config> {

	/**
	 * Configuration options of {@link CreateInverseTLReferenceProcessor}.
	 */
	@TagName("create-inverse-reference")
	public interface Config extends AbstractEndAspectProcessor.Config<CreateInverseTLReferenceProcessor> {

		/**
		 * Qualified name of the inverse reference.
		 */
		@Mandatory
		@Name(ReferenceConfig.INVERSE_REFERENCE)
		QualifiedPartName getInverseReference();

	}

	private Util _util;

	/**
	 * Creates a {@link CreateInverseTLReferenceProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateInverseTLReferenceProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.get(Util.PROPERTY);
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error(
				"Creating invert reference " + _util.qualifiedName(getConfig().getName()) + " for reference "
					+ _util.qualifiedName(getConfig().getInverseReference()) + " failed at " + getConfig().location(),
				ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedPartName reference = getConfig().getName();
		QualifiedPartName inverseReference = getConfig().getInverseReference();
		_util.createInverseTLReference(log, connection, reference,
			inverseReference, getConfig().isMandatory(), getConfig().isComposite(), getConfig().isAggregate(),
			getConfig().isMultiple(), getConfig().isBag(), getConfig().isOrdered(), getConfig().canNavigate(), getConfig());

		log.info("Created inverse reference " + _util.qualifiedName(reference) + " for reference "
			+ _util.qualifiedName(inverseReference));
	}

}
