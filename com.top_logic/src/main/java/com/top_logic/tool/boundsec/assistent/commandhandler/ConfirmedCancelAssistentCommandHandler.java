/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent.commandhandler;

import com.top_logic.basic.config.InstantiationContext;


/**
 * Overridden to set needsConfirm flag.
 *
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class ConfirmedCancelAssistentCommandHandler extends CancelAssistentCommandHandler {

    public static final String COMMAND = "confirmedAssistentCancel";

    

    public ConfirmedCancelAssistentCommandHandler(InstantiationContext context, Config config) {
        super(context, config);
        confirm = true;
    }

}
