/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;
import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.db2.FlexAttributeFetch;

/**
 * Algorithm for bulk-loading multiple object by their {@link KnowledgeItem#tId()}.
 * 
 * <p>
 * Usage: After construction a set of {@link ObjectKey}s can be {@link #add(ObjectKey) added} for a
 * load operation. Afterwards, a single {@link #load()} retrieves all objects with the added keys
 * from the persistency.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BulkIdLoad {
	
	private static final long NO_SPECIAL_DATA_REVISION = -1;
	
	private static final TLID NO_ID = StringID.valueOf("none");

	private Map<ObjectKey, Set<TLID>> _idsByCategory = new HashMap<>();

	private final KnowledgeBase _kb;

	private final List<KnowledgeItem> _result = new ArrayList<>();

	/**
	 * Creates a {@link BulkIdLoad}.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to resolve objects from.
	 */
	public BulkIdLoad(KnowledgeBase kb) {
		_kb = kb;
	}

	/**
	 * {@link #add(ObjectKey) Adds} multiple {@link ObjectKey} for this load operation.
	 */
	public void addAll(Iterable<? extends ObjectKey> keys) {
		for (ObjectKey key : keys) {
			add(key);
		}
	}

	/**
	 * Adds the given {@link ObjectKey} to the preload operation.
	 */
	public void add(ObjectKey key) {
		KnowledgeItem cachedObject = _kb.resolveCachedObjectKey(key);
		if (cachedObject == null) {
			scheduleForLoad(key);
		} else {
			_result.add(cachedObject);
		}
	}

	private void scheduleForLoad(ObjectKey key) {
		DefaultObjectKey category =
			new DefaultObjectKey(key.getBranchContext(), key.getHistoryContext(), key.getObjectType(), NO_ID);
		MultiMaps.add(_idsByCategory, category, key.getObjectName());
	}

	/**
	 * Bulk-loads all objects with keys {@link #add(ObjectKey) added} so far.
	 * 
	 * @return The loaded objects.
	 */
	public List<KnowledgeItem> load() {
		return loadUncachedInRevision(NO_SPECIAL_DATA_REVISION);
	}

	/**
	 * <p>
	 * HANDLE THIS METHOD WITH CARE!
	 * </p>
	 * 
	 * <p>
	 * Bulk-loads all objects with keys {@link #add(ObjectKey) added} so far in the given data
	 * revision.
	 * </p>
	 * 
	 * <p>
	 * This makes only sense when all added items have current history context. Only newly loaded
	 * items have given data revision. Items already cached are not refetched or updated. present in
	 * cache.
	 * </p>
	 * 
	 * @return The loaded objects.
	 */
	@FrameworkInternal
	public List<KnowledgeItem> loadUncachedInRevision(long dataRevision) {
		loadTableAttributesUncached(dataRevision);
		loadFlexAttributes(dataRevision);
		return _result;
	}

	/**
	 * Loads the attributes stored in the database table of the object.
	 * <p>
	 * See {@link #loadUncachedInRevision(long)} for details.
	 * </p>
	 */
	protected void loadTableAttributesUncached(long dataRevision) {
		if (_idsByCategory.size() > 0) {
			int chunkSize = getChunkSize(_kb);
			for (Entry<ObjectKey, Set<TLID>> entry : _idsByCategory.entrySet()) {
				ObjectKey category = entry.getKey();

				Set<TLID> monomorphicIds = entry.getValue();
				for (List<TLID> chunk : toIterable(chunk(chunkSize, monomorphicIds.iterator()))) {
					_result.addAll(resolveIdentifiers(_kb, category, chunk, dataRevision));
				}
			}

			_idsByCategory.clear();
		}
	}

	/**
	 * Loads the attributes stored in the flex data table.
	 * <p>
	 * Unlike {@link #loadTableAttributesUncached(long)} the revision is not specified, as the data
	 * is being loaded in the revision of the {@link KnowledgeItem}.
	 * </p>
	 */
	protected void loadFlexAttributes(long revision) {
		FlexAttributeFetch.INSTANCE.prepareKnowledgeItems(revision, _result);
	}

	/**
	 * Removes the formerly loaded cached objects and keys that are scheduled for next load, i.e.
	 * {@link #load()} returns an empty result.
	 */
	public void clear() {
		_result.clear();
		_idsByCategory.clear();
	}

	/**
	 * Loads all items represented by the given id's and returns them.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to resolve items in.
	 * @param ids
	 *        Set of {@link ObjectKey} of the items to load.
	 * 
	 * @return List of items corresponding to the given identifiers. The sizes must not match,
	 *         because e.g. the object for a given id does not exist.
	 */
	public static List<KnowledgeItem> load(KnowledgeBase kb, Collection<? extends ObjectKey> ids) {
		switch (ids.size()) {
			case 0:
				return Collections.emptyList();
			case 1:
				KnowledgeItem resolvedItem = kb.resolveObjectKey(ids.iterator().next());
				return CollectionUtil.singletonOrEmptyList(resolvedItem);
			default:
				BulkIdLoad loader = new BulkIdLoad(kb);
				loader.addAll(ids);
				return loader.load();
		}
	}

	private static Collection<KnowledgeItem> resolveIdentifiers(KnowledgeBase kb, ObjectKey category, List<TLID> ids,
			long dataRevision) {
		RevisionQuery<KnowledgeItem> meQuery = queryUnresolved(
			filter(
				allOf(category.getObjectType().getName()),
				inSet(
					attribute(BasicTypes.ITEM_TYPE_NAME, BasicTypes.IDENTIFIER_ATTRIBUTE_NAME),
					setLiteral(ids))), KnowledgeItem.class).setFullLoad();
		RevisionQueryArguments args = revisionArgs();
		args.setRequestedBranch(kb.getHistoryManager().getBranch(category.getBranchContext()));
		args.setRequestedRevision(category.getHistoryContext());
		if (dataRevision != NO_SPECIAL_DATA_REVISION) {
			args.setDataRevision(dataRevision);
		}

		return kb.search(meQuery, args);
	}

	private static int getChunkSize(KnowledgeBase kb) {
		final int maxSize = 1000;
		try {
			return Math.min(maxSize, KBUtils.getConnectionPool(kb).getSQLDialect().getMaxSetSize());
		} catch (SQLException ex) {
			return maxSize;
		}
	}

}

