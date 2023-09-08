/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.resource;

import java.awt.Frame;
import java.awt.Image;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.util.resource.ImageManager;


/**
 * Tests for ImageManager, i.e. inner DefaultImageManager.
 * getFile and getInputStream is not tested as it is only 
 * redirection to FileManager-Singleton that may be differently initialized
 * than super of DefaultImageManager. 
 *
 * @author   <a href="mailto:dkh@top-logic.com">Dirk K&ouml;hlhoff</a>
 */
public class TestImageManager extends BasicTestCase {


    /** Number of time to repeat the test when measutring Speed */
    private static final int SPEED_COUNT = 1;

    /** reset to default FileMgr after usage */
    private FileManager  oldFileMgr;

    private FileManager  fileMgr_srcTestDir;
    
	private static final String IMAGE_NAME0              = "TestPic.png";
    private static final String IMAGE_NAME1              = "maus.jpg";
    private static final String IMAGE_NAME2              = "test.png";
    
    private static final String[] IMAGES                = 
        {IMAGE_NAME0, IMAGE_NAME1, IMAGE_NAME2};
    

	private static final String IMAGE_PATH_FROM_ROOT =
		ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/util/resource/";

	private static final String IMAGE_PATH_FROM_FileMGR = "com/top_logic/util/resource/";
	private static final String IMAGE_FILEDESCRIPTOR    = "file://";
	private static final String IMAGE_NOT_THERE         = "testkaes.png";
	
	private static final Frame  NO_OBSERVER = null;

	/** 
	 * Constructor for a special test.
	 *
	 * @param aName function name of the test to execute.
	 */
	public TestImageManager (String aName) {
		super (aName);
	}
	
    /**
     * initialize 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
		fileMgr_srcTestDir = new DefaultFileManager("./" + ModuleLayoutConstants.SRC_TEST_DIR + "/test");
        oldFileMgr = FileManager.getInstance();
        FileManager.setInstance(fileMgr_srcTestDir);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
	protected void tearDown() throws Exception {
        super.tearDown();
        FileManager.setInstance(oldFileMgr);
        fileMgr_srcTestDir  = null;
        
    }   

	/**
	 * tests loading image with path specified as file://...
	 */
	public void testGetImage_File() throws Exception {
		
		Image fetchedImage;
		
		//complete relative path
		fetchedImage = ImageManager.getImage(IMAGE_FILEDESCRIPTOR + IMAGE_PATH_FROM_ROOT + IMAGE_NAME0);
		assertTrue(-1 < fetchedImage.getHeight( NO_OBSERVER));
		
		//file !exist 
		assertNull(ImageManager.getImage(IMAGE_FILEDESCRIPTOR + IMAGE_NOT_THERE));
		
	}
	
	/**
	 * Tets How long loading of Images takes. 
	 */
	public void testSpeed() throws Exception {
        
        int size = IMAGES.length;
        Image images[] = new Image[size];
        
        startTime();        
        for (int j=0; j < SPEED_COUNT; j++) {
            
            for (int i=0; i < size; i++) {
                images[i] = ImageManager.getImage(IMAGE_PATH_FROM_FileMGR + IMAGES[i]);
            }
    
            for (int i=0; i < size; i++) {
            	assertNotNull("No image. i=" + i, images[i]);
                int h = images[i].getHeight( NO_OBSERVER);
                assertTrue(h > 0);
            }
        }
        logTime("Loading " + size + " images " + SPEED_COUNT + " Times");
	}
	
    /**
     * tests loading image with path specified relative 
     */
    public void testGetImage_Relative() throws Exception {

        Image fetchedImage;
        
        //file exists
        fetchedImage = ImageManager.getImage(IMAGE_PATH_FROM_FileMGR + IMAGE_NAME0);
        assertTrue(-1 < fetchedImage.getHeight( NO_OBSERVER));
                
        //file !exist
        assertNull(ImageManager.getImage(IMAGE_PATH_FROM_FileMGR + IMAGE_NOT_THERE));

        
        //file !exist in this path
        assertNull(ImageManager.getImage(IMAGE_NAME0));
            
    }
	
	/**
	 * Return the suite of tests to perform. 
	 */
	public static Test suite () {
		return TLTestSetup.createTLTestSetup(TestImageManager.class);
        // return new TestImageManager("testSpeed");
	}

	public static void main(String[] args) {
		SHOW_TIME             = true;     // for debugging
		Logger.configureStdout();
		junit.textui.TestRunner.run(suite());
	}
	
}
