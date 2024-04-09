/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import org.w3c.dom.Document;

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

		/**
		 * Setter for {@link #getInverseReference()}.
		 */
		void setInverseReference(QualifiedPartName value);

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
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.get(Util.PROPERTY);
			internalDoMigration(log, connection, tlModel);
			return true;
		} catch (Exception ex) {
			log.error("Creating invert reference " + _util.qualifiedName(getConfig().getName()) + " for reference "
					+ _util.qualifiedName(getConfig().getInverseReference()) + " failed at " + getConfig().location(),
				ex);
			return false;
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		QualifiedPartName reference = getConfig().getName();
		QualifiedPartName inverseReference = getConfig().getInverseReference();
		_util.createInverseTLReference(log, connection, reference,
			inverseReference, getConfig().isMandatory(), getConfig().isComposite(), getConfig().isAggregate(),
			getConfig().isMultiple(), getConfig().isBag(), getConfig().isOrdered(), getConfig().canNavigate(), getConfig());
		
		if (tlModel != null) {
			MigrationUtils.createBackReference(log, tlModel, reference, inverseReference, nullIfUnset(Config.MANDATORY),
				nullIfUnset(Config.COMPOSITE), nullIfUnset(Config.AGGREGATE), nullIfUnset(Config.MULTIPLE),
				nullIfUnset(Config.BAG), nullIfUnset(Config.ORDERED), nullIfUnset(Config.NAVIGATE),
				nullIfUnset(Config.HISTORY_TYPE), getConfig(),
				null);
		}
		log.info("Created inverse reference " + _util.qualifiedName(reference) + " for reference "
			+ _util.qualifiedName(inverseReference));
	}

}
