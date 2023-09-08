/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.reporting.office.ReportResult;


/**
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ReportResultTest extends TestCase {

    
    public void testConstructor () throws Exception {
        // first test the correct instanciation
        try{
			new ReportResult(true, (BinaryContent) null, null);
            fail ("construction of the object should have failed!");
        } catch (IllegalArgumentException ex) {
            // we expect an error here!
        }
        try{
			new ReportResult(false, (BinaryContent) null, null);
            fail ("construction of the object should have failed!");
        } catch (IllegalArgumentException ex) {
            // we expect an error here!
        }
        try{
			BinaryContent validFile = FileManager.getInstance().getData(CommonTestFiles.TEMPLATE_PPT);
            ReportResult theResult = new ReportResult (true, validFile,null);
			ReportResult error = new ReportResult(false, (BinaryContent) null, "Some error text here");
            assertNotNull("No result object found",theResult);
            assertNotNull("No result object found",error);
        } catch (IllegalArgumentException ex) {
            fail ("construction of the objects should be successfull!");
        }
    }
    public void testGetReportFile() throws Exception {
        String errorText = "useless text here";
		ReportResult theResult =
			new ReportResult(true, FileManager.getInstance().getData(CommonTestFiles.TEMPLATE_PPT), errorText);
        assertNotNull (theResult.getReportFile());
        assertTrue (!errorText.equals(theResult.getErrorMessage()));
    }

    public ReportResultTest (String arg) {
        super(arg);
    }
    /** Return the suite of Tests to perform */
    public static Test suite () {
        return OfficeTestSetup.createOfficeTestSetup(new TestSuite (ReportResultTest.class));
    }
 
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
