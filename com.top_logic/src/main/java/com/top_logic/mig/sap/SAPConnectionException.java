/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.sap;


/** This Exception is thrown by MetaObject on appropriate circumstances.
 *
 * @author 	Dierk Kogge
 */
public class SAPConnectionException extends SAPException
{

    /** Constructs a new SAPConnectionException with the specified
      * error message.
      */
    public SAPConnectionException (String message)
	{
        super (message);
    }
}
