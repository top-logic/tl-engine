
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.filt;

import com.top_logic.basic.col.Filter;
import com.top_logic.dob.DataObject;

/** Filter to select Objects by matching MetaObjectName (= Type).
 *
 * @author   <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class DOTypeNameFilter implements Filter<DataObject> {

    /** The MetaObject used to filter the dataObjects */
    String name;

    /** 
     * Create a Filter using the MetaObject name.
     */
    public DOTypeNameFilter(String aName) {
       name = aName;
    }

    /** Check if the Metaobject of the given Dataobject is the same.
     *
     * @param  theObject DataObject where MeatObject will be taken from.
     * @return true in case the MeatObjects are the same (not equals)...
     */
    @Override
	public boolean accept(DataObject theObject) {
        return theObject.isInstanceOf(name); 
    }
}
