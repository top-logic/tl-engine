/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.id.factory;

import com.top_logic.knowledge.service.db2.PersistentIdFactory;

/**
 * Interface for alternative implementations for the {@link PersistentIdFactory}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface IdFactory {

	/**
	 * Creates a new unique ID.
	 */
	long createId();

}
