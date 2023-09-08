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
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.element.meta.kbbased.KBBasedMetaElement;
import com.top_logic.element.meta.kbbased.KBBasedMetaElementFactory;
import com.top_logic.element.model.migration.model.Module;
import com.top_logic.element.model.migration.model.Type;
import com.top_logic.element.model.migration.model.Util;
import com.top_logic.knowledge.service.db2.SourceReference;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.generated.TlModelFactory;

/**
 * {@link MigrationProcessor} ensuring that all {@link TLClass} extends (inherited)
 * {@link TLObject}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExtendsTLObject implements MigrationProcessor {

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Unable to ensure that all TLClasses extends tl.model:TLObject.", ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		Module tlModelModule = Util.getTLModuleOrFail(connection, TlModelFactory.TL_MODEL_STRUCTURE);
		Type tlObjectType = Util.getTLTypeOrFail(connection, tlModelModule, TLObject.TL_OBJECT_TYPE);

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
			Util.addGeneralization(connection, branch, classWithoutGeneralization.getKey(), tlObjectID,
				OrderedLinkUtil.MAX_ORDER / 2);
			log.info("Added generalization " + Util.toString(tlObjectType) + " to "
					+ classWithoutGeneralization.getValue() + " (" + classWithoutGeneralization.getKey() + ")");
		}

	}

	private void removeClassesWithGeneralizations(PooledConnection connection, Set<TLID> identifiers, long branch)
			throws SQLException {
		String source = "source";
		CompiledStatement classesWithGeneralizations = query(
		parameters(
			parameterDef(DBType.LONG, "branch")),
		selectDistinct(
			columns(
				columnDef(ReferencePart.name.getReferenceAspectColumnName(
					SQLH.mangleDBName(SourceReference.REFERENCE_SOURCE_NAME)), NO_TABLE_ALIAS, source)),
			table(SQLH.mangleDBName(SQLH.mangleDBName(ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS))),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch"))))).toSql(connection.getSQLDialect());
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
			parameterDef(DBType.LONG, "branch")),
		selectDistinct(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME, NO_TABLE_ALIAS, identifierAlias),
				columnDef(SQLH.mangleDBName(KBBasedMetaElement.NAME_ATTR), NO_TABLE_ALIAS, nameAlias)),
			table(SQLH.mangleDBName(KBBasedMetaElement.META_ELEMENT_KO)),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
				eqSQL(
					column(SQLH.mangleDBName(KBBasedMetaElement.META_ELEMENT_IMPL)),
					literalString(KBBasedMetaElementFactory.CLASS_TYPE))))).toSql(connection.getSQLDialect());
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
