/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

/**
 * Simple class holding some constants  needed in the whole system.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface Constants  {

    /** The name of the application. */
    public static final String APPLICATION      = "top-logic";

    /** The standard encoding for the application. */
    public static final String DEFAULT_ENCODING = "iso-8859-1";

    /** The name of the xml portal to load at login */
    public static final String APPL_STARTFRAME = "masterframe";
    
    /** The name of the xml portal to load at login */
    public static final String APPL_STARTFRAMEPASSWD = "masterframepasswd";

    /** This page is hwon in case some feature is disabled */
    public static final String FEATURE_DISABLED = "/html/disabled.html";


    public static final String SERVLET_HOSTNAME = "servlet.hostname";


    public static final String TEMP_DIR         = "dir.temp";


    public static final String START_FOLDER     = "INBOX";

    /** The user name of the administrator. */
    public static final String USERNAME_ADMIN   = "admin";

    /** The default navigation node name for the menus sub-tree. */
    public static final String DEFAULT_MENU_NAME = "Menus";


}
