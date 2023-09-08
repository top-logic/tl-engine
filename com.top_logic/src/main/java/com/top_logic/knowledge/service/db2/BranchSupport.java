/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;

/**
 * {@link DBKnowledgeBase} internal types and utilities for supporting branches.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BranchSupport {

	/**
	 * Name of the {@link MetaObject} that holds for each type and branch the id of the data branch,
	 * i.e. the branch on which the data for that type on the branch lie.
	 * 
	 * @see #LINK_BRANCH_ATTRIBUTE_NAME
	 * @see #LINK_TYPE_ATTRIBUTE
	 * @see #LINK_DATA_BRANCH_ATTRIBUTE
	 */
	public static final String BRANCH_SWITCH_TYPE_NAME = "BranchSwitch";

	/**
	 * Name of the {@link MOAttribute} of type {@link #BRANCH_SWITCH_TYPE_NAME} which contains the
	 * id of the branch containing the data for the type.
	 * 
	 * @see #LINK_BRANCH_ATTRIBUTE_NAME Attribute containing the branch.
	 * @see #LINK_TYPE_ATTRIBUTE Attribute containing the type name.
	 */
	public static final String LINK_DATA_BRANCH_ATTRIBUTE = "_sourceBranch";

	/**
	 * Name of the {@link MOAttribute} of type {@link #BRANCH_SWITCH_TYPE_NAME} which contains the
	 * name of the type.
	 * 
	 * @see #LINK_BRANCH_ATTRIBUTE_NAME Attribute containing the branch.
	 * @see #LINK_DATA_BRANCH_ATTRIBUTE Attribute containing the data branch.
	 */
	public static final String LINK_TYPE_ATTRIBUTE = "_targetType";

	/**
	 * Name of the {@link MOAttribute} of type {@link #BRANCH_SWITCH_TYPE_NAME} which contains the
	 * id of the branch.
	 * 
	 * @see #LINK_TYPE_ATTRIBUTE Attribute containing the branch.
	 * @see #LINK_DATA_BRANCH_ATTRIBUTE Attribute containing the data branch.
	 */
	public static final String LINK_BRANCH_ATTRIBUTE_NAME = "_targetBranch";

	static final String BASE_BRANCH_ATTRIBUTE_NAME = "baseBranch";

	static final String CREATE_REVISION_ATTRIBUTE_NAME = "createRev";

	static final String BASE_REVISION_ATTRIBUTE_NAME = "baseRev";

	static final String BRANCH_ID_ATTRIBUTE_NAME = BasicTypes.IDENTIFIER_ATTRIBUTE_NAME;

	/**
	 * Column name of the attribute {@link #LINK_BRANCH_ATTRIBUTE_NAME}
	 */
	public static final String LINK_BRANCH_COLUMN = "BRANCH";

	/**
	 * Column name of the attribute {@link #LINK_TYPE_ATTRIBUTE}
	 */
	public static final String LINK_TYPE_COLUMN = "TYPE";

	/**
	 * Column name of the attribute {@link #LINK_DATA_BRANCH_ATTRIBUTE}
	 */
	public static final String LINK_DATA_BRANCH_COLUMN = "DATA_BRANCH";

	/**
	 * Create a new {@link KnowledgeItem} for the type {@link #BRANCH_SWITCH_TYPE_NAME}.
	 */
	public static AbstractDBKnowledgeItem newBranchSwitchLink(DBKnowledgeBase kb, DBContext context, Long branchId,
			MetaObject type, Long baseBranchId) {
		MOKnowledgeItemImpl branchSwitchType = kb.lookupType(BRANCH_SWITCH_TYPE_NAME);
		AbstractDBKnowledgeItem link = kb.newImmutableItem(branchSwitchType);
		
		Object[] values = link.getValuesForInitialisation(context);
		{
			link.initAttribute(branchSwitchType.getAttributeOrNull(LINK_BRANCH_ATTRIBUTE_NAME), values, branchId);
			link.initAttribute(branchSwitchType.getAttributeOrNull(LINK_TYPE_ATTRIBUTE), values, type.getName());
			link.initAttribute(branchSwitchType.getAttributeOrNull(LINK_DATA_BRANCH_ATTRIBUTE), values, baseBranchId);
		}
		
		return link;
	}

	/**
	 * Create a new {@link MOKnowledgeItemImpl} to store {@link Branch}es.
	 */
	public static MOKnowledgeItemImpl newBranchType() {
		return BranchType.createBranchType();
	}

	/**
	 * Table that stores where (in which {@link #LINK_DATA_BRANCH_ATTRIBUTE branch}) data is stored
	 * for each {@link #LINK_BRANCH_ATTRIBUTE_NAME branch} and {@link #LINK_TYPE_ATTRIBUTE type}.
	 */
	public static MOKnowledgeItemImpl newBranchSwitchType() {
		return BranchSwitchType.createBranchSwitch();
	}

	/**
	 * Returns the {@link DBAttribute} for the attribute {@link #LINK_BRANCH_ATTRIBUTE_NAME} of the
	 * given type.
	 */
	public static DBAttribute getLinkBranchAttr(MOClass branchLinkType) {
		return (MOAttributeImpl) branchLinkType.getAttributeOrNull(LINK_BRANCH_ATTRIBUTE_NAME);
	}

	/**
	 * Returns the {@link DBAttribute} for the attribute {@link #LINK_TYPE_ATTRIBUTE} of the given
	 * type.
	 */
	public static DBAttribute getLinkTypeAttr(MOClass branchLinkType) {
		return (MOAttributeImpl) branchLinkType.getAttributeOrNull(LINK_TYPE_ATTRIBUTE);
	}

	/**
	 * Returns the {@link DBAttribute} for the attribute {@link #LINK_DATA_BRANCH_ATTRIBUTE} of the
	 * given type.
	 */
	public static DBAttribute getLinkDataBranchAttr(MOClass branchLinkType) {
		return (MOAttributeImpl) branchLinkType.getAttributeOrNull(LINK_DATA_BRANCH_ATTRIBUTE);
	}

	/**
	 * Returns the {@link DBAttribute} for the attribute {@link #BRANCH_ID_ATTRIBUTE_NAME} of the
	 * given type.
	 */
	public static DBAttribute getBranchIDAttr(MOClass branchType) {
		return (MOAttributeImpl) branchType.getAttributeOrNull(BRANCH_ID_ATTRIBUTE_NAME);
	}

	/**
	 * Returns the {@link DBAttribute} for the attribute {@link #BASE_BRANCH_ATTRIBUTE_NAME} of the
	 * given type.
	 */
	public static DBAttribute getBaseBranchAttr(MOClass branchType) {
		return (MOAttributeImpl) branchType.getAttributeOrNull(BASE_BRANCH_ATTRIBUTE_NAME);
	}

	/**
	 * Returns the {@link DBAttribute} for the attribute {@link #BASE_REVISION_ATTRIBUTE_NAME} of
	 * the given type.
	 */
	public static DBAttribute getBaseRevAttr(MOClass branchType) {
		return (MOAttributeImpl) branchType.getAttributeOrNull(BASE_REVISION_ATTRIBUTE_NAME);
	}

	/**
	 * Returns the {@link DBAttribute} for the attribute {@link #CREATE_REVISION_ATTRIBUTE_NAME} of
	 * the given type.
	 */
	public static DBAttribute getCreateRevAttr(MOClass branchType) {
		return (MOAttributeImpl) branchType.getAttributeOrNull(CREATE_REVISION_ATTRIBUTE_NAME);
	}

}
