/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.data;

import java.util.Collection;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.MetaObject;

/** 
 * A Dataobject, that is a collection.
 *
 * @author Marco Perra
 */
public interface DOCollection extends Collection, DataObject {

    /** 
     * The element type of this collection.
     */
    public MetaObject getCollectionType ();
}
