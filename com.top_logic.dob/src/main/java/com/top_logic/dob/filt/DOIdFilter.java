
/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.filt;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Filter;
import com.top_logic.dob.DataObject;

/** Filtering Objects by Identifier.
 * <p>
 *   This should only used in rare circumstances since the
 *   DOs are usually mapped by id, but sometimes this class
 *   is needed.
 *</p>
 * @author   <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class DOIdFilter implements Filter {

    /** The attribute used to filter the dataObjects */
	TLID id;

    /** 
     * Create a Filter using the given id.
     */
    public DOIdFilter(TLID anId) {
       id = anId;
    }

    /** Extract the Attributes Value and call test(Object).
     *
     * null-values will never be considered and always result in a 
     * false-test.
     *
     * @param  theObject DataObject where the value will be extracted.
     * @return true in case the Object matches the your desires.
     */
    @Override
	public boolean accept(Object theObject) {
        return id.equals(((DataObject) theObject).getIdentifier());
    }
}
