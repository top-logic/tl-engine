
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.filt;

import com.top_logic.basic.col.Filter;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MetaObject;

/** Filter to select Objects by matching MetaObject (= Type).
 * <p>
 *  For better performance this is done using a == test and no
 *  (slow) equals Test,
 *</p>
 * @author   <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class DOTypeFilter implements Filter {

    /** The MetaObject used to filter the dataObjects */
    MetaObject  meta;

    /** 
     * Create a Filter using the given MetaObject.
     */
    public DOTypeFilter(MetaObject aMeta) {
       meta = aMeta;
    }

    /** Check if the Metaobject of the given Dataobject is the same.
     *
     * @param  theObject DataObject where MeatObject will be taken from.
     * @return true in case the MeatObjects are the same (not equals)...
     */
    @Override
	public boolean accept(Object theObject) {
        return meta == ((DataObject) theObject).tTable(); 
    }
}
