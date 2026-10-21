/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextListener;

import com.top_logic.basic.version.Version;

/** 
 * Listener for initialize and destroy the system.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class StartStopListener extends AbstractStartStopListener implements ServletContextListener {

	@Override
	protected Version getVersion() {
		return Version.getApplicationVersion();
	}

    /**
     * Here sub-classes can add completely new initializations.
     * <p>
     * This is prefered to overriding the init method,
     * since it does some checks, exception handling etc.
     * which might get lost else.
     * </p>
     *
     * @param aContext Use this to derive resources etc.
     */
    @Override
	protected void initSubClassHook(ServletContext aContext) throws Exception {
        this.initPersons();
    }
    
    /**
     * Init the Persons from the Security System.
     * @see com.top_logic.knowledge.wrap.person.RefreshUsersTask
     * 
     * Note: destroys TLContext if one exists...
     */
    @Deprecated
    protected void initPersons() {
        // Nothing to do.
    }
    
    /**
     * Is called from contextDestroyed before basic features are shut down.
     * 
     * @deprecated Services should not need this as they should know what to do when shutting down
     */
    @Deprecated
    @Override
	protected void exitSubClassHook(ServletContext context) {
    }

}
