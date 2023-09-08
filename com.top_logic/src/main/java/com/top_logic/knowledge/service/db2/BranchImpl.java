/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.LongID;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.InternalBranch;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.util.TLContext;

/**
 * {@link DBKnowledgeBase} internal {@link Branch} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/ 
class BranchImpl extends DBSystemObject implements Branch, InternalBranch {

	static final long NO_BASE_BRANCH = 0;

	private Map<MetaObject, Long> baseBranchIdByType;
	
	/** @see #createRevision() */
	private long _createRevision;

	/** @see #baseRevision() */
	private long _baseRevision;

	/** @see #branchId() */
	private long _branchId;

	/** @see #baseBranch() */
	private long _baseBranch = NO_BASE_BRANCH;

	public BranchImpl(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		super(kb, staticType);
	}
	
	@Override
	public void onLoad(PooledConnection readConnection) {
		super.onLoad(readConnection);
		initBaseBranchIdByType(readConnection);
	}

	@Override
	public long getBranchId() {
		return branchId();
	}
	
	@Override
	public Revision getCreateRevision() {
		return this.getKnowledgeBase().getRevision(createRevision());
	}

	@Override
	public Branch getBaseBranch() {
		long baseBranchId = baseBranch();
		if (baseBranchId == NO_BASE_BRANCH) {
			// Only in case of trunk.
			return null;
		}
		return getKnowledgeBase().getBranch(baseBranchId);
	}

	@Override
	public Revision getBaseRevision() {
		return getKnowledgeBase().getRevision(baseRevision());
	}

	@Override
	public HistoryManager getHistoryManager() {
		return getKnowledgeBase();
	}

	/* package protected */void initNew(long branchId, long createRev, long baseBranchId, long baseRev,
			Map<MetaObject, Long> baseBranchIdByType) {
		initBranchId(branchId);
		initCreateRevision(createRev);
		initBaseBranch(baseBranchId);
		initBaseRevision(baseRev);
		
		this.baseBranchIdByType = baseBranchIdByType;
		addSystemTypes();

		initIdentifier(LongID.valueOf(branchId), TLContext.TRUNK_ID);
	}

	Set<String> getBranchedTypes() {
		HashSet<String> branchedTypes = new HashSet<>();
		long branchId = getBranchId();
		for (Entry<MetaObject, Long> idByType : baseBranchIdByType.entrySet()) {
			if (idByType.getValue().longValue() == branchId) {
				branchedTypes.add(idByType.getKey().getName());
			}
		}
		return branchedTypes;
	}

	@Override
	public List<Branch> getChildBranches() {
		DBKnowledgeBase kb = getKnowledgeBase();
		CompiledQuery<Branch> compiledQuery = kb._expressions.childBranchQuery();
		RevisionQueryArguments arguments = kb._expressions.childBranchArguments(Long.valueOf(getBranchId()));
		return compiledQuery.search(arguments);
	}

	@Override
	public long getBaseBranchId(MetaObject type) {
		Long baseBranch = baseBranchIdByType.get(type);
		if (baseBranch == null) {
			return getBranchId();
		}
		return baseBranch.longValue();
	}
	
	private void initBaseBranchIdByType(PooledConnection readConnection) {
		baseBranchIdByType = readBaseBranchIdByType(getKnowledgeBase(), readConnection, this);
		addSystemTypes();
	}

	private void addSystemTypes() {
		// mark system types to have types on trunk
		for (MetaObject systemType : getKnowledgeBase().getSystemTypes()) {
			Long clash = baseBranchIdByType.put(systemType, Long.valueOf(TLContext.TRUNK_ID));
			assert clash == null : "A different branch than trunk '" + clash
			+ "' was registered as base branch for type '" + systemType + "'";
		}
	}

	private static Map<MetaObject, Long> readBaseBranchIdByType(DBKnowledgeBase kb, PooledConnection readConnection,
			Branch branch) {
		CompiledQuery<KnowledgeItemInternal> compiledQuery = kb._expressions.dataBranchByTypeQuery();
		RevisionQueryArguments arguments =
			kb._expressions.dataBranchByTypeArguments(Long.valueOf(branch.getBranchId()));
		MOClass branchLinkType = kb.getBranchSwitchType();
		MOAttribute linkType = BranchSupport.getLinkTypeAttr(branchLinkType).getAttribute();
		MOAttribute linkDataBranch = BranchSupport.getLinkDataBranchAttr(branchLinkType).getAttribute();
		try (CloseableIterator<KnowledgeItemInternal> stream = compiledQuery.searchStream(readConnection, arguments)) {
			MORepository moRepository = kb.getMORepository();
			
			HashMap<MetaObject, Long> result = new HashMap<>();
			while (stream.hasNext()) {
				KnowledgeItemInternal link = stream.next();

				String typeName = (String) link.getValue(linkType);
				Long sourceBranchId = (Long) link.getValue(linkDataBranch);
				MOKnowledgeItemImpl type = (MOKnowledgeItemImpl) moRepository.getTypeOrNull(typeName);
				if (type == null) {
					Logger.error(
						"No definition for table reference '" + typeName + "' found in branch '" + branch + "'.",
						BranchImpl.class);
					continue;
				}

				result.put(type, sourceBranchId);
			}
			return result;
		}
	}

	/**
	 * Returns the set of names of types which are branched in branch with given branch id
	 */
	static Set<String> readBranchedTypeNames(PooledConnection readConnection, DBKnowledgeBase kb, Long branchId) {
		CompiledQuery<KnowledgeItemInternal> query = kb._expressions.branchedTypesQuery();
		RevisionQueryArguments arguments = kb._expressions.branchedTypesArguments(branchId);
		MOClass branchLinkType = kb.getBranchSwitchType();
		MOAttribute linkType = BranchSupport.getLinkTypeAttr(branchLinkType).getAttribute();

		try (CloseableIterator<KnowledgeItemInternal> stream = query.searchStream(readConnection, arguments)) {
			Set<String> result = new HashSet<>();
			while (stream.hasNext()) {
				KnowledgeItemInternal link = stream.next();

				String typeName = (String) link.getValue(linkType);
				result.add(typeName);
			}
			return result;
		}
	}

	Map<MetaObject, Long> getBaseBranchIdByNonSystemType() {
		Map<MetaObject, Long> result = new HashMap<>(baseBranchIdByType);
		for (MOKnowledgeItem systemType : getKnowledgeBase().getSystemTypes()) {
			Long id = result.remove(systemType);
			assert id != null : "System type " + systemType + " not contained in cached base branch by ID";
		}
		return result;
	}

	@Override
	public long getCreateCommitNumber() {
		return createRevision();
	}

	/**
	 * {@link Revision#getCommitNumber() Commit number} of the revision in which this branch was
	 * created.
	 * 
	 * @see Branch#getCreateRevision()
	 */
	@Override
	public long createRevision() {
		return _createRevision;
	}

	/**
	 * Initialises value of {@link #createRevision()}.
	 */
	void initCreateRevision(long revision) {
		_createRevision = revision;
	}

	/**
	 * {@link Revision#getCommitNumber() Revision} of the {@link #baseBranch()} from which this
	 * branch was spawned.
	 * 
	 * @see Branch#getBaseRevision()
	 */
	@Override
	public long baseRevision() {
		return _baseRevision;
	}

	/**
	 * Initialises value of {@link #baseRevision()}.
	 */
	void initBaseRevision(long revision) {
		_baseRevision = revision;
	}

	/**
	 * Identifier of this {@link Branch}.
	 * 
	 * @see Branch#getBranchId()
	 */
	@Override
	public long branchId() {
		return _branchId;
	}

	/**
	 * Initialises value of {@link #branchId()}.
	 */
	void initBranchId(long id) {
		_branchId = id;
	}

	/**
	 * The identifier of the {@link Branch} this branch bases on, or <code>null</code> if there is
	 * no base branch.
	 * 
	 * @see Branch#getBaseBranch()
	 */
	@Override
	public long baseBranch() {
		return _baseBranch;
	}

	/**
	 * Initialises value of {@link #baseBranch()}.
	 */
	void initBaseBranch(long baseBranch) {
		_baseBranch = baseBranch;
	}
}
