/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.db2.DBKnowledgeItem;
import com.top_logic.knowledge.service.db2.FlexData;

/**
 * {@link FlexDataManager} delegating all calls to a proxy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public class FlexDataManagerProxy implements FlexDataManager {

	private final FlexDataManager _impl;

	/**
	 * Creates a {@link FlexDataManagerProxy}.
	 * 
	 * @param impl
	 *        The delegate.
	 */
	public FlexDataManagerProxy(FlexDataManager impl) {
		_impl = impl;
	}

	@Override
	public FlexData load(KnowledgeBase kb, ObjectKey key, boolean mutable) {
		return _impl.load(kb, key, mutable);
	}

	@Override
	public FlexData load(KnowledgeBase kb, ObjectKey key, long dataRevision, boolean mutable) {
		return _impl.load(kb, key, dataRevision, mutable);
	}

	@Override
	public <T> void loadAll(long dataRevision, AttributeLoader<T> callback,
			Mapping<? super T, ? extends ObjectKey> keyMapping, List<T> baseObjects, KnowledgeBase kb) {
		_impl.loadAll(dataRevision, callback, keyMapping, baseObjects, kb);
	}

	@Override
	public boolean store(ObjectKey key, FlexData flexData, CommitContext context) {
		return _impl.store(key, flexData, context);
	}

	@Override
	public boolean delete(ObjectKey key, CommitContext context) {
		return _impl.delete(key, context);
	}

	@Override
	public void branch(PooledConnection context, long branchId, long createRev, long baseBranchId, long baseRevision,
			Collection<String> branchedTypNames) throws SQLException {
		_impl.branch(context, branchId, createRev, baseBranchId, baseRevision, branchedTypNames);
	}

	@Override
	public void addAll(List<DBKnowledgeItem> items, CommitContext context) throws SQLException {
		_impl.addAll(items, context);
	}

	@Override
	public void updateAll(List<DBKnowledgeItem> items, CommitContext context) throws SQLException {
		_impl.updateAll(items, context);
	}

	@Override
	public void deleteAll(List<DBKnowledgeItem> items, CommitContext context) throws SQLException {
		_impl.deleteAll(items, context);
	}
}
