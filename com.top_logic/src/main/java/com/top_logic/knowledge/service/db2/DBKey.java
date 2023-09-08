/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.knowledge.objects.identifier.MutableObjectKey;

/**
 * Key that can can construct stable keys from its current state.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DBKey extends MutableObjectKey<DBObjectKey> {

	@Override
	public DBObjectKey toStableKey() {
		return new DBObjectKey(this.getBranchContext(), this.getHistoryContext(), (MOKnowledgeItem) this.getObjectType(), this.getObjectName());
	}

}

