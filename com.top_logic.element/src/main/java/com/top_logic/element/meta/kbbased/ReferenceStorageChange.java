/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.kbbased.storage.LinkStorage;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Adapts the storage of a persistent {@link TLReference} to a change of a property of its
 * {@link TLReference#getEnd() end} that determines its default storage, e.g.
 * {@link TLReference#isBag()} or {@link TLReference#getDeletionPolicy()}.
 *
 * <p>
 * The change is captured {@link #before(PersistentEnd) before} the property changes and
 * {@link #apply() applied} afterwards in the same transaction: The storage implementations of the
 * reference and of the overrides copying its storage are recomputed. If the new storage differs
 * from the old one, the values of all objects holding a value are read through the old storage,
 * the old links are removed, and the values are written through the new storage.
 * </p>
 *
 * <p>
 * A reference without values costs a single query for the links of the reference in the table of
 * its old storage.
 * </p>
 */
final class ReferenceStorageChange {

	/**
	 * {@link ReferenceStorageChange} for a change that does not affect any storage.
	 */
	private static final ReferenceStorageChange NONE = new ReferenceStorageChange(null, null, null);

	private final PersistentReference _reference;

	private final StorageImplementation _oldStorage;

	private final HistoryType _oldHistoryType;

	private ReferenceStorageChange(PersistentReference reference, StorageImplementation oldStorage,
			HistoryType oldHistoryType) {
		_reference = reference;
		_oldStorage = oldStorage;
		_oldHistoryType = oldHistoryType;
	}

	/**
	 * Captures the storage of the reference of the given end before a property of the end
	 * changes.
	 *
	 * @param end
	 *        The end whose property is about to change.
	 * @return The change to {@link #apply()} after the property has changed.
	 */
	static ReferenceStorageChange before(PersistentEnd end) {
		if (end.tHandle().getState() != KnowledgeItem.State.PERSISTENT) {
			// A new end has no values, its reference computes the storage when accessed.
			return NONE;
		}
		if (TLModelUtil.getEndIndex(end) == 0) {
			// The end of a back reference: A back reference has no storage of its own, its values
			// are those of the forward reference, whose storage depends only on the properties of
			// its own end. Besides, the end of a back reference is shared by all overrides of the
			// back reference, so its properties are set while such an override is created and not
			// yet complete.
			return NONE;
		}
		if (!(end.getReference() instanceof PersistentReference reference)) {
			return NONE;
		}
		return new ReferenceStorageChange(reference, reference.getStorageImplementation(),
			reference.getHistoryType());
	}

	/**
	 * Recomputes the storage of the reference and moves its values to the new storage, if it
	 * differs from the old storage.
	 */
	void apply() {
		if (_reference == null) {
			return;
		}

		List<ConfiguredAttributeImpl> parts = partsWithStorageOf(_reference);
		for (ConfiguredAttributeImpl part : parts) {
			part.discardStorageImplementation();
		}

		StorageImplementation newStorage = _reference.getStorageImplementation();
		if (sameStorage(_oldStorage, newStorage)) {
			return;
		}

		if (!(_oldStorage instanceof LinkStorage<?> oldLinks) || !(newStorage instanceof LinkStorage<?>)) {
			Logger.warn("Values of reference '" + TLModelUtil.qualifiedName(_reference)
				+ "' are not moved from storage '" + _oldStorage + "' to '" + newStorage
				+ "', only values of link storages can be moved.", ReferenceStorageChange.class);
			return;
		}

		moveValues(oldLinks, newStorage);
	}

	private void moveValues(LinkStorage<?> oldStorage, StorageImplementation newStorage) {
		TLStructuredTypePart definition = _reference.getDefinition();
		String table = oldStorage.getTable();
		List<KnowledgeAssociation> links = _reference.tKnowledgeBase().search(queryUnresolved(
			filter(
				allOf(table),
				eqBinary(
					reference(table, WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR),
					literal(definition.tHandle()))),
			KnowledgeAssociation.class));
		if (links.isEmpty()) {
			return;
		}

		Map<TLObject, List<KnowledgeAssociation>> linksByOwner = new LinkedHashMap<>();
		for (KnowledgeAssociation association : links) {
			TLObject owner = association.getSourceObject().getWrapper();
			if (definition != _reference && !isOwner(owner)) {
				// Links of an object of another override of the same definition.
				continue;
			}
			linksByOwner.computeIfAbsent(owner, x -> new ArrayList<>()).add(association);
		}

		boolean toCurrent = _oldHistoryType != HistoryType.CURRENT
			&& _reference.getHistoryType() == HistoryType.CURRENT;
		boolean multiple = _reference.isMultiple();
		for (Map.Entry<TLObject, List<KnowledgeAssociation>> entry : linksByOwner.entrySet()) {
			TLObject owner = entry.getKey();
			Object value = oldStorage.getAttributeValue(owner, _reference);

			for (KnowledgeAssociation link : entry.getValue()) {
				// Clearing the references keeps composite parts, which would be deleted together
				// with the link otherwise.
				DBKnowledgeAssociation.clearReferencesAndRemoveLink(link);
			}

			newStorage.setAttributeValue(owner, _reference, convert(owner, value, multiple, toCurrent));
		}
	}

	/**
	 * Converts a value read from the old storage to a value of the new storage.
	 */
	private Object convert(TLObject owner, Object value, boolean multiple, boolean toCurrent) {
		List<Object> values = new ArrayList<>();
		if (value instanceof Collection<?> collection) {
			values.addAll(collection);
		} else if (value != null) {
			values.add(value);
		}
		if (toCurrent) {
			// A reference to the current revision cannot store an object in a former revision.
			values.replaceAll(v -> v instanceof TLObject obj ? WrapperHistoryUtils.getCurrent(obj) : v);
			values.removeIf(v -> v == null);
		}

		if (multiple) {
			return values;
		}
		if (values.size() > 1) {
			Logger.warn("Reference '" + TLModelUtil.qualifiedName(_reference) + "' of '" + owner
				+ "' is no longer multiple, keeping only its first value of " + values + ".",
				ReferenceStorageChange.class);
		}
		return values.isEmpty() ? null : values.get(0);
	}

	/**
	 * Whether the given object holds its value of the definition in {@link #_reference}.
	 */
	private boolean isOwner(TLObject owner) {
		return owner.tType() instanceof TLClass type && TLModelUtil.isGeneralization(_reference.getOwner(), type);
	}

	/**
	 * The given reference and all its overrides, whose storage is copied from the given reference.
	 */
	private static List<ConfiguredAttributeImpl> partsWithStorageOf(PersistentReference reference) {
		List<ConfiguredAttributeImpl> result = new ArrayList<>();
		result.add(reference);
		addOverridesWithStorageOf(result, reference, reference);
		return result;
	}

	private static void addOverridesWithStorageOf(List<ConfiguredAttributeImpl> result, TLClassPart part,
			PersistentReference template) {
		for (TLClassPart override : TLModelUtil.getOverridingParts(part)) {
			if (override instanceof ConfiguredAttributeImpl persistent
				&& StorageImplementationFactory.storageTemplate(override) == template) {
				result.add(persistent);
			}
			addOverridesWithStorageOf(result, override, template);
		}
	}

	/**
	 * Whether the given storages store the values in the same way.
	 */
	private static boolean sameStorage(StorageImplementation oldStorage, StorageImplementation newStorage) {
		if (oldStorage == newStorage) {
			return true;
		}
		if (oldStorage == null || newStorage == null || oldStorage.getClass() != newStorage.getClass()) {
			return false;
		}
		if (oldStorage instanceof LinkStorage<?> oldLinks) {
			return oldLinks.getTable().equals(((LinkStorage<?>) newStorage).getTable());
		}
		return true;
	}

}
