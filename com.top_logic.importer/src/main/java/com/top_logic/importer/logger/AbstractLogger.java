/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.logger;

import java.util.Date;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.Resources;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public abstract class AbstractLogger implements ImportLogger{
	
	private final Resources res;

    protected AbstractLogger() {
        this.res = Resources.getInstance();
	}
	
	protected void log(String type, Object caller, ResKey key, Object... params) {
		log(this.getLogText(type, ResKey.message(key, params), caller));
	}

	@Override
	public void error(Object caller, ResKey key, Throwable e, Object... params) {
	    String theMessage = (e != null) ? e.getLocalizedMessage() : null;

	    if (theMessage == null) {
	        theMessage = "---";
	    }

	    if ((params != null) && (params.length > 0)) { 
	        List<Object> theColl = CollectionUtil.toList(params);

	        theColl.add(theMessage);

	        error(caller, key, theColl.toArray());
	    }
	    else {
	        error(caller, key, theMessage);
	    }

	    if (e != null) {
	        log(ExceptionUtil.printThrowableToString(e));
	    }
	}

	@Override
	public void error(Object caller, ResKey key, Object... params) {
	    log(ERROR, caller, key, params);
	}
	
	@Override
	public void warn(Object caller, ResKey key, Object... params) {
	    log(WARN, caller, key, params);
	}
	
	@Override
	public void info(Object caller, ResKey key, Object... params) {
	    log(INFO, caller, key, params);
	}

	protected String getLogText(String type, ResKey message, Object caller) {
	    Date date = new Date();
		String dateTime = HTMLFormatter.getInstance().formatDateTime(date);
		StringBuffer hlp = new StringBuffer();
		hlp.append(dateTime);
		hlp.append(" ");
		hlp.append(type);
		hlp.append(" [");
		hlp.append(Thread.currentThread().getName());
		hlp.append("]");
		hlp.append(": ");
		if(caller != null){
			hlp.append(getCallerName(caller));
		}
		hlp.append(" - ");
		hlp.append(this.getRes().getString(message));
		hlp.append("\n");
		String logText = hlp.toString();
		return logText;
	}
    
    protected Resources getRes() {
        return res;
    }

	/**
	 * Get an output name for the given object
	 *
	 * @param aCaller the calling object
	 *
	 * @return an output name
	 */
	private static String getCallerName (Object aCaller) {
		if (aCaller == null) {
			return "null";
		}
		if (aCaller instanceof String) {
			return (String) aCaller;
		}
		if (aCaller instanceof Class) {
			return ((Class<?>) aCaller).getSimpleName();
		}
		return aCaller.getClass ().getSimpleName ();
	}
	

	protected abstract void log(String stack);
}
