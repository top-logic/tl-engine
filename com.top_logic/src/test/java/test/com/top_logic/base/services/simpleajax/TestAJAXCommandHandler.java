/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import java.io.IOException;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import test.com.top_logic.layout.basic.AbstractCommandModelTest;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Testcase for the {@link AJAXCommandHandler}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestAJAXCommandHandler extends TestCase {

    public TestAJAXCommandHandler(String name) {
        super(name);
    }

    /**
     * Test a default AJAXCommandHandler.
     */
    public void testDefaultCommand() {
		TestedAJAXCommandHandler tajax = AbstractCommandHandler.newInstance(TestAJAXCommandHandler.TestedAJAXCommandHandler.class,
			"mrProper");
        
        assertEquals("mrProper", tajax.getID());
        
        try { // cannot really test this, well
			tajax.handleCommand(null, null, null, null);
        }
        catch (NullPointerException expected) { /* expected */ }
        
    }
    
    /**
     * Tets a deadonly command.
     */
    public void testReadOnly() {
		TestedAJAXCommandHandler tajax = AbstractCommandHandler.newInstance(
			AJAXCommandHandler.updateReadOnly(
					AbstractCommandHandler.<AJAXCommandHandler.Config> createConfig(
						TestAJAXCommandHandler.TestedAJAXCommandHandler.class,
						"mrProper"),
				true));
        
        assertEquals("mrProper", tajax.getID());
        
        try { // cannot really test this, well
			tajax.handleCommand(null, null, null, null);
        }
        catch (NullPointerException expected) { /* expected */ }
        
    }

    /*

    public void testGetCommandScriptBody() {
        fail("Not yet implemented");
    }

    public void testAppendBodyStatements() {
        fail("Not yet implemented");
    }

    public void testAppendCallStatement() {
        fail("Not yet implemented");
    }

    public void testGetInvokeExpression() {
        fail("Not yet implemented");
    }

    public void testAppendInvokeExpressionStringBufferJSObject() {
        fail("Not yet implemented");
    }

    public void testAppendInvokeExpressionStringBufferStringJSObject() {
        fail("Not yet implemented");
    }

    public void testWriteComponentHeader() {
        fail("Not yet implemented");
    }

    public void testWriteBALInclude() {
        fail("Not yet implemented");
    }
    */
	public static class TestedAJAXCommandHandler extends AJAXCommandHandler {

		/**
		 * Allow access to superclass CTor.
		 */
        public TestedAJAXCommandHandler(InstantiationContext context, Config config) {
            super(context, config);
        }

        /**
         * always {@link HandlerResult#DEFAULT_RESULT}.
         */
        @Override
		public HandlerResult handleCommand(DisplayContext aCommandContext,
                LayoutComponent aComponent, Object model, Map<String, Object> aArguments) {
            return HandlerResult.DEFAULT_RESULT;
        }

    }
    
    /** 
     * Return the suite of Tests to execute.
     */
    public static Test suite() {
		return AbstractCommandModelTest.suite(TestAJAXCommandHandler.class);
    }

    /** 
     * Main function for direct execution.
     */
    public static void main (String[] args) throws IOException {
        TestRunner.run(suite());
    }


}

