/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.ConcatenatedIterable;
import com.top_logic.basic.col.InlineSet;
import com.top_logic.basic.col.MappedIterable;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.BulkIdLoad;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.model.TLObject;

/**
 * Helper class to collect all changes that must be done for the deletion of an
 * {@link DBKnowledgeItem} and execute them if no problems are found.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReferenceDeletionHelper {

	private HashMap<MetaObject, Set<DBKnowledgeItem>> allDeletions = new HashMap<>();

	private HashMap<Pair<DBKnowledgeItem, MOReference>, Object> toBeUpdated =
		new HashMap<>();

	private final DBKnowledgeBase _kb;

	private final DefaultDBContext _context;

	ReferenceDeletionHelper(DBKnowledgeBase kb, DefaultDBContext context) {
		_kb = kb;
		_context = context;
	}

	private void clear() {
		allDeletions.clear();
		toBeUpdated.clear();
	}

	private boolean delete(MetaObject type, DBKnowledgeItem item) {
		if (!item.isAlive(_context)) {
			// item already deleted, e.g. in other thread
			return false;
		}
		Set<DBKnowledgeItem> deletionsForType = allDeletions.get(type);
		if (deletionsForType == null) {
			deletionsForType = new HashSet<>();
			allDeletions.put(type, deletionsForType);
		}
		return deletionsForType.add(item);
	}

	private void collectReferenceChange(DBKnowledgeItem referer, MOReference reference, Object newValue) {
		toBeUpdated.put(new Pair<>(referer, reference), newValue);
	}

	private void applyReferenceChanges() throws DataObjectException {
		for (Entry<Pair<DBKnowledgeItem, MOReference>, Object> entry : toBeUpdated.entrySet()) {
			DBKnowledgeItem referer = entry.getKey().getFirst();
			MOReference attribute = entry.getKey().getSecond();
			Object newValue = entry.getValue();
			referer.setValue(attribute, newValue);
		}
	}

	private void collectDeletions(DBKnowledgeItem deleted) {
		Map<MetaObject, List<DBKnowledgeItem>> itemsToProcess = new HashMap<>();
		deleteAndEnqueueNew(itemsToProcess, deleted);
		processNewlyDeleted(itemsToProcess);
	}

	private void collectDeletions(DBKnowledgeItem[] items, int size) {
		Map<MetaObject, List<DBKnowledgeItem>> itemsToProcess = new HashMap<>();
		for (int i = 0; i < size; i++) {
			deleteAndEnqueueNew(itemsToProcess, items[i]);
		}
		processNewlyDeleted(itemsToProcess);
	}

	/**
	 * @param itemsToProcess
	 *        Map from table to the list of newly deleted objects stored in the table.
	 */
	private void processNewlyDeleted(Map<MetaObject, List<DBKnowledgeItem>> itemsToProcess) {
		@SuppressWarnings("unchecked")
		Entry<MetaObject, List<DBKnowledgeItem>>[] entriesToProcess = new Entry[5];
		do {
			Set<Entry<MetaObject, List<DBKnowledgeItem>>> entrySet = itemsToProcess.entrySet();
			if (entrySet.isEmpty()) {
				/* No remaining items to process. */
				break;
			}
			/* Copy entries, because when processing the items of a given type causes deletion of an
			 * item of a different type, a ConcurrentModificationException is thrown. */
			entriesToProcess = entrySet.toArray(entriesToProcess);
			for (int i = 0, size = entriesToProcess.length; i < size; i++) {
				Entry<MetaObject, List<DBKnowledgeItem>> entry = entriesToProcess[i];
				if (entry == null) {
					/* Contract in Collection#toArray(T[]): if contents fits into the given array,
					 * null indicated the end of the size */
					break;
				}

				MetaObject type = entry.getKey();
				List<DBKnowledgeItem> list = entry.getValue();

				int nextIndex = 0;
				processDeletionsForType:
				while (true) {
					int endIndex = list.size();
					Object itemsToDelete;
					switch (endIndex - nextIndex) {
						case 0:
							// All objects for type processed.
							break processDeletionsForType;
						case 1:
							itemsToDelete = list.get(nextIndex);
							nextIndex = endIndex;
							break;
						default:
							itemsToDelete = list.subList(nextIndex, endIndex);
							// Must install a new list to avoid concurrent modification.
							list = new ArrayList<>();
							entry.setValue(list);
							nextIndex = 0;
					}
					deleteReferenceSources(itemsToProcess, type, itemsToDelete);
					deleteContents(itemsToProcess, type, itemsToDelete);
				}
				itemsToProcess.remove(type);
			}
		} while (true);
	}

	/**
	 * Deletes objects referencing the given items and adds them to the items to process.
	 * 
	 * @param itemsToProcess
	 *        Data structure holding the objects to process.
	 * @param type
	 *        The type of the given items.
	 * @param itemsToDelete
	 *        Either a {@link DBKnowledgeItem} or a {@link List} of {@link DBKnowledgeItem}.
	 */
	@SuppressWarnings("unchecked")
	private void deleteContents(Map<MetaObject, List<DBKnowledgeItem>> itemsToProcess, MetaObject type,
			Object itemsToDelete) {
		// Collect content keys for a bulk load.
		Object keysToLoad = InlineSet.newInlineSet();
		List<? extends MOReference> allAttributes = MetaObjectUtils.getReferences(type);
		for (MOReference reference : allAttributes) {
			if (!reference.isContainer()) {
				continue;
			}
			if (itemsToDelete instanceof DBKnowledgeItem) {
				DBKnowledgeItem item = (DBKnowledgeItem) itemsToDelete;
				ObjectKey currentReference = item.getReferencedKey(reference);
				if (currentReference != null) {
					keysToLoad = InlineSet.add(ObjectKey.class, keysToLoad, currentReference);
				}
			} else {
				List<DBKnowledgeItem> itemList = (List<DBKnowledgeItem>) itemsToDelete;
				for (int i = 0; i < itemList.size(); i++) {
					DBKnowledgeItem item = itemList.get(i);
					ObjectKey currentReference = item.getReferencedKey(reference);
					if (currentReference != null) {
						keysToLoad = InlineSet.add(ObjectKey.class, keysToLoad, currentReference);
					}
				}
			}
		}
		List<KnowledgeItem> contentsOfDeleted = BulkIdLoad.load(_kb, InlineSet.toSet(ObjectKey.class, keysToLoad));
		for (int i = 0; i < contentsOfDeleted.size(); i++) {
			deleteAndEnqueueNew(itemsToProcess, (DBKnowledgeItem) contentsOfDeleted.get(i));
		}
	}

	/**
	 * Deletes objects referencing the given items and adds them to the items to process.
	 * 
	 * @param itemsToProcess
	 *        Data structure holding the objects to process.
	 * @param type
	 *        The type of the given items.
	 * @param itemsToDelete
	 *        Either a {@link DBKnowledgeItem} or a {@link List} of {@link DBKnowledgeItem}.
	 */
	@SuppressWarnings("unchecked")
	private void deleteReferenceSources(Map<MetaObject, List<DBKnowledgeItem>> itemsToProcess, MetaObject type,
			Object itemsToDelete) {
		if (itemsToDelete instanceof DBKnowledgeItem) {
			deleteReferenceSources(itemsToProcess, type, (DBKnowledgeItem) itemsToDelete);
		} else {
			deleteReferenceSources(itemsToProcess, type, (List<DBKnowledgeItem>) itemsToDelete);
		}
	}

	private void deleteReferenceSources(Map<MetaObject, List<DBKnowledgeItem>> itemsToProcess, MetaObject type,
			DBKnowledgeItem item) {
		Map<MetaObject, CompiledQuery<DBKnowledgeItem>> queries =
			_kb.anyRefereesQuery(type, DeletionPolicy.DELETE_REFERER, Boolean.FALSE, DBKnowledgeItem.class);
		if (queries.isEmpty()) {
			return;
		}
		RevisionQueryArguments arguments = _kb.anyRefereesArguments(Revision.CURRENT, item);
		deleteReferenceSources(itemsToProcess, queries, arguments);
	}

	private void deleteReferenceSources(Map<MetaObject, List<DBKnowledgeItem>> itemsToProcess, MetaObject type,
			List<DBKnowledgeItem> items) {
		Map<MetaObject, CompiledQuery<DBKnowledgeItem>> queries =
			_kb.anyRefereesQuery(type, DeletionPolicy.DELETE_REFERER, Boolean.TRUE, DBKnowledgeItem.class);
		if (queries.isEmpty()) {
			return;
		}
		int maxSetSize = _kb.dbHelper.getMaxSetSize();
		if (items.size() > maxSetSize) {
			Iterator<List<DBKnowledgeItem>> it = CollectionUtil.chunk(maxSetSize, items.iterator());
			while (it.hasNext()) {
				RevisionQueryArguments arguments = _kb.anyRefereesArguments(Revision.CURRENT, it.next());
				deleteReferenceSources(itemsToProcess, queries, arguments);
			}
		} else {
			RevisionQueryArguments arguments = _kb.anyRefereesArguments(Revision.CURRENT, items);
			deleteReferenceSources(itemsToProcess, queries, arguments);
		}
	}

	private void deleteReferenceSources(Map<MetaObject, List<DBKnowledgeItem>> toBeProcessed,
			Map<MetaObject, CompiledQuery<DBKnowledgeItem>> queries, RevisionQueryArguments arguments) {
		for (Entry<MetaObject, CompiledQuery<DBKnowledgeItem>> entry : queries.entrySet()) {
			try (CloseableIterator<DBKnowledgeItem> result = entry.getValue().searchStream(arguments)) {
				while (result.hasNext()) {
					deleteAndEnqueueNew(toBeProcessed, result.next());
				}
			}
		}
	}

	private void deleteAndEnqueueNew(Map<MetaObject, List<DBKnowledgeItem>> toBeProcessed, DBKnowledgeItem item) {
		MOKnowledgeItem type = item.tTable();
		boolean isNewlyDeleted = delete(type, item);
		if (isNewlyDeleted) {
			List<DBKnowledgeItem> queueForType = toBeProcessed.get(type);
			if (queueForType == null) {
				queueForType = new ArrayList<>();
				toBeProcessed.put(type, queueForType);
			}
			queueForType.add(item);
		}
	}

	private void handleStabilizeReferenceValue() throws DataObjectException {
		long lastLocalRevision = _kb.getSessionRevision();
		Revision lastRevision = _kb.getHistoryManager().getRevision(lastLocalRevision);
		collectStabilizeReferenceValue(allDeletions, lastRevision);
	}

	private void collectStabilizeReferenceValue(HashMap<MetaObject, Set<DBKnowledgeItem>> deletedItems,
			Revision lastRevision) throws DataObjectException {
		for (Entry<MetaObject, Set<DBKnowledgeItem>> entry : deletedItems.entrySet()) {
			List<DBKnowledgeItem> allReferer = _kb.getAnyReferer(Revision.CURRENT, entry.getKey(), entry.getValue(),
					DeletionPolicy.STABILISE_REFERENCE, DBKnowledgeItem.class);
			collectStabilizeReferenceValue(entry.getValue(), allReferer, lastRevision);
		}
	}

	private void collectStabilizeReferenceValue(Set<? extends DBKnowledgeItem> deletedItems,
			Iterable<? extends DBKnowledgeItem> allReferer, Revision lastRevision) throws DataObjectException {
		for (DBKnowledgeItem referer : allReferer) {
			MetaObject metaObject = referer.tTable();
			List<? extends MOReference> allAttributes = MetaObjectUtils.getReferences(metaObject);
			for (MOReference reference : allAttributes) {
				if (reference.getDeletionPolicy() != DeletionPolicy.STABILISE_REFERENCE) {
					continue;
				}
				KnowledgeItem currentReferenceValue = (KnowledgeItem) referer.getValue(reference);
				if (deletedItems.contains(currentReferenceValue)) {
					KnowledgeItem newValue = HistoryUtils.getKnowledgeItem(lastRevision, currentReferenceValue);
					collectReferenceChange(referer, reference, newValue);
				}
			}
		}
	}

	private void handleClearReferenceValue() {
		collectClearReferenceValue(allDeletions);
	}

	/**
	 * Changes the value of each reference with the given {@link DeletionPolicy} from the given
	 * {@link KnowledgeObject} to the new value.
	 */
	private void collectClearReferenceValue(HashMap<MetaObject, Set<DBKnowledgeItem>> items) {
		for (Entry<MetaObject, Set<DBKnowledgeItem>> entry : items.entrySet()) {
			List<DBKnowledgeItem> allReferer =
				_kb.getAnyReferer(Revision.CURRENT, entry.getKey(), entry.getValue(), DeletionPolicy.CLEAR_REFERENCE,
					DBKnowledgeItem.class);
			collectClearReferenceValue(entry.getValue(), allReferer);
		}
	}

	/**
	 * Changes the value of each reference with the given {@link DeletionPolicy} in the given
	 * collection from the given {@link KnowledgeObject} to the new value.
	 */
	private void collectClearReferenceValue(Set<? extends DBKnowledgeItem> deletedItems,
			Iterable<? extends DBKnowledgeItem> allReferer) {
		for (DBKnowledgeItem referer : allReferer) {
			MetaObject metaObject = referer.tTable();
			List<? extends MOReference> allAttributes = MetaObjectUtils.getReferences(metaObject);
			for (MOReference reference : allAttributes) {
				if (reference.getDeletionPolicy() != DeletionPolicy.CLEAR_REFERENCE) {
					continue;
				}
				KnowledgeItem currentReferenceValue = (KnowledgeItem) referer.getValue(reference);
				if (deletedItems.contains(currentReferenceValue)) {
					collectReferenceChange(referer, reference, null);
				}
			}
		}
	}
	

	private void applyCollectedDeletions() {
		for (Set<DBKnowledgeItem> deletedForType : allDeletions.values()) {
			for (DBKnowledgeItem deleted : deletedForType) {
				_context.removeItem(deleted);
			}
		}
	}

	/**
	 * Executes all changes formerly collected by {@link #collectChangesForDeleted(DBKnowledgeItem)}
	 */
	List<Modification> executeCollectedChanges() throws DataObjectException {
		applyReferenceChanges();
		List<Modification> modifications = fireDeleteEvents();
		applyCollectedDeletions();
		clear();
		return modifications;
	}

	private List<Modification> fireDeleteEvents() {
		new DeleteChecker(deletions()).checkAll();

		List<Modification> modifications = Collections.emptyList();
		for (Set<DBKnowledgeItem> deletedForType : allDeletions.values()) {
			for (DBKnowledgeItem deleted : deletedForType) {
				TLObject wrapper = deleted.getWrapper();
				if (wrapper instanceof PersistentObject) {
					Modification mod = ((PersistentObject) wrapper).notifyUpcomingDeletion();
					if (mod != Modification.NONE) {
						modifications = mkModifiable(modifications);
						modifications.add(mod);
					}
				}

				List<Modification> result = _kb.fireUpcomingDeletionEvent(_context, deleted);
				if (!result.isEmpty()) {
					modifications = mkModifiable(modifications);
					modifications.addAll(result);
				}
			}
		}
		return modifications;
	}

	private static <T> List<T> mkModifiable(List<T> list) {
		if (list == Collections.emptyList()) {
			return new ArrayList<>();
		}
		return list;
	}

	private Iterable<? extends TLObject> deletions() {
		return new MappedIterable<>(item -> item.getWrapper(), ConcatenatedIterable.concat(allDeletions.values()));
	}

	/**
	 * Collects all changes that must be done to delete the given {@link DBKnowledgeItem}.
	 * 
	 * No changes are done until {@link #executeCollectedChanges()} is called.
	 */
	void collectChangesForDeleted(DBKnowledgeItem deleted) throws DataObjectException {
		collectDeletions(deleted);
		handleClearReferenceValue();
		handleStabilizeReferenceValue();
	}

	/**
	 * Collects all changes that must be done to delete the given {@link DBKnowledgeItem}s.
	 * 
	 * No changes are done until {@link #executeCollectedChanges()} is called.
	 */
	void collectChangesForDeleted(DBKnowledgeItem[] items, int size) throws DataObjectException {
		collectDeletions(items, size);
		handleClearReferenceValue();
		handleStabilizeReferenceValue();
	}
}

