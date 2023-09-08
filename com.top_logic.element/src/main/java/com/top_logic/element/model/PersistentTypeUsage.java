/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.impl.TLTypeUsageImpl;
import com.top_logic.model.internal.PersistentQuery;
import com.top_logic.model.util.TLTypeUsage;

/**
 * Dummy {@link TLTypeUsage} implementation for a {@link PersistentTLModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersistentTypeUsage extends TLTypeUsageImpl implements PersistentQuery {

	@Override
	public void init(KnowledgeBase kb) throws DataObjectException {
		// Ignore.
	}

}
