/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.sap;

/**
 * Holds constant values used as defaults etc.
 */
public final class SAPConst
{
	/**
	  * The string that indicates a BAPI function name in a physical resource string.
	  */
	public static final String FUNCTION = "function";
    /**
      * The string that indicates a SAP client id in a physical resource string.
      */
    public static final String CLIENT   = "client";
    /**
      * The string that indicates a server name in a physical resource string.
      */
    public static final String SERVER   = "server";
    /**
      * The string that indicates a SAP system language in a physical resource string.
      */
    public static final String LANGUAGE = "language";
    /**
      * The string that indicates a SAP system id in a physical resource string.
      */
    public static final String SYSTEM   = "system";
    /**
      * The string that indicates a user name in a physical resource string.
      */
    public static final String USER     = "user";
    /**
      * The string that indicates a users password in a physical resource string.
      */
    public static final String PASSWORD = "password";

    /**
	  * The separator that separates parameters in a physical resource string
	  */
    public static final String COMMA_SEP = ",";
    
	/**
      * The separator that separates name and value of a physical resource parameter
      */
    public static final String PARAM_SEP = "=";

	/**
      * The separator that separates sub names in a fully qualified object name
      */
    public static final String NAME_SEP = ".";

    /**
	  * The extension that is appended to a BAPI function name in case of a
	  * meta or data object for input parameter is to be created for the BAPI
	  * function.
	  */
	public static final String BAPI_INPUT_EXTENSION = "(IMPORT)";
	
	/**
	  * The extension that is appended to a table  meta object name
	  * to differentiate it from the meta object name of structure in
	  * the table (collection) that contains this structure.
	  */
	public static final String TABLE_NAME_EXTENSION = "(TBL)";
	
	/**
	  * The extension that is appended to a table structure meta object name
	  * to differentiate it from the meta object name of the table (collection)
	  * that contains this structure.
	  */
	public static final String TABLE_STRUCTURE_EXTENSION = "STR";
	
	/**
	 * two extensions needed for the evaluation of the dataobject
	 * returned from the sap. see sapdatasourceadaptor method:
	 * getObjectEntry (jza)
	 */
	public static final String BAPI_INPUT = ".INPUT";
	public static final String BAPI_OUTPUT = ".OUTPUT";
	public static final String BAPI_OUTPUT_RETURN = ".OUTPUT.BAPIRETURN";   
}
