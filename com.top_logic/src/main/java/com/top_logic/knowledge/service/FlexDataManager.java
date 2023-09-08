/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.NamedValues;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.DBKnowledgeItem;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.service.db2.NoFlexData;

/**
 * A {@link FlexDataManager} associates an {@link NamedValues untyped collection of named values}
 * with a {@link ObjectKey}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public interface FlexDataManager {

	/**
	 * Loads the values associated with the object identified by the given key with data in the
	 * revision of the object, i.e. if the key represents a historic object in the same history
	 * context, otherwise in the session revision.
	 * 
	 * @see #load(KnowledgeBase, ObjectKey, long, boolean)
	 */
	FlexData load(KnowledgeBase kb, ObjectKey key, boolean mutable);

	/**
	 * Loads the values associated with the object identified by the given key with data in the
	 * given revision.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to read data from.
	 * @param key
	 *        The identifier of the object for which the untyped values are requested.
	 * @param dataRevision
	 *        The revision the data shall have.
	 * @param mutable
	 *        Whether the result should be modifiable.
	 * 
	 * @return A collection of named values associated with the given object.
	 * 
	 * @implNote If no mutable {@link FlexData} are requested and no data are available, then
	 *           {@link NoFlexData} should be returned.
	 */
	FlexData load(KnowledgeBase kb, ObjectKey key, long dataRevision, boolean mutable);

	/**
	 * Bulk-load attribute data for the given base objects.
	 * 
	 * @param callback
	 *        The {@link AttributeLoader} that should consume the loaded attribute data.
	 * @param keyMapping
	 *        The {@link Mapping} that reports the key for each base object.
	 * @param baseObjects
	 *        The objects to load attribute data for.
	 * @param kb
	 *        {@link KnowledgeBase} which knows the base objects.
	 */
	default <T> void loadAll(AttributeLoader<T> callback, Mapping<? super T, ? extends ObjectKey> keyMapping,
			List<T> baseObjects, KnowledgeBase kb) {
		loadAll(DBKnowledgeBase.IN_SESSION_REVISION, callback, keyMapping, baseObjects, kb);
	}

	/**
	 * Bulk-load attribute data for the given base objects.
	 * 
	 * @param dataRevision
	 *        The revision whose data should be loaded.
	 * @param callback
	 *        The {@link AttributeLoader} that should consume the loaded attribute data.
	 * @param keyMapping
	 *        The {@link Mapping} that reports the key for each base object.
	 * @param baseObjects
	 *        The objects to load attribute data for.
	 * @param kb
	 *        {@link KnowledgeBase} which knows the base objects.
	 */
	<T> void loadAll(long dataRevision, AttributeLoader<T> callback, Mapping<? super T, ? extends ObjectKey> keyMapping,
			List<T> baseObjects, KnowledgeBase kb);

	/**
	 * Stores the given modified values.
	 * 
	 * @param key
	 *        The Key of the object for which the values should be stored.
	 * @param flexData
	 *        The modified values. The argument must be retrieved by a corresponding call to
	 *        {@link #load(KnowledgeBase, ObjectKey, boolean)} with the same key and the
	 *        <code>mutable</code> flag set.
	 * @param context
	 *        The current commit context.
	 * 
	 * @return Whether storing succeeded.
	 */
	boolean store(ObjectKey key, FlexData flexData, CommitContext context);

	/**
	 * Delete the values associated with the given object.
	 * 
	 * @param key
	 *        The object whose values should be cleared.
	 * @param context
	 *        The current commit context.
	 * @return whether deleting succeeded.
	 */
	boolean delete(ObjectKey key, CommitContext context);

	/**
	 * Copies the data of the attributes whose type is contained in <code>branchedTypNames</code>
	 * from one branch to another.
	 * 
	 * @param context
	 *        the context in which branching occurs
	 * @param branchId
	 *        the Id of the branch to copy to
	 * @param baseBranchId
	 *        the Id of the branch to copy from
	 * @param branchedTypNames
	 *        the names of the types whose attributes must be branched
	 * @throws SQLException
	 *         if some problems occur in communication with database
	 */
	void branch(PooledConnection context, long branchId, long createRev, long baseBranchId, long baseRevision,
			Collection<String> branchedTypNames) throws SQLException;

	/**
	 * Writes the dynamic attributes of the given list of new items to the storage.
	 * 
	 * @param items
	 *        List of new items to store.
	 * @param context
	 *        Current commit context.
	 * @throws SQLException
	 *         if some problems occur in communication with database
	 */
	void addAll(List<DBKnowledgeItem> items, CommitContext context) throws SQLException;

	/**
	 * Updates the dynamic attributes of the given list of updated items to the storage.
	 * 
	 * @param items
	 *        List of changed items to store.
	 * @param context
	 *        Current commit context.
	 * @throws SQLException
	 *         if some problems occur in communication with database
	 */
	void updateAll(List<DBKnowledgeItem> items, CommitContext context) throws SQLException;

	/**
	 * Deletes the dynamic attributes of the given list of deleted items in the storage.
	 * 
	 * @apiNote For best performance, order items first by {@link ObjectKey#getBranchContext()
	 *          branch} and then by {@link ObjectKey#getObjectType() type}.
	 * 
	 * @param items
	 *        List of deleted items to store.
	 * @param context
	 *        Current commit context.
	 * @throws SQLException
	 *         if some problems occur in communication with database
	 */
	void deleteAll(List<DBKnowledgeItem> items, CommitContext context) throws SQLException;

}
