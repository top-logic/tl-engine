/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import static com.top_logic.dob.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.db.sql.SQLTableReference;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.element.meta.AssociationStorageDescriptor;
import com.top_logic.element.meta.SeparateTableStorage;
import com.top_logic.element.model.cache.ElementModelCacheService;
import com.top_logic.element.model.cache.ModelTables;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.LifecycleStorageModified;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;

/**
 * Computation of the {@link Revision} in which a persistent {@link TLObject} was changed the last
 * time.
 *
 * <p>
 * A change of an object is a change of any of its stored attribute values. Besides the values
 * stored in the object's own row, this covers all values stored in separate tables, such as
 * references stored in link tables or translations of internationalized attributes. These tables
 * are described by the {@link AssociationStorageDescriptor}s of the object's
 * {@link SeparateTableStorage}s, the same information the change log uses to attribute a row
 * change to the owner of the row.
 * </p>
 *
 * <p>
 * In contrast, {@link TLObject#tLastModificationDate()}, {@link TLObject#tLastModificationTime()}
 * and {@link TLObject#tLastModifier()} only reflect changes of the object's own row. A change of a
 * value that is stored in a separate table does not show up there.
 * </p>
 *
 * <p>
 * Only the object itself is considered, not the objects it contains through compositions: A change
 * of a part of a composition is not a change of its container, but adding or removing a part is.
 * </p>
 *
 * <p>
 * The result is based on the committed state of the database as visible to the history context of
 * the given object: For a current object, this is the session revision, for a historic object its
 * revision. Changes made in a transaction that is not yet committed are not considered.
 * </p>
 *
 * <p>
 * In a table that is not versioned, removed rows are deleted from the database, when the history
 * of the table is cleaned up. The removal of a value from such a table is only reported until then.
 * Afterwards, the last change of the object may be reported to be earlier.
 * </p>
 *
 * @implNote For each table in which the attributes of the object's type store values (and each
 *           column in that table referencing the owner of a row), a single aggregating query
 *           computes both the latest row creation ({@link BasicTypes#REV_MIN_DB_NAME}) and the
 *           latest row removal ({@link BasicTypes#REV_MAX_DB_NAME} plus one) of rows that belong
 *           to the object. For tables that store objects of their own (compositions stored
 *           in the table of the parts), only rows entering or leaving the object's reference
 *           count, which requires joining each row version with its predecessor and successor.
 *           A change of the order of such a composition is therefore not detected.
 */
public class LastChangeRevision {

	/** Table alias of the row whose revision range is analyzed. */
	private static final String ROW_ALIAS = "r";

	/** Table alias of the previous version of the analyzed row. */
	private static final String PREVIOUS_ALIAS = "p";

	/** Table alias of the next version of the analyzed row. */
	private static final String NEXT_ALIAS = "n";

	/** Result column: latest revision in which a row of the object was created. */
	private static final String LAST_CREATED = "lastCreated";

	/** Result column: latest revision in which a row of the object was valid for the last time. */
	private static final String LAST_REMOVED = "lastRemoved";

	/** Parameter: branch of the object. */
	private static final String BRANCH_PARAM = "branch";

	/** Parameter: identifier of the object. */
	private static final String ID_PARAM = "id";

	/** Parameter: revision up to which changes are reported. */
	private static final String REVISION_PARAM = "rev";

	/**
	 * The revision in which the given object was changed the last time.
	 *
	 * @param object
	 *        The object to analyze. May be <code>null</code>.
	 * @return The revision of the last change of the given object. <code>null</code>, if the given
	 *         object is <code>null</code> or has not yet been committed. {@link Revision#CURRENT}
	 *         for a transient object.
	 */
	public static Revision of(TLObject object) {
		if (object == null) {
			return null;
		}
		if (object.tTransient()) {
			return Revision.CURRENT;
		}
		KnowledgeItem handle = object.tHandle();
		Revision ownRevision = LifecycleStorageModified.lastUpdateRevision(handle);
		if (ownRevision == null) {
			return null;
		}

		KnowledgeBase kb = handle.getKnowledgeBase();
		HistoryManager hm = kb.getHistoryManager();
		ObjectKey key = handle.tId();
		long contextRevision = key.getHistoryContext();
		if (contextRevision == Revision.CURRENT_REV) {
			contextRevision = hm.getSessionRevision();
		}

		long lastChange = ownRevision.getCommitNumber();
		ModelTables modelTables = ElementModelCacheService.getApplicationModelTables();
		TLStructuredType type = object.tType();
		Map<MOStructure, List<AssociationStorageDescriptor>> storage = modelTables.lookupSeparateStorage(type);
		if (!storage.isEmpty()) {
			ConnectionPool pool = KBUtils.getConnectionPool(kb);
			try {
				PooledConnection connection = pool.borrowReadConnection();
				try {
					for (Entry<MOStructure, List<AssociationStorageDescriptor>> entry : storage.entrySet()) {
						MOStructure table = entry.getKey();
						boolean objectTable = !modelTables.getClassesForTable(table).isEmpty();
						for (DBAttribute baseColumn : baseColumns(table, entry.getValue())) {
							long tableChange =
								lastChange(connection, pool, table, baseColumn, objectTable, key, contextRevision);
							lastChange = Math.max(lastChange, tableChange);
						}
					}
				} finally {
					pool.releaseReadConnection(connection);
				}
			} catch (SQLException ex) {
				throw new KnowledgeBaseRuntimeException("Unable to determine the last change of " + key + ".", ex);
			}
		}

		return hm.getRevision(lastChange);
	}

	private static Set<DBAttribute> baseColumns(MOStructure table, List<AssociationStorageDescriptor> descriptors) {
		Set<DBAttribute> result = new LinkedHashSet<>();
		for (AssociationStorageDescriptor descriptor : descriptors) {
			MOAttribute attribute = table.getAttributeOrNull(descriptor.getBaseObjectColumn());
			if (attribute instanceof MOReference reference) {
				result.add(reference.getColumn(ReferencePart.name));
			}
		}
		return result;
	}

	/**
	 * The latest revision not after the given context revision in which a row of the given table
	 * that belongs to the object with the given key was created or removed, or
	 * {@link Revision#FIRST_REV} if there is no such row.
	 */
	private static long lastChange(PooledConnection connection, ConnectionPool pool, MOStructure table,
			DBAttribute baseColumn, boolean objectTable, ObjectKey key, long contextRevision)
			throws SQLException {
		DBTableMetaObject dbTable = table.getDBMapping();
		String idColumn = dbColumn(table, BasicTypes.IDENTIFIER_ATTRIBUTE_NAME);
		String revMinColumn = dbColumn(table, BasicTypes.REV_MIN_ATTRIBUTE_NAME);
		String revMaxColumn = dbColumn(table, BasicTypes.REV_MAX_ATTRIBUTE_NAME);
		String branchColumn = dbTable.multipleBranches() ? dbColumn(table, BasicTypes.BRANCH_ATTRIBUTE_NAME) : null;

		SQLExpression revParam = parameter(DBType.LONG, REVISION_PARAM);
		SQLExpression idParam = parameter(baseColumn, ID_PARAM);

		SQLExpression rowRevMin = column(ROW_ALIAS, revMinColumn, NOT_NULL);
		SQLExpression rowRevMax = column(ROW_ALIAS, revMaxColumn, NOT_NULL);
		SQLExpression removedBefore = lt(rowRevMax, revParam);

		SQLExpression where = and(
			eqSQL(column(ROW_ALIAS, baseColumn), idParam),
			le(rowRevMin, revParam));
		if (branchColumn != null) {
			where = and(eqSQL(column(ROW_ALIAS, branchColumn, NOT_NULL), parameter(DBType.LONG, BRANCH_PARAM)),
				where);
		}

		SQLTableReference from = table(dbTable, ROW_ALIAS);
		SQLExpression created;
		SQLExpression removed;
		if (objectTable) {
			/* The rows are objects on their own. A new version of such a row is not a change of the
			 * object with the given key, unless the row starts or stops referencing it. */
			from = leftJoin(from, table(dbTable, PREVIOUS_ALIAS),
				and(
					sameRow(idColumn, branchColumn, PREVIOUS_ALIAS),
					eqSQL(column(PREVIOUS_ALIAS, revMaxColumn, NOT_NULL), sub(rowRevMin, literalLong(1))),
					eqSQL(column(PREVIOUS_ALIAS, baseColumn), idParam)));
			from = leftJoin(from, table(dbTable, NEXT_ALIAS),
				and(
					sameRow(idColumn, branchColumn, NEXT_ALIAS),
					eqSQL(sub(column(NEXT_ALIAS, revMinColumn, NOT_NULL), literalLong(1)), rowRevMax),
					eqSQL(column(NEXT_ALIAS, baseColumn), idParam)));
			created = sqlCase(isNull(column(PREVIOUS_ALIAS, idColumn)), rowRevMin, literalNull(DBType.LONG));
			removed = sqlCase(and(removedBefore, isNull(column(NEXT_ALIAS, idColumn))), rowRevMax,
				literalNull(DBType.LONG));
		} else {
			created = rowRevMin;
			removed = sqlCase(removedBefore, rowRevMax, literalNull(DBType.LONG));
		}

		SQLSelect select = select(
			columns(
				columnDef(max(created), LAST_CREATED),
				columnDef(max(removed), LAST_REMOVED)),
			from,
			where);

		SQLQuery<SQLSelect> query;
		Object[] arguments;
		if (branchColumn != null) {
			query = query(
				parameters(
					parameterDef(DBType.LONG, BRANCH_PARAM),
					parameterDef(baseColumn, ID_PARAM),
					parameterDef(DBType.LONG, REVISION_PARAM)),
				select);
			arguments = new Object[] { key.getBranchContext(), key.getObjectName(), contextRevision };
		} else {
			query = query(
				parameters(
					parameterDef(baseColumn, ID_PARAM),
					parameterDef(DBType.LONG, REVISION_PARAM)),
				select);
			arguments = new Object[] { key.getObjectName(), contextRevision };
		}
		CompiledStatement statement = query.toSql(pool.getSQLDialect());

		long result = Revision.FIRST_REV;
		try (ResultSet resultSet = statement.executeQuery(connection, arguments)) {
			if (resultSet.next()) {
				long lastCreated = resultSet.getLong(LAST_CREATED);
				if (!resultSet.wasNull()) {
					result = Math.max(result, lastCreated);
				}
				long lastRemoved = resultSet.getLong(LAST_REMOVED);
				if (!resultSet.wasNull()) {
					// The row was valid until the found revision and has been removed in the next one.
					result = Math.max(result, lastRemoved + 1);
				}
			}
		}
		return result;
	}

	private static SQLExpression sameRow(String idColumn, String branchColumn, String otherAlias) {
		SQLExpression sameId =
			eqSQL(column(otherAlias, idColumn, NOT_NULL), column(ROW_ALIAS, idColumn, NOT_NULL));
		if (branchColumn == null) {
			return sameId;
		}
		return and(sameId,
			eqSQL(column(otherAlias, branchColumn, NOT_NULL), column(ROW_ALIAS, branchColumn, NOT_NULL)));
	}

	private static String dbColumn(MOStructure table, String attributeName) {
		return table.getAttributeOrNull(attributeName).getDbMapping()[0].getDBName();
	}

}
