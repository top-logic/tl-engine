/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
 package com.top_logic.base.security;

/**
 * This Exception is thrown when the internal security of top_logic 
 * is viloated.
 *
 *  TODO TSA This should be a subclass of SecurityException.
 *
 *  @author  mpe
 */
public class UserSecurityException extends RuntimeException
{
    /** Standard constructor with message. */
    public UserSecurityException( String message )
    {
        super( message );
    }
}
