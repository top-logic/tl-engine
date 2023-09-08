/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.meta.MORepository;

/**
 * Factory class to create {@link DBAccess} for a given type.
 * 
 * @see MOKnowledgeItemImpl#getDBAccess()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public interface DBAccessFactory {

	/**
	 * Creates a {@link DBAccess} for the given type.
	 * 
	 * @param sqlDialect
	 *        SQL dialect to access the database
	 * @param type
	 *        the type to create {@link DBAccess} for
	 * @param repository
	 *        the {@link MORepository} to get informations about other types.
	 */
	DBAccess createDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl type, MORepository repository);
}

