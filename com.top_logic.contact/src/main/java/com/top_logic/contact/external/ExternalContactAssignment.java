/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.external;

import java.util.Date;


/**
 * Extend ExternalContact with assignment info (from - to)
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public interface ExternalContactAssignment extends ExternalContact {

    /** 
     * the start date of the assignment
     */
    public Date getStartDate();
    
    /** 
     * the end date of the assignment
     */
    public Date getEndDate();
}

