/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.execution;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Test the {@link ExecutableState}
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestExecutableState extends TestCase {

	static final ResKey REASON = null;

	public void testExecutableState() {
        try {
			new ExecutableState(42, REASON);
            fail("Exspected IllegalArgumentException: Given visibility is invalid");
        } catch (IllegalArgumentException iax) { /* expected */ }

        try {
			new ExecutableState(ExecutableState.CommandVisibility.HIDDEN, REASON);
            fail("Exspected IllegalArgumentException. Reason must not be null or empty");
        } catch (IllegalArgumentException iax) { /* expected */ }
        
        try {
			ExecutableState.createDisabledState(null);
            fail("Exspected IllegalArgumentException. Reason must not be null or empty");
        } catch (IllegalArgumentException iax) { /* expected */ }
        
		new ExecutableState(ExecutableState.CommandVisibility.VISIBLE, REASON);
		new ExecutableState(ExecutableState.CommandVisibility.VISIBLE, ResKey.text("some.reason"));
		new ExecutableState(ExecutableState.CommandVisibility.DISABLED, ResKey.text("some.reason"));
        
		ExecutableState es = new ExecutableState(ExecutableState.CommandVisibility.HIDDEN, ResKey.text("some.reason"));
        assertNotNull(es.toString());
        assertNotNull(es.getReason());
    }
    
    /**
     * the suite of Tests to execute.
     */
    public static Test suite () {
        return TLTestSetup.createTLTestSetup(new TestSuite (TestExecutableState.class));
    }   
    
    public static void main(String[] args) {
        Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }
}

