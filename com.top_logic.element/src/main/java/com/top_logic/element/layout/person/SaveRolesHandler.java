/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.person;

import com.top_logic.basic.config.InstantiationContext;

/**
 * Corresponding save handler for {@link ApplyRolesHandler}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class SaveRolesHandler extends ApplyRolesHandler {

    public static final String COMMAND_ID = "savePersonOrGroupRoles";

    
    

    public SaveRolesHandler(InstantiationContext context, Config config) {
        super(context, config);
    }

    @Override
	protected boolean isSaveCommmand() {
        return true;
    }
}
