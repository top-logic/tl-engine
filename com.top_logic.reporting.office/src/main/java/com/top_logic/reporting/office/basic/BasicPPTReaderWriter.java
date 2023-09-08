/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office.basic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.ppt.Powerpoint;
import com.top_logic.base.office.ppt.PowerpointStyledString;
import com.top_logic.reporting.office.AbstractReportReaderWriter;
import com.top_logic.reporting.office.ExpansionObject;
import com.top_logic.reporting.office.ReportHandler;
import com.top_logic.reporting.office.ReportReaderWriter;
import com.top_logic.util.error.TopLogicException;


/**
 * Basic implementation example for a ReportReaderWriter for PPT Documents. This
 * reader writer assumes that no special handling is neccessary, to replace
 * symbols in ppt template with the expanded content in the generated report
 * file. Explicitly we are not able to handle multiple symbols in one ppt shape,
 * so enumerations with different symbols are not possible at the moment
 * 
 * @author <a href=mailto:jco@top-logic.com>jco</a>
 */
public class BasicPPTReaderWriter extends AbstractReportReaderWriter implements ReportReaderWriter {

    protected static final String PPT = ".ppt";
    /** the access from/to powerpoint is done by the OfficeAccess framework */
    private Powerpoint powerPointAccess;
    
    /**
     * This ReaderWriter uses the {@link BasicSymbolParser} to parse the fields of the
     * ppt file for symbols.
     */
    public BasicPPTReaderWriter (){
        super();
        symbolParser = new BasicSymbolParser();
    }
    
    /**
     * here we actually read the template file (via @see com.top_logic.base.office.Powerpoint)
     * and extract all fields in the template which have symbols.
     * 
     * @see com.top_logic.reporting.office.ReportReaderWriter#getSymbols()
     */
    @Override
	public List getSymbols() {
		powerPointAccess = Powerpoint.getInstance(Powerpoint.isXmlFormat(templateFile));
        List theSymbols = null;
        try {
			Map fields = powerPointAccess.getValues(templateFile.getStream());
            theSymbols = createExpansionObjects(fields);
        }
		catch (OfficeException | IOException exp) {
            throw new TopLogicException(this.getClass(),"getSymbols",null,exp);
        }
        return theSymbols;
    }

    /**
     * here we get the expanded objects and write them into the newly to create file!
     * 
     * TODO JCO/MGA interpret stylesheet infos here!
     * @see com.top_logic.reporting.office.ReportReaderWriter#writeExpansionObjects(java.util.List)
     */
    @Override
	public void writeExpansionObjects(List aListOfExpansionObjects) {
        try {
			powerPointAccess = Powerpoint.getInstance(Powerpoint.isXmlFormat(templateFile));
			final InputStream template = templateFile.getStream();
            try {
            	powerPointAccess.setValues(template,reportFile,convertExpansionObjectsToMap(aListOfExpansionObjects));
			} finally {
				template.close();
			}
        }
        catch (Exception exp) {
            throw new TopLogicException(this.getClass(),"writeExpansionObjects",null,exp);
        }
       
    }
    /**
     * No postprocessing here!
     * @see com.top_logic.reporting.office.ReportReaderWriter#finishReportWriting(com.top_logic.reporting.office.ReportHandler)
     */
    @Override
	public void finishReportWriting(ReportHandler aHandler) {
        //noop
    }
    /**
     * @see com.top_logic.reporting.office.ReportReaderWriter#getResultFilePath()
     */
    @Override
	public File getResultFilePath() {
        return reportFile;
    }

    /**
     * To write to the report file we must convert the List of expanded ExpansionObjects 
     * to a map where the key is the original field content and the value is the new content, to 
     * satisfy the interface of {@link Powerpoint}
     * 
     * @param aListOfExpansionObjects the expanded objects
     * @return a Map with [String | String] pairs
     */
    protected Map convertExpansionObjectsToMap (List aListOfExpansionObjects) {
        Iterator iter = aListOfExpansionObjects.iterator();
        Map result = new HashMap (aListOfExpansionObjects.size());
        while (iter.hasNext()) {
            ExpansionObject current = (ExpansionObject) iter.next();
            String mapKey = current.getFieldContent();
            Object mapValue = replaceSymbolWithValue(current);
            result.put(mapKey,mapValue);
        }
        return result;
    }
    
    /**
	 * We evaluate the expanded object and return the expanded. In case the expanded object is a
	 * string there is special handling for replacement. We replace only the part of the field
	 * content representing the symbol, e.g.:
	 * <code>'&lt;exp type=static>NAME&lt;/exp> was here'</code> would convert to
	 * <code>'Kilroy was here'</code>
	 * 
	 * @param anObject
	 *        the ExpansionObject to get the information from.
	 * @return the String representation of the expanded object or the old field value.
	 */
    protected Object replaceSymbolWithValue (ExpansionObject anObject) {
        if (anObject.isExpanded()) {
            Object expanded = anObject.getExpandedObject();
            if (expanded instanceof String) {
                // we use the PowerpointStyledString class to propagate the expanded content.
                return new PowerpointStyledString (replaceSymbolInText(anObject.getFieldContent (),anObject.getSymbol(),(String)expanded),anObject.getContentStyle());
            } else {
                return expanded;
            }
        } else {
            return anObject.getFieldContent();
        }
    }
    
    @Override
	protected String getTemplateSuffix () {
        return PPT;
    }
}
