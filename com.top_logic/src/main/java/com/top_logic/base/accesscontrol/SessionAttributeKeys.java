/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

/**
  * This class just holds the keys used to request attributes from a session.
  * I found it a good idea, to have all attribute keys at a central place like this 
  * class, to have a quick overview over all attributes bound to a session and their keys.
  * If you want to bind an additional object to a session everywhere in Top Logic, please add
  * the key you are using to this class and use it to reference to the key. Thanks.
  *
  * @author   Thomas.Richter@top-logic.com
  */
public class SessionAttributeKeys {
    
    // Use TLContext now to retrieve the currentUser.
    // public static final String USER = "user";
    
    /**
    used in LoginPageServlet
    */
    public static final String LOGIN_COUNT = "login_count";

    /**
    used in MigrateBean
    */
    public static final String UPDATE_TREE = "updateTree";

    /**
    used in mail.jsp
    */
    public static final String PASSWORD = "password";

}
