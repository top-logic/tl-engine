/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.importer.ImportUtils;
import com.top_logic.util.error.TopLogicException;

/**
 * Write the log messages into a file.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class FileLogger extends AbstractLogger implements Closeable {

	/** Alias for the logging directory */
	public static final String IMPORT_LOG_PATH = "%IMPORT_LOG_PATH%";

	public interface Config extends ImportLogger.Config {
		String getFilePrefix();
	}

    private DateFormat TS_FORMAT = CalendarUtil.newSimpleDateFormat("yyyyMMdd_HHmmss");

	private PrintWriter _out;

	private File _file;

	private final String _filePrefix;

    /** 
     * Creates a {@link FileLogger}.
     */
    public FileLogger(InstantiationContext context, Config config) {
		this(config.getFilePrefix());
	}

    /** 
     * Creates a {@link FileLogger}.
     */
    public FileLogger(String filePrefix) {
		super();
		_filePrefix = filePrefix;
		_out        = createPrintWriter();
	}	

    @Override
    public void close() {
        _out = (PrintWriter) StreamUtilities.close(_out);
    }

    @Override
	protected void log(String stack) {
		_out.write(stack);
	}

    /** 
     * Return the file the log messages went to.
     * 
     * @return    The requested file.
     */
    public File getFile() {
        return _file;
    }

    /**
	 * Create a PrintWriter based on {@link #_file}
	 * 
	 * You may wish to override this to care for a special encoding.
	 */
	private PrintWriter createPrintWriter(){
		try {

			_file = getNewFile();
			Logger.info("Logging into '" + _file.getAbsolutePath() + "'" ,this);
			return new PrintWriter(_file);
		} catch (IOException e) {
			Logger.error("Can not create LogFile, abort.",e,this);
			throw new TopLogicException(this.getClass(), "canNotCreateLogFile", e);
		}
	}

	private File getNewFile() {
		while (true) {
			String namePattern = IMPORT_LOG_PATH + '/' + getBaseName() + TS_FORMAT.format(new Date()) + ".log";
			String fileName = AliasManager.getInstance().replace(namePattern);
			File file = new File(fileName);
			if (!file.exists()) {
				return file;
			}
			
			// Wait one second to prevent overwriting the existing old file.
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Ignore.
			}
		}
    }

    private String getBaseName() {
        return _filePrefix + '-' + ImportUtils.INSTANCE.getServerNodeName() + '-';
    }
}
