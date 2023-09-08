/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.util.error.TopLogicException;


/**
 * Abstract superclass for ReportReaderWriter.
 * This class simply provides some utility methods for the concrete subclasses.
 * The init methods are implemented here with a special hook up to the subclasses.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public abstract class AbstractReportReaderWriter implements ReportReaderWriter {

    /** the separator is used when contatinating the right file name for the template with regard to the locale */
    private static final String SEP = "_";
    
    /** the specific Symbol parser */
    protected SymbolParser symbolParser;
    
    /** the template to use*/
	protected BinaryContent templateFile;
    
    /** the destination of the report */
	protected File reportFile;

    /**
     * Replace a symbol inside a text with a given value
     * @param aText     the original text
     * @param aSymbol   the symbol to replace
     * @param aValue    the value to insert for the symbol
     * @return          the modified text where the symbol was replaced by the value.
     */
    protected String replaceSymbolInText(String aText, String aSymbol, String aValue) {
        StringBuffer result = new StringBuffer ();
        int symbolStart = aText.indexOf(aSymbol);
        result.append (aText.substring(0,symbolStart));
        result.append (aValue);
        result.append (aText.substring(symbolStart+aSymbol.length()));
        return result.toString();
    }

    /**
     * The reading and creation of the ExpansionObjects in their symbol state is
     * done by using the SymbolParser for each field found in the template file.
     * 
     * @param aFieldMap the fields from the template with [String|String] pairs.
     * @return a List of ExpansionObjects in symbol state for all fields which
     *         the parser found a symbol content for
     */
    protected List createExpansionObjects(Map aFieldMap) {
        if (symbolParser == null) {
            throw new IllegalStateException ("a SymbolParser is needed to create the expansion objects!");
        }
        List result = new ArrayList(aFieldMap.size());
        Iterator iter = aFieldMap.keySet().iterator();
        while (iter.hasNext()) {
            String fieldKey = (String) iter.next ();
            String fieldContent = (String) aFieldMap.get(fieldKey);
            ExpansionObject current = symbolParser.parse(fieldKey,fieldContent);
            if (current != null) {
                result.add(current);
            }
        }
        return result;
    }

    /**
     * Resolve the template file by using the locale.
     * The strategy to find the right file here is : 
     * 1. templatePath + nameprefix + '_' + localeString + namesuffix
     * 2. templatePath + nameprefix + '_' + localeLanguage + namesuffix
     * 3. templatePathName
     * 
     * @see com.top_logic.reporting.office.ReportReaderWriter#init(java.lang.String, java.util.Locale, java.io.File)
     */
    @Override
	public void init(String aTemplatePathName, Locale aLocale, File aGeneratedReportPath) {
        String theSuffix = getTemplateSuffix ();
        if (aTemplatePathName == null || !aTemplatePathName.endsWith(theSuffix)) {
            throw new IllegalArgumentException ("not a valid template file name provided, it must end with " + theSuffix + "!");
        }
        String prefix = aTemplatePathName.substring(0,aTemplatePathName.indexOf(theSuffix)) + SEP;
		BinaryContent templateFile = null;
        try{
			templateFile = FileManager.getInstance().getDataOrNull(prefix + aLocale.toString() + theSuffix);
            if (templateFile == null) {
				templateFile = FileManager.getInstance().getDataOrNull(prefix + aLocale.getLanguage() + theSuffix);
            }
            if (templateFile == null) {
				templateFile = FileManager.getInstance().getDataOrNull(aTemplatePathName);
            }
            if (templateFile == null) {
                throw new IllegalArgumentException ("no template file was found!");
            }
        } catch (Exception e) {
            throw new TopLogicException (this.getClass(),".fileManagerError",null,e);
        }
        init (templateFile,aGeneratedReportPath);
        
    }

    /**
	 * we simply check if the precondition for reporting are fullfilled and store the given files in
	 * attributes for further use!
	 * 
	 * @see com.top_logic.reporting.office.ReportReaderWriter#init(BinaryContent, java.io.File)
	 */
    @Override
	public void init(BinaryContent aTemplatePath, File aGeneratedReportPath) {
		if (aTemplatePath == null) {
            throw new IllegalArgumentException ("not a valid template file provided");
        }
        if (aGeneratedReportPath != null && aGeneratedReportPath.exists() && aGeneratedReportPath.isFile()) {
            Logger.warn ("report already existing, overwriting now",this);
        }
        templateFile = aTemplatePath;
        reportFile = aGeneratedReportPath;
    }
    
    /**
     * the right suffix for the template file as specified by the subclass
     */
    protected abstract String getTemplateSuffix ();

}
