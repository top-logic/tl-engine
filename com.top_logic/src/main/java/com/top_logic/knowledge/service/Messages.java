/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.message.AbstractMessages;

/**
 * Messages for this package and sub-packages.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Messages extends AbstractMessages {
	

	public static Template0 INITIAL_IMPORT;
	public static Template1 DATA_IMPORT__RES;

	/** Commit message for branch creation. */
	public static Template1 BRANCH_CREATED__ID;
	
	/** Commit message, if not provided by the application. */
	public static Template0 NO_COMMIT_MESSAGE;
	
	public static Template0 SYNTHESIZED_COMMIT_DURING_REPLAY;

	static {
		init(Messages.class);
		initDefaults(Messages.class);
	}
	
}
