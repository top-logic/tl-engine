/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.layout.ContactApplyHandler;

/**
 * ContactApplyHandler with restricted write command group.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class PersonContactApplyHandler extends ContactApplyHandler {

    /** The ID of this command. */
    public static final String COMMAND_ID = "personContactApply";

    /**
     * Creates a new instance of this class.
     */
    public PersonContactApplyHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

}
