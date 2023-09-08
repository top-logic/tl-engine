/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import java.io.IOException;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.base.services.simpleajax.TestClientAction.TestedClientAction;

import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.DOMAction;

/**
 * Testcase for the {@link DOMAction}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestDOMAction extends ActionTestcase {

  
    public TestDOMAction(String aName) {
        super(aName);
    }

    /**
     * Test the main feature of the DOMAction.
     */
    public void testWriteChildrenAsXML() throws Exception {
		DOMAction doma = new TestedDOMAction("testDOMAction");

		assertEquals("testDOMAction", doma.getElementID());
		assertXMLOutput(doma, "<"
				+ AJAXConstants.AJAX_ACTION_ELEMENT + ' '
				+ TestedClientAction.XSI_NAMESPACE_DECL + ' '
				+ AJAXConstants.XSI_TYPE_ATTRIBUTE + "=\"TestedDOMAction\">"
				+ "<" + AJAXConstants.AJAX_ID_ELEMENT + ">testDOMAction</"
				+ AJAXConstants.AJAX_ID_ELEMENT + ">"
				+ "</" + AJAXConstants.AJAX_ACTION_ELEMENT + '>');
    }

    /**
     * Must override DOMAction to allow actual testing.
     */
    public static class TestedDOMAction extends DOMAction {

        /** 
         * Allow access to supeclass CTor.
         */
        public TestedDOMAction(String aElementID) {
            super(aElementID);
        }

        /**
         * The XSI-Type is usually just the base-name of the implementing class.
         */
        @Override
		protected String getXSIType() {
            return "TestedDOMAction";
        }

    }
    
   /** 
     * Return the suite of Tests to execute.
     */
    public static Test suite() {
		return suite(TestDOMAction.class);
    }

    /** 
     * Main function for direct execution.
     */
    public static void main (String[] args) throws IOException {

        TestRunner.run(suite());
    }

}

