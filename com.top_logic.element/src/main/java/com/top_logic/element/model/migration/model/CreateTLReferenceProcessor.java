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
import com.top_logic.element.config.PartConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLReference;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;
import com.top_logic.util.TLContext;

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

		/**
		 * Setter for {@link #getType()}
		 */
		void setType(QualifiedTypeName value);

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
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			internalDoMigration(log, connection, tlModel);
			return true;
		} catch (Exception ex) {
			log.error("Creating part migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		QualifiedPartName partName = getConfig().getName();

		Type ownerType =
			_util.getTLTypeOrNull(connection, TLContext.TRUNK_ID, partName.getModuleName(), partName.getTypeName());
		if (ownerType == null) {
			log.info("Type of part does not exist: " + partName.getName(), Log.WARN);
			return;
		}
		TypePart part = _util.getTLTypePart(connection, ownerType, partName.getPartName());
		if (part != null) {
			log.info("Part already exists: " + partName.getName(), Log.WARN);
			return;
		}

		QualifiedTypeName targetType = getConfig().getType();
		_util.createTLReference(log,
			connection, partName, targetType,
			getConfig().isMandatory(), getConfig().isAbstract(), getConfig().isComposite(),
			getConfig().isAggregate(), getConfig().isMultiple(), getConfig().isBag(),
			getConfig().isOrdered(),
			getConfig().canNavigate(),
			getConfig().getHistoryType(), getConfig().getDeletionPolicy(), getConfig());

		if (tlModel != null) {
			MigrationUtils.createReference(log, tlModel, partName, targetType, nullIfUnset(Config.MANDATORY),
				nullIfUnset(Config.COMPOSITE), nullIfUnset(Config.AGGREGATE), nullIfUnset(Config.MULTIPLE),
				nullIfUnset(Config.BAG), nullIfUnset(Config.ORDERED), nullIfUnset(Config.ABSTRACT),
				nullIfUnset(Config.NAVIGATE), nullIfUnset(Config.HISTORY_TYPE), nullIfUnset(Config.DELETION_POLICY),
				getConfig(), null);
		}
		log.info("Created reference " + _util.qualifiedName(partName));
	}

}
