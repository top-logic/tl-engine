/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db;

import java.util.Iterator;

import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Content of a non {@link KnowledgeBase} table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TableContent {

	/**
	 * The name of the table.
	 */
	MOStructure getTable();

	/**
	 * The actual content of the table.
	 */
	Iterator<RowValue> getRows();

}
