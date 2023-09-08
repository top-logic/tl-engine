/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office;

/**
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public class OfficeNotReadyException extends RuntimeException {

    /** 
     * Creates a {@link OfficeNotReadyException}.
     * 
     */
    public OfficeNotReadyException() {
    }

    /** 
     * Creates a {@link OfficeNotReadyException}.
     */
    public OfficeNotReadyException(String someMessage) {
        super(someMessage);
    }

    /** 
     * Creates a {@link OfficeNotReadyException}.
     */
    public OfficeNotReadyException(Throwable someCause) {
        super(someCause);
    }

    /** 
     * Creates a {@link OfficeNotReadyException}.
     */
    public OfficeNotReadyException(String someMessage, Throwable someCause) {
        super(someMessage, someCause);
    }

}

