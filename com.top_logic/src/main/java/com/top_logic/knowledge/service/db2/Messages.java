/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.message.AbstractMessages;

/**
 * Messages of this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Messages extends AbstractMessages {

	/** Reason for commit failure, if a (pseudo) nested transaction is rolled back. */
	public static Template2 NESTED_TRANSACTION_ROLLED_BACK__COMMIT_REASON;

	
	static {
		init(Messages.class);
	}
}
