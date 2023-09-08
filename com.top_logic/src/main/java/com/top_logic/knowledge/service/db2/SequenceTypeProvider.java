/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.Log;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.meta.MORepository;

/**
 * {@link TypeProvider} creating the tables for {@link RowLevelLockingSequenceManager}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SequenceTypeProvider implements TypeProvider {

	/** Name of the sequence type created by this {@link SequenceTypeProvider}. */
	public static final String SEQUENCE_TYPE_NAME = "Sequence";

	/**
	 * Singleton {@link SequenceTypeProvider} instance.
	 */
	public static final SequenceTypeProvider INSTANCE = new SequenceTypeProvider();

	private SequenceTypeProvider() {
		// Singleton constructor.
	}

	static final MOAttributeImpl SEQUENCE_ID_ATTR;
	static {
		SEQUENCE_ID_ATTR = new MOAttributeImpl("id", MOPrimitive.STRING, MOAttributeImpl.MANDATORY);
		SEQUENCE_ID_ATTR.setBinary(true);
	}

	static final MOAttributeImpl CURRENT_VALUE_ATTR = new MOAttributeImpl("value", MOPrimitive.LONG);

	static final MOKnowledgeItemImpl SEQUENCE_TYPE;
	static {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(SEQUENCE_TYPE_NAME);
		MOKnowledgeItemUtil.setSystem(type, true);
		try {
			type.addAttribute(SEQUENCE_ID_ATTR);
			type.addAttribute(CURRENT_VALUE_ATTR);
		} catch (DuplicateAttributeException e) {
			throw new UnreachableAssertion(e);
		}
		type.setPrimaryKey(SEQUENCE_ID_ATTR);
		type.freeze();

		SEQUENCE_TYPE = type;
	}

	@Override
	public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
		try {
			typeRepository.addMetaObject(SEQUENCE_TYPE.copy());
		} catch (DataObjectException ex) {
			log.error("Creating sequence type failed.", ex);
		}
	}

}
