/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.BranchIdType;
import com.top_logic.model.migration.data.TypePart;

/**
 * Deletes the data that belongs to model elements and objects removed by a migration.
 *
 * <p>
 * The values of references are stored in the link tables, the tables derived from
 * {@link ApplicationObjectUtil#WRAPPER_ATTRIBUTE_ASSOCIATION_BASE}. A migration does not know, in
 * which of these tables a reference stores its values. Therefore, all link tables of the
 * {@link MigrationContext#getPersistentRepository() stored schema} are cleaned.
 * </p>
 */
public class ModelDataCleanup {

	private final MigrationContext _context;

	private final Log _log;

	private final PooledConnection _connection;

	private final Util _util;

	private MORepository _repository;

	private List<MOClass> _linkTables;

	/**
	 * Creates a {@link ModelDataCleanup}.
	 *
	 * @param context
	 *        The context of the running migration.
	 * @param log
	 *        Receives information about the deleted rows.
	 * @param connection
	 *        The connection to the database to migrate.
	 */
	public ModelDataCleanup(MigrationContext context, Log log, PooledConnection connection) {
		_context = context;
		_log = log;
		_connection = connection;
		_util = context.getSQLUtils();
	}

	/**
	 * Deletes the values of the references among the given (already deleted) model parts.
	 *
	 * <p>
	 * Only references that are their own definition store values. Values of an overriding
	 * reference are stored with the identifier of its definition. All other parts are ignored.
	 * </p>
	 *
	 * @param branch
	 *        The branch in which the parts are deleted.
	 * @param parts
	 *        The deleted model parts.
	 */
	public void deleteReferenceValues(long branch, Collection<? extends TypePart> parts) throws SQLException {
		Set<TLID> references = parts.stream()
			.filter(part -> part.getKind() == TypePart.Kind.REFERENCE)
			.filter(part -> part.getID().equals(part.getDefinition()))
			.map(BranchIdType::getID)
			.collect(Collectors.toSet());
		if (references.isEmpty()) {
			return;
		}

		for (MOClass linkTable : linkTables()) {
			MOReference attributeRef = (MOReference) linkTable.getAttribute(ApplicationObjectUtil.META_ATTRIBUTE_ATTR);
			CompiledStatement delete = query(
				delete(
					table(linkTable.getDBMapping().getDBName()),
					and(
						eqBranch(branch),
						inSet(
							column(attributeRef.getColumn(ReferencePart.name).getDBName()),
							setLiteral(references, DBType.ID))))).toSql(_connection.getSQLDialect());

			int deletedRows = delete.executeUpdate(_connection);
			if (deletedRows > 0) {
				_context.invalidateXRef(linkTable);
				_log.info("Deleted " + deletedRows + " values of deleted references from table '"
					+ linkTable.getName() + "'.");
			}
		}
	}

	/**
	 * Deletes the objects of the given types stored in the given table, together with their
	 * {@link AbstractFlexDataManager#FLEX_DATA flex attributes} and all links from or to these
	 * objects.
	 *
	 * @param branch
	 *        The branch in which the objects are deleted.
	 * @param typeTableName
	 *        The name of the table in which the objects are stored.
	 * @param types
	 *        Identifiers of the types whose instances are deleted.
	 * @return The number of deleted rows in the given table.
	 */
	public int deleteInstances(long branch, String typeTableName, Collection<TLID> types) throws SQLException {
		if (types.isEmpty()) {
			return 0;
		}
		MetaObject type = repository().getTypeOrNull(typeTableName);
		if (!(type instanceof MOClass)) {
			throw new IllegalArgumentException("No table '" + typeTableName + "' in the stored schema.");
		}
		MOClass typeTable = (MOClass) type;
		MOReference typeRef = (MOReference) typeTable.getAttribute(PersistentObject.TYPE_REF);
		String typeColumn = typeRef.getColumn(ReferencePart.name).getDBName();
		String typeTableDBName = typeTable.getDBMapping().getDBName();

		// Flex attributes and links must be deleted before the objects, because the objects to
		// delete are selected through the table of the objects.
		CompiledStatement deleteFlexData = query(
			delete(
				table(AbstractFlexDataManager.FLEX_DATA_DB_NAME),
				and(
					eqBranch(AbstractFlexDataManager.BRANCH_DBNAME, branch),
					eqSQL(
						column(AbstractFlexDataManager.TYPE_DBNAME),
						literal(DBType.STRING, typeTableName)),
					inSetSelect(
						column(AbstractFlexDataManager.IDENTIFIER_DBNAME),
						selectInstances(branch, typeTableDBName, typeColumn, types)))))
							.toSql(_connection.getSQLDialect());
		int deletedFlexData = deleteFlexData.executeUpdate(_connection);
		_log.info("Deleted " + deletedFlexData + " flex attribute values of objects in table '" + typeTableName
			+ "'.");

		for (MOClass linkTable : linkTables()) {
			for (String end : new String[] { DBKnowledgeAssociation.REFERENCE_SOURCE_NAME,
				DBKnowledgeAssociation.REFERENCE_DEST_NAME }) {
				deleteLinks(branch, linkTable, (MOReference) linkTable.getAttribute(end), typeTable, typeTableDBName,
					typeColumn, types);
			}
		}

		CompiledStatement deleteObjects = query(
			delete(
				table(typeTableDBName),
				and(
					eqBranch(branch),
					inSet(
						column(typeColumn),
						setLiteral(types, DBType.ID))))).toSql(_connection.getSQLDialect());
		int deletedObjects = deleteObjects.executeUpdate(_connection);
		if (deletedObjects > 0) {
			_context.invalidateXRef(typeTable);
		}
		return deletedObjects;
	}

	private void deleteLinks(long branch, MOClass linkTable, MOReference end, MOClass typeTable,
			String typeTableDBName, String typeColumn, Collection<TLID> types) throws SQLException {
		DBAttribute endTypeColumn = end.getColumn(ReferencePart.type);
		SQLExpression typeMatch;
		if (endTypeColumn == null) {
			// Monomorphic end.
			if (!typeTable.isSubtypeOf(end.getMetaObject())) {
				// End can not point to the objects to delete.
				return;
			}
			typeMatch = literalTrueLogical();
		} else {
			typeMatch = eqSQL(
				column(endTypeColumn.getDBName()),
				literal(DBType.STRING, typeTable.getName()));
		}

		CompiledStatement delete = query(
			delete(
				table(linkTable.getDBMapping().getDBName()),
				and(
					eqBranch(branch),
					typeMatch,
					inSetSelect(
						column(end.getColumn(ReferencePart.name).getDBName()),
						selectInstances(branch, typeTableDBName, typeColumn, types)))))
							.toSql(_connection.getSQLDialect());
		int deletedRows = delete.executeUpdate(_connection);
		if (deletedRows > 0) {
			_context.invalidateXRef(linkTable);
			_log.info("Deleted " + deletedRows + " links in table '" + linkTable.getName() + "' whose '"
				+ end.getName() + "' is a deleted object of table '" + typeTable.getName() + "'.");
		}
	}

	private SQLSelect selectInstances(long branch, String typeTableDBName, String typeColumn,
			Collection<TLID> types) {
		return select(
			columns(columnDef(BasicTypes.IDENTIFIER_DB_NAME)),
			table(typeTableDBName),
			and(
				eqBranch(branch),
				inSet(
					column(typeColumn),
					setLiteral(types, DBType.ID))));
	}

	private SQLExpression eqBranch(long branch) {
		return eqBranch(BasicTypes.BRANCH_DB_NAME, branch);
	}

	private SQLExpression eqBranch(String branchColumn, long branch) {
		if (!_util.hasBranches()) {
			return literalTrueLogical();
		}
		return eqSQL(column(branchColumn), literalLong(branch));
	}

	private MORepository repository() {
		if (_repository == null) {
			_repository = _context.getPersistentRepository();
		}
		return _repository;
	}

	/**
	 * All concrete tables storing values of references.
	 */
	private List<MOClass> linkTables() {
		if (_linkTables == null) {
			List<MOClass> result = new ArrayList<>();
			MetaObject base = repository().getTypeOrNull(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION_BASE);
			if (base != null) {
				for (MetaObject type : repository().getMetaObjects()) {
					if (!(type instanceof MOClass)) {
						continue;
					}
					MOClass table = (MOClass) type;
					if (table.isAbstract() || !table.isSubtypeOf(base)) {
						continue;
					}
					result.add(table);
				}
			}
			_linkTables = result;
		}
		return _linkTables;
	}

}
