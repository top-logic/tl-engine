/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import java.sql.SQLException;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} for deleting a {@link TLEnumeration}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DeleteTLEnumerationProcessor extends AbstractConfiguredInstance<DeleteTLEnumerationProcessor.Config>
		implements TLModelBaseLineMigrationProcessor {

	/** {@link ConfigurationItem} for the {@link DeleteTLEnumerationProcessor}. */
	@TagName("delete-enumeration")
	public interface Config extends PolymorphicConfiguration<DeleteTLEnumerationProcessor>,
			TLModelBaseLineMigrationProcessor.SkipModelBaselineApaption {

		/** Qualified name of the {@link TLEnumeration} to delete. */
		@Mandatory
		QualifiedTypeName getName();

		/** @see #getName() */
		void setName(QualifiedTypeName value);

		/** Whether it is a failure if the {@link TLEnumeration} still has a {@link TLClassifier}. */
		boolean isFailOnExistingClassifiers();

	}

	private Util _util;

	/** {@link TypedConfiguration} constructor for {@link DeleteTLEnumerationProcessor}. */
	@CalledByReflection
	public DeleteTLEnumerationProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel)
			throws SQLException, MigrationException {
		QualifiedTypeName classToDelete = getConfig().getName();

		Type type = findType(log, connection, classToDelete);
		if (type == null) {
			return false;
		}

		_util.deleteTLType(connection, type, getConfig().isFailOnExistingClassifiers());
		log.info("Deleted TLEnumeration '" + _util.toString(type) + "'.");

		if (tlModel == null || getConfig().isSkipModelBaselineChange()) {
			return false;
		}
		return MigrationUtils.deleteType(log, tlModel, classToDelete);
	}

	private Type findType(Log log, PooledConnection connection, QualifiedTypeName tlEnumeration) throws SQLException {
		try {
			return _util.getTLTypeOrFail(connection, tlEnumeration);
		} catch (MigrationException ex) {
			String message = "No class with name '" + _util.qualifiedName(tlEnumeration) + "' to delete available at "
				+ getConfig().location();
			log.info(message, Log.WARN);
			return null;
		}
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Delete TLEnumeration '" + getConfig().getName() + "' migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

}
