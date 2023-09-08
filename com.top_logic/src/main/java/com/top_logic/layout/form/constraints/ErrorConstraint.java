/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.CheckException;

/**
 * The ErrorConstraint will always fail a check with the given error message.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class ErrorConstraint extends AbstractConstraint {

    /** The internationalized error message. */
    private String message;

    /**
     * Creates a new instance of this class.
     *
     * @param errorMessage an already internationalized error message.
     */
    public ErrorConstraint(String errorMessage) {
        message = errorMessage;
    }

    @Override
	public boolean check(Object aValue) throws CheckException {
        throw new CheckException(message);
    }

}
