/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.event.PendingChanges;

/**
 * {@link PendingChanges} of a commit of a {@link DefaultDBContext}.
 *
 * <p>
 * The index by table and the changed attributes of an item are computed on first request.
 * </p>
 */
final class DBPendingChanges implements PendingChanges {

	private final Map<ObjectKey, DBKnowledgeItem> _created;

	private final Map<ObjectKey, DBKnowledgeItem> _updated;

	private final Map<ObjectKey, DBKnowledgeItem> _removed;

	/**
	 * The changed items grouped by their table, <code>null</code> until requested.
	 */
	private Map<MetaObject, TableChanges> _byTable;

	/**
	 * The changed attribute names of the updated items that have been requested so far.
	 */
	private final Map<ObjectKey, Set<String>> _changedAttributes = new HashMap<>();

	/**
	 * Creates a {@link DBPendingChanges}.
	 *
	 * @param created
	 *        See {@link #getCreated()}.
	 * @param updated
	 *        See {@link #getUpdated()}.
	 * @param removed
	 *        See {@link #getRemoved()}.
	 */
	DBPendingChanges(Map<ObjectKey, DBKnowledgeItem> created, Map<ObjectKey, DBKnowledgeItem> updated,
			Map<ObjectKey, DBKnowledgeItem> removed) {
		_created = created;
		_updated = updated;
		_removed = removed;
	}

	@Override
	public Map<ObjectKey, DBKnowledgeItem> getCreated() {
		return _created;
	}

	@Override
	public Map<ObjectKey, DBKnowledgeItem> getUpdated() {
		return _updated;
	}

	@Override
	public Map<ObjectKey, DBKnowledgeItem> getRemoved() {
		return _removed;
	}

	@Override
	public Set<? extends MetaObject> getTables() {
		return Collections.unmodifiableSet(byTable().keySet());
	}

	@Override
	public List<DBKnowledgeItem> getCreated(MetaObject table) {
		TableChanges changes = byTable().get(table);
		return changes == null ? Collections.emptyList() : Collections.unmodifiableList(changes._created);
	}

	@Override
	public List<DBKnowledgeItem> getUpdated(MetaObject table) {
		TableChanges changes = byTable().get(table);
		return changes == null ? Collections.emptyList() : Collections.unmodifiableList(changes._updated);
	}

	@Override
	public List<DBKnowledgeItem> getRemoved(MetaObject table) {
		TableChanges changes = byTable().get(table);
		return changes == null ? Collections.emptyList() : Collections.unmodifiableList(changes._removed);
	}

	@Override
	public Set<String> getChangedAttributes(KnowledgeItem updatedItem) {
		ObjectKey key = updatedItem.tId();
		DBKnowledgeItem item = _updated.get(key);
		if (item == null) {
			throw new IllegalArgumentException("Not updated in this transaction: " + updatedItem);
		}
		return _changedAttributes.computeIfAbsent(key,
			x -> Collections.unmodifiableSet(item.getChangedAttributeNames()));
	}

	@Override
	public Object getCommittedValue(KnowledgeItem item, String attributeName) {
		ObjectKey key = item.tId();
		DBKnowledgeItem dbItem = _updated.get(key);
		if (dbItem == null) {
			dbItem = _removed.get(key);
			if (dbItem == null) {
				throw new IllegalArgumentException("Neither updated nor removed in this transaction: " + item);
			}
		}
		try {
			return dbItem.getCommittedValue(attributeName);
		} catch (NoSuchAttributeException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	private Map<MetaObject, TableChanges> byTable() {
		if (_byTable == null) {
			Map<MetaObject, TableChanges> byTable = new HashMap<>();
			for (DBKnowledgeItem item : _created.values()) {
				tableChanges(byTable, item)._created.add(item);
			}
			for (DBKnowledgeItem item : _updated.values()) {
				tableChanges(byTable, item)._updated.add(item);
			}
			for (DBKnowledgeItem item : _removed.values()) {
				tableChanges(byTable, item)._removed.add(item);
			}
			_byTable = byTable;
		}
		return _byTable;
	}

	private static TableChanges tableChanges(Map<MetaObject, TableChanges> byTable, DBKnowledgeItem item) {
		return byTable.computeIfAbsent(item.tTable(), x -> new TableChanges());
	}

	/**
	 * The changed items of a single table.
	 */
	private static final class TableChanges {

		final List<DBKnowledgeItem> _created = new ArrayList<>();

		final List<DBKnowledgeItem> _updated = new ArrayList<>();

		final List<DBKnowledgeItem> _removed = new ArrayList<>();

	}

}
