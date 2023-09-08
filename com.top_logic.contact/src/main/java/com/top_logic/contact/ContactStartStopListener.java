/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact;

import com.top_logic.element.ElementStartStop;

/**
 * Show how to set APP_CONTEXT and APP_ROOT for webapps.
 * This one is registered in web.xml, but you must register your own 
 * StartStopListener if needed (at least to set up your Version)
 * 
 * There is a Bug in WAS 5.x when this class does dot (re-)implement
 * {@link javax.servlet.ServletContextListener} it will fail to load 
 *
 * @author   <a href="mailto:jco@top-logic.com">Jörg Conotte</a>
 */
public class ContactStartStopListener extends ElementStartStop
        /* impllements ServletContextListener // may be needed for WAS 5.x */
{
	
    /**
     * Need empty CTor for ServletContextListener.
     */
    public ContactStartStopListener() { /* nothing to do here */ }
	
}
