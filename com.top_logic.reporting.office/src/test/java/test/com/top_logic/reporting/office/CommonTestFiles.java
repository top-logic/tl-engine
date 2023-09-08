/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office;

import java.io.File;
import java.io.IOException;

import com.top_logic.base.office.ppt.Powerpoint;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Settings;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;


/**
 * Constants used for generating the names and paths of the test files.
 * <br/>
 * The files are copied to a temp directory, as they are changed by the tests.
 * <b>They are deleted when the VM shuts down.</b>
 * <br/>
 * <b>The files defined here must not have identical names, even if they have different paths.</b>
 * They are all copied to a temp directory and their names would clash there.
 * 
 * @see #AUTO_CLEANUP
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class CommonTestFiles {
	
	/** Should the temporary files be deleted when the VM shuts down? */
	public static final boolean AUTO_CLEANUP = true;
	
	private static final String ORIGINAL_TEMPLATE_PPT = "/templates/ppt/pptTemplate.ppt";
	public static final String TEMPLATE_PPT = copyFileToTemp(ORIGINAL_TEMPLATE_PPT);
	
	public static final String RESULT_PREFIX_PPT = "tmp/pptResult";
	
	public static final String RESULT_SUFFIX_PPT =".ppt";
	
	private static final String ORIGINAL_RESULT_FILE = RESULT_PREFIX_PPT + RESULT_SUFFIX_PPT;
	public static final String RESULT_FILE = ORIGINAL_RESULT_FILE;
	
	public static final File getResultFile() {
		return new File(RESULT_FILE);
	}
	
	private static String copyFileToTemp(String resourceName) {
		try {
			BinaryData originalFile = FileManager.getInstance().getData(resourceName);
			File tempCopy = File.createTempFile(CommonTestFiles.class.getSimpleName(), Powerpoint.PPT_EXT,
				Settings.getInstance().getTempDir());
			FileUtilities.copyToFile(originalFile, tempCopy);
			handleTempFile(tempCopy);
			return "file://" + tempCopy.getCanonicalPath();
		}
		catch (IOException exception) {
			throw new RuntimeException("Initializing " + CommonTestFiles.class + " failed: " + exception.getMessage(), exception);
		}
	}

	public static File createUniqueResultFile(String theID) {
		File tempFile = new File(RESULT_PREFIX_PPT + '_' + 
	                    System.currentTimeMillis() + '_' + 
	                    theID + 
	                    RESULT_SUFFIX_PPT);
		handleTempFile(tempFile);
		return tempFile;
	}
	
	/** Calls {@link File#deleteOnExit()} if {@link #AUTO_CLEANUP} is enabled. */
	public static void handleTempFile(File tempFile) {
		if (AUTO_CLEANUP) {
			tempFile.deleteOnExit(); // Clean up when the tests are done.
		}
	}
	
}
