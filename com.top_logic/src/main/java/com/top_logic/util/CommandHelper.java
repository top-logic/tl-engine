/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Small helper for checking submit commands in forms,   
 * no matter whether they came from input type="submit" or  
 * input type="image".
 *  
 * @author                      Michael Röschter 
 */
public class CommandHelper {

    /**
     * Checks whether the given command was submitted with the given request.
     *
     * @param aRequest
     *        The current request.
     * @param command
     *        The name of the submit button (also of an image button).
     * @return Whether the request contains a parameter for the given command.
     */
    public static boolean hasCommand (HttpServletRequest aRequest,
                                      String command) {
        String value1 = aRequest.getParameter (command);
        String value2 = aRequest.getParameter (command + ".x");
        return (value1 != null) || (value2 != null);
    }
}
