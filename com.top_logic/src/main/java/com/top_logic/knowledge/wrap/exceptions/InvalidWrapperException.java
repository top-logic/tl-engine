/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.exceptions;

/**
 * This exception indicates an attempt to access an invalid wrapper.
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class InvalidWrapperException extends WrapperRuntimeException {
    public InvalidWrapperException(String aMessage) {
        super (aMessage);
    }
}
