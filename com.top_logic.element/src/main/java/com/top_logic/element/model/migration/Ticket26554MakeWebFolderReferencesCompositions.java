/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.objects.meta.DefaultMOFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.processors.SQLProcessor;
import com.top_logic.knowledge.wrap.WebFolder;

/**
 * {@link MigrationProcessor} that converts all references of type {@link WebFolder} to compositions
 * and moves the corresponding link objects to the composition child table.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Ticket26554MakeWebFolderReferencesCompositions implements MigrationProcessor {

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			tryMigrate(log, connection);
		} catch (SQLException ex) {
			log.error("Failed to update folder references: " + ex.getMessage(), ex);
		}
	}

	@Override
	public boolean isDeferred() {
		// Note: Migration tries to parse the table baseline schema. Therefore all code migrations
		// must have been applied to this baseline before the configuration can be loaded.
		return true;
	}

	private void tryMigrate(Log log, PooledConnection connection) throws SQLException {
		SchemaConfiguration persistentSchema =
			KBSchemaUtil.loadSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		SchemaSetup setup = TypedConfigUtil.createInstance(persistentSchema);
		MORepository types = setup.createMORepository(DefaultMOFactory.INSTANCE);

		SQLProcessor processor = new SQLProcessor(connection);

		Long folderModuleId = processor.querySingleValue(
			select(
				columns(columnDef("IDENTIFIER")),
				table("TL_MODULE"),
				eq(literalString("tl.folder"), column("NAME"))),
			Long.class);

		if (folderModuleId == null) {
			log.info("Skipping folder migration due to missing 'tl.folder' module.");
			return;
		}

		Long folderTypeId = processor.querySingleValue(
			select(
				columns(columnDef("IDENTIFIER")),
				table("META_ELEMENT"),
				and(
					eq(literalLong(folderModuleId), column("MODULE_ID")),
					eq(literalString("WebFolder"), column("NAME")))),
			Long.class);

		List<Long> folderEndIds = processor.queryValues(
			select(
				columns(columnDef("IDENTIFIER")),
				table("META_ATTRIBUTE"),
				and(
					eq(literalLong(folderTypeId), column("TYPE_ID")),
					eq(literalString("association-end"), column("IMPL")))),
			Long.class);

		if (folderEndIds.isEmpty()) {
			log.info("No folder references, skipping migration.");
			return;
		}

		List<Long> folderRefIds = processor.queryValues(
			select(
				columns(columnDef("IDENTIFIER")),
				table("META_ATTRIBUTE"),
				inSet(column("END_ID"),
					setLiteral(folderEndIds, DBType.LONG))),
			Long.class);

		int cntUpdate = processor.execute(
			update(
				table("META_ATTRIBUTE"),
				inSet(column("IDENTIFIER"),
					setLiteral(folderEndIds, DBType.LONG)),
				columnNames("COMPOSITE"),
				expressions(literalBooleanValue(true))));
		log.info("Updated '" + cntUpdate + "' folder references (IDs: " + folderRefIds + ").");

		MetaObject genericAssociationBase = types.getMetaObject("hasWrapperAttValueBaseAssociation");
		MOClass hasStructureChild = (MOClass) types.getMetaObject("hasStructureChild");

		List<SQLColumnDefinition> columns = hasStructureChild.getAttributes()
			.stream()
			.flatMap(a -> Arrays.asList(a.getDbMapping()).stream()).map(col -> columnDef(col.getDBName()))
			.collect(Collectors.toList());

		List<String> columnNames = hasStructureChild.getAttributes()
			.stream()
			.flatMap(a -> Arrays.asList(a.getDbMapping()).stream()).map(col -> col.getDBName())
			.collect(Collectors.toList());

		for (MetaObject existingType : types.getMetaObjects()) {
			if (existingType == hasStructureChild || MetaObjectUtils.isAbstract(existingType)
					|| !existingType.isSubtypeOf(genericAssociationBase)) {
				continue;
			}

			SQLExpression condition = inSet(column("META_ATTRIBUTE_ID"),
				setLiteral(folderRefIds, DBType.LONG));
			SQLTable sourceTable = table(((MOClass) existingType).getDBMapping().getDBName());

			int cntMove = processor.execute(
				insert(
					table(hasStructureChild.getDBMapping().getDBName()),
					columnNames,
					select(columns, sourceTable, condition)));
			log.info(
				"Moved '" + cntMove + "' folder links from '" + existingType + "' to '" + hasStructureChild + "'.");

			int cntDelete = processor.execute(
				delete(
					sourceTable,
					condition));
			log.info("Deleted '" + cntDelete + "' folder links from '" + existingType + "'.");
		}

	}

}
