/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.message;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.message.AbstractMessages;

/**
 * Test case for {@link AbstractMessages}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestAbstractMessages extends TestCase {

	public void testArgCount() {
		assertEquals(0, Messages.MSG_0.getParameterCount());
		assertEquals(1, Messages.MSG_1.getParameterCount());
		assertEquals(2, Messages.MSG_2.getParameterCount());
		assertEquals(3, Messages.MSG_3.getParameterCount());
	}
	
	public void testKey() {
		assertEquals("MSG_0", Messages.MSG_0.getLocalName());
		assertEquals("MSG_1", Messages.MSG_1.getLocalName());
		assertEquals("MSG_2", Messages.MSG_2.getLocalName());
		assertEquals("MSG_3", Messages.MSG_3.getLocalName());
	}
	
	public void testArgRetrival() {
		doTestArgRetrieval("arg1", "arg2", "arg3", "arg4");
	}

	public void testArgRetrivalNull() {
		doTestArgRetrieval(null, null, null, null);
	}
	
	private void doTestArgRetrieval(String arg1, String arg2, String arg3, String arg4) {
		assertEquals(null, Messages.MSG_0.fill().getArguments());
		BasicTestCase.assertEquals(new Object[] {arg1}, Messages.MSG_1.fill(arg1).getArguments());
		BasicTestCase.assertEquals(new Object[] {arg1, arg2}, Messages.MSG_2.fill(arg1, arg2).getArguments());
		BasicTestCase.assertEquals(new Object[] {arg1, arg2, arg3}, Messages.MSG_3.fill(arg1, arg2, arg3).getArguments());
	}
	
	public void testTemplateRetrival() {
		assertEquals(Messages.MSG_0, Messages.MSG_0.fill().getTemplate());
		assertEquals(Messages.MSG_1, Messages.MSG_1.fill("").getTemplate());
		assertEquals(Messages.MSG_2, Messages.MSG_2.fill("", "").getTemplate());
		assertEquals(Messages.MSG_3, Messages.MSG_3.fill("", "", "").getTemplate());
	}
	
	public void testDefault() {
		assertEquals(Messages.MSG_2, Messages.MSG_DEFAULT.getDefault());
	}
	
	public static Test suite() {
		return new TestSuite(TestAbstractMessages.class);
	}
	
}
