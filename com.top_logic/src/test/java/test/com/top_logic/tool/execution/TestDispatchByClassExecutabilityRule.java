/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.execution;

import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;
import com.top_logic.tool.execution.DispatchByClassExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Test the {@link DispatchByClassExecutabilityRule}
 * 
 * @author    <a href=mailto:klaus.halfmann@top-logic.com>Klaus Halfmann</a>
 */
public class TestDispatchByClassExecutabilityRule extends BasicTestCase {

    
    public TestDispatchByClassExecutabilityRule(String name) {
        super(name);
    }

    public void testIsExecutable() {
        
        ExecutabilityRule eInt = new ExecutabilityRule() {
            @Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
                return ExecutableState.EXECUTABLE;
            }
        };
        
        ExecutabilityRule eString = new ExecutabilityRule() {
            @Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
				return new ExecutableState(ExecutableState.CommandVisibility.VISIBLE, ResKey.forTest("strings.are.allowed"));
            }
        };
        
        
        DispatchByClassExecutabilityRule drule = new DispatchByClassExecutabilityRule(
			ExecutableState.createDisabledState(ResKey.forTest("default.disabled")), ExecutableState.NOT_EXEC_HIDDEN);
        drule.addRuleForClass(Integer.class, eInt);
        drule.addRuleForClass(String.class , eString);
        
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, drule.isExecutable(null, null, null));

		assertEquals("default.disabled", drule.isExecutable(null, this, null).getI18NReasonKey().toString());

		assertSame(ExecutableState.EXECUTABLE, drule.isExecutable(null, Integer.valueOf(42), null));
        
		assertEquals("strings.are.allowed", drule.isExecutable(null, "TestDispatchByClassExecutabilityRule", null)
			.getI18NReasonKey().toString());
    }

	/**
	 * the suite of Tests to execute.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(TestDispatchByClassExecutabilityRule.class,
			BoundHelper.Module.INSTANCE,
			SecurityObjectProviderManager.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(test);
	}
    
    public static void main(String[] args) {
        Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }
}

