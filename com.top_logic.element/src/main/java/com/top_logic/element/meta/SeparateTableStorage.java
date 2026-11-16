/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.List;
import java.util.function.BiConsumer;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.changelog.ChangeLogBuilder;
import com.top_logic.element.meta.kbbased.storage.ColumnStorage;
import com.top_logic.knowledge.service.event.PendingChanges;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link StorageImplementation} that stores values in a table other than the object table.
 * 
 * <p>
 * The object table defines the identity of the base object that defines the attributes. Besides the
 * identity the object table may store values of other attributes of the base object. Attributes
 * that are stored in the base table must use a storage that implements {@link ColumnStorage}. All
 * other attributes whose values are not stored in the base table must use a storage that implements
 * this interface to allow finding changes of those attributes when computing a change log.
 * </p>
 * 
 * @see ColumnStorage
 * @see ChangeLogBuilder
 */
public interface SeparateTableStorage extends StorageImplementation {

	/**
	 * {@link AssociationStorageDescriptor} which are used by this {@link SeparateTableStorage} to
	 * store values.
	 */
	List<? extends AssociationStorageDescriptor> getStorageDescriptors();


	/**
	 * Registers a locator for the table of each {@link #getStorageDescriptors() storage
	 * descriptor}, see
	 * {@link AssociationStorageDescriptor#findChangedOwners(PendingChanges, MetaObject, ObjectKey, java.util.function.Consumer)}.
	 */
	@Override
	default void addChangeLocators(TLStructuredTypePart attribute,
			BiConsumer<? super MetaObject, ? super ChangeLocator> locators) {
		addChangeLocators(attribute, getStorageDescriptors(), locators);
	}

	/**
	 * Registers a locator for the table of each of the given descriptors.
	 * 
	 * @param attribute
	 *        The attribute whose values are stored as described by the given descriptors.
	 * @param descriptors
	 *        The descriptors of the rows storing values of the given attribute.
	 * @param locators
	 *        See {@link #addChangeLocators(TLStructuredTypePart, BiConsumer)}.
	 */
	static void addChangeLocators(TLStructuredTypePart attribute,
			List<? extends AssociationStorageDescriptor> descriptors,
			BiConsumer<? super MetaObject, ? super ChangeLocator> locators) {
		ObjectKey partId = attribute.getDefinition().tId();
		MORepository repository = attribute.tKnowledgeBase().getMORepository();
		for (AssociationStorageDescriptor descriptor : descriptors) {
			MetaObject table = repository.getTypeOrNull(descriptor.getTable());
			if (table == null) {
				continue;
			}
			ChangeLocator locator =
				(changes, objects) -> descriptor.findChangedOwners(changes, table, partId, objects);
			locators.accept(table, locator);
		}
	}

	@Override
	default boolean canDetectValueChanges() {
		return true;
	}

}
