/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.logger;

import com.top_logic.basic.util.ResKey;

/**
 * Stores if an error is logged.
 * 
 * <p>Used to check if any error occurred during import.</p>
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class HasAnyErrorLogger implements ImportLogger {

	private boolean technicalErrorLogged=false;

    @Override
    public void error(Object caller, ResKey key, Object... params) {
        technicalErrorLogged=true;
    }

    @Override
    public void error(Object caller, ResKey key, Throwable e, Object... params) {
        technicalErrorLogged=true;
    }

    @Override
    public void warn(Object caller, ResKey key, Object... params) {
    }

    @Override
    public void info(Object caller, ResKey key, Object... params) {
    }

	public boolean hasError(){
		return technicalErrorLogged;
	}
	
}
