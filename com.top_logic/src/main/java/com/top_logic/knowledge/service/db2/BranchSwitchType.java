/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.ex.DuplicateAttributeException;

/**
 * Persistent type of the branch switch table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class BranchSwitchType extends MOKnowledgeItemImpl {

	static MOKnowledgeItemImpl createBranchSwitch() {
		BranchSwitchType type = new BranchSwitchType(BranchSupport.BRANCH_SWITCH_TYPE_NAME);
		type.init();
		return type;
	}

	private BranchSwitchType(String name) {
		super(name);
	}

	private void init() {
		MOKnowledgeItemUtil.setSystem(this, true);
		MOAttributeImpl linkBranch = newLinkBranchAttribute();
		MOAttributeImpl linkType = newLinkTypeAttribute();
		try {
			addAttribute(linkBranch);
			addAttribute(linkType);
			addAttribute(newLinkDataBranchAttribute());
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		setPrimaryKey(linkBranch, linkType);

		MOKnowledgeItemUtil.setImplementationFactory(this, DBSystemLink.FACTORY);
		MOKnowledgeItemUtil.setDBAccessFactory(this, BranchSwitchDBAccess.Factory.INSTANCE);
	}

	private MOAttributeImpl newLinkDataBranchAttribute() {
		return IdentifierTypes.newBranchReference(BranchSupport.LINK_DATA_BRANCH_ATTRIBUTE, BranchSupport.LINK_DATA_BRANCH_COLUMN);
	}

	private MOAttributeImpl newLinkTypeAttribute() {
		return IdentifierTypes.newTypeReference(BranchSupport.LINK_TYPE_ATTRIBUTE, BranchSupport.LINK_TYPE_COLUMN);
	}

	private MOAttributeImpl newLinkBranchAttribute() {
		return IdentifierTypes.newBranchReference(BranchSupport.LINK_BRANCH_ATTRIBUTE_NAME, BranchSupport.LINK_BRANCH_COLUMN);
	}

	@Override
	public MetaObject copy() {
		BranchSwitchType copy = new BranchSwitchType(getName());
		copy.initFromItemType(this);
		return copy;
	}

}

