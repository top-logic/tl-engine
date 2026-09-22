/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.column;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.KnowledgeReferenceStorageImpl;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.util.TLContext;

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
 * store their rows with a revision range. An association table is an item table like any other: its
 * {@code source} and {@code dest} columns are the {@link Table#getReferences() references} of that
 * table.
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

		private final List<Reference> _references;

		private final List<Reference> _pinnedReferences;

		Table(MOKnowledgeItem type, DBAttribute revMin, DBAttribute revMax, DBAttribute revCreate,
				DBAttribute identifier, DBAttribute branch, List<Reference> references) {
			_type = type;
			_revMin = revMin;
			_revMax = revMax;
			_revCreate = revCreate;
			_identifier = identifier;
			_branch = branch;
			_references = references;
			_pinnedReferences = pinned(references);
		}

		private static List<Reference> pinned(List<Reference> references) {
			List<Reference> result = null;
			for (Reference reference : references) {
				if (!reference.isPinned()) {
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
		 * All reference attributes of this table, in the order of their declaration.
		 *
		 * @see Reference
		 */
		public List<Reference> getReferences() {
			return _references;
		}

		/**
		 * The reference attributes that pin their target to a certain revision.
		 *
		 * <p>
		 * The {@link #getReferences() references} of {@link MOReference#getHistoryType() history
		 * type} {@link HistoryType#HISTORIC} or {@link HistoryType#MIXED}. Such a reference has a
		 * {@link ReferencePart#revision revision column}. The list is empty for a table without
		 * such references.
		 * </p>
		 *
		 * @see Reference#isPinned()
		 */
		public List<Reference> getPinnedReferences() {
			return _pinnedReferences;
		}

		/**
		 * The branch a row of this table belongs to, the trunk when the table has no
		 * {@link #getBranch() branch column}.
		 */
		public SQLExpression branchExpression() {
			if (_branch == null) {
				return literalLong(TLContext.TRUNK_ID);
			}
			return column(NO_TABLE_ALIAS, _branch, NOT_NULL);
		}

		/**
		 * The branch a value of the given reference refers to.
		 *
		 * <p>
		 * A {@link MOReference#isBranchGlobal() branch global} reference stores that branch in a
		 * column of its own. A branch local reference refers to the
		 * {@link #branchExpression() branch of the row} that holds it; it has a branch column only
		 * to name the branch of a historic target and stores
		 * {@link KnowledgeReferenceStorageImpl#DUMMY_BRANCH_VALUE} there while it holds a current
		 * one.
		 * </p>
		 *
		 * @param reference
		 *        A reference of this table.
		 */
		public SQLExpression viewBranchExpression(Reference reference) {
			DBAttribute branchColumn = reference.getBranchColumn();
			if (branchColumn == null) {
				return branchExpression();
			}
			SQLExpression stored = column(NO_TABLE_ALIAS, branchColumn, NOT_NULL);
			if (reference.getReference().isBranchGlobal()) {
				return stored;
			}
			return sqlCase(eq(stored, literalLong(KnowledgeReferenceStorageImpl.DUMMY_BRANCH_VALUE)),
				branchExpression(), stored);
		}

		@Override
		public String toString() {
			return _type.getName();
		}
	}

	/**
	 * A reference attribute of an item {@link Table}, with the columns it is stored in.
	 *
	 * <p>
	 * The value of a reference is composed of up to four columns: the
	 * {@link #getIdColumn() identifier} of the target object, the {@link #getTypeColumn() type} of
	 * the target object (absent for a {@link #getMonomorphicTargetType() monomorphic} reference),
	 * the {@link #getBranchColumn() branch} the target is looked up in (absent for a branch local
	 * reference) and the {@link #getRevisionColumn() revision} the target is pinned to (absent for
	 * a reference that always refers to the current state).
	 * </p>
	 *
	 * @see Table#getReferences()
	 */
	public static final class Reference {

		private final MOReference _reference;

		private final DBAttribute _id;

		private final DBAttribute _type;

		private final DBAttribute _branch;

		private final DBAttribute _revision;

		private final String _monomorphicTargetType;

		Reference(MOReference reference) {
			_reference = reference;
			_id = reference.getColumn(ReferencePart.name);
			_type = reference.getColumn(ReferencePart.type);
			_branch = reference.getColumn(ReferencePart.branch);
			_revision = reference.getColumn(ReferencePart.revision);
			_monomorphicTargetType =
				reference.isMonomorphic() ? reference.getMetaObject().getName() : null;
		}

		/**
		 * The attribute this reference describes.
		 */
		public MOReference getReference() {
			return _reference;
		}

		/**
		 * The name of the reference attribute.
		 */
		public String getName() {
			return _reference.getName();
		}

		/**
		 * The column holding the identifier of the target object.
		 */
		public DBAttribute getIdColumn() {
			return _id;
		}

		/**
		 * The column holding the concrete type of the target object, or <code>null</code> for a
		 * {@link #getMonomorphicTargetType() monomorphic} reference.
		 */
		public DBAttribute getTypeColumn() {
			return _type;
		}

		/**
		 * The column holding the branch the target object is looked up in, or <code>null</code> for
		 * a branch local reference.
		 *
		 * @see MOReference#isBranchGlobal()
		 */
		public DBAttribute getBranchColumn() {
			return _branch;
		}

		/**
		 * The column holding the revision the target object is pinned to, or <code>null</code> for
		 * a reference that always refers to the current state.
		 *
		 * @see MOReference#getHistoryType()
		 */
		public DBAttribute getRevisionColumn() {
			return _revision;
		}

		/**
		 * Whether this reference pins its target to a certain revision.
		 *
		 * @see #getRevisionColumn()
		 */
		public boolean isPinned() {
			return _revision != null;
		}

		/**
		 * Whether a value must be assigned to this reference.
		 *
		 * @see MOAttribute#isMandatory()
		 */
		public boolean isMandatory() {
			return _reference.isMandatory();
		}

		/**
		 * Whether the referring object is the container of the referenced object.
		 *
		 * @see MOReference#isContainer()
		 */
		public boolean isContainer() {
			return _reference.isContainer();
		}

		/**
		 * The history context values of this reference.
		 *
		 * @see MOReference#getHistoryType()
		 */
		public HistoryType getHistoryType() {
			return _reference.getHistoryType();
		}

		/**
		 * The declared type of the target object.
		 *
		 * <p>
		 * A polymorphic reference also accepts subtypes thereof and names the concrete type of its
		 * target in its {@link #getTypeColumn() type column}.
		 * </p>
		 *
		 * @see MOReference#getMetaObject()
		 */
		public MetaObject getTargetType() {
			return _reference.getMetaObject();
		}

		/**
		 * The name of the only type this reference can refer to, or <code>null</code> if it is
		 * polymorphic and names the type of its target in its {@link #getTypeColumn() type column}.
		 *
		 * @see MOReference#isMonomorphic()
		 */
		public String getMonomorphicTargetType() {
			return _monomorphicTargetType;
		}

		/**
		 * Whether a value of this reference can refer to an object of the given type.
		 *
		 * @param targetType
		 *        The concrete type of a potential target object.
		 */
		public boolean acceptsTarget(MetaObject targetType) {
			if (_monomorphicTargetType != null) {
				return _monomorphicTargetType.equals(targetType.getName());
			}
			return targetType.isSubtypeOf(getTargetType());
		}

		@Override
		public String toString() {
			return _reference.toString();
		}
	}

	private final MORepository _repository;

	private final List<Table> _itemTables;

	private final Map<String, Table> _itemTableByTypeName;

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
		Map<String, Table> tableByTypeName = new HashMap<>();
		for (Table table : _itemTables) {
			tableByTypeName.put(table.getType().getName(), table);
		}
		_itemTableByTypeName = tableByTypeName;
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
				dbColumn(table, BasicTypes.BRANCH_ATTRIBUTE_NAME), references(table)));
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

	private static List<Reference> references(MOKnowledgeItem table) {
		List<Reference> result = null;
		for (MOAttribute attribute : table.getAttributes()) {
			if (!(attribute instanceof MOReference)) {
				continue;
			}
			if (result == null) {
				result = new ArrayList<>();
			}
			result.add(new Reference((MOReference) attribute));
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
	 * The item table storing objects of the given type, or <code>null</code> if the type is none of
	 * the {@link #getItemTables() item tables}.
	 *
	 * @param typeName
	 *        The {@link MetaObject#getName() name} of the type to look up.
	 */
	public Table getItemTable(String typeName) {
		return _itemTableByTypeName.get(typeName);
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
