/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config.annotation;

import java.util.List;

import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;

/**
 * {@link IndexColumnsStrategy} that uses exactly the columns defined in the index without
 * modification.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DirectIndexColumnsStrategy implements IndexColumnsStrategy {

	/**
	 * Singleton {@link DirectIndexColumnsStrategy} instance.
	 */
	public static final DirectIndexColumnsStrategy INSTANCE = new DirectIndexColumnsStrategy();

	private DirectIndexColumnsStrategy() {
		// Singleton constructor.
	}

	@Override
	public List<DBAttribute> createIndexColumns(MOClass type, DBIndex index) {
		return index.getKeyAttributes();
	}

}
