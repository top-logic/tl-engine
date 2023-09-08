/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InViewModeExecutable;

/**
 * Default command that will switch a Component to {EditModeAware#EDIT_MODE},
 * even if the component has no model.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class NullModelSwitchEditCommandHandler extends SwitchEditCommandHandler {

    /** The ID of this command. */
    public static final String COMMAND_ID = "nullModelSwitchToAJAXEdit";

	public NullModelSwitchEditCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
        return InViewModeExecutable.INSTANCE;
    }

}
