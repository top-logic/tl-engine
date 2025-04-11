/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent.commandhandler;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.tool.boundsec.confirm.CommandConfirmation;
import com.top_logic.tool.boundsec.confirm.DefaultConfirmation;


/**
 * Overridden to set needsConfirm flag.
 *
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class ConfirmedCancelAssistentCommandHandler extends CancelAssistentCommandHandler {

    public static final String COMMAND = "confirmedAssistentCancel";

	/**
	 * Configuration options for {@link ConfirmedCancelAssistentCommandHandler}.
	 */
	public interface Config extends CancelAssistentCommandHandler.Config {
		@Override
		@ItemDefault(DefaultConfirmation.class)
		PolymorphicConfiguration<? extends CommandConfirmation> getConfirmation();
	}

    public ConfirmedCancelAssistentCommandHandler(InstantiationContext context, Config config) {
        super(context, config);
    }

}
