/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.evt;

import com.top_logic.basic.Logger;
import com.top_logic.dsa.ex.DataChangeException;

/** 
 * Abstract implementation of the listener to dispatch the request. 
 * 
 * As implementing class you only have to overwrite the abstract methods.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class AbstractDataChangeListener implements DataChangeListener {

    protected abstract void dataCreated (DataChangeEvent anEvent);

    protected abstract void dataModified (DataChangeEvent anEvent);

    protected abstract void dataDeleted (DataChangeEvent anEvent);

    /** Called in case some Objects is deleted Recursivly ... */
    protected abstract void dataDeletedR (DataChangeEvent anEvent);

    /**
     * Notification, that something in the observed adaptor has changed.
     *
     * @param    anEvent    The event holding the information about 
     *                      the change.
     */
    @Override
	public void dataChanged (DataChangeEvent anEvent) {
        if (Logger.isDebugEnabled (this)) {
            Logger.debug (this.getClass ().getName () + ".dataChanged(" +
                          anEvent + ") called!", this);
        }

        switch (anEvent.getChangeMode () & DataChangeEvent.CHANGEMASK) {
            case DataChangeEvent.CREATED:  this.dataCreated (anEvent);
                                           break;
            case DataChangeEvent.MODIFIED: this.dataModified (anEvent);
                                           break;
            case DataChangeEvent.DELETED:  this.dataDeleted (anEvent);
                                           break;
            case DataChangeEvent.DELETEDREC: this.dataDeletedR (anEvent);
                                           break;
                                           
            default: Logger.error("Unknown ChangeMode in dataChanged(" + anEvent + ")", this);
        }
    }

    /**
     * This method will be called before the change of data will be done.
     *
     * If, and only if, no listener is throwing an exception, the change 
     * will be allowed and executed. This method is needed, if the changes 
     * in the system will affect securiy reasons.
     *
     * @param    anEvent                The event holding the information
     *                                  about the change.
     * @throws   DataChangeException    Thrown, when the change is not allowed,
     *                                  the contained message should explain
     *                                  the reason.
     */
    @Override
	public void checkChangeAllow (DataChangeEvent anEvent) 
                                                throws DataChangeException {
        // override as needed
    }
}
