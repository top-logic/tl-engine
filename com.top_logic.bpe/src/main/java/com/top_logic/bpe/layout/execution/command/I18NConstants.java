/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution.command;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Are you sure that you want to complete the task?
	 */
	public static ResKey CONFIRM_FINISH_TASK;

	public static ResPrefix SELECT_TRANSITION_DIALOG;

	/**
	 * @en Abort task
	 */
	public static ResKey EDGE_OPTION_LABEL_ABORT;

	/**
	 * @en Cancel task
	 */
	public static ResKey EDGE_OPTION_LABEL_CANCEL;

	/**
	 * @en (Continue)
	 */
	public static ResKey EDGE_OPTION_NO_NAME;

	/**
	 * @en ! {0}
	 */
	public static ResKey1 IMPOSSIBLE_EDGE;

    static {
        initConstants(I18NConstants.class);
    }

}
