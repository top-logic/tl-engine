/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import java.sql.SQLException;
import java.util.List;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} deleting no longer used {@link TLStructuredType}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeleteTLClassProcessor extends TLModelBaseLineMigrationProcessor<DeleteTLClassProcessor.Config> {

	/**
	 * Configuration options of {@link DeleteTLClassProcessor}.
	 */
	@TagName("delete-class")
	public interface Config extends TLModelBaseLineMigrationProcessor.Config<DeleteTLClassProcessor> {

		/**
		 * Qualified name of the {@link TLClass} to delete.
		 */
		@Mandatory
		QualifiedTypeName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedTypeName value);

		/**
		 * Whether it is a failure if the class is not empty.
		 */
		boolean isFailOnExistingAttributes();

		/**
		 * The {@link MetaObject table} in which {@link #getName()} stores the data.
		 * 
		 * <p>
		 * When the class is deleted, data for the class in the given table are removed, together
		 * with their flex attributes and all links from or to these objects.
		 * </p>
		 */
		@Nullable
		String getTypeTable();

	}

	private Util _util;

	/**
	 * Creates a {@link DeleteTLClassProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeleteTLClassProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	private boolean internalDoMigration(MigrationContext context, Log log, PooledConnection connection,
			Document tlModel) throws SQLException, MigrationException {
		QualifiedTypeName classToDelete = getConfig().getName();

		Type type;
		try {
			type = _util.getTLTypeOrFail(connection, classToDelete);
		} catch (MigrationException ex) {
			log.info(
				"No class with name '" + _util.qualifiedName(classToDelete) + "' to delete available at "
					+ getConfig().location(),
				Log.WARN);
			return false;
		}

		List<TypePart> parts = _util.getTLTypeParts(connection, type);
		_util.deleteTLType(connection, type, getConfig().isFailOnExistingAttributes());
		log.info("Deleted type " + _util.toString(type) + ".");

		ModelDataCleanup cleanup = new ModelDataCleanup(context, log, connection);
		cleanup.deleteReferenceValues(type.getBranch(), parts);

		String typeTable = getConfig().getTypeTable();
		if (typeTable != null) {
			int deletedRows = cleanup.deleteInstances(type.getBranch(), typeTable, List.of(type.getID()));
			log.info("Deleted " + deletedRows + " instances of type '" + _util.toString(type) + "' from table "
				+ typeTable + ".");
		}
		if (tlModel == null) {
			return false;
		}
		if (type.getKind() == Type.Kind.ASSOCIATION) {
			if (TLStructuredTypeColumns.isSyntheticAssociationName(type.getTypeName())) {
				// Synthetic associations are not contained in the model baseline.
				return false;
			}
		}
		return MigrationUtils.deleteType(log, tlModel, classToDelete);
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			return internalDoMigration(context, log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Delete tl class migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

}
