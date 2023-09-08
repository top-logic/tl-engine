/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;

import java.io.File;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileBasedBinaryContent;


/**
 * A container to store the results of the report generation.
 * In most cases this is a Document reference, indicating the generated reports location.
 * 
 * TODO JCO for now we just return a File object.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ReportResult {

    /**
     * <code>true</code> if and only if the reportHandler finished a report
     * sucessfully and a valid report document is generated
     */
    private boolean successfull;

    /** must not be <code>null</code> if the report was successfull */
	private BinaryContent generatedReport;

    /**
     * stores the error message when something was wrong, is <code>null</code>
     * if reporting was successfull
     */
    private String errorMessage;
    
    /** stores the exception which occurred during reporting, may be <code>null</code> */ 
    private Throwable exception; // TODO JCO never used ?

    /**
     * The constructor checks some constraints between flag, file path and error message.
     * 
     * @param wasSuccessfull    if <code>true</code> the result contains a usefull report file
     * @param aReportFile       the result of a report generation is a report file
     * @param anErrorMessage    if the generation was unsuccessfull an error message is provided.
     */
    public ReportResult (boolean wasSuccessfull, File aReportFile, String anErrorMessage) {
		this(wasSuccessfull, aReportFile == null ? null : FileBasedBinaryContent.createBinaryContent(aReportFile),
			anErrorMessage);
    }
 
	/**
	 * The constructor checks some constraints between flag, file path and error message.
	 * 
	 * @param wasSuccessfull
	 *        if <code>true</code> the result contains a usefull report file
	 * @param aReportFile
	 *        the result of a report generation is a report file
	 * @param anErrorMessage
	 *        if the generation was unsuccessfull an error message is provided.
	 */
	public ReportResult(boolean wasSuccessfull, BinaryContent aReportFile, String anErrorMessage) {
		if (wasSuccessfull && aReportFile == null)
            throw new IllegalArgumentException ("successull reporting needs a report file");
        if (!wasSuccessfull && StringServices.isEmpty (anErrorMessage))
             throw new IllegalArgumentException ("failed reporting needs a usefull error message");
        successfull = wasSuccessfull;
        generatedReport = aReportFile;
        errorMessage = anErrorMessage;
    }
    
    /**
	 * @see ReportResult#ReportResult(boolean, BinaryContent, String)
	 */
	public ReportResult(boolean wasSuccessfull, BinaryContent aReportFile, String anErrorMessage,
			Throwable anException) {
        this(wasSuccessfull,aReportFile,anErrorMessage);
        exception = anException;
    }
    
    /**
     * the generated report file or <code>null</code> if reporting was unsuccessull.
     */
	public BinaryContent getReportFile() {
        return successfull ? generatedReport : null;
    }
    
    /**
     * an error message if the reporting was unsuccessfull or <code>null</code>. 
     */
    public String getErrorMessage () {
        return !successfull ? errorMessage : "no Errors";
    }
    
    /**
     * an exception that may have been occurred during reporting, may be <code>null</code>
     */
    public Throwable getException () {
        return exception;
    }
}
