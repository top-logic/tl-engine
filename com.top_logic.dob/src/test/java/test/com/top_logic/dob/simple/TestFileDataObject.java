
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.simple;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.simple.FileDataObject;
import com.top_logic.dob.util.MetaObjectUtils;


/**
 * Test cases for the {@link com.top_logic.dob.simple.FileDataObject}.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestFileDataObject extends TestCase {

    /**
     * Create a Test for the given (function-) name.
     *
     * @param name the (funxtion)-name of the test to perform.
     */
    public TestFileDataObject (String name) {
        super (name);
    }

    /** Testing the main fucntions */
    public void testMain() throws Exception  {
    
		File aFolder = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/dob/simple");
        FileDataObject  folder = new FileDataObject(aFolder);
    
        File            aFile = new File(aFolder, "TestFileDataObject.java");
        FileDataObject  file = new FileDataObject(aFile);
        
        
        assertEquals(Boolean.TRUE , folder.getAttributeValue("isContainer"));
        assertEquals(Boolean.FALSE, folder.getAttributeValue("isEntry"));
        assertEquals("simple"     , folder.getAttributeValue("name"));
        
        folder.getAttributeValue("physicalResource");

        assertEquals(Boolean.FALSE, file.getAttributeValue("isContainer"));
        assertEquals(Boolean.TRUE , file.getAttributeValue("isEntry"));
        assertEquals(Boolean.TRUE , file.getAttributeValue("isReadable"));
        
        assertEquals("TestFileDataObject.java" 
                                  , file.getAttributeValue("name"));
                                  
        file.getAttributeValue("physicalResource");
        
        try {
            file.setAttributeValue("any", null);
            fail("setAttributeValue should not be suppported");
		} catch (UnsupportedOperationException expected) {
			/* expected */
		}
    }
    
    /** Testing access and usage o the FileMetaObject */
    public void testMetaObject() throws Exception  {
    
		File aFolder = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com.top_logic.mig/dataobjects/simple");
        FileDataObject  folder  = new FileDataObject(aFolder);
        MetaObject      theMeta = folder.tTable();    
        
        String attrs[] = MetaObjectUtils.getAttributeNames(theMeta);
        int    len     = attrs.length;
        assertTrue("No Attributes ?", len > 0);
        for (int i=0; i <len; i++)
            assertNotNull(folder.getAttributeValue(attrs[i]));
    }
    
    /**
     * Test {@link FileDataObject#equals(Object)} with some arbitrary object.
     * #author bha
     */
    public void testEqualsWithBadObject() throws Exception {
    	
		File aFolder = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com.top_logic.mig/dataobjects/simple");
         FileDataObject  folder = new FileDataObject(aFolder);
         
         boolean test = folder.equals(new Object());
         
         assertFalse("equals() failed", test);    	
    }

    /**
     * Test {@link FileDataObject#toString()}.
     * #author bha
     */
    public void testToString () throws Exception {
    
		File aFolder = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com.top_logic.mig/dataobjects/simple");
         FileDataObject  folder  = new FileDataObject(aFolder);
         
         String result = "FileDataObject [" + aFolder.toString() + "]";
         
         boolean test = result.equals(folder.toString());
         
         assertTrue("toString() failed", test);
    }
    
    /**
     * Test {@link FileDataObject#isInstanceOf(MetaObject)}.
     * #author bha
     */
    public void  testIsInstanceOf() throws Exception {
    	
		File aFolder = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com.top_logic.mig/dataobjects/simple");
        FileDataObject  folder = new FileDataObject(aFolder);
        MetaObject      theMeta = folder.tTable();
        
        assertTrue ("isInstanceOf() failed" , folder.isInstanceOf(theMeta));
    }
    
    /**
     * Test {@link FileDataObject#getAttributes()}.
     * #author bha
     */
    public void testGetAttributes() throws Exception {
    	
		File aFolder = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com.top_logic.mig/dataobjects/simple");
        FileDataObject  folder = new FileDataObject(aFolder);
    	
        assertNotNull ( "iterator is null!", folder.getAttributes());    	
    }
    
    /**
     * Test {@link FileDataObject#getAttributeNames()}.
     * #author bha
     */
   public void testGetAttributeNames() throws Exception {
    	
		File aFolder = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com.top_logic.mig/dataobjects/simple");
        FileDataObject  folder = new FileDataObject(aFolder);
        
        String [] testArray = folder.getAttributeNames();
        
        assertNotNull ( "array is null!", testArray);
        assertEquals  ( 9,                testArray.length);
    }
    
    /**
     * The suite of Test to execute.
     */
    public static Test suite () {
       return new TestSuite (TestFileDataObject.class);
        // return new TestFileDataObject("testMain");
    }

    /**
     * Main fucntion for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
