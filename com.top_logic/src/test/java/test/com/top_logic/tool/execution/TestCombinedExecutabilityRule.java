/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.execution;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestCombinedExecutabilityRule extends TestCase {

    public void testIsExecutable() {
        
        ExecutabilityRule e1 = new ExecutabilityRule() {
            @Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
                return ExecutableState.EXECUTABLE;
            }
        };
        
        ExecutabilityRule e2 = new ExecutabilityRule() {
            @Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
				return new ExecutableState(ExecutableState.CommandVisibility.VISIBLE, ResKey.text("executable2"));
            }
        };
        
        ExecutabilityRule d1 = new ExecutabilityRule() {
            @Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
                return ExecutableState.createDisabledState(ResKey.forTest("disabled1"));
            }
        };
        
        ExecutabilityRule d2 = new ExecutabilityRule() {
            @Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
                return ExecutableState.createDisabledState(ResKey.forTest("disabled2"));
            }
        };
        
        ExecutabilityRule h1 = new ExecutabilityRule() {
            @Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
                return ExecutableState.NOT_EXEC_HIDDEN;
            }
        };
        
        ExecutabilityRule h2 = new ExecutabilityRule() {
            @Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
				return new ExecutableState(ExecutableState.CommandVisibility.HIDDEN, ResKey.forTest("hidden2"));
            }
        };
        
        // hidden state is superior to all other states
		ExecutabilityRule rule = CombinedExecutabilityRule.combine(e1, d1, h1);
		assertTrue(rule.isExecutable(null, null, null) == ExecutableState.NOT_EXEC_HIDDEN);
        
        // must return first hidden state
        rule  = CombinedExecutabilityRule.combine(h1, h2, e1);
		assertTrue(rule.isExecutable(null, null, null) == ExecutableState.NOT_EXEC_HIDDEN);
        
        rule  = CombinedExecutabilityRule.combine(d2, h2, d1);
		ExecutableState state = rule.isExecutable(null, null, null);
        assertFalse(state.isExecutable());
        assertFalse(state.isDisabled());
        assertTrue(state.isHidden());
        assertEquals("hidden2", state.getI18NReasonKey().getKey());
        
        // must return first disabled state
        rule = CombinedExecutabilityRule.combine(e1, d1, d2);
		state = rule.isExecutable(null, null, null);
        assertFalse(state.isExecutable());
        assertTrue(state.isDisabled());
        assertFalse(state.isHidden());
        assertEquals("disabled1", state.getI18NReasonKey().getKey());
        
        rule = CombinedExecutabilityRule.combine(e1, d1, e2);
		state = rule.isExecutable(null, null, null);
        assertFalse(state.isExecutable());
        assertTrue(state.isDisabled());
        assertFalse(state.isHidden());
        assertEquals("disabled1", state.getI18NReasonKey().getKey());
        
        // must return first executable state
        rule = CombinedExecutabilityRule.combine(e1, e2);
		state = rule.isExecutable(null, null, null);
        assertTrue(state.isExecutable());
        assertFalse(state.isDisabled());
        assertFalse(state.isHidden());
        assertTrue(state == ExecutableState.EXECUTABLE);
    }

    /**
     * the suite of Tests to execute.
     */
    public static Test suite () {
        TestSuite theSuite = new TestSuite (TestCombinedExecutabilityRule.class);
        return TLTestSetup.createTLTestSetup(theSuite);
    }   
    
    public static void main(String[] args) {
        Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }
}

