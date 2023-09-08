/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.queue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.base.office.AbstractOffice;
import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.excel.ExcelAccess;
import com.top_logic.base.office.ppt.Powerpoint;
import com.top_logic.base.office.word.WordAccess;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.reporting.remote.ReportDescriptor;
import com.top_logic.reporting.remote.common.ReportDescriptorImpl;
import com.top_logic.reporting.remote.common.ReportFactory;

/**
 * TODO use File as soon as possible and stick with it.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ReportCreator implements ReportQueueConstants{

    /**
     * triggers the ReportCreator to look for the next file in OPEN directory.
     */
    public void createReport(){
        // find oldest job
        
        // if anything left in WORK this is the oldest job, 
        // else check OPEN-directory
        // (this only works if this is the only "Worker" using this WORK-directory)
        
        // if found
        // move file to WORK directory
        // if successful
        // create report
        // write data to DONE directory
        // delete data in work directory
        // else write to ERROR
        
        
        String  fileName = getNextReportFileName(WORK_PATH);
        if (fileName != null) {
            File dest = new File(WORK_PATH+fileName);
            createReport(dest);
            return; // Sucesfully processed file in WORK_PATH
        }
        fileName = getNextReportFileName(OPEN_PATH);
        if(fileName != null){
            File source = new File(OPEN_PATH+fileName);
            File dest   = new File(WORK_PATH+fileName);
            
            if (source.renameTo(dest)) {
                createReport(dest);
            } 
            else {
                Logger.warn("Problem moving '" + source.getAbsolutePath() + "' to '" + dest.getAbsolutePath() + "'",this);
            }
        }
    }

    /**
     * creates a report from the given file
     * the file is deleted, the result is written 
     * to DONE directory if report could be created, to ERROR directory if not
     */
    private void createReport(File aFile) {
        String fileName = aFile.getName();

        try{
            doCreateReport(aFile);
            if (!aFile.delete()) {
                Logger.warn("Problem deleting the input-File '" + fileName + "' after creating the Report", this);
            }
        }
        catch(Exception e){
            Logger.warn("Problem creating report "+fileName,e,this);
            File errorFile = new File(ERROR_PATH+fileName);
            
            if (!aFile.renameTo(errorFile)) {
                Logger.warn("Problem moving data for report to ERROR directory",this);
            }
        }
    }

    /**
     * creates the report and writes the result file
     */
    private  void doCreateReport(File aWorkFile) throws OfficeException, IOException, ClassNotFoundException {
        
        // get data (ReportDescriptor) from file:
        //
        // -> template
        // -> map of key-values
        // -> template: name of template 
        // -> type of report
        // 
        // call OfficeReporting to create result
        
        ReportDescriptor theReportDescriptor = ReportDescriptorImpl.getDescriptor(aWorkFile);
                    
        AbstractOffice office = null; // depends on type
        String theType = theReportDescriptor.getType();
        
        if (ReportDescriptor.PPT.equals(theType)) {
			office = Powerpoint.getInstance(Powerpoint.isXmlFormat(aWorkFile));
        }
        
        if (ReportDescriptor.WORD.equals(theType)) {
            office = WordAccess.getInstance();
        }
        
        if (ReportDescriptor.EXCEL.equals(theType)) {
			office = ExcelAccess.newInstance();
        }
        if(office == null){
            throw new IllegalArgumentException("The type '" +theType+"' is not known for reporting");
        }
        
        // get the name of the given file
        String fileName = aWorkFile.getName();
        // remove the suffix from the file
        String destName = fileName.substring(0, fileName.length() - POSTFIX.length());
        // add the proper suffix for the report-type (e.g. ppt, doc, xls)
        String theSuffix = theType;
        if (ReportDescriptor.MODE_GETVALUES.equals(theReportDescriptor.getMode())) {
            theSuffix = ".ser";
        }
        destName = destName+theSuffix;
        
        File     dest = new File(DONE_PATH+destName);
        Map  valueMap = theReportDescriptor.getValues();
        
        // some special handling for images:
        ReportFactory.handleSpecialContentForRead(valueMap);
        
        byte[]          templateString = theReportDescriptor.getTemplate();
        ByteArrayInputStream  template = new ByteArrayInputStream(templateString);
       
        try {
	        if (ReportDescriptor.MODE_SETVALUES.equals(theReportDescriptor.getMode())) {
	            office.setValues(template, dest, valueMap);
	        }
	        else if (ReportDescriptor.MODE_GETVALUES.equals(theReportDescriptor.getMode())) {
	            File theFile = null;
	            Map  theValues = null;
	            try {
	
	                if (valueMap != null) { // Only supported by Excel for now (ask MGA)
						theFile = File.createTempFile("tmp", theSuffix, Settings.getInstance().getTempDir());
						FileUtilities.copyToFile(template, theFile);
						theValues =
							((ExcelAccess) office).getValues(BinaryDataFactory.createBinaryData(theFile), valueMap);
	                }
	                else {
	                    theValues = office.getValues(template);
	                }
	                
	                ReportFactory.handleSpecialContentForWrite(theValues);
	            } 
	            catch (IOException ex) {
	                Logger.error("failed to getValues()", ex, this);
	            }
	            finally {
	                if (theFile != null) {
	                    theFile.delete();
	                }
	            }
	            
	            OutputStream fos = new FileOutputStream(dest); 
	            try {
	            	ObjectOutputStream o = new ObjectOutputStream(fos); 
	            	o.writeObject(theValues); 
				} finally {
					fos.close();
				}
	        }
        } finally {
        	template.close();
        }
    }

	/**
     * Get the file name of the next report
     * 
     * @param aDir a directory file name
     * @return the file name of the next report
     */
    protected String getNextReportFileName(String aDir) {
        return this.getNextReportFileName(aDir, GettersFirstComparator.INSTANCE);
    }
    
    public static int getReportPosition(String aReportID) {
        List theFiles = getSortedReportFileList(OPEN_PATH, GettersFirstComparator.INSTANCE);
        if (theFiles == null || theFiles.isEmpty()) {
            return -1;
        }
        
        int thePos   = -1;
        int theIndex = 0;
        for (Iterator theFileIt = theFiles.iterator(); thePos == -1 && theFileIt.hasNext();) {
            String theFileName = ((File) theFileIt.next()).getName();
            if (theFileName.startsWith(aReportID)) {
                thePos = theIndex;
            }
            theIndex++;
        }
        
        return thePos;
    }
    
    protected static File getFirstInFileList(String aDir, Comparator aComp) {
        File theDir = new File(aDir);
        FindSmallestFile finder = new FindSmallestFile(aComp);
        theDir.listFiles(finder);
        return finder.smallest;
    }
    
    /**
     * Misusage of FileFilter for minimum search.
     */
    static class FindSmallestFile implements FileFilter {
        
        /** The smallest File found */
        Comparator comp;

        /** The smallest File found */
        File smallest;

        /**
         * Find the smallest File using aComp as comparator.
         */
        public FindSmallestFile(Comparator aComp) {
            this.comp = aComp;
        }

        @Override
		public boolean accept(File aFile) {
            if (!aFile.isFile()) {
                Logger.warn("Non-File found , please remove :'" + aFile.getAbsolutePath() + "'", this);
                return false;
            }
            if (smallest != null && comp.compare(aFile, smallest) >= 0) {
                return false; // already greater than smallest
            }
            smallest = aFile;
            return true;
        }
        
        
        
    }

    protected static List getSortedReportFileList(String aDir, Comparator aComp) {
        if (StringServices.isEmpty(aDir)) {
            return null;
        }
        
        File theDir = new File(aDir);
        List fileNames = CollectionUtil.toList(theDir.listFiles());
        if (fileNames.isEmpty()) {
            return null;
        }
        
        Collections.sort(fileNames, aComp);
        
        return fileNames;
    }
    
    /**
     * the name of the oldest file in the OPEN directory, or null, if there is no file
     */
    protected String getNextReportFileName(String aDir, Comparator aComp) {
        File theFile = getFirstInFileList(aDir, aComp);
        if (theFile != null) {
            return theFile.getName();
        }
        
        return null;
    }
    
    protected static class LastModifiedComparator implements Comparator {
        public static final LastModifiedComparator INSTANCE = new LastModifiedComparator();
        
        @Override
		public int compare(Object obj, Object obj1) {
            long theMod  = ((File) obj).lastModified();
            long theMod1 = ((File) obj1).lastModified();
            long theDiff = theMod - theMod1;
            if (theDiff > 0) {
                return 1;
            }
            else if (theDiff < 0) {
                return -1;
            }
            return 0;
        }
    }
    
    protected static class GettersFirstComparator implements Comparator {
        public static final GettersFirstComparator INSTANCE = new GettersFirstComparator();
        
        @Override
		public int compare(Object obj, Object obj1) {
            boolean isGet  = ((File) obj).getName().indexOf(ReportDescriptor.MODE_GETVALUES) >= 0;
            boolean isGet1 = ((File) obj1).getName().indexOf(ReportDescriptor.MODE_GETVALUES) >= 0;
            
            if (isGet && !isGet1) {
                return -1;
            }
            else if (!isGet && isGet1) {
                return 1;
            }
            
            return LastModifiedComparator.INSTANCE.compare(obj, obj1);
        }
    }
}
