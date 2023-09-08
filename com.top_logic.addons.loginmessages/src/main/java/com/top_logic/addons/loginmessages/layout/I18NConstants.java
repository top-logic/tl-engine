/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.addons.loginmessages.layout;

import com.top_logic.addons.loginmessages.model.intf.LoginMessage;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N of this package.
 * 
 * @author <a href="mailto:Dmitry.Ivanizki@top-logic.com">Dmitry Ivanizki</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/** A key for the dialog title of a {@link LoginMessage}. */
	public static ResKey LOGIN_MESSAGE_DIALOG_TITLE;

	/** A key for the reset info message box of a {@link LoginMessage}. */
	public static ResKey RESET_INFO_MESSAGE;

	/** A key for the warning that the message of a {@link LoginMessage} is empty. */
	public static ResKey LOGIN_MESSAGE_WARNING_EMPTY_MESSAGE;

    static {
        initConstants(I18NConstants.class);
    }

}
