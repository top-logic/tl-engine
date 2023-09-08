/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.services.simpleajax.AJAXRequest;
import com.top_logic.base.services.simpleajax.AJAXRequestParser;
import com.top_logic.base.services.simpleajax.CommandRequest;
import com.top_logic.basic.Logger;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.mig.html.layout.ComponentName;

/**
 * Test the {@link com.top_logic.base.services.simpleajax.AJAXRequestParser}.
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestAJAXRequestParser extends TestCase {

    /** 
     * Test given function-name.
     */
    public TestAJAXRequestParser(String aName) {
        super(aName);
    }

	/**
	 * Tests ajaxRequest with multiple commands
	 */
	public void testMultipleCommand() throws SAXNotRecognizedException, SAXNotSupportedException,
			FactoryConfigurationError, ParserConfigurationException, SAXException, IOException {
		AJAXRequestParser arp = new AJAXRequestParser();
		File xml = new File(
			ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/services/simpleajax/ajaxmultiple.xml");
		BasicTestCase.parseXML(xml, arp);

		AJAXRequest areq = arp.getAJAXRequest();
		assertFalse(areq.hasTxSequence());
		List<CommandRequest> commands = areq.getCommands();
		assertEquals(2, commands.size());
		CommandRequest first = commands.get(0);
		CommandRequest second = commands.get(1);

		assertEquals("lazy", first.getCommand());
		assertTrue(first.getArguments().isEmpty());
		assertNotNull(first.getContextComponentID());
		assertEquals(ComponentName.newName("comp1"), first.getContextComponentID());
		assertEquals(null, first.getTargetComponentID());
		assertTrue(first.isStatic());
		assertEquals(156, first.getSubmitNumber());

		assertEquals("notifyUnload", second.getCommand());
		List<Object> value = new ArrayList<>();
		value.add(15);
		value.add("ddd");
		value.add(true);
//		Object value = Collections.singletonMap("valueKey", 15);
		assertEquals(Collections.singletonMap("key", value), second.getArguments());
		assertComponentNames(second, "masterFrame", "masterFrame");
		assertFalse(second.isStatic());
	}

    /**
     * Test a notifyUnload Command.
     */
    public void testAJAXRequestParser1() throws Exception {
        AJAXRequestParser arp = new AJAXRequestParser();
		File xml =
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/services/simpleajax/ajax1.xml");
        BasicTestCase.parseXML(xml, arp);
        
        AJAXRequest areq = arp.getAJAXRequest();
        
        assertEquals("notifyUnload", areq.getCommand());
        assertTrue  (                areq.getArguments().isEmpty());
		assertComponentNames(areq, "masterFrame", "masterFrame");
        // assertNull  (areq.getTxSequence()); is blocked by assert, well
        assertFalse (areq.hasTxSequence());
        assertFalse (areq.isStatic());
    }

	private void assertComponentNames(CommandRequest req, String contextComponentName, String targetComponentName) {
		assertNotNull(req.getContextComponentID());
		assertEquals(ComponentName.newName(contextComponentName), req.getContextComponentID());
		assertNotNull(req.getTargetComponentID());
		assertEquals(ComponentName.newName(targetComponentName), req.getTargetComponentID());
	}

    /**
     * Test a tabSwitch command.
     */
    public void testAJAXRequestParser2() throws Exception {
        AJAXRequestParser arp = new AJAXRequestParser();
		File xml =
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/services/simpleajax/ajax2.xml");
        BasicTestCase.parseXML(xml, arp);
        
        AJAXRequest areq = arp.getAJAXRequest();
        
		Map<String, Object> args = areq.getArguments();

        assertEquals("tabSwitch", areq.getCommand());
		assertEquals(Integer.valueOf(13), args.get("tab"));
		assertComponentNames(areq, "masterFrame_0_t_0_b", "masterFrame_0_t_0_b");
		assertEquals(Integer.valueOf(17397), areq.getTxSequence());
        
        assertTrue  (areq.hasTxSequence());
        assertFalse (areq.isStatic());
    }
    
    /**
     * Test a formFieldValueChanged command.
     */
    public void testAJAXRequestParser3() throws Exception {
        AJAXRequestParser arp = new AJAXRequestParser();
		File xml =
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/services/simpleajax/ajax3.xml");
        BasicTestCase.parseXML(xml, arp);
        
        AJAXRequest areq = arp.getAJAXRequest();
        
		Map<String, Object> args = areq.getArguments();

        assertEquals("formFieldValueChanged", areq.getCommand());
        assertEquals("test.order.purchaser.surname" , args.get("name"));
        assertEquals("Halfmann"                     , args.get("value"));
		assertComponentNames(areq, "masterFrame_0_t_0_t_0_t_0", "masterFrame_0_t_0_t_0_t_0");
		assertEquals(Integer.valueOf(23), areq.getTxSequence());
        
        assertTrue  (areq.hasTxSequence());
        assertFalse (areq.isStatic());
    }

    /**
     * Test that parser is stateless when parsing two files...
     */
    public void testStatelessParse() throws Exception {
        AJAXRequestParser arp = new AJAXRequestParser();
        File xml;
        
		xml = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/services/simpleajax/ajax2.xml");
        BasicTestCase.parseXML(xml, arp);

		xml = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/services/simpleajax/ajax3.xml");
        BasicTestCase.parseXML(xml, arp);
        
        AJAXRequest areq = arp.getAJAXRequest();
        
		Map<String, Object> args = areq.getArguments();

        assertEquals("formFieldValueChanged", areq.getCommand());
        assertEquals("test.order.purchaser.surname" , args.get("name"));
        assertEquals("Halfmann"                     , args.get("value"));
		assertComponentNames(areq, "masterFrame_0_t_0_t_0_t_0", "masterFrame_0_t_0_t_0_t_0");
		assertEquals(Integer.valueOf(23), areq.getTxSequence());
        
        // Must forget values parsed in first run !
        assertEquals(null , args.get("tab"));

        assertTrue  (areq.hasTxSequence());
        assertFalse (areq.isStatic());
    }
    
    /**
     * Test that parser is stateless even after parsing a broken file.
     */
    public void testBrokenParse() throws Exception {
        AJAXRequestParser arp = new AJAXRequestParser();
        File xml;
        
		xml = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/services/simpleajax/ajax3.xml");
        BasicTestCase.parseXML(xml, arp);
        
        AJAXRequest areq = arp.getAJAXRequest();
        
		Map<String, Object> args = areq.getArguments();

        assertEquals("formFieldValueChanged", areq.getCommand());
        assertEquals("test.order.purchaser.surname" , args.get("name"));
        assertEquals("Halfmann"                     , args.get("value"));
		assertComponentNames(areq, "masterFrame_0_t_0_t_0_t_0", "masterFrame_0_t_0_t_0_t_0");
		assertEquals(Integer.valueOf(23), areq.getTxSequence());
        
        assertTrue  (areq.hasTxSequence());
        assertFalse (areq.isStatic());
    }

    /** 
     * Return the suite of Tests to execute.
     */
    public static Test suite() {
        return new TestSuite(TestAJAXRequestParser.class);
        // return new TestAJAXRequestParser("testBrokenParse");
    }

    /** 
     * Main function for direct execution.
     */
	public static void main(String[] args) {
        Logger.configureStdout();
        TestRunner.run(suite());
    }

}
