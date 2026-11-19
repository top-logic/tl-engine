/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.ChangeLocator;
import com.top_logic.element.meta.SeparateTableStorage;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.event.PendingChanges;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.access.WithStorageAttribute;

/**
 * {@link StorageImplementation} that stores values in a column of the object's table.
 * 
 * <p>
 * If a storage does not implement this interface and does not represent a derived attribute, it
 * must implement {@link SeparateTableStorage}.
 * </p>
 * 
 * @see SeparateTableStorage
 * @see WithStorageAttribute
 */
public interface ColumnStorage extends StorageImplementation {

	/**
	 * The name of the {@link MOAttribute} of the objects table storing the attribute value.
	 * 
	 * @see WithStorageAttribute#getStorageAttribute()
	 */
	String getStorageAttribute();


	/**
	 * Reports the objects whose value of this storage's attribute may have been changed by the
	 * rows of the given table in the given pending changes.
	 * 
	 * <p>
	 * An updated object is reported if the {@link #getStorageAttribute() storage attribute} of its
	 * row has changed. A created object is reported if it has a value in the storage attribute, a
	 * removed object if it had a committed value. Since several types can store their instances in
	 * the same table and use the same column for different attributes, a reported object may not
	 * have the attribute of this storage.
	 * </p>
	 * 
	 * @param changes
	 *        The changes of the transaction being committed.
	 * @param table
	 *        A table that stores instances of a type with the attribute of this storage.
	 * @param objects
	 *        Receives the rows of the changed objects.
	 */
	default void findChangedObjects(PendingChanges changes, MetaObject table, Consumer<? super KnowledgeItem> objects) {
		String storageAttribute = getStorageAttribute();
		for (KnowledgeItem item : changes.getUpdated(table)) {
			if (changes.isChanged(item, storageAttribute)) {
				objects.accept(item);
			}
		}
		for (KnowledgeItem item : changes.getCreated(table)) {
			if (item.getAttributeValue(storageAttribute) != null) {
				objects.accept(item);
			}
		}
		for (KnowledgeItem item : changes.getRemoved(table)) {
			if (changes.getCommittedValue(item, storageAttribute) != null) {
				objects.accept(item);
			}
		}
	}

	/**
	 * Registers a locator for each table in which the instances of the attribute's owner and its
	 * specializations are stored, see {@link #findChangedObjects(PendingChanges, MetaObject, Consumer)}.
	 */
	@Override
	default void addChangeLocators(TLStructuredTypePart attribute,
			BiConsumer<? super MetaObject, ? super ChangeLocator> locators) {
		if (!(attribute.getOwner() instanceof TLClass owner)) {
			return;
		}
		MORepository repository = attribute.tKnowledgeBase().getMORepository();
		for (String tableName : AttributeOperations.typesByTableName(owner).keySet()) {
			MetaObject table = repository.getTypeOrNull(tableName);
			if (table == null) {
				continue;
			}
			ChangeLocator locator =
				(changes, objects) -> findChangedObjects(changes, table, item -> objects.accept(item.tId()));
			locators.accept(table, locator);
		}
	}

	@Override
	default boolean canDetectValueChanges() {
		return true;
	}

}
