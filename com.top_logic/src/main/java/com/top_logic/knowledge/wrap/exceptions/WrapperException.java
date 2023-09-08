/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.exceptions;

/**
 * General Exception for wrappers.
 *
 * @author <a href="mailto:mer@top-logic.com">Michael Eriksson</a>
 * 
 * @deprecated Throw or catch {@link WrapperRuntimeException} instead.
 */
@Deprecated
public class WrapperException extends WrapperRuntimeException {

	@Deprecated
    public WrapperException (String aMessage) {
		throw new WrapperRuntimeException(aMessage);
    }

	@Deprecated
    public WrapperException (Throwable aThrowable) {
		throw new WrapperRuntimeException(aThrowable);
    }

	@Deprecated
    public WrapperException (String aMessage, Throwable aThrowable) {
		throw new WrapperRuntimeException(aMessage, aThrowable);
    }

}

