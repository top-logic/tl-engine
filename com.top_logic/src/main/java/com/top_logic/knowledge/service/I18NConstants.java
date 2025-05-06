/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 NO_KBCONFIG__NAME;

	/**
	 * @en Initial import.
	 */
	public static ResKey INITIAL_IMPORT;

	/**
	 * @en Import of {0}.
	 */
	public static ResKey1 DATA_IMPORT__RES;

	/**
	 * @en Created branch {0}.
	 */
	public static ResKey1 BRANCH_CREATED__ID;

	/**
	 * @en No message.
	 */
	public static ResKey NO_COMMIT_MESSAGE;

	/**
	 * @en Synthesized commit during replay.
	 */
	public static ResKey SYNTHESIZED_COMMIT_DURING_REPLAY;

	static {
		initConstants(I18NConstants.class);
	}
}
