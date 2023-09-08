/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.exceptions;

/**
 * Problem when accessing a persistent object.
 * 
 * @author <a href=mailto:fma@top-logic.com>fma</a>
 */
public class WrapperRuntimeException extends RuntimeException{

    public WrapperRuntimeException() {
        super();
    }

	public WrapperRuntimeException(Throwable e) {
       super(e);
    }
    
    public WrapperRuntimeException(String aMessage) {        
       super(aMessage);
    }
    
    public WrapperRuntimeException(String aMessage, Throwable aThrowable) {        
       super(aMessage, aThrowable);
    }    
}
