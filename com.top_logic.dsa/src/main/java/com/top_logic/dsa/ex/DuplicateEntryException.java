/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.ex;

import com.top_logic.dsa.DataSourceAdaptor;
import com.top_logic.dsa.DatabaseAccessException;

/** 
 * This Exception can be thrown in case some entry in a {@link DataSourceAdaptor} already exist.
 *
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class DuplicateEntryException extends DatabaseAccessException {

    /**
     * Creates an exception with the given message.
     *
     * @param    aMessage    The message describing the problem.
     */
    public DuplicateEntryException (String aMessage) {
        super (aMessage);
    }

}
