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
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.Module;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} updating a {@link TLEnumeration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpdateTLEnumerationProcessor
		extends TLModelBaseLineMigrationProcessor<UpdateTLEnumerationProcessor.Config> {

	/**
	 * Configuration options of {@link UpdateTLEnumerationProcessor}.
	 * 
	 * @see CreateTLClassifierProcessor.Config
	 */
	@TagName("update-enum")
	public interface Config
			extends TLModelBaseLineMigrationProcessor.Config<UpdateTLEnumerationProcessor>,
			AnnotatedConfig<TLTypeAnnotation> {

		/**
		 * Qualified name of the {@link TLEnumeration} to update.
		 */
		QualifiedTypeName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedTypeName value);

		/**
		 * New name of the {@link TLEnumeration} including the new module (in case of a move
		 * operation).
		 */
		@Nullable
		QualifiedTypeName getNewName();

		/**
		 * Setter for {@link #getNewName()}.
		 */
		void setNewName(QualifiedTypeName value);

	}

	private Util _util;

	/**
	 * Creates a {@link UpdateTLEnumerationProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UpdateTLEnumerationProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Update enum migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		QualifiedTypeName typeName = getConfig().getName();
		Type type;
		try {
			type = _util.getTLTypeOrFail(connection, typeName);
		} catch (MigrationException ex) {
			log.info(
				"Unable to find enum to update " + _util.qualifiedName(typeName) + " at " + getConfig().location(),
				Log.WARN);
			return false;
		}
		Module newModule;
		QualifiedTypeName newName = getConfig().getNewName();
		if (newName == null || typeName.getModuleName().equals(newName.getModuleName())) {
			newModule = null;
		} else {
			newModule = _util.getTLModuleOrFail(connection, newName.getModuleName());
		}
		String localName;
		if (newName == null || typeName.getTypeName().equals(newName.getTypeName())) {
			localName = null;
		} else {
			localName = newName.getTypeName();
		}
		_util.updateTLEnumeration(connection, type, newModule, localName, getConfig());
		if (tlModel != null) {
			MigrationUtils.updateEnum(log, tlModel, typeName, newName, getConfig());
		}
		log.info("Updated enum " + _util.qualifiedName(typeName));
		return true;
	}

}
