/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dsa.repos.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.dsa.DSATestSetup;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.dsa.repos.EntryInfo;
import com.top_logic.dsa.repos.NoEntryException;
import com.top_logic.dsa.repos.RepositoryException;
import com.top_logic.dsa.repos.VersionInfo;
import com.top_logic.dsa.repos.file.FileRepository;
import com.top_logic.dsa.repos.file.FileRepository.Config;

/**
 * Tests for the working of the repository system.
 * 
 * 
 * <p>
 *  Windows (2000) does not allow the follwing Chracters in Filenames:
 *  <code> '"','?','>','<','|', '\'</code>.
 *  Java additionaly forbids <code> '/'</code>
 *  With Windows (2000) and the SunJDK it is possible to create a File ending
 *  in a single '.' which cannot be removed by Java or the Explorer (but from 
 *  the commandline ;-)
 * </p>
 *
 * TODO KHA add Active (Threaded) Test...
 *
 * @author     Klaus Halfmann 
 */
public class TestFileRepository extends BasicTestCase {

    /** Temporary Folder for the Repository */
    public static String TEST_TEXT = 
        "This File was created by test.com.top_logic.base.repository.TestFileRepository";

    /** Temporary Folder for the Repository */
    public static final File TESTREPOSITORY1  = BasicTestCase.createNamedTestFile("testrepos");

    /** Temporary Folder for the same Repository mounted / eventually mapped somewehere else */
    public static final File TESTREPOSITORY2  = BasicTestCase.createNamedTestFile("testrepos");

    /** Temporary Folder for the (optional) WorkArea */
    public static final File TESTWORKAREA1    = BasicTestCase.createNamedTestFile("testwork");
    
    /** Temporary Folder for the (optional) WorkArea */
    public static final File TESTWORKAREA2    = BasicTestCase.createNamedTestFile("testwork");

    /** Temporary Folder for the (optional) WorkArea */
    public static final File TESTATTIC1       = BasicTestCase.createNamedTestFile("attic");
    
    /** Temporary Folder for the (optional) WorkArea / eventually mapped somewehere else */
    public static final File TESTATTIC2       = BasicTestCase.createNamedTestFile("attic");

    /** Number of Iterattions used for calibration */
    public static final int CALIBRATECOUNT   = 100;
    
    /** Repository Operation (without WorkAreea) may at most be that slower */
    public static final int FACTOR_EX       = 6;

    /** Repository Operation (without WorkAreea) may at most be that slower */
    public static final int FACTOR_CUM      = 6;

	private static final boolean NOFORCE = false;

    /** Number of Milliseconds needed for a Create/Delete Cycle */
    protected static long createDelTime;
    
    /**
     * Constructor with name of Testcase to execute.
     *
     * @param    aName    The name of the function to test.
     */
    public TestFileRepository (String aName) {
        super (aName);
    }

     // Helper functions for the Tests
     
    /** Return a InputStream for creating Repositories */     
    private InputStream getStream()
    {
	    return new ByteArrayInputStream(TEST_TEXT.getBytes());
    }

    /** Write to and Close an OuptputStream */     
    private void writeToStream(OutputStream out) throws IOException
    {
        Writer w = new OutputStreamWriter(out);
        w.write(TEST_TEXT);
        w.close();
    }

    /** Read an InputStream into a String */     
    private String readFromStream(InputStream is) throws IOException
    {
        InputStreamReader wrapper = new InputStreamReader(is);
        
        StringWriter      result  = new StringWriter();
		try {
			FileUtilities.copyReaderWriterContents(wrapper, result);
		} finally {
			result.close();
		}
        return result.toString();
    }

    // Actual Testcases
    
	/**
	 *  This function is called first to measure times and cleanup the dirs.
	 */
	public void calibrate() throws Exception {
	    createdCleanTestDir("repository");
	    createdCleanTestDir("workarea");
        
        TESTREPOSITORY1.mkdir();
        startTime();
        for (int i=0; i < CALIBRATECOUNT; i++) {
            File newFile = new File (TESTREPOSITORY1,"someFile.txt");
			InputStream input = getStream();
            try {
				FileUtilities.copyToFile(input, newFile);
			} finally {
				input.close();
			}
            assertTrue("Unable to delete plain file", newFile.delete());        
        }
        
        createDelTime = logTime("Normal file create/delete\t\t\t");
	}

	/**
 	 *  Tests Functions using an empty Repository
	 */
	public void testEmpty() throws Exception {
		FileRepository frep = repository(TESTREPOSITORY1);
        
        try  {
            frep.getCurrentRevisionNum("nobody", "noSuchFile");
            fail ("Expected NoEntryException here");
        } catch (NoEntryException expected)  { /* expected */  } 

	    try  {
	        frep.getCurrentRevisionNum("nobody", "noSuchDir/NoSuchFile");
	        fail ("Expected NoEntryException here");
	    } catch (NoEntryException expected)  { /* expected */ } 
    
    	assertTrue  (!frep.exists("HamwaNich.nix"));
    	assertTrue  (!frep.exists("}x{/Sumsel/Gru°us.gox^"));

        // These are ok since this is at Top-Level
	    assertNotNull(frep.getEntries(null));
	    assertNotNull(frep.getEntries(""));
	    try  {
	        frep.getEntries("no such Thing");
	        fail ("Expected NoEntryException here");
	    } catch (NoEntryException expected)  { /* expected */ } 
	}
	
    /**
     *  Test creation of simple entries.
     */
    public void testEntries() throws Exception {
        
		FileRepository frep = repository(TESTREPOSITORY1);
    
        // First try create  with empty dirs ...
        assertEquals(1, frep.create("Heinz",  null, "Dadei", getStream()));
        try {
            final InputStream is = frep.get("~MisslMassul`", "Dadei");
            try {
            	readFromStream(is);   
            	fail("Cannot read a revision not yet unlocked");
            } finally {
            	is.close();
            }
        } catch (NoEntryException expected)  { /* expected */  } 

        assertTrue(     frep.unlock("Heinz",        "Dadei"));

        assertTrue(     frep.lock  ("Müller",       "Dadei"));
    	assertEquals(2, frep.create("Müller", null, "Dadei", getStream()));
        assertTrue(     frep.unlock("Müller",       "Dadei"));
        
        writeToStream(frep.create("_Öhmchen_"      , null,      "Pfeil"));
        assertEquals(0,frep.getCurrentRevisionNum("_Männlein_", "Pfeil"));
        assertEquals(1,frep.getCurrentRevisionNum("_Öhmchen_",  "Pfeil"));
        assertTrue(   frep.unlock("_Öhmchen_",                  "Pfeil"));
        
        assertTrue(     frep.lock  ("~Dähmchen~",            "Pfeil"));
        writeToStream(  frep.create("~Dähmchen~"     , null, "Pfeil"));
        assertTrue(     frep.unlock("~Dähmchen~",            "Pfeil"));

        assertTrue(     frep.lock  ("[Knallinger]",        "Pfeil"));
        assertEquals(3, frep.create("[Knallinger]" , null, "Pfeil", getStream()));
        assertEquals(3, frep.create("[Knallinger]" , null, "Pfeil", getStream()));
        assertTrue(     frep.unlock("[Knallinger]",        "Pfeil"));

        assertEquals(4, frep.delete("'PobelHobel'",       "Pfeil", NOFORCE));
        
        assertTrue(     frep.lock  ("^PforzHaus%" ,       "Pfeil"));
                        // Should ressurrect the deleted file
        assertEquals(5, frep.create("^PforzHaus%" , null, "Pfeil", getStream()));
        assertEquals(5, frep.create("^PforzHaus%" , null, "Pfeil", getStream()));
        assertEquals(TEST_TEXT, readFromStream(
                        frep.get   ("^PforzHaus%" ,       "Pfeil")));
        assertTrue(     frep.unlock("^PforzHaus%" ,       "Pfeil"));
        assertEquals(6, frep.delete("°a!"         ,       "Pfeil", NOFORCE));
        assertTrue  (frep.exists("Pfeil"));
        
        try {
            readFromStream(frep.get("^PforzHaus%", "Pfeil"));   // should fail since revision is deleted
            fail("Cannot read form deleted revision");
        } catch (NoEntryException expected)  { /* expected */ } 
    
        
        assertTrue(   frep.lock  (")SahneKuchen[",         "Pfeil"));
        writeToStream(frep.create(")SahneKuchen["  , null, "Pfeil"));
    
        assertEquals(TEST_TEXT, readFromStream(frep.get("Pfeil", 3)));
    
        try {
            readFromStream(frep.get("Pfeil", 6));   // should fail since revision was deleted
            fail("Cannot read form deleted revision");
        } catch (NoEntryException expected)  { /* expected */ } 
        
        
            Runtime rt = Runtime.getRuntime();
            rt.gc();
        startTime();
        
        int j=1;
        assertEquals(j++, frep.create("Usa",  null, "someFile.txt", getStream()));
        assertTrue(       frep.unlock("Usa",        "someFile.txt"));
        for (int i=0 ; i <= CALIBRATECOUNT; i++) {
            assertTrue(       frep.lock  ("Usa",        "someFile.txt"));
            assertEquals(j++, frep.create("Usa",  null, "someFile.txt", getStream()));
            assertTrue(       frep.unlock("Usa",        "someFile.txt"));
            assertEquals(j++, frep.delete("Losa"      , "someFile.txt", NOFORCE));
        }
        /* long time1 = */ logTime("Repository create/delete (Input)\t\t");
        j = 2;
        writeToStream(  frep.create("Another User"   , null, "OtherFile.txt"));
        for (int i=0; i <= CALIBRATECOUNT; i++, j+=2) {
            assertTrue(     frep.lock  ("Another User",          "OtherFile.txt"));
            writeToStream(  frep.create("Another User"   , null, "OtherFile.txt"));
            assertTrue(     frep.unlock("Another User",          "OtherFile.txt"));
            assertEquals(j, frep.delete("Another Looser" ,       "OtherFile.txt", NOFORCE));
        }
        /* long time2 = */ logTime("Repository create/delete (Output)\t");
    
        // assertTrue("Input  slower than expected", time1 <= (createDelTime * FACTOR_EX));
        // assertTrue("Output slower than expected", time2 <= (createDelTime * FACTOR_EX));
        
    }
    
    /**
     *  Test Information found in the Repository.
     */
    public void testInfo() throws Exception {
        
		FileRepository frep = repository(TESTREPOSITORY1);

        // First create some crude Versioned Entries ...
        assertTrue(frep.mkdir(null     , "+x-"));
        assertTrue(frep.mkdir("+x-"    , "´k`"));
        
        assertEquals(1, frep.create("Mörental", "+x-/´k`", "Dadei", getStream()));
        EntryInfo eInfo =  (EntryInfo)frep.getInformation("+x-/´k`/Dadei");
        assertTrue(eInfo.isLocked());        
        assertTrue(eInfo.isInUpdate());
        assertTrue(frep.unlock("Mörental", "+x-/´k`/Dadei"));        
        assertTrue(     frep.lock(  "Rübezähl", "+x-/´k`/Dadei"));        
    	assertEquals(2, frep.create("Rübezähl", "+x-/´k`", "Dadei", getStream()));
        assertTrue(     frep.unlock("Rübezähl", "+x-/´k`/Dadei"));        
    	assertEquals(3, frep.delete("Zack!Bumm","+x-/´k`/Dadei", NOFORCE));
        assertTrue(     frep.lock(  "#,;-)ugh", "+x-/´k`/Dadei"));        
    	assertEquals(4, frep.create("#,;-)ugh",  "+x-/´k`", "Dadei", getStream()));
        
        String path1 = "+x-/´k`";
        String path2 = "+x-/´k`/Dadei";

        eInfo =  (EntryInfo) frep.getInformation("+x-/´k`/Dadei");
        assertEquals("Dadei"            , eInfo.getName());
        // String syspath = "+x-" + File.separator + "´k`" + File.separator +  "Dadei";
        // assertEquals(syspath            , eInfo.getSystemPath());
        assertEquals(path1              , eInfo.getParentPath());
        assertEquals(path2              , eInfo.getPath());
        assertEquals("#,;-)ugh"         , eInfo.getAuthor());
        assertEquals(4                  , eInfo.getNumVersions ("#,;-)ugh"));
        assertEquals(3                  , eInfo.getNumVersions (null));
                                          eInfo.getLastModified();    
        assertEquals(TEST_TEXT.length() , eInfo.getSize());
        assertTrue  ( eInfo.getIsEntry    ());
        assertTrue  (!eInfo.getIsContainer());
        assertTrue  (!eInfo.isDeleted  ());
        

        /** Return Number of Versions */
		VersionInfo top = eInfo.getLastVersionInfo();

        assertEquals(TEST_TEXT.length() , top.getSize());
        assertEquals("#,;-)ugh"         , top.getAuthor());
        assertTrue  (!top.isDeleted  ());
        
		VersionInfo thre = eInfo.getVersionInfo(3);

        // One cannot assert on the length of a deleted File ...
        // assertEquals(TEST_TEXT.length() , thre.getSize());
        assertEquals("Zack!Bumm"        , thre.getAuthor());
        assertTrue  (thre.isDeleted  ());

        assertTrue  (frep.unlock(  "#,;-)ugh", "+x-/´k`/Dadei"));        
    }

    /**
     *  Test Locking and Unlocking.
     */
    public void testLocking() throws Exception {
        
		FileRepository frep = repository(TESTREPOSITORY1);
        EntryInfo       eInfo;
        
        // First create some crude Versioned Entries ...
        assertTrue(frep.mkdir(null     , "äöü"));
        assertTrue(frep.mkdir("äöü"    , "--]"));
        
        assertEquals(1, frep.create("Gruber"        , "äöü/--]", "dada", getStream()));
        assertTrue(     frep.unlock("Gruber",         "äöü/--]/dada"));
        assertTrue(     frep.lock  ("Fäller",         "äöü/--]/dada"));
        writeToStream(frep.create("Fäller"          , "äöü/--]", "dada"));
        assertEquals(2, frep.create("Fäller"        , "äöü/--]", "dada", getStream()));
        assertTrue(     frep.unlock("Fäller"        , "äöü/--]/dada"));
        assertEquals(3, frep.delete("Reinfall"      , "äöü/--]/dada", NOFORCE));
        assertTrue(frep.lock("Blödmensch"           , "äöü/--]/dada"));
        try {
            frep.create("Zisch~Knall"   , "äöü/--]", "dada", getStream());
            fail("Must not create new entry for locked file with wrong user");
        }
        catch (RepositoryException expected) { /* expected */  }
        assertEquals(4, frep.create("Blödmensch" , "äöü/--]", "dada", getStream()));
        assertTrue(     frep.unlock("Blödmensch" , "äöü/--]/dada"));
        eInfo =  (EntryInfo) frep.getInformation("äöü/--]/dada");
        assertTrue(!eInfo.isLocked());
        assertNull(eInfo.getLocker());
        assertTrue(frep.lock("Zisch~Knall",             "äöü/--]/dada"));
        eInfo =  (EntryInfo)frep.getInformation("äöü/--]/dada");
        assertTrue(eInfo.isLocked());
        assertEquals("Zisch~Knall", eInfo.getLocker());
        assertTrue(!frep.unlock("Darf leider nicht",    "äöü/--]/dada"));
        assertTrue(frep.unlock("Zisch~Knall",           "äöü/--]/dada"));
        eInfo =  (EntryInfo)frep.getInformation("äöü/--]/dada");
        assertTrue(!eInfo.isLocked());
        assertNull(eInfo.getLocker());
    }
        
	/**
	 *  Test creation an deletion of empty and non-empty directories.
	 */
	public void testDirectories() throws Exception {
	    
		FileRepository frep = repository(TESTREPOSITORY1);

	    // First try create / delete with empty dirs ...
	    assertTrue(frep.mkdir(null     , "blah"));
		assertTrue(frep.mkdir("blah"   , "blub"));
		assertTrue(!frep.rmdir("blah"));
		assertTrue(frep.exists("blah/blub"));
		assertTrue(frep.rmdir("blah/blub"));
		assertTrue(frep.rmdir("blah"));

	    // create / delete with "evil" names
	    assertTrue(frep.mkdir(null     , "_blah_"));
	    assertTrue(frep.mkdir("_blah_" , "_blub_"));
	    assertTrue(!frep.rmdir("_blah_"));
	    assertTrue(frep.exists("_blah_/_blub_"));
	    assertTrue(frep.rmdir("_blah_/_blub_"));
	    assertTrue(frep.rmdir("_blah_"));
    
    	// create / delete with (not-deleted) file inside
    	assertTrue(frep.mkdir(null     , "~blah#"));
    	assertTrue(frep.mkdir("~blah#" , "}blub{"));
    	assertEquals(1, frep.create("§Pengß", "~blah#/}blub{", "Pfeil", getStream()));
        assertTrue(     frep.unlock("§Pengß", "~blah#/}blub{/Pfeil"));
    	assertTrue(frep.exists("~blah#/}blub{/Pfeil"));
    	assertTrue(!frep.rmdir("~blah#/}blub{"));
    	assertEquals(2, frep.delete("&Porks$", "~blah#/}blub{/Pfeil", NOFORCE));
    	assertTrue(frep.rmdir("~blah#/}blub{"));
    	assertTrue(frep.rmdir("~blah#"));
        // Recreating Subdirectory  hidden by deleted parent must fail
        assertTrue(!frep.mkdir("~blah#" , "}blub{"));
        
        // This should ressurrect the Parent and the child
        assertTrue(frep.mkdir(null     , "~blah#"));
        assertTrue(frep.mkdir("~blah#" , "}blub{"));
	}

    /**
     *  Test Mixing of files / folder with evil names and wrong usage.
     *
     *  This fucntion knows some evil files that still should be handled
     *  correctly by the implementation.
     */
    public void testConfusion() throws Exception {
        
		FileRepository frep = repository(TESTREPOSITORY1);
    
        assertTrue(frep.mkdir(null     , "_file"));
        assertTrue(frep.mkdir("_file" , "_dir"));
        assertEquals(1, frep.create("§Pengß", "_file/_dir", "_file"     , getStream()));
        assertEquals(1, frep.create("Prok}" , "_file/_dir", "_dir"      , getStream()));
        assertEquals(1, frep.create("{Flork", "_file/_dir", "_0_version", getStream()));
        
        try {
            frep.create("_0_user"   , "_file", "_dir", getStream());
            fail("Cannot create file where there is a directory");
        }
        catch (RepositoryException expected) { /* expected */  }
    
        try {
            frep.delete("&Porks$", "_file/_dir", NOFORCE);
            fail("Cannot delete() a directory");
        }
        catch (RepositoryException expected) { /* expected */  }
        
        try {
            frep.mkdir("_file/_dir", "_file");
            fail("Cannot create dir where there is a file");
        }
    
        catch (RepositoryException expected) { /* expected */ }
        try {
            frep.rmdir("_file/_dir/_file");
            fail("Cannot rmdir() a file");
        }
        catch (RepositoryException expected) { /* expected */ }
    }

    /**
     *  Testig the GetEntries function.
     *
     *  This function knows some evil files that still should be handled
     *  correctly by the implementation.
     */
    public void testGetEntries() throws Exception {
        
		FileRepository frep = repository(TESTREPOSITORY1);
        assertTrue(frep.mkdir(""         , "entryDir"));
        assertTrue(frep.mkdir("entryDir" , "dir1"));
        
        // Provoke "deleted" directory "dir2"
        assertTrue(frep.mkdir("entryDir" , "dir2"));
        assertEquals(1, frep.create("User1", "entryDir/dir2", "file1"     , getStream()));
        assertTrue(     frep.unlock("User1", "entryDir/dir2/file1"));
        assertEquals(2, frep.delete("User2", "entryDir/dir2/file1", NOFORCE));
        assertTrue(frep.rmdir("entryDir/dir2"));
        
        assertEquals(1, frep.create("User1", "entryDir", "file1"     , getStream()));
        assertTrue(     frep.unlock("User1", "entryDir/file1"));
        assertTrue(     frep.lock  ("User1", "entryDir/file1"));
        assertEquals(2, frep.create("User1", "entryDir", "file1"     , getStream()));
        assertTrue(     frep.unlock("User1", "entryDir/file1"));
        assertEquals(1, frep.create("User2", "entryDir", "file2"     , getStream()));
        assertTrue(     frep.unlock("User2", "entryDir/file2"));
        assertEquals(2, frep.delete("User2", "entryDir/file2", NOFORCE));
        
        Collection col = frep.getEntries("entryDir");
        
        assertTrue(col.contains(frep.getInformation("entryDir/dir1" )));
        assertTrue(col.contains(frep.getInformation("entryDir/dir2" )));
        assertTrue(col.contains(frep.getInformation("entryDir/file1")));
        assertTrue(col.contains(frep.getInformation("entryDir/file2")));
    }
    
    /**
     *  Same as GetEntries but using two FileRepositories.
     *
     *  This is neede for distibuted Repositories shared via
     *  a FileSystem.
     */
    public void testGetEntries2() throws Exception {
        
		FileRepository frep1 = repository(TESTREPOSITORY1);
		FileRepository frep2 = repository(TESTREPOSITORY2);
        
        assertTrue(frep1.mkdir(""         , "entryDir2"));
        assertTrue(frep2.mkdir("entryDir2" , "dir1"));
        
        // Provoke "deleted" directory "dir2"
        assertTrue(frep1.mkdir("entryDir2" , "dir2"));
        assertEquals(1, frep2.create("User1", "entryDir2/dir2", "file1"     , getStream()));
        assertTrue(     frep1.unlock("User1", "entryDir2/dir2/file1"));
        assertEquals(2, frep2.delete("User2", "entryDir2/dir2/file1", NOFORCE));
        assertTrue(frep1.rmdir("entryDir2/dir2"));
        
        assertEquals(1, frep2.create("User1", "entryDir2", "file1"     , getStream()));
        assertTrue(     frep1.unlock("User1", "entryDir2/file1"));
        assertTrue(     frep2.lock  ("User1", "entryDir2/file1"));
        assertEquals(2, frep1.create("User1", "entryDir2", "file1"     , getStream()));
        assertTrue(     frep2.unlock("User1", "entryDir2/file1"));
        assertEquals(1, frep1.create("User2", "entryDir2", "file2"     , getStream()));
        assertTrue(     frep2.unlock("User2", "entryDir2/file2"));
        assertEquals(2, frep1.delete("User2", "entryDir2/file2", NOFORCE));
        
        Collection col = frep2.getEntries("entryDir2");
        
        assertTrue(col.contains(frep1.getInformation("entryDir2/dir1" )));
        assertTrue(col.contains(frep2.getInformation("entryDir2/dir2" )));
        assertTrue(col.contains(frep1.getInformation("entryDir2/file1")));
        assertTrue(col.contains(frep2.getInformation("entryDir2/file2")));
    }

    /**
     *  Test Repository with work Area.
     */
    public void testWorkArea() throws Exception {
        
		FileRepository frep = repository(TESTREPOSITORY1, TESTWORKAREA1);
    
        File theFile = new File (TESTWORKAREA1,"tarau");
        assertEquals(1, frep.create("Ännchen",  null, "tarau", getStream()));
        assertTrue  (   frep.unlock("Ännchen",        "tarau"));
        assertTrue(theFile.exists());
        assertTrue(theFile.isFile());
        assertTrue  (   frep.lock  ("Gretel",         "tarau"));
        writeToStream(  frep.create("Gretel"  , null, "tarau"));
        assertTrue  (   frep.unlock("Gretel",         "tarau"));
        assertTrue(theFile.exists());
        assertTrue(theFile.isFile());
        assertEquals(3, frep.delete("Hänsel"        , "tarau", NOFORCE));
        assertTrue(!theFile.exists());
    
        assertTrue(frep.mkdir(""     , "Ordna"));
        File subdir = new File (TESTWORKAREA1,"Ordna");
        assertTrue(subdir.exists());
        assertTrue(subdir.isDirectory());
        theFile = new File (subdir,"so far");
        assertEquals(1, frep.create("Ännchen",  "Ordna", "so far", getStream()));
        assertTrue  (   frep.unlock("Ännchen",  "Ordna/so far"));
        assertTrue(theFile.exists());
        assertTrue(theFile.isFile());
        assertTrue  (   frep.lock  ("Gretel" , "Ordna/so far"));
        writeToStream(  frep.create("Gretel" , "Ordna", "so far"));
        assertTrue  (   frep.unlock("Gretel" , "Ordna/so far"));
        assertTrue(theFile.exists());
        assertTrue(theFile.isFile());
        assertEquals(3, frep.delete("Hexe"   , "Ordna/so far", NOFORCE));
        assertTrue(!theFile.exists());
        assertTrue(frep.rmdir("Ordna"));
        assertTrue(!subdir.exists());
        assertTrue(frep.mkdir(""     , "Ordna"));
        assertTrue(subdir.exists());
        assertTrue  (   frep.lock  ("Jäger" , "Ordna/so far"));
        assertEquals(4, frep.create("Jäger" , "Ordna", "so far", getStream()));
        assertTrue  (   frep.unlock("Jäger" , "Ordna/so far"));
        assertTrue(theFile.exists());	    
            
        FileUtilities.deleteR(TESTWORKAREA1);
        TESTWORKAREA1.mkdir();
        frep.createWorkarea(""); // ReCreate ...
    
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        startTime();
        int j = 1;
        assertEquals(j++, frep.create("Usa",  "", "someOtherFile.txt", getStream()));
        assertTrue  (     frep.unlock("Usa",      "someOtherFile.txt"));
        for (int i=0; i <= CALIBRATECOUNT; i++) {
            assertTrue  (     frep.lock  ("Usa",      "someOtherFile.txt"));
            assertEquals(j++, frep.create("Usa",  "", "someOtherFile.txt", getStream()));
            assertTrue  (     frep.unlock("Usa",      "someOtherFile.txt"));
            assertEquals(j++, frep.delete("Losa"    , "someOtherFile.txt", NOFORCE));
        }
        /* long time1 = */ logTime("Repository create/delete (Input,  with WorkArea)\t");
        
        j = 3;
        writeToStream(  frep.create("Another User"   , "", "AnotherFile.txt"));
        assertTrue   (  frep.unlock("Another User",        "AnotherFile.txt"));
        for (int i=0; i <= CALIBRATECOUNT; i++, j+=2) {
            assertTrue   (  frep.lock  ("Another User",        "AnotherFile.txt"));
            writeToStream(  frep.create("Another User"   , "", "AnotherFile.txt"));
            assertTrue   (  frep.unlock("Another User",        "AnotherFile.txt"));
            assertEquals(j, frep.delete("Another Looser" ,     "AnotherFile.txt", NOFORCE));
        }
        /* long time2 = */ logTime("Repository create/delete (Output, with WorkArea)\t");
        
        // assertTrue("Input with WorkArea slower than expected",  time1 <= (createDelTime  * FACTOR_CUM) );
        // assertTrue("Output with WorkArea slower than expected", time2 <= (createDelTime  * FACTOR_CUM) );
    }

    /**
     *  Test Repository with an Attic.
     */
    public void testAttic() throws Exception {
        
		FileRepository frep = repository(TESTREPOSITORY1, null, TESTATTIC1);
    
        assertEquals (1, frep.create("Bärbel" ,  null, "blah", getStream()));
        assertTrue   (   frep.unlock("Bärbel" ,        "blah"));
        assertTrue   (   frep.lock  ("Dimple" ,        "blah"));
    	writeToStream(   frep.create("Dimple" ,  null, "blah"));
        assertTrue   (   frep.unlock("Dimple" ,        "blah"));
        assertEquals (0, frep.delete("Kohl"          , "blah", NOFORCE));
    
        assertTrue(frep.mkdir(""     , "Kasten"));
        assertEquals (1, frep.create("Bärbel",  "Kasten", "go away", getStream()));
        assertTrue   (   frep.unlock("Bärbel" , "Kasten/go away"));
        assertTrue   (   frep.lock  ("Dimple" , "Kasten/go away"));
        writeToStream(   frep.create("Dimple" , "Kasten", "go away"));
        assertTrue   (   frep.unlock("Dimple" , "Kasten/go away"));
        assertEquals (0, frep.delete("Kohl"   , "Kasten/go away", NOFORCE));
            // After delete the folder exists in the Attic
        File atticdir = new File (TESTATTIC1,"Kasten");
        assertTrue(atticdir.exists());
        assertTrue(atticdir.isDirectory());
        File theFile = new File (atticdir,"go away");
        assertTrue(theFile.exists());
    
        assertTrue(frep.rmdir("Kasten"));
        assertTrue(frep.mkdir(""     , "Kasten"));
    
       // Version starts with 1 again since old versions where moved to the attic
        assertEquals(1, frep.create("Jäger",  "Kasten", "go away", getStream()));
        assertTrue  (   frep.unlock("Jäger" , "Kasten/go away"));
        assertEquals(0, frep.delete("Wolf" ,  "Kasten/go away", NOFORCE));
        
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        startTime();
        for (int i=0; i <= CALIBRATECOUNT; i++) {
            assertEquals(1, frep.create("Usa",  "", "someKasten.txt", getStream()));
            assertTrue  (   frep.unlock("Usa" ,     "someKasten.txt"));
            assertEquals(0, frep.delete("Losa"    , "someKasten.txt", NOFORCE));
        }
        /* long time1 = */ logTime("Repository create/delete (Input,  with Attic)\t");
    
        for (int i=0; i <= CALIBRATECOUNT; i++) {
            writeToStream(frep.create("Another User", "", "AnotherKasten.txt"));
            assertTrue   (frep.unlock("Another User",     "AnotherKasten.txt"));
            assertEquals (0, frep.delete("Another Looser","AnotherKasten.txt", NOFORCE));
        }
        /* long time2 = */ logTime("Repository create/delete (Output, with Attic)\t");
    
        // assertTrue("Input with Attic slower than expected",  time1 <= (createDelTime  * FACTOR_CUM) );
        // assertTrue("Output with Attic slower than expected", time2 <= (createDelTime  * FACTOR_CUM) );
        // not checked for now is slow but I dont know why :-(
    }

    /**
     *  The Cache is based on Weak Objects, try to confuse it with GCs.
     */
    public void testCache() throws Exception {
    
        Runtime rt = Runtime.getRuntime();
    
        Random rand = new Random(0xDEADBEAF);
        
		FileRepository frep = null;
    
        startTime();
        for (int i=0; i < 100; i++) {
            FileUtilities.deleteR(TESTREPOSITORY1);
            FileUtilities.deleteR(TESTWORKAREA1);
    
			frep = repository(TESTREPOSITORY1, TESTWORKAREA1);
    
            File theFile = new File (TESTWORKAREA1,"cachedFile");
            assertEquals(1, frep.create("Ännchen",  null, "cachedFile", getStream()));
            assertTrue(     frep.unlock("Ännchen",        "cachedFile"));
            if (rand.nextInt(16) == 0)    rt.gc();
    	    assertTrue(theFile.exists());
    	    assertTrue(theFile.isFile());
            assertTrue(     frep.lock  ("Gretel" ,       "cachedFile"));
    	    writeToStream(  frep.create("Gretel" , null, "cachedFile"));
            assertTrue(     frep.unlock("Gretel" ,       "cachedFile"));
            if (rand.nextInt(16) == 0)    rt.gc();
            assertTrue(theFile.exists());
            assertTrue(theFile.isFile());
            assertEquals(3, frep.delete("Hänsel"        , "cachedFile", NOFORCE));
            if (rand.nextInt(16) == 0)    rt.gc();
            assertTrue(!theFile.exists());
    
            assertTrue(frep.mkdir(""     , "cachedFlodder"));
            if (rand.nextInt(16) == 0)    rt.gc();
            File subdir = new File (TESTWORKAREA1,"cachedFlodder");
            assertTrue(subdir.exists());
            assertTrue(subdir.isDirectory());
            if (rand.nextInt(16) == 0)    rt.gc();
            theFile = new File (subdir,"so far");
            assertEquals(1, frep.create("Ännchen"   , "cachedFlodder", "so far", getStream()));
            assertTrue(     frep.unlock("Ännchen"   , "cachedFlodder/so far"));
            assertTrue(theFile.exists());
            assertTrue(theFile.isFile());
            assertTrue(     frep.lock  ("Gretel"   , "cachedFlodder/so far"));
            writeToStream(  frep.create("Gretel"   , "cachedFlodder", "so far"));
            assertTrue(     frep.unlock("Gretel"   , "cachedFlodder/so far"));
            if (rand.nextInt(16) == 0)    rt.gc();
            assertTrue(theFile.exists());
            assertTrue(theFile.isFile());
            assertEquals(3, frep.delete("Hexe"     , "cachedFlodder/so far", NOFORCE));
            if (rand.nextInt(16) == 0)    rt.gc();
            assertTrue(!theFile.exists());
            assertTrue(frep.rmdir(                  "cachedFlodder"));
            assertTrue(!subdir.exists());
            if (rand.nextInt(16) == 0)   rt.gc();
            assertTrue(frep.mkdir(""              , "cachedFlodder"));
            assertTrue(subdir.exists());
            assertTrue(     frep.lock  ("Jäger"   , "cachedFlodder/so far"));
            assertEquals(4, frep.create("Jäger"   , "cachedFlodder", "so far", getStream()));
            assertTrue(     frep.unlock("Jäger"   , "cachedFlodder/so far"));
            if (rand.nextInt(16) == 0)   rt.gc();
            assertTrue(theFile.exists());	    
        }
        logTime("Test Cache1");
        
        int j=1;
        assertEquals(j++, frep.create("Usa",  "", "cachedOtherFile.txt", getStream()));
        assertTrue(       frep.unlock("Usa"     , "cachedOtherFile.txt"));
        for (int k=0 ; k <= 0x10; k++) {
            if ((k & 0x01) != 0)    rt.gc();
            assertTrue(       frep. lock ("Usa"     , "cachedOtherFile.txt"));
            assertEquals(j++, frep.create("Usa",  "", "cachedOtherFile.txt", getStream()));
            assertTrue(       frep.unlock("Usa"     , "cachedOtherFile.txt"));
            if ((k & 0x02) != 0)    rt.gc();
            assertEquals(j++, frep.delete("Losa"    , "cachedOtherFile.txt", NOFORCE));
        }
        logTime("Test Cache2");
        j = 3;
        writeToStream(  frep.create("Another User" , "", "xAnotherFile.txt"));
        assertTrue(     frep.unlock("Another User" ,     "xAnotherFile.txt"));
        for (int k=0; k <= 0x10; k++, j+=2) {
            if ((k & 0x01) != 0)    rt.gc();
            assertTrue(     frep.lock  ("Another User" ,       "xAnotherFile.txt"));
            writeToStream(  frep.create("Another User"   , "", "xAnotherFile.txt"));
            assertTrue(     frep.unlock("Another User" ,       "xAnotherFile.txt"));
            if ((k & 0x02) != 0)    rt.gc();
            assertEquals(j, frep.delete("Another Looser" ,     "xAnotherFile.txt", NOFORCE));
        }
    	logTime("Test Cache3");
    }

    /**
     *  Worse than testCache: do it with two FileRepositories on the same data.
     */
    public void testCache2() throws Exception {
    
        Runtime rt = Runtime.getRuntime();
    
        Random rand = new Random(0xDEADBEAF);
        
		FileRepository frep1 = null;
		FileRepository frep2 = null;
    
        startTime();
        for (int i=0; i < 100; i++) {
            FileUtilities.deleteR(TESTREPOSITORY1);
            FileUtilities.deleteR(TESTWORKAREA1);
    
			frep1 = repository(TESTREPOSITORY1, TESTWORKAREA1);
			frep2 = repository(TESTREPOSITORY2, TESTWORKAREA2);
    
            File theFile = new File (TESTWORKAREA1,"cachedFile");
            assertEquals(1, frep1.create("Ännchen",  null, "cachedFile", getStream()));
            assertTrue(     frep2.unlock("Ännchen",        "cachedFile"));
            if (rand.nextInt(16) == 0)    rt.gc();
            assertTrue(theFile.exists());
            assertTrue(theFile.isFile());
            assertTrue(     frep2.lock  ("Gretel" ,       "cachedFile"));
            writeToStream(  frep1.create("Gretel" , null, "cachedFile"));
            assertTrue(     frep2.unlock("Gretel" ,       "cachedFile"));
            if (rand.nextInt(16) == 0)    rt.gc();
            assertTrue(theFile.exists());
            assertTrue(theFile.isFile());
            assertEquals(3, frep1.delete("Hänsel"        , "cachedFile", NOFORCE));
            if (rand.nextInt(16) == 0)    rt.gc();
            assertTrue(!theFile.exists());
    
            assertTrue(frep2.mkdir(""     , "cachedFlodder"));
            if (rand.nextInt(16) == 0)    rt.gc();
            File subdir = new File (TESTWORKAREA1,"cachedFlodder");
            assertTrue(subdir.exists());
            assertTrue(subdir.isDirectory());
            if (rand.nextInt(16) == 0)    rt.gc();
            theFile = new File (subdir,"so far");
            assertEquals(1, frep2.create("Ännchen"   , "cachedFlodder", "so far", getStream()));
            assertTrue(     frep1.unlock("Ännchen"   , "cachedFlodder/so far"));
            assertTrue(theFile.exists());
            assertTrue(theFile.isFile());
            assertTrue(     frep2.lock  ("Gretel"   , "cachedFlodder/so far"));
            writeToStream(  frep1.create("Gretel"   , "cachedFlodder", "so far"));
            assertTrue(     frep1.unlock("Gretel"   , "cachedFlodder/so far"));
            if (rand.nextInt(16) == 0)    rt.gc();
            assertTrue(theFile.exists());
            assertTrue(theFile.isFile());
            assertEquals(3, frep2.delete("Hexe"     , "cachedFlodder/so far", NOFORCE));
            if (rand.nextInt(16) == 0)    rt.gc();
            assertTrue(!theFile.exists());
            assertTrue(frep2.rmdir(                  "cachedFlodder"));
            assertTrue(!subdir.exists());
            if (rand.nextInt(16) == 0)   rt.gc();
            assertTrue(frep1.mkdir(""              , "cachedFlodder"));
            assertTrue(subdir.exists());
            assertTrue(     frep1.lock  ("Jäger"   , "cachedFlodder/so far"));
            assertEquals(4, frep2.create("Jäger"   , "cachedFlodder", "so far", getStream()));
            assertTrue(     frep1.unlock("Jäger"   , "cachedFlodder/so far"));
            if (rand.nextInt(16) == 0)   rt.gc();
            assertTrue(theFile.exists());       
        }
        logTime("Test Cache2-1");
        
        int j=1;
        assertEquals(j++, frep2.create("Usa",  "", "cachedOtherFile.txt", getStream()));
        assertTrue(       frep1.unlock("Usa"     , "cachedOtherFile.txt"));
        for (int k=0 ; k <= 0x10; k++) {
            if ((k & 0x01) != 0)    rt.gc();
            assertTrue(       frep2. lock ("Usa"     , "cachedOtherFile.txt"));
            assertEquals(j++, frep2.create("Usa",  "", "cachedOtherFile.txt", getStream()));
            assertTrue(       frep1.unlock("Usa"     , "cachedOtherFile.txt"));
            if ((k & 0x02) != 0)    rt.gc();
            assertEquals(j++, frep1.delete("Losa"    , "cachedOtherFile.txt", NOFORCE));
        }
        logTime("Test Cache2-2");
        j = 3;
        writeToStream(  frep1.create("Another User" , "", "xAnotherFile.txt"));
        assertTrue(     frep2.unlock("Another User" ,     "xAnotherFile.txt"));
        for (int k=0; k <= 0x10; k++, j+=2) {
            if ((k & 0x01) != 0)    rt.gc();
            assertTrue(     frep2.lock  ("Another User" ,     "xAnotherFile.txt"));
            writeToStream(  frep1.create("Another User" , "", "xAnotherFile.txt"));
            assertTrue(     frep2.unlock("Another User" ,     "xAnotherFile.txt"));
            if ((k & 0x02) != 0)    rt.gc();
            assertEquals(j, frep2.delete("Another Looser" ,   "xAnotherFile.txt", NOFORCE));
        }
        logTime("Test Cache2-3");
    }

	private FileRepository repository(File path) {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config(path));
	}

	private FileRepository repository(File path, File workarea) {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config(path, workarea));
	}

	private FileRepository repository(File path, File workarea, File attic) {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config(path, workarea, attic));
	}

	private Config<?> config(File path, File workarea, File attic) {
		Config<?> config = config(path, workarea);
		config.setAttic(path(attic));
		return config;
	}

	private Config<?> config(File path, File workarea) {
		Config<?> config = config(path);
		config.setWorkarea(path(workarea));
		return config;
	}

	private Config<?> config(File path) {
		Config<?> config = TypedConfiguration.newConfigItem(FileRepository.Config.class);
		config.setPath(path(path));
		return config;
	}

	private String path(File path) {
		if (path == null) {
			return null;
		}
		return path.getAbsolutePath();
	}

	/**
	 *  Method added last to remove the whole mess we created
	 */
	public void doCleanup()
    {
        if (TESTREPOSITORY1.exists())
	       assertTrue ("Unable to remove " + TESTREPOSITORY1, 
	                   FileUtilities.deleteR(TESTREPOSITORY1));
        if (TESTWORKAREA1.exists())
	       assertTrue ("Unable to remove " + TESTWORKAREA1, 
	                    FileUtilities.deleteR(TESTWORKAREA1));
        if (TESTATTIC1.exists())
        	assertTrue ("Unable to remove " + TESTATTIC1, 
	                   FileUtilities.deleteR(TESTATTIC1));
	}

    /**
     * The method constructing a test suite for this class.
     *
     * @return    The test to be executed.
     */
    public static Test suite () {

        TestSuite suite = new TestSuite ();
		suite.addTest(new TestFileRepository("doCleanup"));
        suite.addTest(new TestFileRepository("calibrate"));

		suite.addTest(new TestFileRepository("doCleanup"));
        suite.addTest(new TestSuite (TestFileRepository.class));
        // suite.addTest(new TestFileRepository("testDirectories"));

        suite.addTest(new TestFileRepository("doCleanup"));
        
        return DSATestSetup.createDSATestSetup(suite);
    }

    /**
     * The main program for executing this test also from console.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        
        SHOW_TIME = true;
    
 	    TestRunner.run (suite ());
  
    }
	
}
