/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.simple;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Date;
import java.util.Set;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.simple.AttributesMetaObject;
import com.top_logic.dob.util.MetaObjectUtils;

/**
 * Test cases for the {@link com.top_logic.dob.simple.AttributesMetaObject}.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestAttributesMetaObject extends TestCase {

    /**
     * Create a Test for the given (function-) name.
     *
     * @param name the (funxtion)-name of the test to perform.
     */
    public TestAttributesMetaObject (String name) {
        super (name);
    }

	/** Single and only testcase for now */
	public void testMain() throws Exception {

		Date now = new Date();

		Attributes attrs = new BasicAttributes();
		attrs.put("aaa", "aaa");
		attrs.put("bbb", Integer.valueOf(0xdeadbeef));
		attrs.put("ccc", now);

		final AttributesMetaObject amo = new AttributesMetaObject(attrs);

		assertNotNull(amo.getName());
		assertTrue(MetaObjectUtils.isStructure(amo));

		try {
			MetaObjectUtils.getAttribute(amo, "xxx");
			fail("There is no such Attribute");
		} catch (NoSuchAttributeException expected) { /* expected */
		}

		MOAttribute attrAAA = MetaObjectUtils.getAttribute(amo, "aaa");
		assertEquals("aaa", attrAAA.getName());

		final MOAttribute attrBBB = MetaObjectUtils.getAttribute(amo, "bbb");
		assertEquals("bbb", attrBBB.getName());

		final MOAttribute attrCCC = MetaObjectUtils.getAttribute(amo, "ccc");
		assertEquals("ccc", attrCCC.getName());

		assertFalse(MetaObjectUtils.hasAttribute(amo, "Blah"));
		assertTrue(MetaObjectUtils.hasAttribute(amo, "aaa"));

		final Set<String> expectedNames = set(attrAAA.getName(), attrBBB.getName(), attrCCC.getName());

		Set<String> nameSet = Mappings.mapIntoSet(new Mapping<MOAttribute, String>() {
			
			@Override
			public String map(MOAttribute input) {
				return input.getName();
			}
			
		}, MetaObjectUtils.getAttributes(amo));

		assertEquals(expectedNames, nameSet);
		assertEquals(expectedNames, CollectionUtil.toSet(MetaObjectUtils.getAttributeNames(amo)));
	}

    /** Test some trivial fucntions */
    public void testTrivial() throws Exception  {
    
        Attributes           attrs  = new BasicAttributes();
        AttributesMetaObject amo    = new AttributesMetaObject(attrs);
        
        assertTrue(amo.isSubtypeOf(amo));
        assertTrue(amo.isSubtypeOf(attrs.toString()));

        assertTrue(!MetaObjectUtils.isCollection(amo));
        assertTrue(!MetaObjectUtils.isClass(amo));
        assertTrue(!MetaObjectUtils.isPrimitive(amo));
    }
    
    /**
     * The suite of Test to execute.
     */
    public static Test suite () {
        return new TestSuite (TestAttributesMetaObject.class);
        // return new TestAttributesMetaObject("testMain");
    }

    /**
     * Main fucntion for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
