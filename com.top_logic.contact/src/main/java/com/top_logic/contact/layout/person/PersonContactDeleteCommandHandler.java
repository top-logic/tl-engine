/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.layout.ContactDeleteHandler;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.NullModelDisabled;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class PersonContactDeleteCommandHandler extends ContactDeleteHandler {

    public static final String COMMAND_ID = "personContactDelete";

    /** 
     * Creates a {@link PersonContactDeleteCommandHandler}.
     */
    public PersonContactDeleteCommandHandler(InstantiationContext context, Config config) {
        super(context, config);
    }

    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		return CombinedExecutabilityRule.combine(super.createExecutabilityRule(), AlivePersonContactDeleteRule.INSTANCE);
    }
    
    public static class AlivePersonContactDeleteRule implements ExecutabilityRule {
        public static final ExecutabilityRule INSTANCE = new AlivePersonContactDeleteRule();
        
        @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
			PersonContact aModel = (PersonContact) model;
			if (aModel == null) {
				return NullModelDisabled.EXEC_STATE_DISABLED;
			}
            if (aModel.getPerson()!=null && aModel.getPerson().isAlive()) {
            	return ExecutableState.createDisabledState(I18NConstants.ERROR_USER_STILL_ALIVE);
            }
            return ExecutableState.EXECUTABLE;
        }
    }
}

