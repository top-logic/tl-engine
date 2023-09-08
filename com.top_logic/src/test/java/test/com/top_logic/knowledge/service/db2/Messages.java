/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import com.top_logic.basic.message.AbstractMessages;

/**
 * Message constants for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Messages extends AbstractMessages {

	public static Template0 SETUP_TEST;
	public static Template0 SOME_MODIFICATION;
	public static Template0 MULTI_BRANCH_MODIFICATION;
	public static Template1 INITIAL_DATA_IMPORT__KB_NAME;
	
	static {
		init(Messages.class);
	}
}
