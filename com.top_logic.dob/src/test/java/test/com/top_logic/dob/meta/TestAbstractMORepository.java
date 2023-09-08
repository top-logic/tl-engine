/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.meta;

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.DefaultMORepository;
import com.top_logic.dob.simple.FileMetaObject;

/**
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestAbstractMORepository extends TestCase {

    /**
     * Test the methods from AbstractMORepository.
     */
    public void testMethods() throws Exception{
        FileRepository repos = new FileRepository();
        
        assertEquals ("File", repos.getMetaObjectNames().get(0));
        assertTrue   (repos.containsMetaObject(FileMetaObject.SINGLETON));
        assertSame   (FileMetaObject.SINGLETON, repos.getMetaObject("File"));
        
        try {
            repos.getMetaObject("NoSuchmeta");
            fail("Expected UnknownTypeException");
        } catch (UnknownTypeException expected) { /* expected */ }
        try {
            repos.addMetaObject(FileMetaObject.SINGLETON);
            fail("Expected DuplicateTypeException");
        } catch (DuplicateTypeException expected) { /* expected */ }
    }

    /**
     * Test method for getMOCollection(String, String)'
     */
    public void testGetMOCollection() {
        FileRepository repos = new FileRepository();
       
         try {
             repos.getMOCollection("unkind", "File");
             fail("Expected UnknownTypeException");
        } catch (UnknownTypeException expected) { /* expected */ }
    }

    /**
     * Mock Impelementation of a FileRepository. 
     */
    static class FileRepository extends DefaultMORepository {
        
        /** 
         * Create a new FileRepository for testing.
         */
        public FileRepository() {
			super(Collections.<String, MetaObject> singletonMap("File", FileMetaObject.SINGLETON), false);
        }

    }

    /**
     * the suite of Tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestAbstractMORepository.class);
        return suite;
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
}
