/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.v5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.ByValueReferenceStorageImpl;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.BulkIdLoad;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.export.PreloadOperation;

/**
 * {@link PreloadOperation} that loads each object that is referenced by an object of the base
 * collection in a given reference.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReferencePreload implements PreloadOperation {

	private final String _referenceType;

	private final String _referenceAttribute;

	/**
	 * Creates a new {@link ReferencePreload}.
	 * 
	 * @param referenceType
	 *        The name of the type defining the reference to load.
	 * @param referenceAttribute
	 *        Name of the {@link MOReference} which is used to load referenced objects.
	 */
	public ReferencePreload(String referenceType, String referenceAttribute) {
		_referenceType = referenceType;
		_referenceAttribute = referenceAttribute;
	}

	@Override
	public void prepare(PreloadContext context, Collection<?> baseObjects) {
		List<KnowledgeObject> items = getKnowledgeItems(baseObjects);
		if (items.isEmpty()) {
			return;
		}
		KnowledgeBase kb = items.get(0).getKnowledgeBase();
		if (kb == null) {
			return;
		}
		MOReference reference;
		try {
			reference = getReference(kb.getMORepository());
		} catch (UnknownTypeException ex) {
			Logger.error("No such type: " + _referenceType, ex, ReferencePreload.class);
			return;
		} catch (NoSuchAttributeException ex) {
			Logger.error("No attribute '" + _referenceAttribute + "' in type '" + _referenceType + "'.", ex,
				ReferencePreload.class);
			return;
		}

		HashSet<ObjectKey> referencedKeys = getReferencedKeys(items, reference);
		List<TLObject> references;
		switch (referencedKeys.size()) {
			case 0: {
				references = new ArrayList<>();
				break;
			}
			case 1: {
				references = new ArrayList<>(1);
				KnowledgeItem resolvedItem = kb.resolveObjectKey(referencedKeys.iterator().next());
				references.add(WrapperFactory.getWrapper((KnowledgeObject) resolvedItem));
				break;
			}
			default: {
				BulkIdLoad bulkIdLoad = new BulkIdLoad(kb);
				bulkIdLoad.addAll(referencedKeys);
				List<KnowledgeItem> loadedItems = bulkIdLoad.load();
				references = WrapperFactory.getWrappersForKOsGeneric(loadedItems);
			}
		}

		loadReferenceByValue(items, reference);

		processReferences(context, references);
	}

	/**
	 * Handles the newly loaded referenced objects.
	 * 
	 * @param context
	 *        Context in which pre-loading occurred.
	 * @param references
	 *        the newly loaded referenced objects
	 */
	protected void processReferences(PreloadContext context, List<TLObject> references) {
		for (TLObject item : references) {
			context.keepObject(item);
		}
	}

	private void loadReferenceByValue(List<KnowledgeObject> items, MOReference reference) {
		if (reference.getStorage() instanceof ByValueReferenceStorageImpl) {
			/* MOReference is a reference that holds the complete object in cache. Internally the
			 * cache is either the item itself or the ObjectKey of the referenced item if the item
			 * is not loaded yet. It should be ensured that the cache of the reference now is the
			 * item itself, cause it is present. */
			for (KnowledgeObject item : items) {
				/* fetching value causes reference internally to change cache value from ObjectKey
				 * to KnowledgeItem. */
				item.getValue(reference);
			}
		}
	}

	private MOReference getReference(MORepository typeRepository) throws UnknownTypeException, NoSuchAttributeException {
		MetaObject metaObject = typeRepository.getMetaObject(_referenceType);
		return MetaObjectUtils.getReference(metaObject, _referenceAttribute);
	}

	private HashSet<ObjectKey> getReferencedKeys(Collection<? extends KnowledgeItem> items, MOReference reference) {
		HashSet<ObjectKey> referencedKeys = CollectionUtil.newSet(items.size());
		for (KnowledgeItem item : items) {
			if (item.tTable().isSubtypeOf(reference.getOwner())) {
				continue;
			}

			ObjectKey referencedKey = item.getReferencedKey(reference);
			if (referencedKey == null) {
				// currently no reference set, nothing to load
				continue;
			}
			referencedKeys.add(referencedKey);
		}
		return referencedKeys;
	}

	private List<KnowledgeObject> getKnowledgeItems(Collection<?> wrappers) {
		List<KnowledgeObject> baseKOs = new ArrayList<>(wrappers.size());
		for (Object obj : wrappers) {
			TLObject wrapper = (TLObject) obj;
			// It does only make sense to retrieve wrapped objects from wrappers,
			// which are alive and therefore fetchable.
			if (PreloadOperation.canFetch(wrapper)) {
				baseKOs.add((KnowledgeObject) wrapper.tHandle());
			}
		}
		return baseKOs;
	}

}

