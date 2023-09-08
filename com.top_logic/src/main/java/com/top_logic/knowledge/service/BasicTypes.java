/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * Access to basic types of the {@link PersistencyLayer}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BasicTypes extends com.top_logic.dob.meta.BasicTypes {

	/**
	 * The persistent type of {@link Branch} objects.
	 */
	public static MOClass getBranchType(KnowledgeBase kb) {
		return getBranchType(kb.getMORepository());
	}

	/**
	 * The persistent type of {@link Revision} objects.
	 */
	public static MOClass getRevisionType(KnowledgeBase kb) {
		return getRevisionType(kb.getMORepository());
	}

	/**
	 * The persistent type of {@link KnowledgeItem} objects.
	 */
	public static MOClass getItemType(KnowledgeBase kb) {
		return getItemType(kb.getMORepository());
	}

	/**
	 * The persistent type of all application objects.
	 */
	public static MOClass getObjectType(KnowledgeBase kb) {
		return getObjectType(kb.getMORepository());
	}

	/**
	 * The persistent type of {@link KnowledgeObject} objects.
	 */
	public static MOClass getKnowledgeObjectType(KnowledgeBase kb) {
		return getKnowledgeObjectType(kb.getMORepository());
	}

	/**
	 * The persistent type of {@link KnowledgeAssociation} objects.
	 */
	public static MOClass getKnowledgeAssociationType(KnowledgeBase kb) {
		return getKnowledgeAssociationType(kb.getMORepository());
	}

}
