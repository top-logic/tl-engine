/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.BasicTypes;

/**
 * The database tables of a {@link MORepository} that store rows with a revision range.
 *
 * <p>
 * A row of such a table is valid for all revisions {@code R} with
 * {@link Table#getRevMin()} {@code <= R <=} {@link Table#getRevMax()}. Maintenance operations that
 * rewrite the history of a {@link DBKnowledgeBase} work on exactly these tables: the
 * {@link #getItemTables() item tables} that store the {@link KnowledgeItem}s themselves and the
 * {@link #getFlexData() flexible data table} that stores their dynamic attribute values.
 * </p>
 *
 * <p>
 * An item table is a concrete (non {@link MOClass#isAbstract() abstract}) {@link MOKnowledgeItem}
 * that is a subtype of {@link BasicTypes#ITEM_TYPE_NAME} and therefore inherits the
 * {@link BasicTypes#REV_MIN_ATTRIBUTE_NAME}, {@link BasicTypes#REV_MAX_ATTRIBUTE_NAME} and
 * {@link BasicTypes#REV_CREATE_ATTRIBUTE_NAME} attributes. Both
 * {@link MOClass#isVersioned() versioned} and unversioned item tables are reported, because both
 * store their rows with a revision range.
 * </p>
 *
 * <p>
 * The tables that implement the versioning machinery itself ({@link BasicTypes#REVISION_TYPE_NAME},
 * {@link RevisionXref#REVISION_XREF_TYPE_NAME}, {@link BasicTypes#BRANCH_TYPE_NAME},
 * {@link BranchSupport#BRANCH_SWITCH_TYPE_NAME} and the {@link HistoryCleanup} bookkeeping table)
 * are none of those: they are declared without a supertype and therefore carry neither
 * {@link BasicTypes#REV_MIN_ATTRIBUTE_NAME} nor {@link BasicTypes#REV_MAX_ATTRIBUTE_NAME}. They are
 * excluded by the subtype test alone and must be maintained by dedicated operations.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ItemTables {

	/**
	 * A single table with a revision range.
	 *
	 * @see ItemTables
	 */
	public static final class Table {

		private final MOKnowledgeItem _type;

		private final DBAttribute _revMin;

		private final DBAttribute _revMax;

		private final DBAttribute _revCreate;

		private final DBAttribute _identifier;

		private final DBAttribute _branch;

		private final List<MOReference> _pinnedReferences;

		Table(MOKnowledgeItem type, DBAttribute revMin, DBAttribute revMax, DBAttribute revCreate,
				DBAttribute identifier, DBAttribute branch, List<MOReference> pinnedReferences) {
			_type = type;
			_revMin = revMin;
			_revMax = revMax;
			_revCreate = revCreate;
			_identifier = identifier;
			_branch = branch;
			_pinnedReferences = pinnedReferences;
		}

		/**
		 * The type describing this table.
		 */
		public MOKnowledgeItem getType() {
			return _type;
		}

		/**
		 * The database name of this table.
		 */
		public String getDBName() {
			return _type.getDBName();
		}

		/**
		 * The column holding the first revision a row is valid in.
		 *
		 * @see BasicTypes#REV_MIN_ATTRIBUTE_NAME
		 */
		public DBAttribute getRevMin() {
			return _revMin;
		}

		/**
		 * The column holding the last revision a row is valid in.
		 *
		 * @see BasicTypes#REV_MAX_ATTRIBUTE_NAME
		 */
		public DBAttribute getRevMax() {
			return _revMax;
		}

		/**
		 * The column holding the revision the object was created in, or <code>null</code> if this
		 * table has no such column.
		 *
		 * @see BasicTypes#REV_CREATE_ATTRIBUTE_NAME
		 */
		public DBAttribute getRevCreate() {
			return _revCreate;
		}

		/**
		 * The column holding the identity of the object a row belongs to.
		 * 
		 * @see BasicTypes#IDENTIFIER_ATTRIBUTE_NAME
		 */
		public DBAttribute getIdentifier() {
			return _identifier;
		}

		/**
		 * The column holding the branch a row belongs to, or <code>null</code> if this table has no
		 * such column.
		 * 
		 * <p>
		 * Without branch support the branch is constantly the trunk and no column is written.
		 * </p>
		 * 
		 * @see BasicTypes#BRANCH_ATTRIBUTE_NAME
		 */
		public DBAttribute getBranch() {
			return _branch;
		}

		/**
		 * Whether rows of this table carry a {@link #getBranch() branch column}.
		 */
		public boolean hasBranchColumn() {
			return _branch != null;
		}

		/**
		 * The reference attributes that pin their target to a certain revision.
		 * 
		 * <p>
		 * One entry per {@link MOReference} of {@link MOReference#getHistoryType() history type}
		 * {@link HistoryType#HISTORIC} or {@link HistoryType#MIXED}. Such a reference has a
		 * {@link ReferencePart#revision revision column}, see
		 * {@link MOReference#getColumn(ReferencePart)}. The list is empty for a table without such
		 * references.
		 * </p>
		 */
		public List<MOReference> getPinnedReferences() {
			return _pinnedReferences;
		}

		@Override
		public String toString() {
			return _type.getName();
		}
	}

	private final MORepository _repository;

	private final List<Table> _itemTables;

	private final Table _flexData;

	/**
	 * Creates a {@link ItemTables} view of the given type repository.
	 *
	 * @param repository
	 *        The repository to enumerate the tables of.
	 */
	public ItemTables(MORepository repository) {
		_repository = repository;
		_itemTables = Collections.unmodifiableList(lookupItemTables(repository));
		_flexData = lookupFlexData(repository);
	}

	private static List<Table> lookupItemTables(MORepository repository) {
		MOClass itemType = BasicTypes.getItemType(repository);
		List<Table> result = new ArrayList<>();
		for (MetaObject type : repository.getMetaObjects()) {
			if (!(type instanceof MOKnowledgeItem)) {
				continue;
			}
			if (MetaObjectUtils.isAbstract(type)) {
				continue;
			}
			if (!type.isSubtypeOf(itemType)) {
				continue;
			}
			MOKnowledgeItem table = (MOKnowledgeItem) type;
			DBAttribute revMax = dbColumn(table, BasicTypes.REV_MAX_ATTRIBUTE_NAME);
			if (revMax == null) {
				continue;
			}
			DBAttribute revMin = dbColumn(table, BasicTypes.REV_MIN_ATTRIBUTE_NAME);
			if (revMin == null) {
				continue;
			}
			DBAttribute identifier = dbColumn(table, BasicTypes.IDENTIFIER_ATTRIBUTE_NAME);
			if (identifier == null) {
				continue;
			}
			result.add(new Table(table, revMin, revMax,
				dbColumn(table, BasicTypes.REV_CREATE_ATTRIBUTE_NAME), identifier,
				dbColumn(table, BasicTypes.BRANCH_ATTRIBUTE_NAME), pinnedReferences(table)));
		}
		result.sort(Comparator.comparing(Table::getDBName));
		return result;
	}

	private static Table lookupFlexData(MORepository repository) {
		MetaObject type = repository.getTypeOrNull(AbstractFlexDataManager.FLEX_DATA);
		if (!(type instanceof MOKnowledgeItem)) {
			return null;
		}
		MOKnowledgeItem table = (MOKnowledgeItem) type;
		DBAttribute revMin = dbColumn(table, AbstractFlexDataManager.REV_MIN);
		DBAttribute revMax = dbColumn(table, AbstractFlexDataManager.REV_MAX);
		if (revMin == null || revMax == null) {
			return null;
		}
		return new Table(table, revMin, revMax, null, dbColumn(table, AbstractFlexDataManager.IDENTIFIER),
			dbColumn(table, AbstractFlexDataManager.BRANCH), Collections.emptyList());
	}

	private static List<MOReference> pinnedReferences(MOKnowledgeItem table) {
		List<MOReference> result = null;
		for (MOAttribute attribute : table.getAttributes()) {
			if (!(attribute instanceof MOReference)) {
				continue;
			}
			MOReference reference = (MOReference) attribute;
			if (reference.getColumn(ReferencePart.revision) == null) {
				// A reference of history type CURRENT always points to the current object and is
				// therefore not pinned to a revision.
				continue;
			}
			if (result == null) {
				result = new ArrayList<>();
			}
			result.add(reference);
		}
		if (result == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(result);
	}

	private static DBAttribute dbColumn(MOKnowledgeItem table, String attributeName) {
		MOAttribute attribute = table.getAttributeOrNull(attributeName);
		if (attribute == null) {
			return null;
		}
		DBAttribute[] dbMapping = attribute.getDbMapping();
		if (dbMapping.length != 1) {
			return null;
		}
		return dbMapping[0];
	}

	/**
	 * The repository this view was built from.
	 */
	public MORepository getRepository() {
		return _repository;
	}

	/**
	 * All item tables of the {@link #getRepository() repository}, ordered by
	 * {@link Table#getDBName()}.
	 */
	public List<Table> getItemTables() {
		return _itemTables;
	}

	/**
	 * The table storing dynamic attribute values, or <code>null</code> if the
	 * {@link #getRepository() repository} has none.
	 *
	 * @see AbstractFlexDataManager#FLEX_DATA
	 */
	public Table getFlexData() {
		return _flexData;
	}

}
