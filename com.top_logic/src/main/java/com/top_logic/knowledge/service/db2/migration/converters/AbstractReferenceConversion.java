/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.converters;

import com.google.inject.Inject;

import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.service.db2.migration.Migration;

/**
 * Common superclass for {@link ReferenceConversion}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractReferenceConversion implements ReferenceConversion {

	private Migration _migration;

	/**
	 * Initialises {@link #migration()}.
	 */
	@Inject
	public void init(Migration migration) {
		_migration = migration;
	}

	/**
	 * The {@link Migration} which uses this {@link AbstractReferenceConversion}.
	 */
	public Migration migration() {
		return _migration;
	}

	@Override
	public void handleNullReference(ItemChange event) {
		// nothing to do here
	}

}
