/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.Log;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Definition of the table used in {@link HistoryCleanup}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HistoryCleanupTable implements TypeProvider {

	static final MOAttributeImpl NODE_ID = new MOAttributeImpl("node", MOPrimitive.LONG, MOAttributeImpl.MANDATORY);

	/** Definition of the column storing the identifier of the {@link KnowledgeBase}. */
	static final MOAttributeImpl KB_NAME = new MOAttributeImpl("kb", MOPrimitive.STRING, MOAttributeImpl.MANDATORY);
	static {
		KB_NAME.setBinary(true);
	}

	static final MOAttributeImpl REVISION = IdentifierTypes.newRevisionReference("rev", "REV",
		!MOAttributeImpl.IMMUTABLE);

	static final MOKnowledgeItemImpl CLEANUP_TABLE;
	static {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl("HistoryCleanUp");
		MOKnowledgeItemUtil.setSystem(type, true);
		type.setVersioned(false);
		try {
			type.addAttribute(NODE_ID);
			type.addAttribute(KB_NAME);
			type.addAttribute(REVISION);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		type.setPrimaryKey(NODE_ID, KB_NAME);
		type.freeze();

		CLEANUP_TABLE = type;
	}

	/** Singleton {@link HistoryCleanupTable} instance. */
	public static final HistoryCleanupTable INSTANCE = new HistoryCleanupTable();

	private HistoryCleanupTable() {
		// singleton instance
	}

	@Override
	public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
		try {
			typeRepository.addMetaObject(CLEANUP_TABLE.copy());
		} catch (DuplicateTypeException ex) {
			log.error("Unable to add type " + CLEANUP_TABLE.getName() + " to type repository.", ex);
		}
	}

}
