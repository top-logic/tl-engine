/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;


import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.storage.ImmutableLongStorage;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.InternalBranch;

/**
 * {@link BranchType} is persistent type of {@link Branch}
 * 
 * @see BranchSupport#newBranchType()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class BranchType extends MOKnowledgeItemImpl {

	static MOKnowledgeItemImpl createBranchType() {
		BranchType branchType = new BranchType(BasicTypes.BRANCH_TYPE_NAME);
		branchType.init();
		return branchType;
	}

	private BranchType(String name) {
		super(name);
	}

	private void init() {
		setFinal(true);
		MOKnowledgeItemUtil.setSystem(this, true);
		MOAttributeImpl branchId = newBranchIdAttribute();
		try {
			addAttribute(branchId);
			addAttribute(newCreateRevAttribute());
			addAttribute(newBaseBranchAttribute());
			addAttribute(newBaseRevAttribute());
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		setPrimaryKey(branchId);
		MOKnowledgeItemUtil.setImplementationFactory(this, BranchFactory.INSTANCE);
		MOKnowledgeItemUtil.setDBAccessFactory(this, BranchDBAccess.Factory.INSTANCE);
	}

	private MOAttributeImpl newBaseRevAttribute() {
		MOAttributeImpl baseRevAttr =
			IdentifierTypes.newRevisionReference(BranchSupport.BASE_REVISION_ATTRIBUTE_NAME, "BASE_REV",
				MOAttribute.IMMUTABLE);
		baseRevAttr.setStorage(new ImmutableLongStorage() {

			@Override
			public void initLongValue(MOAttribute attribute, DataObject item, Object[] storage, long cacheValue) {
				((BranchImpl) item).initBaseRevision(cacheValue);
			}

			@Override
			public long getLongValue(MOAttribute attribute, DataObject item, Object[] storage) {
				return ((InternalBranch) item).baseRevision();
			}

		});
		return baseRevAttr;
	}

	private MOAttributeImpl newBaseBranchAttribute() {
		MOAttributeImpl baseBranchAttr =
			IdentifierTypes.newBranchReference(BranchSupport.BASE_BRANCH_ATTRIBUTE_NAME, "BASE_BRANCH");
		baseBranchAttr.setStorage(new ImmutableLongStorage() {

			@Override
			public void initLongValue(MOAttribute attribute, DataObject item, Object[] storage, long cacheValue) {
				((BranchImpl) item).initBaseBranch(cacheValue);
			}

			@Override
			public long getLongValue(MOAttribute attribute, DataObject item, Object[] storage) {
				return ((InternalBranch) item).baseBranch();
			}

		});
		return baseBranchAttr;
	}

	private MOAttributeImpl newCreateRevAttribute() {
		MOAttributeImpl createRevAttr =
			IdentifierTypes.newRevisionReference(BranchSupport.CREATE_REVISION_ATTRIBUTE_NAME, "REV",
				MOAttribute.IMMUTABLE);
		createRevAttr.setStorage(new ImmutableLongStorage() {

			@Override
			public void initLongValue(MOAttribute attribute, DataObject item, Object[] storage, long cacheValue) {
				((BranchImpl) item).initCreateRevision(cacheValue);
			}

			@Override
			public long getLongValue(MOAttribute attribute, DataObject item, Object[] storage) {
				return ((InternalBranch) item).createRevision();
			}

		});
		return createRevAttr;
	}

	private MOAttributeImpl newBranchIdAttribute() {
		MOAttributeImpl idAttr = IdentifierTypes.newBranchReference(BranchSupport.BRANCH_ID_ATTRIBUTE_NAME, "BRANCH");
		idAttr.setStorage(new ImmutableLongStorage() {

			@Override
			public void initLongValue(MOAttribute attribute, DataObject item, Object[] storage, long cacheValue) {
				((BranchImpl) item).initBranchId(cacheValue);
			}

			@Override
			public long getLongValue(MOAttribute attribute, DataObject item, Object[] storage) {
				return ((InternalBranch) item).branchId();
			}

		});
		idAttr.setSystem(true);
		return idAttr;
	}

	@Override
	public MetaObject copy() {
		BranchType copy = new BranchType(getName());
		copy.initFromItemType(this);
		return copy;
	}

}

