/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.CSVReader;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.simple.GenericDataObject;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.element.genericimport.GenericValueMapImpl;
import com.top_logic.element.genericimport.interfaces.GenericImporter.GenericFileImporter;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.element.structured.wrap.Mandator;

/**
 * The PlainCSVImporter can return a list of DataObjects or GenericValueMaps reading them
 * in from a CSV file.
 * 
 * @author    <a href="mailto:TEH@top-logic.com">TEH</a>
 */
public class PlainCSVImporter extends DataObjectImportTask implements GenericFileImporter {

    public static final String DELETE_WHEN_DONE = "deleteWhenDone";
    public static final String IMPORT_FILE      = "importFile";
    public static final String MO_TYPE          = "moType";

    /**
     * when true file will be deleted after import
     */
    protected boolean deleteWhenDone;

    /**
     * The (csv/zip) file we import from
     */
	protected BinaryData importFile;

    protected String[] firstColumnNames;

    private String moType;

    /**
     * The List of lines representing the CSV file.
     */
    protected List content;

    public PlainCSVImporter(Properties aProp) {
        super(aProp);
		{
            String theFile = aProp.getProperty(IMPORT_FILE);
            if (!StringServices.isEmpty(theFile)) {
				this.importFile = FileManager.getInstance().getData(aProp.getProperty(IMPORT_FILE));
            }

            //this.sheetName      = aProp.getProperty(SHEET_NAME);
            this.deleteWhenDone = Boolean.valueOf(aProp.getProperty(DELETE_WHEN_DONE, "false")).booleanValue();
            this.moType         = aProp.getProperty(MO_TYPE, "ExternalContact");
        }
    }

    /**
     * Ctor with params needed.
     *
     * @param importSource      the import file. May be an XLS or a ZIP containing an XLS
     * @param aSheetName        the sheet name in the XLS to use
     * @param doDeleteWhenDone  if true, delete the file after upload
     */
	public PlainCSVImporter(BinaryData importSource, String aSheetName, boolean doDeleteWhenDone) {
		super("Import" + importSource.getName());
        this.importFile     = importSource;
        this.deleteWhenDone = doDeleteWhenDone;
        this.moType         = "ExternalContact";
    }

    @Override
	public void setImportFile(BinaryData aFile) {
        this.importFile = aFile;
        this.firstColumnNames = null;
    }

    @Override
	public String[] getSupportedFileExtensions() {
        return new String[] { ".zip", getExtension() };
    }

    /**
     * Override this to read the column names from the XLS
     *
     * @return true if we should read the first line as column names
     */
    protected boolean readFirstLineAsColumnNames() {
        return true;
    }

    /**
     * Transform the CSV data into DataObjects
     *
     * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#getImportObjects(com.top_logic.element.structured.wrap.Mandator)
     */
    @Override
	protected List getImportObjects(Mandator aMandator)
            throws DatabaseAccessException, DataObjectException {
        
        // Transform data to DataObjects
        // TODO fills DOs with empty Strings to fix checkFormat problems...
        List theRes = new ArrayList();
        List     theContent  = this.getContent();
        String[] theColNames = this.getColumns();
        int theColSize = theColNames.length;
        for (int theRow = 1; theRow < theContent.size(); theRow++) { // First row is title
			DataObject theDO = new GenericDataObject(this.moType, StringID.valueOf(this.moType + theRow), theColSize);
            
            List theRowData = (List)theContent.get(theRow);
            for (int theCol = 0; theCol < theColSize; theCol++) {
                
                Object theValue = theRowData.get(theCol);
                if (theValue== null) {
                    theValue = "";
                }
                theDO.setAttributeValue(theColNames[theCol], theValue);
            }

            theRes.add(theDO);
        }

        return theRes;
    }

    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericImporter#getPlainObjects()
     */
    @Override
	public List getPlainObjects() throws DatabaseAccessException, DataObjectException {
        try {
            setupImport();
            List theList   = this.getImportObjects(null);
            int  theSize   = theList.size();
            List theResult = new ArrayList(theSize);
            for (int i=0; i<theSize; i++) {
                DataObject      theDO    = (DataObject) theList.get(i);
                String[]        theAttrs = theDO.getAttributeNames();
                GenericValueMap theMap   = new GenericValueMapImpl(theDO.tTable().getName(), theDO.getIdentifier(), theAttrs.length);
                theResult.add(theMap);
                for (int k=0; k<theAttrs.length; k++) {
                    String theAttr = theAttrs[k];
                    theMap.setAttributeValue(theAttr, theDO.getAttributeValue(theAttr));
                }
            }
            return theResult;
        } catch (Exception ex) {
            Logger.error("Unable to set up import!", ex, this.getClass());
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#checkFormat(com.top_logic.dob.MetaObject)
     */
    @Override
	protected void checkFormat(MetaObject aMeta) throws DataObjectException {
        String[] theColNames = this.getColumns();
        // skip the first column while checking since that is always the ID
        for (int i = 1; i < theColNames.length; i++) {
            String col   = theColNames[i];
            if (null == MetaObjectUtils.getAttribute(aMeta, col)) {
                throw new NoSuchAttributeException("Expected column '" + col + "' not found in " + aMeta);
            }
        }
    }

    /**
     * Get the column names used as DataObject attribute names.
     *
     * Note that these attribute names have to be complete
     * at least from the first column to the last column to be imported!
     *
     * @return the column names used as DataObject attribute names
     */
    @Override
	public String[] getColumns() {
        if (this.readFirstLineAsColumnNames()) {
            if (firstColumnNames == null) {
                List theHeader = (List)this.getContent().get(0);
                firstColumnNames = (String[])theHeader.toArray(new String[theHeader.size()]);
            }

            return firstColumnNames;
        }

        throw new RuntimeException("Column names not set - cannot create DOs");
    }

    /**
     * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#setupImport()
     */
    @Override
	protected void setupImport() throws Exception {
        super.setupImport();
        this.readFile();
    }
    
    private void readFile() {
        try {
            Reader theReader = null;
            if (this.importFile.getName().endsWith(getExtension())) {
				theReader = new InputStreamReader(this.importFile.getStream());
            } else if (this.importFile.getName().endsWith(".zip")) {
				ZipInputStream zipper = new ZipInputStream(importFile.getStream());
				while (true) {
					ZipEntry zentry = zipper.getNextEntry();
					if (zentry == null) {
						break;
					}
                    String fName = zentry.getName().toLowerCase();
                    if (fName.endsWith(getExtension())) {
						theReader = new InputStreamReader(zipper);
                        break;
                    }
                }
            }

            this.content = new CSVReader(theReader).readAllLines();
            
        }
        catch (Exception ex) {
            logError("Import failed: " + ex, ex);
        }
    }

    /**
     * Return the extension of the file you expect.
     *
     * @return ".csv" here, override as needed.
     */
    protected String getExtension() {
        return ".csv";
    }

    /**
     * Return the parsed content of the file.
     *
     * @return   The values from the file as a list of lists, may be <code>null</code>.
     */
    protected List getContent() {
        if (this.content == null) {
            this.readFile(); // init the workbook
        }
        return content;
    }

    @Override
	protected void importItem(DataObject aDo, Mandator aMandator) throws Exception {
    }

    @Override
	protected void startImport() {
        this.startImport(null, false);
    }
}

