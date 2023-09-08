/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Export the values provided by the {@link AttributedSearchResultSet} to an HTML template.
 * 
 * That template needs a tag {@link #DATA_WORD}, where the values from the search result set
 * will be exported to. They will then be presented in a table which CSS classes can be
 * defined in the template itself.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class HTMLValueExporter {

    /** Name suffix of the extended template file. */
    public static final String TEMPLATE_FILE_SUFIX  = ".html";

    public static final Object DATA_WORD = "<export:data />";

    private final TLClass metaElement;

    private final List<String> columns;

    /** 
     * Creates a {@link HTMLValueExporter}.
     */
    public HTMLValueExporter(TLClass aME, List<String> someColumns) {
        this.metaElement = aME;
        this.columns     = someColumns;
    }

    /** 
     * Return the OfficeExportValueHolder for an extended / pivot export.
     * 
     * @return   The holder with all values for the excel sheet.
     */
    public OfficeExportValueHolder createExtendedExport(String anOption, String aTemplate, String aDownloadName, AttributedSearchResultSet aModel) {
		List<? extends TLObject> theValues = aModel.getResultObjects();
        OfficeExportValueHolder    theHolder = new OfficeExportValueHolder(null, null, null);
        String                     theName   = aDownloadName;

        if (!aDownloadName.endsWith(".html")) {
            theName = aDownloadName.substring(0, aDownloadName.lastIndexOf('.')) + ".html";
        }

        try {
            theHolder.myFile           = this.createHTMLExportFile(theValues, aTemplate + TLContext.getLocale().getLanguage() + TEMPLATE_FILE_SUFIX);
            theHolder.downloadFileName = theName;
        }
        catch (TopLogicException tx) {
            throw tx; // Rethrow, will probably be "to many rows ..."
        }
        catch (Exception ex) {
            Logger.error("Failed to createExtendedExport", ex, this);
        }
        
        return theHolder;
    }

	protected File createHTMLExportFile(List<? extends TLObject> someObjects, String aTemplate)
			throws FileNotFoundException, IOException {
		File theResult = File.createTempFile("HTMLExportHandler", ".html", Settings.getInstance().getTempDir());
        InputStream theSource = this.getTemplateFileInputStream(aTemplate);
		try {
			this.exportValues(someObjects, theSource, theResult);
		} finally {
			theSource.close();
		}

        return theResult;
    }

	protected void exportValues(List<? extends TLObject> someObjects, InputStream aSource, File aResult)
			throws IOException {
        BufferedReader theReader = new BufferedReader(new InputStreamReader(aSource));
        FileWriter     theFile   = new FileWriter(aResult);
        BufferedWriter theWriter = new BufferedWriter(theFile);
        TagWriter     theOut    = new TagWriter(theFile);
        boolean        tagFound  = false;

        while (theReader.ready()) {
            String theLine = theReader.readLine();
			if (StringServices.isEmpty(theLine)) {
				break;
			}
            if (DATA_WORD.equals(theLine.trim())) {
                theWriter.flush();

                tagFound = true;
                try {
                    this.exportValues(someObjects, theOut);
                }
                catch (IOException ex) {
                    throw ex;
                }
                catch (Exception ex) {
                    throw new TopLogicException(HTMLValueExporter.class, "export.values", ex);
                }

                theWriter.newLine();
            }
            else {
                theWriter.write(theLine);
                theWriter.newLine();
            }
        }

		theOut.flushBuffer();
        theWriter.flush();
        theWriter.close();

        if (!tagFound) {
            Logger.warn("Failed to export values, '" + DATA_WORD + "' not found in export template!", HTMLValueExporter.class);
        }
    }

	protected void exportValues(List<? extends TLObject> someObjects, TagWriter anOut)
			throws IOException, NoSuchAttributeException {
        List<String> theColumns = this.getColumnNames();
        int          thePos     = 0;

        HTMLUtil.beginTable(anOut, "Export", "dataTable");

        this.exportHeader(anOut, theColumns);

		for (TLObject theAttributed : someObjects) {
            this.exportRow(anOut, theAttributed, theColumns, thePos++);
        }
        HTMLUtil.endTable(anOut);
    }

    protected void exportHeader(TagWriter anOut, List<String> someColumns) throws IOException, NoSuchAttributeException {
        Resources theRes = Resources.getInstance();
        int       thePos = 0;

        HTMLUtil.beginBeginTr(anOut);
        HTMLUtil.writeClassAttribute(anOut, "headerRow");
		HTMLUtil.endBeginTr(anOut);
        for (String theString : someColumns) {
            String theClazz  = "h h" + thePos++;

			HTMLUtil.beginBeginTh(anOut);
			HTMLUtil.writeClassAttribute(anOut, theClazz);
			HTMLUtil.endBeginTh(anOut);
            anOut.writeText(theRes.getString(this.getHeaderName(theString)));
            HTMLUtil.endTh(anOut);
        }
        HTMLUtil.endTr(anOut);
    }

	protected ResKey getHeaderName(String aColumn) throws NoSuchAttributeException {
		return TLModelI18N.getI18NKey(MetaElementUtil.getMetaAttribute(this.metaElement, aColumn));
    }

	protected void exportRow(TagWriter aWriter, TLObject anObject, List<String> someColumns, int aPos)
			throws IOException {
        int thePos = 0;

		HTMLUtil.beginBeginTr(aWriter);
		HTMLUtil.writeClassAttribute(aWriter, "contentRow r" + aPos);
		HTMLUtil.endBeginTr(aWriter);

        for (String theColumn : someColumns) {
            this.exportCell(aWriter, anObject, theColumn, thePos++);
        }

        HTMLUtil.endTr(aWriter);
    }

	protected void exportCell(TagWriter aWriter, TLObject anObject, String aColumn, int aPos) throws IOException {
		Object theObject = WrapperAccessor.INSTANCE.getValue(anObject, aColumn);
        String theValue  = MetaLabelProvider.INSTANCE.getLabel(theObject);
        String theClazz  = "c c" + aPos;

		HTMLUtil.beginBeginTd(aWriter);
		HTMLUtil.writeClassAttribute(aWriter, theClazz);
		HTMLUtil.endBeginTd(aWriter);
        if (theObject != null) {
            aWriter.writeText(theValue);
        }
        HTMLUtil.endTd(aWriter);
    }

    protected List<String> getColumnNames() {
        return this.columns;
    }

    /** 
     * Return the file to be used as template for this export.
     * 
     * @param    aFileName    The file name of the template file, must not be <code>null</code>.
     * @return   An input stream for the template file with the given name, never <code>null</code>.
     * @throws   IOException    If the given file doesn't exist or cannot be read.
     */
    protected InputStream getTemplateFileInputStream(String aFileName) throws IOException {
        try{
            DataAccessProxy theDAP = new DataAccessProxy("webinf://reportTemplates/html");

            theDAP = theDAP.getChildProxy(aFileName);

            return theDAP.getEntry();
        }
        catch(Exception ex){
            String theMessage = "Problem getting template file'" + aFileName + "'!";

            Logger.error(theMessage, ex, this);

            throw new IOException(theMessage);
        }
    }

}

