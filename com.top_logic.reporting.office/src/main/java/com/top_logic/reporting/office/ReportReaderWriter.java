/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;

import java.io.File;
import java.util.List;
import java.util.Locale;

import com.top_logic.basic.io.BinaryContent;

/**
 * This interface controls the access (read/write) to an office document/format.
 * Depending on the type of office document certain different implementations are necessary.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public interface ReportReaderWriter {
    
	public void init(String aTemplatePathName, Locale aLocale, File aGeneratedReportPath);
    /**
     * Make sure the ReaderWriter can operate properly. So at least the template
     * file must be present.
     * 
     * @param aTemplatePath the template document to use for reporting
     * @param aGeneratedReportPath the path to the report file
     */
	public void init(BinaryContent aTemplatePath, File aGeneratedReportPath);
    
    /**
     * Extract the ExpansionObjects in their symbol state from the template
     * document.
     * 
     * @return a List of {@link ExpansionObject}s in a not expanded state.
     */
    public List getSymbols ();
    
    /**
     * Write the given list of expansion objects to the report document by interpreting the
     * expanded content in the expansion object.
     * 
     * @param aListOfExpansionObjects the {@link ExpansionObject}s to write
     */
    public void writeExpansionObjects (List aListOfExpansionObjects);
      
    /**
     * Hook up for some postprocessing after writing the expanded symbols.
     *  
     * @param aHandler the ReportHandler is the best parameter we can think of.
     */
    public void finishReportWriting (ReportHandler aHandler);
    
    /**
     * This method is needed to create the ReportResult.
     * @return the explicit file path to the newly created document.
     */
	public File getResultFilePath();
}
