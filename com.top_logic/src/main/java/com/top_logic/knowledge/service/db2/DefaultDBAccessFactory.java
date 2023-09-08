/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.BasicTypes;

/**
 * Default implementation of {@link DBAccessFactory} returned by most {@link MOKnowledgeItemImpl}
 * instances.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public class DefaultDBAccessFactory implements DBAccessFactory {

	/** {@link DefaultDBAccessFactory} instance */
	public static final DBAccessFactory INSTANCE = new DefaultDBAccessFactory();

	@Override
	public DBAccess createDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl type, MORepository repository) {
		DBAccess dbAccess;
		if (type.isSubtypeOf(getType(repository, BasicTypes.ITEM_TYPE_NAME))) {
			if (type.isVersioned()) {
				dbAccess = new VersionedDBAccess(sqlDialect, type);
			} else {
				dbAccess = new UnversionedDBAccess(sqlDialect, type);
			}
		} else {
			dbAccess = NoDBAccess.INSTANCE;
		}
		return dbAccess;
	}

	private MetaObject getType(MORepository repository, String typeName) {
		try {
			return repository.getType(typeName);
		} catch (UnknownTypeException ex) {
			throw new RuntimeException("Unable to create " + DBAccess.class.getName(), ex);
		}
	}

}

