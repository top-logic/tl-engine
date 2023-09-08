/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.bean;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.bean.BeanMORepository;

/**
 * Testcase for {@link com.top_logic.dob.bean.BeanMORepository}.
 * 
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestBeanMORepository extends TestCase {

    /**
     * Default Constructor.
     *
     * @param name  name of testFunction to perform. 
     */
    public TestBeanMORepository (String name) {
        super (name);
    }

    /**
     * Main tescase for now.
     */
    public void testMain() throws Exception {
    	BeanMORepository mor = BeanMORepository.getInstance();
        
        MetaObject mo     = mor.getMetaObject("test.com.top_logic.dob.bean.TestBeanDataObject");
        assertNotNull(mo);
        assertTrue(mor.containsMetaObject(mo));

		assertTrue(mor.getMetaObjectNames().contains(
				"test.com.top_logic.dob.bean.TestBeanDataObject"));
    }

    /**
     * the suite of tests to perform.
     */
    public static Test suite () {
        return new TestSuite (TestBeanMORepository.class);
    }


    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
