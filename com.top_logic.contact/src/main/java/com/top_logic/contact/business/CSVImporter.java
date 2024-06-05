/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.CSVTokenizer;
import com.top_logic.basic.io.SimpleCSVTokenizer;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.sched.BatchImpl;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.progress.ProgressInfo;

/**
 * Abstarct Superclass for importing csv-files plain or from a zip-file
 * 
 * TODO KHA move to top-logic and/or refactor when complete enough.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public abstract class CSVImporter extends BatchImpl implements ProgressInfo {

    /**
     * Inital (maximum) size of stringWriter.
     */
    protected static final int INITAL_LOGSIZE = 10240;

    /** usefull constate to use for CTor */
    public static final boolean DELETE_WHEN_DONE = true;
    
    /** Default encoding we assume */
    public static final String ENCONDING = "ISO_8859_1";

    /** The (csv/zip) file we import from */
    protected File importFile;
    
    /** Total size of data being imported */
    protected long totalSize;

    /** Current Size of data already imported [0..totalSize] 
     * 
     * When encoding is not bytewise (e.g. UTF-8) this may be incorrect. 
     */
    protected long currentSize;

    /** when true file will be deted after import */
    protected boolean deleteWhenDone;
    
    /** Set to true when Import was completed */
    protected boolean finished;
    
    /** Message to show to User via progressInfo */
    protected String message;
    
    /** PrintWriter to log messages to, will be ignored when null. */
    protected PrintWriter logWriter;

    /** PrintWriter is by default based on this StringWriter*/
    protected StringWriter stringWriter;

    /**
	 * Create a new {@link CSVImporter} for the given {@link File}.
	 * 
	 * @param doDeleteWhenDone
	 *        when true file will be deleted after import.
	 */
    public CSVImporter(File importSource, boolean doDeleteWhenDone) {
        super("Import" + importSource.getName());
        this.importFile     = importSource;
        this.deleteWhenDone = doDeleteWhenDone;
    }

    /**
     * Return the encoding to use when creating a Writer.
     * 
     * @param aFile the file to import
     * 
     * @return ENCONDING here, override as needed.
     */
    protected String getEncoding(File aFile) {
        return ENCONDING;
    }
    
    /**
     * Return the extenion of the file you expect.
     * 
     * @return ".csv" here, override as needed.
     */
    protected String getExtension() {
        return ".csv";
    }

    /** 
     * Open the file, check for .zip-file then forward to csv-import.
     * 
     * In case of a zip-file the first file matching the {@link #getExtension()} will be used.
     */
    @Override
	public void run() {
        InputStream iStream = null;
        ZipFile     zipper  = null;
        finished    = false;
        try {
            InputStream theStream = null;
            String fName  = importFile.getName().toLowerCase();
            String extens = getExtension();
            if (fName.endsWith(extens)) {
                totalSize = importFile.length();
                iStream = theStream = new FileInputStream(importFile);
            } else if (fName.endsWith(".zip")) {
                zipper = new ZipFile(importFile);
                Enumeration enumEr = zipper.entries();
                while (enumEr.hasMoreElements()) {
                    ZipEntry zentry = (ZipEntry) enumEr.nextElement();
                    fName = zentry.getName().toLowerCase();
                    if (fName.endsWith(extens)) {
                        theStream = zipper.getInputStream(zentry);
                        totalSize = zentry.getSize();
                        break;
                    }
                }
            } 
            if (theStream != null) {
                doCSVImport(new BufferedReader(new InputStreamReader(theStream, ENCONDING)));
            } else {
                logError(message, null);
            }
        } catch (Exception ex) {
            message = ex.toString();
            logError("Failed to run()", ex);
        } finally {
            iStream = StreamUtilities.close(iStream);
            if (zipper != null) try {
                zipper.close();
            } catch (IOException iox) {
                logWarn("Failed to close()" , iox);
            }        
            finished = true;
        }
        if (deleteWhenDone) {
            if (!importFile.delete()) {
                logWarn("Failed to delete() " + importFile.getAbsolutePath(),  null);
            }
        }
            
    }
    
    /**
     * Create the CSVTokenizer your want to use for your file.
     * 
     * @param firstLine the first line of the file.
     * @return a CSVTokenizer using ',' and '"' as delimiter and quote.
     */
    protected SimpleCSVTokenizer createTokenizer(String firstLine) {
        return new CSVTokenizer(firstLine, ',', '"');
    }
    
    /** 
     * Check that the first column (as found in CSVTokenizer) is as expected.
     * 
     * @throws IOException when format is not OK.
     * 
     * @return false to indicate that no check is needed,
     *         (assimung first line contains data)
     */
    abstract protected boolean checkColumnFormat(SimpleCSVTokenizer cvsToken) throws IOException;

    /** 
     * Do the actual Import using a CSVParser.
     */
    protected void doCSVImport(BufferedReader aReader) throws IOException {
        
        String theLine = aReader.readLine();
        SimpleCSVTokenizer csvToken = createTokenizer(theLine);
        
        if (checkColumnFormat(csvToken)) {
            currentSize += theLine.length() + 1; // + 1 for \n
            theLine = aReader.readLine();
        }
        setupImport();
        try {
			while (null != theLine && !getShouldStop()) {
                currentSize += theLine.length() + 1; // + 1 for \n
                csvToken.reset(theLine);
                importLine(theLine, csvToken);
                theLine = aReader.readLine();
            }
            
            // We have to commit the last pending updates. Before this will
			// happen a subclass may want to add some additional information
            beforeLastCommitHook();
            KnowledgeBase theKB = PersistencyLayer.getKnowledgeBase();
            if(!theKB.commit()) {
            	logWarn("Failed to commit", null);
            }
        } catch (Exception ex) {
            logError("Failed to doCSVImport", ex);
        }
        finally {
            tearDownImport();
        }
    }

    /** 
     * Hook for subclasses do setup something before importing.
     * 
     * This will call setupLogWriter() so be sure to call super.
     */
    protected void setupImport() {
        logWriter = setupLogWriter();
    }

    /** 
     * By default will create a PrintWriter bases on a StringWriter.
     * 
     * @return null to signal that you do not want to have logging.
     */
    protected PrintWriter setupLogWriter() {
        int size = totalSize > INITAL_LOGSIZE ? INITAL_LOGSIZE : (int) totalSize;
        stringWriter = new StringWriter(size);
        return new PrintWriter(stringWriter);
    }

    /** 
     * This method is called for every line parsed.
     * 
     * @param aLine     The complete current line
     * @param aCsvToken A CSVTokenizer for that line.
     */
    abstract protected void importLine(String aLine, SimpleCSVTokenizer aCsvToken) throws Exception; 

    /**
     * Log an Error to normal logger and (if configured) the logWriter.
     */
    protected void logError(String aMessage, Throwable anException) {
        Logger.error(aMessage, anException, this);
        if (logWriter != null) {
            logWriter.print("ERROR: ");
            logWriter.println(aMessage);
            if (anException != null) {
                anException.printStackTrace(logWriter);
            }
        }
    }
    
    /**
     * Log a Warning to normal logger and (if configured) the logWriter.
     */
    protected void logWarn(String aMessage, Throwable anException) {
        Logger.warn(aMessage, anException, this);
        if (logWriter != null) {
            logWriter.print("WARNING: ");
            logWriter.println(aMessage);
            if (anException != null) {
                anException.printStackTrace(logWriter);
            }
        }
    }

    /**
     * Log som Information to normal logger and (if configured) the logWriter.
     */
    protected void logInfo(String aMessage, Throwable anException) {
        Logger.info(aMessage, anException, this);
        if (logWriter != null) {
            logWriter.print("INFO: ");
            logWriter.println(aMessage);
            if (anException != null) {
                anException.printStackTrace(logWriter);
            }
        }
    }

    
    /** 
     * Hook for subclasses do tear down something after importing.
     * 
     * This will set message to the content (if not null) of the StringWriter.
     */
    protected void tearDownImport() {
        if (stringWriter != null) {
            message = stringWriter.toString();
            stringWriter = null; 
            logWriter    = null;
        }
    }
    
    /**
     * Hook for sublclasses to add some information before the final commit happens.
     */
    protected void beforeLastCommitHook() {
    	
    }

    /**
     * true when Import was completed 
     */
    @Override
	public boolean isFinished() {
        return finished;
    }

    /** 
     * Return the number of bytes currently imported.
     */
    @Override
	public long getCurrent() {
        return currentSize;
    }

    /** 
     * Return the completeness in %.
     */
    @Override
	public float getProgress() {
        return 100.0f * currentSize / totalSize;
    }

    /** 
     * Return the tital size of the file to import
     */
    @Override
	public long getExpected() {
        return totalSize;
    }


    /** 
     * An (optional) message to show to user 
     *
     * @return null when there is no message.
     */ 
    @Override
	public String getMessage() {
        return message;
    }

    /** 
     * Try using 1 second refresh (Import seems fast enough ..)
     */
    @Override
	public int getRefreshSeconds() {
        return MIN_REFRESH;
    }

    /** 
     * Fix empty Strings by replacing them with null.
     */
    protected static String fixEmpty(String aString) {
        if (aString != null) {
            aString = aString.trim();
            if (aString.length() == 0) {
                aString = null;
            }
        }
        return aString;
    }
    
}
