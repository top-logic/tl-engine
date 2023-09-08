/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Dispatch to different rules depending on class of {@link LayoutComponent#getModel()}.
 * 
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class DispatchByClassExecutabilityRule implements ExecutabilityRule {
    
    private Map<Class<?>, ExecutabilityRule> dispatch;
    
    /** Will be used when no match in {@link #dispatch} is found */
    ExecutableState defaultState;
    
    /** Will be used when model found is <code>null</code> */
    ExecutableState nullState;

    /**
     * @param rules Map
     */
    public DispatchByClassExecutabilityRule(Map<Class<?>, ExecutabilityRule> rules, ExecutableState aDefault, ExecutableState aNull) {
        this.dispatch     = rules;
        this.defaultState = aDefault;
        this.nullState    = aNull;
    }

    public DispatchByClassExecutabilityRule(ExecutableState aDefault, ExecutableState aNull) {
		this(new HashMap<>(), aDefault, aNull);
    }
    
    
    public ExecutabilityRule addRuleForClass(Class<?> aClass, ExecutabilityRule aRule) {
        return this.dispatch.put(aClass, aRule);
    }

    /**
     * Dispatch to {@link #dispatch} base on {@link LayoutComponent#getModel()}.getClass().
     * 
     * Will fall back to {@link #defaultState} in case no class macthes or {@link #nullState} for a null model.
     */
    @Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aMap) {
		if (model != null) {
			ExecutabilityRule theRule = this.dispatch.get(model.getClass());
            if (theRule != null) {
				return theRule.isExecutable(aComponent, model, aMap);
            }
            return defaultState;
        }
        return nullState;
    }
}