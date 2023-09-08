/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout;

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

	public static ResPrefix EXECUTION_ATTRIBUTE_GROUP;

	public static ResPrefix SELECT_TRANSITION_DIALOG;

	public static ResKey CONFIRM_FINISH_TASK;

	public static ResKey EDGE_OPTION_LABEL_ABORT;

	public static ResKey FINISH_TASK_DISABLED;

	public static ResKey EDGE_OPTION_LABEL_CANCEL;

	public static ResKey EDGE_OPTION_NO_NAME;

	public static ResKey NOT_EXECUTABLE_NO_DISPLAY_DESCRIPTION;

	public static ResKey1 NO_DISPLAY_DESCRIPTION__WF;

    static {
        initConstants(I18NConstants.class);
    }

}
