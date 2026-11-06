/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.queue;

import java.io.File;

import com.top_logic.basic.Configuration;

/**
 * Common Constants (mostly paths) for the Reporting.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface ReportQueueConstants {
	
    /** The base directory for the report directory, configured by the property {@code path}. */
    public static final File IO_TMPDIR = new File(
            Configuration.getConfiguration(ReportQueueConstants.class).getValue("path", System.getProperty("java.io.tmpdir")));

    /** The directory containing all files of the report queue. */
    public static final File REPORT_DIR = new File(IO_TMPDIR, "report");

    /** The path of {@link #REPORT_DIR}. */
    public static final String PATH = REPORT_DIR.getPath();
	
    /** Contains .ser files received by the {@link QueueReporter} via RMI */ 
	public static final String OPEN_PATH  = PATH + File.separator + "open"  + File.separator;	
	
    /** Contains .ser files to be processed by a {@link ReportCreator} */ 
	public static final String WORK_PATH  = PATH + File.separator + "work"  + File.separator;	
    
	/** Contains .ser files that where processed by a {@link ReportCreator} */ 
	public static final String DONE_PATH  = PATH + File.separator + "done"  + File.separator;	

    /** Contains files that could not be processed by a {@link ReportCreator} */ 
	// TODO KBU/FMA/KHA clean up this directory ?
	public static final String ERROR_PATH = PATH + File.separator + "error" + File.separator;	

	/** Used by the {@link ReportCreator} to unpack and process the .-ser files. 
	 * 
	 * ? Will be cleaned when {@link ReportCreator} is done   
	 */ 
	public static final String TEMP_PATH  = PATH + File.separator + "temp"  + File.separator;
	
	/** File name suffix of the serialized report descriptors. */
	public static final String POSTFIX = ".ser";
    
}
