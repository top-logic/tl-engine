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

	/** Attribute name of the sequence identifier ({@link #SEQUENCE_ID_ATTR}). */
	public static final String SEQUENCE_ID_COLUMN_NAME = "id";

	/** Attribute name of the current sequence value ({@link #CURRENT_VALUE_ATTR}). */
	public static final String SEQUENCE_VALUE_COLUMN_NAME = "value";

	/**
	 * Singleton {@link SequenceTypeProvider} instance.
	 */
	public static final SequenceTypeProvider INSTANCE = new SequenceTypeProvider();

	private SequenceTypeProvider() {
		// Singleton constructor.
	}

	static final MOAttributeImpl SEQUENCE_ID_ATTR;
	static {
		SEQUENCE_ID_ATTR = new MOAttributeImpl(SEQUENCE_ID_COLUMN_NAME, MOPrimitive.STRING, MOAttributeImpl.MANDATORY);
		SEQUENCE_ID_ATTR.setBinary(true);
	}

	static final MOAttributeImpl CURRENT_VALUE_ATTR =
		new MOAttributeImpl(SEQUENCE_VALUE_COLUMN_NAME, MOPrimitive.LONG);

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

	/**
	 * Physical name of the sequence table, as used in SQL statements (may differ from
	 * {@link #SEQUENCE_TYPE_NAME}).
	 */
	public static String sequenceTableName() {
		return SEQUENCE_TYPE.getDBName();
	}

	/**
	 * Physical name of the column holding the sequence identifier, as used in SQL statements.
	 */
	public static String sequenceIdColumnName() {
		return SEQUENCE_ID_ATTR.getDBName();
	}

	/**
	 * Physical name of the column holding the current sequence value, as used in SQL statements.
	 */
	public static String sequenceValueColumnName() {
		return CURRENT_VALUE_ATTR.getDBName();
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
