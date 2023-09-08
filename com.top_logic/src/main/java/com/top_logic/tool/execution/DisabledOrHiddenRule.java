/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The DisabledOrHiddenRule can be used to change the visibility (always hide or just
 * disable) of not executable rules.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class DisabledOrHiddenRule implements ExecutabilityRule {

    private ExecutabilityRule innerRule;
    
    /** One of {@link ExecutableState#VISIBILITY_HIDDEN}, {@link ExecutableState#VISIBILITY_DISABLED} */
	private ExecutableState.CommandVisibility innerState;

    /**
     * Creates a new DisabledOrHiddenRule.
     *
     * @param aRule
     *        the rule to disable / hide if not executable
     * @param isHidden
     *        switch for the visibility to return in non executable state
     */
    public DisabledOrHiddenRule(ExecutabilityRule aRule, boolean isHidden) {
        this.innerRule  = aRule;
		this.innerState = isHidden ? ExecutableState.CommandVisibility.HIDDEN
			: ExecutableState.CommandVisibility.DISABLED;
    }

    @Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		ExecutableState theState = this.innerRule.isExecutable(aComponent, model, someValues);
        if (theState.isExecutable()) {
            return theState;
        }
        return new ExecutableState(innerState, theState.getI18NReasonKey());
    }

}
