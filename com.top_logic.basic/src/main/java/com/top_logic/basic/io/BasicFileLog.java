/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.time.CalendarUtil;

/**
 * This class has the ability to log strings into files.
 * There are two mechanisms for logging:
 * <ul>
 * <li><code>
 *      appendIntoLogFile(String aBasicFileName, String aText)
 *      </code> for appending
 * </li><li><code>
 *      logIntoNewFile(String aBasicFileName, String aText)
 *      </code> for creating a new log file
 * </li></ul>
 * <code>aText</code> is the text to log.
 * <code>aBasicFileName</code> describes the type of the log. Some adaptions of the log path
 * can be mode in the configuration of the <code>BasicFileLog</code>.
 * <ul> 
 *  <li> The main log path can be set, all other paths are relative to this one</li>
 *  <li> for every <code>aBasicFileName</code> a path can be set</li>
 *  <li> for every <code>aBasicFileName</code> the usage of year and month folders is possible. 
 *       Year and month folder do the following:
 *       If a new file is created, the file is automatically stored in a 
 *       folder with the name of the actual month.
 *       This folder is stored in a folder with the name of the actual year.</li>
 * </ul>
 * 
 * This class ins intended for small amounts of logging. It maximum throughput
 * on a developer machine is around 120 (new) Files per second.
 *  * When logging more than 500 Messages per day you need some other approach.
 * 
 * TODO KHA/BHU/MGA/FMA join/sync with LoggerAdminComponent.
 * TODO make encoding and dateFormats configurable.
 * 
 * @author    <a href="mailto:fma@top-logic.com">Frank Mausz</a>
 */
public class BasicFileLog extends ConfiguredManagedClass<BasicFileLog.Config> {

	/**
	 * Configuration for {@link BasicFileLog}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<BasicFileLog> {
		/**
		 * The base path for all logs made by {@link BasicFileLog}. See {@link Config#getBasicPath}.
		 */
		String BASIC_PATH = "basicPath";

		/** Getter for {@link Config#BASIC_PATH}. */
		@Name(BASIC_PATH)
		String getBasicPath();

		/** Logs. */
		@Name("logTypes")
		@Key(value = LogType.NAME_ATTRIBUTE)
		@EntryTag("logType")
		Map<String, LogType> getLogTypes();
	}

	/**
	 * Configuration of a log type.
	 */
	public interface LogType extends NamedConfigMandatory {

		/**
		 * See {@link LogType#getFilename()}.
		 */
		String FILENAME = "filename";

		/**
		 * See {@link LogType#getPath}.
		 */
		String PATH = "path";

		/** Default value of {@link LogType#PATH}. */
		public static final String DEFAULT_PATH = "./";

		/**
		 * See {@link LogType#getSuffix}.
		 */
		String SUFFIX = "suffix";

		/** Default value of {@link LogType#SUFFIX}. */
		public static final String DEFAULT_SUFFIX = ".log";

		/**
		 * See {@link LogType#getUseYearMonth}.
		 */
		String USE_YEAR_MONTH = "useYearMonth";

		/**
		 * The base name for the log file.
		 * 
		 * <p>
		 * The base name is appended with a date to differentiate rotated log files.
		 * </p>
		 * 
		 * @see BasicFileLog#getLogfileName(String)
		 */
		@Name(FILENAME)
		String getFilename();

		/** Getter for {@link LogType#PATH}. */
		@Name(PATH)
		@StringDefault(DEFAULT_PATH)
		String getPath();

		/** Getter for {@link LogType#SUFFIX}. */
		@Name(SUFFIX)
		@StringDefault(DEFAULT_SUFFIX)
		String getSuffix();

		/** Getter for {@link LogType#USE_YEAR_MONTH}. */
		@Name(USE_YEAR_MONTH)
		boolean getUseYearMonth();
	}

    /** Default Encoding that is used to convert files to Strings. */
    public static final String ENCODING = "ISO-8859-1";
 
    /** max number of log files of the same type at the same day */
	private static int MAX_FILES=10000;
    
	/** 
	 * variable for basic path, specially used for testing
	 */
	private File basicPath;

	/**
	 * Creates a new {@link BasicFileLog}.
	 */
	public BasicFileLog(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Logs aText in a log file according the given type.
	 * 
	 * If such a log file already exists, the text is appended. After logging, the file is closed. So
	 * the log file can be deleted nearly all the time except when a logging is in progress
	 * 
	 * @param aLogType a configured {@link LogType}.
	 * @param aText
	 *        must not be null.
	 */
	public synchronized void appendIntoLogFile(String aLogType, String aText){
		File theFile = findOrCreateAppendFile(aLogType);
		try{
            Writer theWriter = new OutputStreamWriter (  
                    new FileOutputStream(theFile, /* append */ true), ENCODING);
            theWriter.write (aText);
            // theWriter.flush(); // will flush automatically on close()
            theWriter.close ();
		}
		catch(Exception e){
			Logger.warn("Failed to appendIntoLogFile " +aLogType+ " for File " + theFile.getAbsolutePath(),e,this);
			Logger.warn("Text not logged: " + aText,this);
		}		
		
	}
	
	/**
     * Find or Create the file for aLogType.
     * 
	 * @param aLogType a configured {@link LogType}.
	 * @return returns the file named aBAsicFileNAme + logSuffix in the directory configured for logging
	 * for that basic type
	 */
	private File findOrCreateAppendFile(String aLogType) {
		// create log dir if not existing
		File theDir = getLogPath(aLogType);
		if(! theDir.exists()) {
			theDir.mkdirs();
		}		
		File theFile = new File(theDir, aLogType + getLogSuffix(aLogType));		
		return theFile;
	}
		
	/**
	 * Logs aText in a new file. 
     * 
     * The name of the new file is composed of
	 * aBasicFileName, the current date and, if needed, a running number to ensure 
	 * uniqueness of the filename. The fileType is set to the configured log type.
	 * The file is located in the directory for aBasicFileNae or, if no such directory
	 * is configured, in the default log directory
	 * @param aLogType type of log, will used as default name and enriched with date and suffix
	 * @param aText the text to log
	 */
	public synchronized void logIntoNewFile(String aLogType, String aText){
		File theFile = createNewUniqueFile(aLogType);
		try{
            Writer theWriter = new OutputStreamWriter (  
                    new FileOutputStream(theFile), ENCODING);
            theWriter.write (aText);
            // theWriter.flush(); // will flush automatically on close()
            theWriter.close ();
		}
		catch(Exception e){
			Logger.warn("Logging into file with basic name "+aLogType+" did not work. For the text see the next log message",e,this);
			Logger.warn("Text not logged: "+aText,this);
		}
	}
	
	/**
	 * @param aLogType a configured {@link LogType}.
	 * @return the new created file according to the basic file name
	 */
	private File createNewUniqueFile(String aLogType) {
		// create log dir if not existing
		File theDir = getLogPath(aLogType);
		if(! theDir.exists()){
			theDir.mkdirs();
		}
				
		String theBasicName = getLogfileName(aLogType)
			+ '-' + getFileDateFormat().format(new Date());
		String theSuffix   =  getLogSuffix(aLogType);
		
		File theFile = new File(theDir,theBasicName+theSuffix);
		int i=0;
		while(theFile.exists() && i<MAX_FILES){
			i++;
			String theName= (theBasicName + '(') + i + (')' + theSuffix);
			theFile = new File(theDir, theName);
		}	
        if ( i >= MAX_FILES) {
            // Logger.warn("More than " + MAX_FILES + "for" + theBasicName + " in " + theDir.getAbsolutePath(), this);
            throw new RuntimeException("More than " + MAX_FILES + "for" + theBasicName + " in " + theDir.getAbsolutePath());
        }
		return theFile;
	}

	/**
	 * Resets the basic path and calls super
	 * 
	 * @return the result of the super call
	 * 
	 * @see com.top_logic.basic.Reloadable#reload()
	 */
	public boolean reload() {
		basicPath=null;
		return true;
	}
	

	/**
	 * Returns the path for the given log type
	 * If year and month folders are used their path is added here
	 * @param aLogType a configured {@link LogType}.
	 * @return the path where files with type aLogType are logged
	 */
	private File getLogPath(String aLogType) {
		boolean isYearMonthLog = isYearMonthLog(aLogType);
		File    thePath        = getBasicLogPath(aLogType);
		if(isYearMonthLog) { // Create path like tmp/log/2005/07
			thePath = new File(thePath, getFolderDateFormat().format(new Date()));
		}
		return thePath;
	}
	
	/** DateFomat to create folders */
	DateFormat getFolderDateFormat() {
		return CalendarUtil.newSimpleDateFormat("yyyy/MM");
	}

	/** DateFomat to create new files */
	DateFormat getFileDateFormat() {
		return CalendarUtil.newSimpleDateFormat("yyyy-MM-dd");
	}

	/**
	 * Returns the basic path for all logging activities of the BasicFileLog
	 * 
	 * @return the base path
	 */
	public File getBasicPath() {
		if (basicPath == null) {
			String logDirName = getConfig().getBasicPath();
			{
				basicPath = new File(logDirName);
				if (basicPath == null) {
					throw new NullPointerException("Cannot resolve log dir '" + logDirName + "'.");
				}
				if (!basicPath.exists()) {
					Logger.info("Log dir '" + logDirName + "' does not exist, trying to create.", BasicFileLog.class);
					boolean ok = basicPath.mkdirs();
					if (!ok) {
						Logger.error("Failed to create log dir '" + logDirName + "'.", BasicFileLog.class);
					}
				}
			}
		}
		return basicPath;
	}

	/**
	 * Getter for the {@link LogType}.
	 */
	public LogType getLogType(String logType) {
		return getConfig().getLogTypes().get(logType);
	}
	
	/**
	 * @param logName a configured {@link LogType}.
	 * @return the name for log files with the given log type
	 */
	public String getLogfileName(String logName) {
		LogType logType = getLogType(logName);
		if (logType != null) {
			String filename = logType.getFilename();
			if (!StringServices.isEmpty(filename)) {
				return filename;
			}
		}
		return logName;
	}

	/**
	 * @param logName a configured {@link LogType}.
	 * @return the suffix for log files with the given log type
	 */
	public String getLogSuffix(String logName) {
		LogType logType = getLogType(logName);
		if (logType != null) {
			return logType.getSuffix();
		}
		return LogType.DEFAULT_SUFFIX;
	}
	
	/**
	 * @param logName a configured {@link LogType}.
	 * @return the path
	 */
	public String getPath(String logName) {
		LogType logType = getLogType(logName);
		if (logType != null) {
			return logType.getPath();
		}
		return LogType.DEFAULT_PATH;
	}
	
	/**
	 * @param logName a configured {@link LogType}.
	 * @return the path for log files with the given log type, must not end with an '/'
	 */
	public File getBasicLogPath(String logName) {
		File theBasicPath = getBasicPath();
		String path = getPath(logName);
		return new File(theBasicPath, path);
	}
	
	/**
	 * returns if the given type is logged into folders for years ant  month
	 * 
	 * @param logName a configured {@link LogType}.
	 * @return true if the files will be logged into year and month folders
	 */
	public boolean isYearMonthLog(String logName) {
		LogType logType = getLogType(logName);
		if (logType != null) {
			return getLogType(logName).getUseYearMonth();
		}
		return false;
	}

	/**
	 * Gets the sole instance of this class.
	 */
	public static BasicFileLog getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for instantiation of the {@link BasicFileLog}.
	 */
	public static class Module extends TypedRuntimeModule<BasicFileLog> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<BasicFileLog> getImplementation() {
			return BasicFileLog.class;
		}

	}

}
