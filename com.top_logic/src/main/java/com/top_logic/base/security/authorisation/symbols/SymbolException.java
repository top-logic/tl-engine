/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
 package com.top_logic.base.security.authorisation.symbols;

/**
 * This Exception is thrown if a symbol is not known in the system or
 * if the symbol definition is erroneous. 
 *
 *  TODO TRI This should be a subclass of SecurityException.
 *
 *  @author  kbu
 */
public class SymbolException extends RuntimeException
{
    /** Standard constructor with message. */
    public SymbolException( String message )
    {
        super( message );
    }
}
