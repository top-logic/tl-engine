/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.event.PendingChanges;

/**
 * Values of a row changed in the transaction being committed, in the form the values of an
 * {@link ItemChange} have.
 *
 * <p>
 * The values are either the values of the row in the transaction or its committed values (see
 * {@link PendingChanges#getCommittedValue(KnowledgeItem, String)}). A referenced item is
 * represented by its {@link ObjectKey}, so that the row can be interpreted by an
 * {@link AssociationStorageDescriptor}. Values are read on access.
 * </p>
 */
final class PendingRow extends AbstractMap<String, Object> {

	private final PendingChanges _changes;

	private final KnowledgeItem _item;

	private final boolean _committed;

	/**
	 * Creates a {@link PendingRow}.
	 *
	 * @param changes
	 *        The changes of the transaction being committed.
	 * @param item
	 *        The changed row.
	 * @param committed
	 *        Whether to report the committed values (for an updated or removed row) instead of
	 *        the values in the transaction (for a created or updated row).
	 */
	PendingRow(PendingChanges changes, KnowledgeItem item, boolean committed) {
		_changes = changes;
		_item = item;
		_committed = committed;
	}

	@Override
	public Object get(Object key) {
		if (!(key instanceof String attributeName) || !hasAttribute(attributeName)) {
			return null;
		}
		Object value;
		if (_committed) {
			value = _changes.getCommittedValue(_item, attributeName);
		} else {
			value = _item.getAttributeValue(attributeName);
		}
		if (value instanceof KnowledgeItem referenced) {
			return referenced.tId();
		}
		return value;
	}

	@Override
	public boolean containsKey(Object key) {
		return key instanceof String attributeName && hasAttribute(attributeName);
	}

	private boolean hasAttribute(String attributeName) {
		MetaObject table = _item.tTable();
		return table instanceof MOStructure structure && structure.getAttributeOrNull(attributeName) != null;
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		Map<String, Object> values = new LinkedHashMap<>();
		MetaObject table = _item.tTable();
		if (table instanceof MOStructure structure) {
			for (MOAttribute attribute : structure.getAttributes()) {
				String name = attribute.getName();
				values.put(name, get(name));
			}
		}
		return values.entrySet();
	}

}
