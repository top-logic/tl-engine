/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.KIReferenceConfig;
import com.top_logic.knowledge.KnowledgeReferenceStorageImpl;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} to add {@link MOReference} to a persistent object type.
 */
@Label("Add database reference")
public class AddMOReferenceProcessor extends AddMOAttributeProcessor {

	/**
	 * Configuration options for {@link AddMOReferenceProcessor}.
	 */
	@TagName("add-mo-reference")
	public interface Config<I extends AddMOReferenceProcessor> extends AddMOAttributeProcessor.Config<I> {

		/**
		 * Configuration name of {@link #getReference()}
		 */
		String REFERENCE = MetaObjectConfig.REFERENCE_ELEMENT_PROPERTY;

		/**
		 * Definition of the {@link MOReference} to add to the {@link MetaObject} with name
		 * {@link #getTable()}.
		 */
		@Mandatory
		@Name(REFERENCE)
		KIReferenceConfig getReference();

		@Override
		@DerivedRef(REFERENCE)
		AttributeConfig getAttribute();

	}

	/**
	 * Creates a {@link AddMOReferenceProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AddMOReferenceProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void updateDatabaseTable(MigrationContext context, Log log, PooledConnection connection, MOClass table) {
		String tableName = ((DBTableMetaObject) table).getDBName();
		MOReference reference = (MOReference) table.getAttribute(getConfig().getAttribute().getAttributeName());

		SQLProcessor processor = new SQLProcessor(connection);
		createColumn(log, processor, tableName, reference.getColumn(ReferencePart.type),
			reference.isMandatory() ? "dummyTypeValue" : null);
		createColumn(log, processor, tableName, reference.getColumn(ReferencePart.name),
			IdentifierUtil.nullIdForMandatoryDatabaseColumns());
		createColumn(log, processor, tableName, reference.getColumn(ReferencePart.revision),
			KnowledgeReferenceStorageImpl.NULL_REPLACEMENT);
		if (context.hasBranchSupport()) {
			createColumn(log, processor, tableName, reference.getColumn(ReferencePart.branch),
				KnowledgeReferenceStorageImpl.NULL_REPLACEMENT);
		}

		createIndex(log, processor, table, tableName, reference);
	}

	private void createIndex(Log log, SQLProcessor processor, MOClass table, String tableName, MOReference reference) {
		DBIndex index = (DBIndex) reference.getIndex().resolve(table);
		String[] indexColumns = SchemaSetup.getIndexStrategy(table).createIndexColumns(table, index)
			.stream()
			.map(DBAttribute::getDBName)
			.toArray(String[]::new);
		String indexName = index.getDBName();
		try {
			processor.execute(
				addIndex(table(tableName), indexName, index.isUnique(), indexColumns));
			log.info("Created index '" + indexName + "' in table '" + tableName + "'.");
		} catch (SQLException ex) {
			log.error("Unable to create index '" + indexName + "' in table '" + tableName + "'.", ex);
		}
	}

}
