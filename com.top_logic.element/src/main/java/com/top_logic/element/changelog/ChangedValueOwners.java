/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.meta.AssociationStorageDescriptor;
import com.top_logic.element.meta.SeparateTableStorage;
import com.top_logic.element.model.cache.ModelTables;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.UpdateEvent;

/**
 * Determination of the objects whose attribute values changed through rows stored in separate
 * tables.
 *
 * <p>
 * An attribute stored by a {@link SeparateTableStorage} (e.g. a reference stored in a link table or
 * the translations of an internationalized attribute) is changed by creating, updating, or deleting
 * rows of another table than the table of the object owning the attribute. The owner's row stays
 * untouched. This class maps such row changes back to the owners, based on the
 * {@link AssociationStorageDescriptor}s of the storages.
 * </p>
 *
 * @see KBChangeAnalzyer
 */
public class ChangedValueOwners {

	/**
	 * The (current) keys of the objects whose attribute values stored in separate tables were
	 * changed in the given event.
	 *
	 * <p>
	 * Objects deleted in the given event are not reported.
	 * </p>
	 *
	 * @param modelTables
	 *        Description of the tables used for storing attribute values.
	 * @param event
	 *        The committed change.
	 * @return The keys of the objects with changed attribute values, each key reported once.
	 */
	public static Set<ObjectKey> of(ModelTables modelTables, UpdateEvent event) {
		Set<ObjectKey> result = new LinkedHashSet<>();
		ChangeSet changes = event.getChanges();
		addOwners(result, modelTables, changes.getCreations());
		addOwners(result, modelTables, changes.getUpdates());
		addOwners(result, modelTables, changes.getDeletions());

		if (!result.isEmpty()) {
			for (ObjectKey deleted : event.getDeletedObjectKeys()) {
				result.remove(current(deleted));
			}
		}
		return result;
	}

	private static void addOwners(Set<ObjectKey> result, ModelTables modelTables,
			Collection<? extends ItemChange> changes) {
		for (ItemChange change : changes) {
			Map<String, AssociationStorageDescriptor> descriptors =
				modelTables.getDescriptorsForTable(change.getObjectType());
			if (descriptors.isEmpty()) {
				continue;
			}
			for (AssociationStorageDescriptor descriptor : descriptors.values()) {
				addOwner(result, descriptor, change.getValues());
				if (change instanceof ItemUpdate update) {
					Map<String, Object> oldValues = update.getOldValues();
					if (oldValues != null) {
						// Contains the previous owner, if the row was moved to another owner.
						addOwner(result, descriptor, oldValues);
					}
				}
			}
		}
	}

	private static void addOwner(Set<ObjectKey> result, AssociationStorageDescriptor descriptor,
			Map<String, Object> values) {
		ObjectKey owner = descriptor.getBaseObjectId(values);
		if (owner != null) {
			result.add(current(owner));
		}
	}

	private static ObjectKey current(ObjectKey key) {
		return KBUtils.ensureHistoryContext(key, Revision.CURRENT_REV);
	}

}
