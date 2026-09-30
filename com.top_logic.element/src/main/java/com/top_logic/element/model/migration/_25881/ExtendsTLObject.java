/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration._25881;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.Log;
import com.top_logic.basic.LongID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLStatement;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.element.meta.kbbased.KBBasedMetaElement;
import com.top_logic.knowledge.service.db2.SourceReference;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.Module;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} ensuring that all {@link TLClass} extends (inherited)
 * {@link TLObject}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExtendsTLObject implements MigrationProcessor {

	private Util _util;

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.getSQLUtils();

			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Unable to ensure that all TLClasses extends tl.model:TLObject.", ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		Module tlModelModule = _util.getTLModuleOrFail(connection, TlModelFactory.TL_MODEL_STRUCTURE);
		Type tlObjectType = _util.getTLTypeOrFail(connection, tlModelModule, TLObject.TL_OBJECT_TYPE);

		Map<TLID, String> identifiers = new HashMap<>();
		long branch = tlObjectType.getBranch();
		addTLClasses(connection, identifiers, branch);
		removeClassesWithGeneralizations(connection, identifiers.keySet(), branch);
		if (identifiers.isEmpty()) {
			return;
		}
		TLID tlObjectID = tlObjectType.getID();
		for (Entry<TLID, String> classWithoutGeneralization : identifiers.entrySet()) {
			if (tlObjectID.equals(classWithoutGeneralization.getKey())) {
				// No self generalisation
				continue;
			}
			_util.addGeneralization(connection, branch, classWithoutGeneralization.getKey(), tlObjectID,
				OrderedLinkUtil.MAX_ORDER / 2);
			log.info("Added generalization " + _util.toString(tlObjectType) + " to "
					+ classWithoutGeneralization.getValue() + " (" + classWithoutGeneralization.getKey() + ")");
		}

	}

	private void removeClassesWithGeneralizations(PooledConnection connection, Set<TLID> identifiers, long branch)
			throws SQLException {
		String source = "source";
		CompiledStatement classesWithGeneralizations = query(
		parameters(
			_util.branchParamDef()),
		selectDistinct(
			columns(
				columnDef(ReferencePart.name.getReferenceAspectColumnName(
					SQLH.mangleDBName(SourceReference.REFERENCE_SOURCE_NAME)), NO_TABLE_ALIAS, source)),
			table(SQLH.mangleDBName(SQLH.mangleDBName(ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS))),
			and(
				_util.eqBranch()))).toSql(connection.getSQLDialect());
		try (ResultSet dbResult = classesWithGeneralizations.executeQuery(connection, branch)) {
			while (dbResult.next()) {
				identifiers.remove(LongID.valueOf(dbResult.getLong(source)));
			}
		}
	}

	private void addTLClasses(PooledConnection connection, Map<TLID, String> identifiers, long branch)
			throws SQLException {
		String identifierAlias = "identifier";
		String nameAlias = "name";
		CompiledStatement allClasses = query(
		parameters(
			_util.branchParamDef()),
		selectDistinct(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
				columnDef(SQLH.mangleDBName(KBBasedMetaElement.NAME_ATTR), NO_TABLE_ALIAS, nameAlias)),
			table(SQLH.mangleDBName(KBBasedMetaElement.META_ELEMENT_KO)),
			and(
				_util.eqBranch(),
				eqSQL(
					column(SQLH.mangleDBName(TLStructuredTypeColumns.META_ELEMENT_IMPL)),
					literalString(TLStructuredTypeColumns.CLASS_TYPE))))).toSql(connection.getSQLDialect());
		try (ResultSet dbResult = allClasses.executeQuery(connection, branch)) {
			while (dbResult.next()) {
				identifiers.put(LongID.valueOf(dbResult.getLong(identifierAlias)), dbResult.getString(nameAlias));
			}
		}
	}

	private void execute(PooledConnection connection, SQLStatement stmt) throws SQLException {
		query(stmt).toSql(connection.getSQLDialect()).executeUpdate(connection);
	}

}
