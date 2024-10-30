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
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} creating a primitive attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLPropertyProcessor extends AbstractCreateTypePartProcessor<CreateTLPropertyProcessor.Config> {

	/**
	 * Configuration options of {@link CreateTLPropertyProcessor}.
	 */
	@TagName("create-property")
	public interface Config extends AbstractCreateTypePartProcessor.Config<CreateTLPropertyProcessor> {

		/**
		 * See {@link PartConfig#getTypeSpec()}.
		 */
		@Mandatory
		@Name(PartConfig.TYPE_SPEC)
		QualifiedTypeName getType();

		/**
		 * Setter for {@link #getType()}.
		 */
		void setType(QualifiedTypeName value);

	}

	private Util _util;

	/**
	 * Creates a {@link CreateTLPropertyProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTLPropertyProcessor(InstantiationContext context, Config config) {
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

		Type owner = _util.getTLTypeOrNull(connection, partName.getOwner());
		if (owner == null) {
			log.info("Owner type for property does not exists: " + partName.getName(), Log.WARN);
			return;
		}

		TypePart existing = _util.getTLTypePart(connection, owner, partName.getPartName());
		if (existing != null) {
			log.info("Property already exists: " + partName.getName(), Log.WARN);
			return;
		}

		QualifiedTypeName targetType = (getConfig().getType());

		Type target = _util.getTLTypeOrNull(connection, targetType);
		if (target == null) {
			log.info("Type of property '" + partName.getName() + "' does not exist: " + targetType.getName(), Log.WARN);
			return;
		}

		_util.createTLProperty(log, connection, partName,
			targetType, getConfig().isMandatory(), getConfig().isAbstract(), getConfig().isMultiple(),
			getConfig().isBag(), getConfig().isOrdered(), getConfig());
		if (tlModel != null) {
			MigrationUtils.createAttribute(log, tlModel, partName, targetType, nullIfUnset(Config.MANDATORY),
				nullIfUnset(Config.MULTIPLE), nullIfUnset(Config.BAG), nullIfUnset(Config.ORDERED),
				nullIfUnset(Config.ABSTRACT),
				getConfig());
		}
		log.info("Created part " + _util.qualifiedName(partName));
	}

}
