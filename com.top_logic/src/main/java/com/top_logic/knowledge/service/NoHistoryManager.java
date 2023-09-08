/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectReference;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.model.TransientObject;
import com.top_logic.util.TLContext;

/**
 * Wrapper around a non-versioning {@link KnowledgeBase} that provides a dummy
 * implementation of {@link HistoryManager} to be returned by
 * {@link HistoryUtils} to support backward compatibility.
 * 
 * @see HistoryUtils A static facade with convenience methods.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class NoHistoryManager implements HistoryManager {

	private final class Trunk extends TransientObject implements Branch {

		public Trunk() {
			// nothing to set here
		}

		@Override
		public Branch getBaseBranch() {
			return null;
		}

		@Override
		public long getBaseBranchId(MetaObject type) {
			return TLContext.TRUNK_ID;
		}

		@Override
		public Revision getBaseRevision() {
			return Revision.INITIAL;
		}

		@Override
		public long getBranchId() {
			return TLContext.TRUNK_ID;
		}

		@Override
		public List<Branch> getChildBranches() {
			return Collections.emptyList();
		}

		@Override
		public Revision getCreateRevision() {
			return Revision.CURRENT;
		}

		@Override
		public HistoryManager getHistoryManager() {
			return NoHistoryManager.this;
		}
	}
	
	private final Branch trunk = new Trunk();
	private final KnowledgeBase kb;

	public NoHistoryManager(KnowledgeBase kb) {
		this.kb = kb;
	}
	
	@Override
	public boolean hasHistory() {
		return false;
	}
	
	@Override
	public Branch createBranch(Branch baseBranch, Revision baseRevision, Set branchedTypes) {
		throw new UnsupportedOperationException("Branch creation not supported.");
	}

	@Override
	public Branch getBranch(long branchId) {
		return trunk;
	}

	@Override
	public Branch getContextBranch() {
		return trunk;
	}

	@Override
	public ObjectReference getItemIdentity(KnowledgeItem item, Object original) {
		return new ObjectReference(original, TLContext.TRUNK_ID, Revision.CURRENT_REV, item.tTable(),
			item.getObjectName());
	}

	@Override
	public KnowledgeItem getKnowledgeItem(Revision revision, KnowledgeItem anObject) throws DataObjectException {
		if (revision == Revision.CURRENT) {
			return anObject;
		} else {
			return null;
		}
	}

	@Override
	public KnowledgeItem getKnowledgeItem(Branch branch, Revision revision, MetaObject type, TLID objectName) throws DataObjectException {
		if (revision == Revision.CURRENT) {
			return this.kb.getKnowledgeObject(type.getName(), objectName);
		} else {
			return null;
		}
	}

	@Override
	public long getLastRevision() {
		return Revision.CURRENT_REV;
	}

	@Override
	public long getLastUpdate(KnowledgeItem item) {
		return Revision.CURRENT_REV;
	}

	@Override
	public long getCreateRevision(KnowledgeItem item) {
		return Revision.CURRENT_REV;
	}
	
	@Override
	public List getObjectsByAttribute(Branch branch, Revision revision, String type, String attributeName, Object attributeValue) throws DataObjectException {
		if (revision == Revision.CURRENT) {
			// TODO: Incompatible API.
			Iterator result = this.kb.getObjectsByAttribute(type, attributeName, attributeValue);
			return CollectionUtil.toList(result);
		} else {
			return null;
		}
	}

	@Override
	public List getObjectsByAttributes(Branch branch, Revision revision, String type, String[] attributeNames, Object[] values) {
		if (revision == Revision.CURRENT) {
			// TODO: Incompatible API.
			Iterator result;
			try {
				result = this.kb.getObjectsByAttribute(type, attributeNames, values);
			} catch (UnknownTypeException e) {
				// TODO: Incompatible API.
				throw new KnowledgeBaseRuntimeException(e);
			} catch (NoSuchAttributeException e) {
				// TODO: Incompatible API.
				throw new KnowledgeBaseRuntimeException(e);
			}
			return CollectionUtil.toList(result);
		} else {
			return null;
		}
	}

	@Override
	public Revision getRevision(long commitNumber) {
		if (commitNumber == Revision.CURRENT_REV) {
			return Revision.CURRENT;
		} else {
			return Revision.INITIAL;
		}
	}

	@Override
	public Revision getRevisionAt(long time) {
		return Revision.CURRENT;
	}

	@Override
	public Branch getTrunk() {
		return trunk;
	}

	@Override
	public Branch setContextBranch(Branch branch) {
		return trunk;
	}

	@Override
	public long updateSessionRevision() throws MergeConflictException {
		return getLastRevision();
	}

	@Override
	public long getSessionRevision() {
		return getLastRevision();
	}

}
