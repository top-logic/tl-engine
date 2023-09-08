/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.util.ResKey;


/**
 * Combines several loggers into one logger and dispatches the log messages to all of them.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class MultiLogger implements ImportLogger {

	private final List<ImportLogger> loggers;

	public MultiLogger(ImportLogger... loggers) {
		this.loggers = new ArrayList<>(Arrays.asList(loggers));
	}

	@Override
	public void error(Object caller, ResKey key, Object... params) {
		for(ImportLogger logger : loggers){
			logger.error(caller,key,params);
		}
	}

	@Override
	public void error(Object caller, ResKey key, Throwable e, Object... params) {
		for(ImportLogger logger : loggers){
			logger.error(caller,key,e,params);
		}
	}

	@Override
	public void warn(Object caller, ResKey key, Object... params) {
		for(ImportLogger logger : loggers){
			logger.warn(caller,key,params);
		}
	}

	@Override
	public void info(Object caller, ResKey key, Object... params) {
		for(ImportLogger logger : loggers){
			logger.info(caller,key,params);
		}
	}

	@SuppressWarnings("unchecked")
    public <T> T getLogger(Class<T> expectedType){
		for(ImportLogger logger : loggers){
			if(expectedType.isInstance(logger)){
				return (T)logger;
			}
		}
		return null;
	}
	
	public void close(){
		for(ImportLogger logger : loggers){
			if(logger instanceof FileLogger){
				( (FileLogger)logger).close();
			}
		}
	}

	public boolean hasError() {
		return this.getLogger(HasAnyErrorLogger.class).hasError();
	}

	public void add(ImportLogger logger) {
		loggers.add(logger);
	}

	public void remove(ImportLogger logger) {
		loggers.remove(logger);
	}

}
