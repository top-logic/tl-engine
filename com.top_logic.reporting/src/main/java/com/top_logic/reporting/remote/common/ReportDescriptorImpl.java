/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.remote.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringID;
import com.top_logic.basic.StringServices;
import com.top_logic.reporting.queue.ReportQueueConstants;
import com.top_logic.reporting.remote.ReportDescriptor;

/**
 * Default implementation of {@link ReportDescriptor}.
 *
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ReportDescriptorImpl implements ReportDescriptor, ReportQueueConstants, Serializable{

    public static final String SEPARATOR = "_";

	private static final long serialVersionUID = 2535830033546009800L;

    private byte[] template;
    private String templateName;
    private Map valueMap;
    private String type;
    private String mode;

    public ReportDescriptorImpl(){

    }

	public ReportDescriptorImpl(byte[] aTemplate, String aTemplateName, Map aMap, String aType, String aMode) throws RemoteException {
		init(aTemplate, aTemplateName, aMap,  aType, aMode);
	}


	/**
	 * initialize the ReportDescriptor after creation
	 * @param aMode report mode. is <code>null</code> falls back to {@link ReportDescriptor#MODE_SETVALUES}
	 */
    public void init(byte[] aTemplate, String aTemplateName, Map aValueMap, String aType, String aMode) {
        template = aTemplate;
        templateName = aTemplateName;
        valueMap = aValueMap;
        type = aType;
        mode = aMode == null ? ReportDescriptor.MODE_SETVALUES: aMode;
    }

    /**
     * returns the template for the report
     * @see com.top_logic.reporting.remote.ReportDescriptor#getTemplate()
     */
    @Override
	public byte[] getTemplate() {
        return template;
    }

    /**
     * returns the name of the template-file
     * @see com.top_logic.reporting.remote.ReportDescriptor#getTemplateName()
     */
    @Override
	public String getTemplateName() {
        return templateName;
    }

    /**
     * returns the type of the report. E.g.: PowerPoint, Word or Excel
     * @see com.top_logic.reporting.remote.ReportDescriptor#getType()
     */
    @Override
	public String getType() {
    	return type;
    }

    /**
     * returns the mode of the report.
	 * currently the modes {@link #MODE_GETVALUES} and {@link #MODE_SETVALUES} (default) are supported
     * @see com.top_logic.reporting.remote.ReportDescriptor#getType()
     */
    @Override
	public String getMode() {
        return mode;
    }

    /**
     * returns the value-Map used for generating the report
     * @see com.top_logic.reporting.remote.ReportDescriptor#getValues()
     */
    @Override
	public Map getValues() {
        return valueMap;
    }

    /**
     * Tries to read a ReportDescriptor-Object from the given file
     * @return a ReportDescriptor read from the given file
     */
    public static ReportDescriptor getDescriptor(File someSource) throws IOException, ClassNotFoundException {
        InputStream fis = new FileInputStream(someSource);
        try {
        	ObjectInputStream o = new ObjectInputStream(fis);
        	try {
        		return (ReportDescriptor)o.readObject();
			} finally {
				o.close();
			}
		} finally {
			fis.close();
		}


    }

    /**
     * writes the data from the given ReportDescriptor into the file system
     * @param aDescription to write in the FS
     * @return String : the unique reportID used to identify this request
     */
    public static String write(ReportDescriptor aDescription) throws IOException {
        String reportID = getNewReportID();

        String fileNameTemp = TEMP_PATH + reportID + SEPARATOR + aDescription.getMode() + SEPARATOR + POSTFIX;
        String fileName = OPEN_PATH + reportID + SEPARATOR + aDescription.getMode() + SEPARATOR + POSTFIX;
        FileOutputStream fos = new FileOutputStream(fileNameTemp);
        try {
        	ObjectOutputStream oos = new ObjectOutputStream( fos);
        	try {
        		oos.writeObject(aDescription);
        	} finally {
        		oos.close();
        	}
		} finally {
			fos.close();
		}


        File source = new File(fileNameTemp);
        File dest   = new File(fileName);

        if (!source.renameTo(dest)) {
            Logger.warn("Problem moving '" + source.getAbsolutePath() + "' to '" + dest.getAbsolutePath() + "'",ReportDescriptorImpl.class);
            throw new RuntimeException("Problems moving File '"+fileNameTemp+"' to '"+fileName+"'.");
        }
        return reportID;
    }

    /**
     * IDs from the IDFactory contain ':' which are not allowed in filenames.
     * @return an ID to be used as a filename
     */
    public static String getNewReportID() {
		String id = IdentifierUtil.toExternalForm(StringID.createRandomID());
        id = StringServices.replace(id,":","");
        return id;
    }
}
