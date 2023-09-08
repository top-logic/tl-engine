
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.simple.AttributesDataObject;

/**
 * Test cases for the {@link com.top_logic.dob.simple.AttributesMetaObject}.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestAttributesDataObject extends TestCase {

    /**
     * Create a Test for the given (function-) name.
     *
     * @param name the (funxtion)-name of the test to perform.
     */
    public TestAttributesDataObject (String name) {
        super (name);
    }

    /** Single and only testcase for now */
    public void testMain() throws Exception  {
    
    	Date 	now = new Date();
		Integer ham = Integer.valueOf(0xdeadbeef);
    
    	Attributes attrs = new BasicAttributes();
    	attrs.put("aaa", "aaa");
    	attrs.put("bbb", ham);
    	attrs.put("ccc", now);
    	
    	AttributesDataObject ado = new AttributesDataObject(attrs);
		
		MetaObject meta = ado.tTable();
		assertNotNull(meta);
		assertTrue(ado.isInstanceOf(meta));
		assertTrue(ado.isInstanceOf(meta.getName()));
		assertNotNull(ado.getIdentifier());

		// This is reaply perverted ! (KHA)
		ArrayList<String> tmp  = new ArrayList<>();
		for (MOAttribute attribute : ado.getAttributes())
			tmp.add(attribute.getName());
		assertTrue(tmp.contains("aaa"));
		assertTrue(tmp.contains("bbb"));
		assertTrue(tmp.contains("ccc"));

		List names = Arrays.asList(ado.getAttributeNames());
		assertTrue(names.contains("aaa"));
		assertTrue(names.contains("bbb"));
		assertTrue(names.contains("ccc"));
		
		assertEquals("aaa",	ado.getAttributeValue("aaa"));
		assertEquals(ham,	ado.getAttributeValue("bbb"));
		assertEquals(now,	ado.getAttributeValue("ccc"));

		// Dont try this with normal DataObject ;-)
		ado.setAttributeValue("aaa", now);
		ado.setAttributeValue("bbb", "aaa");
		ado.setAttributeValue("ccc", ham);
		
		assertEquals(now,	ado.getAttributeValue("aaa"));
		assertEquals("aaa",	ado.getAttributeValue("bbb"));
		assertEquals(ham,	ado.getAttributeValue("ccc"));
	}

   /**
     * The suite of Test to execute.
     */
    public static Test suite () {
        return new TestSuite (TestAttributesDataObject.class);
        // return new TestAttributesDataObject("testMain");
    }

    /**
     * Main fucntion for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
