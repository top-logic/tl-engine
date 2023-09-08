/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.storage.AbstractComputedAttributeStorage;
import com.top_logic.dob.attr.storage.ComputedAttributeStorage;
import com.top_logic.util.TLContext;

/**
 * {@link ComputedAttributeStorage} that returns always a fixed value.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TrunkStorage extends AbstractComputedAttributeStorage implements IBranchStorage {

	/** Singleton {@link TrunkStorage} instance. */
	public static final TrunkStorage INSTANCE = new TrunkStorage();

	private TrunkStorage() {
		// singleton instance
	}

	@Override
	public int getCacheSize() {
		return 0;
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		return Long.valueOf(TLContext.TRUNK_ID);
	}

	@Override
	public long getBranch(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOAttribute branchAttribute)
			throws SQLException {
		return TLContext.TRUNK_ID;
	}

}

