/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.internal;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLQuery;

/**
 * Base class for {@link TLQuery} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface PersistentQuery extends TLQuery {

	/**
	 * Installs the {@link KnowledgeBase}.
	 * 
	 * @param kb
	 *        The context {@link KnowledgeBase}.
	 * 
	 * @throws DataObjectException
	 *         If resolving required query details fail.
	 */
	public void init(KnowledgeBase kb) throws DataObjectException;

}
