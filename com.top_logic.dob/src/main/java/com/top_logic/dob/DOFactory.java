/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;


/** 
 * Base interface for a DataObject Factory.
 * 
 * Factories are not used much any more these days. The Container
 * housing the Datobject usually has some method of creating a Datatobject.
 * It may ore may not use a factory to create the final DataObject
 *
 * @deprecated This concept did not really work and was never used widely.
 * 
 * @author  Marco Perra
 */
@Deprecated
public interface DOFactory {

    /**
     * Creates an empty DataObject this MOClass is well
     *
     * @return  an empty DataObject without any set attributes.
     *
     * @throws DataObjectException when creating the Object fails.
     */
    public DataObject createEmptyObject () throws DataObjectException;

}
