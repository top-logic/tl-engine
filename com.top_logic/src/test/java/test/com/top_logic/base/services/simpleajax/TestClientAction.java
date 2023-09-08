/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import java.io.IOException;

import junit.framework.Test;
import junit.textui.TestRunner;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DefaultUpdateQueue;
import com.top_logic.layout.DisplayContext;

/**
 * Testcase for {@link ClientAction}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestClientAction extends ActionTestcase {

    /** 
     * Create a new TestClientAction for given function.
     */
    public TestClientAction(String aTest) {
        super(aTest);
    }

    public void testCreateActions() {
        TestedClientAction tca = new TestedClientAction();
        DefaultUpdateQueue/*<ClientAction>*/ actions = new DefaultUpdateQueue();
        actions.add(tca);
        assertEquals(1, actions.getStorage().size());
        assertTrue  (actions.getStorage().contains(tca));
    }

    /**
     * Must override ClientAction to allow actual testing.
     */
    public class TestedClientAction extends ClientAction {

        /**
         * The XSI-Type is usually just the base-name of the implementing class.
         */
        @Override
		protected String getXSIType() {
            return "TestedClientAction";
        }

        /**
         * Just write a comment to do anything at all.
         */
        @Override
		protected void writeChildrenAsXML(DisplayContext aContext, TagWriter aWriter)
                throws IOException {
            aWriter.writeComment("TestedClientAction");
        }
    }

    /** 
     * Return the suite of Tests to execute.
     */
    public static Test suite() {
		return suite(TestClientAction.class);
    }

    /** 
     * Main function for direct execution.
     */
    public static void main (String[] args) {

        TestRunner.run(suite());
    }


}

