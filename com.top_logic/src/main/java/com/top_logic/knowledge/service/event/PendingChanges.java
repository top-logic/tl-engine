/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.event;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * The changes of the transaction being committed, as handed to
 * {@link ModificationListener#createModification(com.top_logic.knowledge.service.KnowledgeBase, com.top_logic.knowledge.service.CommitActions, PendingChanges)}.
 *
 * <p>
 * The changes are described on the persistence level: An item is a row of a table (its
 * {@link KnowledgeItem#tTable()}), an attribute is an attribute of an item identified by its name
 * (see {@link MOAttribute#getName()}). Besides the attributes of the table, this includes the
 * flexible attributes of an item that are stored without a column of their own.
 * </p>
 *
 * <p>
 * Values reported are the values in the form {@link KnowledgeItem#getAttributeValue(String)}
 * returns them, e.g. a referenced item instead of its key.
 * </p>
 *
 * <p>
 * The answers are only valid during the call of
 * {@link ModificationListener#createModification(com.top_logic.knowledge.service.KnowledgeBase, com.top_logic.knowledge.service.CommitActions, PendingChanges)}:
 * The {@link Modification}s created by the listeners change the transaction afterwards.
 * </p>
 */
public interface PendingChanges {

	/**
	 * All items created in the transaction, indexed by their {@link KnowledgeItem#tId()}.
	 */
	Map<ObjectKey, ? extends KnowledgeItem> getCreated();

	/**
	 * All items modified in the transaction, indexed by their {@link KnowledgeItem#tId()}.
	 *
	 * <p>
	 * An item modified in the transaction whose values were all set back to their committed values
	 * is contained as well, {@link #getChangedAttributes(KnowledgeItem)} is empty for it.
	 * </p>
	 */
	Map<ObjectKey, ? extends KnowledgeItem> getUpdated();

	/**
	 * All items deleted in the transaction, indexed by their {@link KnowledgeItem#tId()}.
	 *
	 * <p>
	 * As the items are deleted, their attributes must not be accessed. Their values before the
	 * transaction are available through {@link #getCommittedValue(KnowledgeItem, String)}.
	 * </p>
	 */
	Map<ObjectKey, ? extends KnowledgeItem> getRemoved();

	/**
	 * The tables that have at least one created, updated or removed item.
	 *
	 * @see #getCreated(MetaObject)
	 * @see #getUpdated(MetaObject)
	 * @see #getRemoved(MetaObject)
	 */
	Set<? extends MetaObject> getTables();

	/**
	 * The items of {@link #getCreated()} whose {@link KnowledgeItem#tTable()} is the given table.
	 *
	 * @param table
	 *        The concrete table of the items. Items of a sub-table are not reported.
	 * @return The created items of the given table, empty if there are none.
	 */
	List<? extends KnowledgeItem> getCreated(MetaObject table);

	/**
	 * The items of {@link #getUpdated()} whose {@link KnowledgeItem#tTable()} is the given table.
	 *
	 * @param table
	 *        The concrete table of the items. Items of a sub-table are not reported.
	 * @return The updated items of the given table, empty if there are none.
	 */
	List<? extends KnowledgeItem> getUpdated(MetaObject table);

	/**
	 * The items of {@link #getRemoved()} whose {@link KnowledgeItem#tTable()} is the given table.
	 *
	 * @param table
	 *        The concrete table of the items. Items of a sub-table are not reported.
	 * @return The removed items of the given table, empty if there are none.
	 */
	List<? extends KnowledgeItem> getRemoved(MetaObject table);

	/**
	 * The names of the attributes of the given updated item whose value in the transaction differs
	 * from the committed value.
	 *
	 * <p>
	 * An attribute whose value was changed and set back to its committed value in the transaction
	 * is not reported. Attributes of the table that derive their value from other attributes
	 * (without a database column) are not reported either, their change is visible through the
	 * attributes they are derived from.
	 * </p>
	 *
	 * @param updatedItem
	 *        An item of {@link #getUpdated()}.
	 * @return The names of the changed attributes, including flexible attributes.
	 *
	 * @throws IllegalArgumentException
	 *         If the given item is not contained in {@link #getUpdated()}.
	 */
	Set<String> getChangedAttributes(KnowledgeItem updatedItem);

	/**
	 * Whether the value of the given attribute of the given updated item differs from the committed
	 * value.
	 *
	 * @param updatedItem
	 *        An item of {@link #getUpdated()}.
	 * @param attributeName
	 *        The name of the attribute to check.
	 * @return Whether the attribute is contained in {@link #getChangedAttributes(KnowledgeItem)}.
	 *
	 * @throws IllegalArgumentException
	 *         If the given item is not contained in {@link #getUpdated()}.
	 */
	default boolean isChanged(KnowledgeItem updatedItem, String attributeName) {
		return getChangedAttributes(updatedItem).contains(attributeName);
	}

	/**
	 * The committed value of the given attribute of the given updated or removed item, i.e. its
	 * value before the transaction.
	 *
	 * @param item
	 *        An item of {@link #getUpdated()} or {@link #getRemoved()}.
	 * @param attributeName
	 *        The name of an attribute of the item's table or of a flexible attribute.
	 * @return The value the attribute had before the transaction. <code>null</code> for a flexible
	 *         attribute without value.
	 *
	 * @throws IllegalArgumentException
	 *         If the given item is neither contained in {@link #getUpdated()} nor in
	 *         {@link #getRemoved()}.
	 */
	Object getCommittedValue(KnowledgeItem item, String attributeName);

}
