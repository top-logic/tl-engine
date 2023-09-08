/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.sap;


/** Generic superclass for SAP Exceptions.
 *
 * @author 	<a href="mailto:dko@top-logic.com">Dierk Kogge</a>
 */
public class SAPException extends Exception {

    /** 
	  * Constructs a new SAPException without error message string.
      */
    public SAPException ()
	{
        super ();
    }

    /** 
	  * Constructs a new SAPException with the specified error message.
      */
    public SAPException (String message)
	{
        super (message);
    }

    /** 
      * Constructor.
      *
      * @param rootCause - original reason for the Exception
      */
    public SAPException (Throwable rootCause)
	{
		super(rootCause);
    }

    /**
      * Constructor.
      *
      * @param s - a message string
      * @param rootCause - original reason for the Exception
      */
    public SAPException (String s, Throwable rootCause)
	{
		super(s, rootCause);
    }
    
}
