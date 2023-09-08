/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.config.diff.zip;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipFile;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.config.diff.zip.ZipReader;
import com.top_logic.config.diff.zip.ZipReader.EntryResult;


/**
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public class TestZipReader extends TestCase {
    
	private static final String PATH_PREFIX = ModuleLayoutConstants.SRC_TEST_DIR + "/test/data/zipCompare/";
    private static final String SEPARATOR = "/";
    
    public static final String ZIP_FILE_1 = PATH_PREFIX+"file1.zip"; 
    public static final String ZIP_FILE_2 = PATH_PREFIX+"file2.zip"; 
    private static ZipReader zipReader = null;
    
    public TestZipReader(String aString) {
        super(aString);
    }
    
    public static ZipReader getZipReader() {
    	if(zipReader== null){
    		zipReader=new ZipReader();
    	}
    	return zipReader;
    }
    
    public void testEqualFiles() throws IOException {
        
        ZipFile zip1 = new ZipFile(ZIP_FILE_1);
        ZipFile zip2 = new ZipFile(ZIP_FILE_1);
        
        EntryResult root = getZipReader().compare(zip1, zip2);
        
        // We expect no changes in this Result
        // All entries must be in state NO_CHANGE
		checkStateRecursively(root, EntryResult.State.NO_CHANGE, true);
        
        checkContentRecursively(root);
    }
    
    public void testDifferentFiles() throws IOException {
        
        ZipFile zip1 = new ZipFile(ZIP_FILE_1);
        ZipFile zip2 = new ZipFile(ZIP_FILE_2);
        
        EntryResult root = getZipReader().compare(zip1, zip2);
        
        EntryResult newEntries = null;
        EntryResult deletedEntries = null;
        EntryResult noChangeEntries = null;
        EntryResult changeEntries = null;
        
        Iterator iter = root.getChildren().iterator();
        while (iter.hasNext()) {
            EntryResult theEntry = (EntryResult) iter.next();
            assertTrue(theEntry.isDirectory());
            assertEquals("", theEntry.getPath());
            String name = theEntry.getName();
			if ((EntryResult.State.NEW + SEPARATOR).equals(name)) {
                newEntries = theEntry;
            }
			else if ((EntryResult.State.DELETED + SEPARATOR).equals(name)) {
                deletedEntries = theEntry;
            }
			else if ((EntryResult.State.CHANGE + SEPARATOR).equals(name)) {
                changeEntries = theEntry;
            }
			else if ((EntryResult.State.NO_CHANGE + SEPARATOR).equals(name)) {
                noChangeEntries = theEntry;
            }
            else{
//            	fail("Do not know name "+name);
            }
        }
        
        assertNotNull(newEntries);
        assertNotNull(deletedEntries);
        assertNotNull(changeEntries);
        assertNotNull(noChangeEntries);
        
		checkStateRecursively(newEntries, EntryResult.State.NEW, true);
		checkStateRecursively(deletedEntries, EntryResult.State.DELETED, true);
		checkStateRecursively(changeEntries, EntryResult.State.CHANGE, false);
		checkStateRecursively(noChangeEntries, EntryResult.State.NO_CHANGE, true);
        
        checkContentRecursively(root);
    }
    
    private void checkContentRecursively(EntryResult aRoot) {
		if (EntryResult.State.CHANGE.equals(aRoot.getState())) {
			if (!aRoot.isDirectory()) {
				assertFalse(aRoot.getSource().equals(aRoot.getDest()));
			}
        }
		else if (EntryResult.State.NO_CHANGE.equals(aRoot.getState())) {
			if (!aRoot.isDirectory()) {
				assertTrue(aRoot.getSource().equals(aRoot.getDest()));
			}
        }
		else if (EntryResult.State.NEW.equals(aRoot.getState())) {
            assertNotNull(aRoot.getDestEntry());
            assertNull(aRoot.getSourceEntry());
        }
		else if (EntryResult.State.DELETED.equals(aRoot.getState())) {
            assertNotNull(aRoot.getSourceEntry());
            assertNull(aRoot.getDestEntry());
        }
        Iterator iter = aRoot.getChildren().iterator();
        while (iter.hasNext()) {
            checkContentRecursively((EntryResult) iter.next());
        }
    }

	private void checkStateRecursively(EntryResult aRoot, EntryResult.State aState, boolean includingRoot) {
        if (includingRoot) {
            assertEquals(aState, aRoot.getState());
        }
        Iterator iter = aRoot.getChildren().iterator();
        while (iter.hasNext()) {
            EntryResult theResult = (EntryResult) iter.next();
            assertEquals(aState, theResult.getState());
            checkStateRecursively(theResult, aState, false);
        }
    }
    
    // TODO
    
//    public void testCompareDocuments()throws Exception{
//    	ConfigCoreBranch mainBranch = TestUtil.getMainBranch();
//    	String versionName = TestDataGenerator.getRandomIntString();
//		ConfigCoreVersion version = TestUtil.createVersion(versionName, mainBranch);
//    	WebFolderAware wfa = (WebFolderAware) mainBranch;
//    	WebFolder webFolder = wfa.getWebFolder();
//    	FileInputStream fis = new FileInputStream(ZIP_FILE_1);
//    	Document d1 = webFolder.createDocument("zzz1", fis);
//    	fis.close();
//    	commitBegin();
//    	fis = new FileInputStream(ZIP_FILE_2);
//    	Document d2 = webFolder.createDocument("zzz2", fis);
//    	fis.close();
//    	commitBegin();
//    	EntryResult compare = getZipReader().compare(d1,d2);
//    	assertNotNull(compare);
//    	
//    	CompareExcelReport report = new CompareExcelReport ();
//    	
//    	ExcelValue[] createReportContent = report.createReportContent(compare, d1, d2, version, version, false);
//    	assertNotNull(createReportContent);
//    	
//    	
//    	// clean up
//    	webFolder.remove(d1.getWrappedObject());
//    	webFolder.remove(d2.getWrappedObject());
//    	commitBegin();
//    	
//    }
    
    
    public void testGetZipFileNames() throws Exception{
        ZipFile zip1 = new ZipFile(ZIP_FILE_1);
        List<String> fileNames = getZipReader().getFileNames(zip1);
        assertEquals(9, fileNames.size());
    }
    
    
    public static Test suite() {
        return ModuleLicenceTestSetup.setupModule(TestZipReader.class);
    }
    
}
