/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.reporting.remote.ReportDescriptor;
import com.top_logic.reporting.remote.ReportResult;
import com.top_logic.reporting.remote.ReportStatus;
import com.top_logic.reporting.remote.ReportTicket;
import com.top_logic.reporting.remote.Reporter;
import com.top_logic.reporting.remote.ReporterState;
import com.top_logic.reporting.remote.common.ReportDescriptorImpl;
import com.top_logic.reporting.remote.common.ReportFactory;
import com.top_logic.reporting.remote.common.ReportResultImpl;
import com.top_logic.reporting.remote.common.ReportStatusImpl;
import com.top_logic.reporting.remote.common.ReportTicketImpl;
import com.top_logic.reporting.remote.common.ReporterStateImpl;

/**
 * Implements the reporter Interface. Makes the data persistent 
 * on the file system.
 * The file system serves as the queue. 
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class QueueReporter implements Reporter, ReportQueueConstants {

   /** Expected average duration of a report in seconds for {@link #doGetExpectedDuration(String)}*/
   public static final int AVERAGE_REPORT_DURATION = 30;

	private CreatorRunner creator;
    
	public QueueReporter(){
	}
	
	private void startCreatorRunner() {
	    creator = new CreatorRunner("ReportCreator"); 
	}
	
	/**
	 * Makes sure, that a complete directory path will exist. It it does NOT exist, it will be
	 * created.
	 *
	 * @param pathName
	 *        the name of the directory
	 */
    static private void enforceReportingDirectory (String pathName) {
        File directory = new File (pathName);
        if (!directory.exists ()) {
            if (!directory.mkdir()) {
                throw new RuntimeException("Failed to create " + directory);
            }
        }
    }
    
    public static void initDirectories() {
	    if (!IO_TMPDIR.exists() || !IO_TMPDIR.exists()) {
	        throw new RuntimeException("No system tmpDir in " + IO_TMPDIR);
	    }
        if (!REPORT_DIR.exists()) {
            if (!REPORT_DIR.mkdir()) {
                throw new RuntimeException("Failed to create " + REPORT_DIR);
            }
        } else {
            if (!REPORT_DIR.isDirectory()) { // Was fuern Witzbold ....
                throw new RuntimeException(REPORT_DIR + " is not a Directory");
            }
        }
	    
        enforceReportingDirectory(OPEN_PATH);
        enforceReportingDirectory(WORK_PATH);
        enforceReportingDirectory(DONE_PATH);
        enforceReportingDirectory(ERROR_PATH);
        enforceReportingDirectory(TEMP_PATH);
	}


	/**
	 * Initiates the creation of a report for the given ReportDescriptor.
	 * The ReportDescriptor will be put into the queue of jobs.
	 * A ticket is returned to the requester. If a problem occurred the 
	 * returned ticket will contain information about the problem but no
	 * reportID to fetch the report.
	 */
	@Override
	public ReportTicket createReport(ReportDescriptor aDescription) {
		ReportTicket  ticket = null;
		ReportStatus  status = null;
		String        reportID = "";
		
		try{
	          reportID = ReportDescriptorImpl.write(aDescription);		
	          status = getStatus(reportID);
		}
		catch(Exception e){
			 status = new ReportStatusImpl(e);
			 Logger.warn("Problem creating report",e,this);
		}
		ticket = new ReportTicketImpl(reportID, status);
		return ticket;
	}

	/**
	 * returns the duration in seconds expected until the report is created
	 */
	private int getExpectedDuration(String aReportID) {
		int res = doGetExpectedDuration(aReportID);
		if(res > -1){
			return res;
		}
		// nothing found, try it again in 100 ms
		try{
			Thread.sleep(100);
		}catch (InterruptedException e) {
				//do nothing
		}
		return doGetExpectedDuration(aReportID);
	}

	private int doGetExpectedDuration(String aReportID) {
		int num  = ReportCreator.getReportPosition(aReportID);
		if(num > -1){
			return (num + 2)* AVERAGE_REPORT_DURATION;
		}
		if(isAtWork(aReportID)){
			return AVERAGE_REPORT_DURATION;
		}
		if(isDone(aReportID)){
			return 0;
		} // isError(aReportID) implies -1 
		return -1;
	}
	
	private boolean isError(String aReportID) {
		return fileExists(aReportID, ERROR_PATH);
	}

	private boolean isDone(String aReportID) {
		return fileExists(aReportID, DONE_PATH);
	}

	private boolean isAtWork(String aReportID) {
		return fileExists(aReportID, WORK_PATH);
	}
	
	private boolean isOpen(String aReportID) {
		return fileExists(aReportID, OPEN_PATH);
	}	

	/**
	 * returns if a file with the given name exists in the given directory.
	 * This is independent from the postfix of the file 
	 */
	private boolean fileExists(final String aReportID, String aWorkPath) {
		File theDir = new File(aWorkPath);
		String[] res;
		try {
			res = FileUtilities.list(theDir, new FilenameFilter() {
				@Override
				public boolean accept(File aDir, String aName) {
					return aName.startsWith(aReportID);
				}

			});
		} catch (IOException ex) {
			return false;
		}
		return res.length>0;
	}

	@Override
	public ReportResult getReport(final String aReportID) {
	    ReportStatus theStatus  = getStatus(aReportID);
	    if (theStatus.isDone()) {
	        
	        File reportFile = null;
            
	        File theDir = new File(DONE_PATH);
			String[] res;
			try {
				res = FileUtilities.list(theDir, new FilenameFilter() {
					@Override
					public boolean accept(File aDir, String aName) {
						return aName.startsWith(aReportID);
					}
				});
			} catch (IOException ex1) {
				Logger.error("Cannot list directory: " + ex1.getMessage(), ex1, QueueReporter.class);
				return new ReportResultImpl(null, theStatus, null);
			}
	        if(res.length == 1){
	            reportFile = new File(DONE_PATH+res[0]);
	            String       reportMode = this.getReportMode(reportFile);
	            
	            Object reportData;
	            try {
	                reportData = this.getReportData(reportFile, reportMode);
	                return new ReportResultImpl(reportData, theStatus, reportMode);
	            }
	            catch (Exception ex) {
	                Logger.warn("Problem reading the Report " + reportFile.getName(), ex, this);
	                return new ReportResultImpl(null, theStatus, reportMode);
	            }
	        }
	        theStatus = new ReportStatusImpl(new FileNotFoundException("No File for '" + aReportID + "' found in '" + theDir + "'"));
            return new ReportResultImpl(null, theStatus, null);
	        
	    }
	    else {
	        return new ReportResultImpl(null, theStatus, null);
	    }
	}

	private String getReportMode(File reportFile) {
		String theFileName = reportFile.getName();
		if (theFileName.indexOf(ReportDescriptor.MODE_SETVALUES) > -1) {
			return ReportDescriptor.MODE_SETVALUES;
		}
		else if (theFileName.indexOf(ReportDescriptor.MODE_GETVALUES) > -1) {
			return ReportDescriptor.MODE_GETVALUES;
		}
		
		return null;
	}
	
	private Object getReportData(File reportFile, String reportMode) throws Exception {
		if (ReportDescriptor.MODE_SETVALUES.equals(reportMode)) {
    		return FileUtilities.getBytesFromFile(reportFile);
    	}
    	else if (ReportDescriptor.MODE_GETVALUES.equals(reportMode)) {
    		return readValues(reportFile);
    	}
    	else {
    		throw new IllegalArgumentException("Cannot read report values for unknown report type " + reportMode);
    	}
	}

	@Override
	public ReportStatus getStatus(String aReportID) {
		int expectedDuration = getExpectedDuration(aReportID);
		return new ReportStatusImpl(expectedDuration);
	}

    /**
     * Tries to read a Map-Object from the given file
     * @return a values map read from the given file
     */
    protected static Object readValues(File someSource) throws IOException, ClassNotFoundException {
        
        InputStream fis = null;
        Map theValues = null;

        fis = new FileInputStream(someSource);
        ObjectInputStream o = new ObjectInputStream(fis); 
        theValues = (Map) o.readObject();
        o.close();
        fis.close();
        
        ReportFactory.handleSpecialContentForRead(theValues);
     
        return theValues;
        
    }

	/**
	 * It is checked if the file with the given ReportID is in the OPEN, DONE or
	 * ERROR directory. If the file was found, it will be tried to delete the file.
	 * Attention: It might happen that during finding the file and deleting it, the 
	 * file is moved to an other directory and therefore can't be deleted.   
	 * @see com.top_logic.reporting.remote.Reporter#cancelReport(java.lang.String)
	 */
	@Override
	public void cancelReport(String aReportID) {
		if(isOpen(aReportID)){
			delete(aReportID, OPEN_PATH);
		}
		if(isDone(aReportID)){
			delete(aReportID, DONE_PATH);
		}
		if(isError(aReportID)){
			delete(aReportID, ERROR_PATH);
		}
	}

	
	private void delete(final String aReportID, String aPath) {
		File theDir = new File(aPath);
		String[] res;
		try {
			res = FileUtilities.list(theDir, new FilenameFilter() {
				@Override
				public boolean accept(File aDir, String aName) {
					return aName.startsWith(aReportID);
				}
			});
		} catch (IOException ex) {
			Logger.error("Cannot delete directory contents: " + ex.getMessage(), ex, QueueReporter.class);
			return;
		}
		if(res.length == 1){
			File theFile = new File(aPath+res[0]);
			theFile.delete();
		}
	}


	@Override
	public ReporterState getReporterState() {
		int numOpen  = getNumFiles(OPEN_PATH);
		int numDone  = getNumFiles(DONE_PATH);
		int numError = getNumFiles(ERROR_PATH);
		ReporterState repState = new ReporterStateImpl(numOpen, numDone, numError);
		return repState;
	}


	private int getNumFiles(String aPath) {
		File theDir = new File(aPath);
		File[] files;
		try {
			files = FileUtilities.listFiles(theDir);
		} catch (IOException ex) {
			Logger.error("Cannot list directory: " + ex.getMessage(), ex, QueueReporter.class);
			return 0;
		}
		return files.length;
	}

    @Override
	public boolean init() {
        initDirectories();
        startCreatorRunner();
        return true;
    }

    @Override
	public boolean isValid() {
        // always true. this is not a remote-Reporter
        return true;
    }

	/**
	 * The Creator-Thread will be stopped when shut down.
	 * 
	 * @see com.top_logic.reporting.remote.Reporter#shutDown()
	 */
    @Override
	public void shutDown() {
    	Logger.info(QueueReporter.class.getSimpleName() + " shut down", QueueReporter.class);
    	creator.stopRunner();
   	}

}
