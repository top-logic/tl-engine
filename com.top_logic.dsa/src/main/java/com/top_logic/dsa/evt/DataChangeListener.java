/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.evt;

import java.util.EventListener;

import com.top_logic.dsa.ex.DataChangeException;

/** 
 * A listener, which will be notified, if changes in the DataSourceAdaptor
 * occured.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface DataChangeListener extends EventListener {

    /**
     * Notification, that something in the observed adaptor has changed.
     *
     * @param    anEvent    The event holding the information about 
     *                      the change.
     */
    public abstract void dataChanged (DataChangeEvent anEvent);

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
    public abstract void checkChangeAllow (DataChangeEvent anEvent) 
                                                throws DataChangeException;
}
