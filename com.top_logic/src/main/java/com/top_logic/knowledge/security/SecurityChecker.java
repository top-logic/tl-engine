/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.security;

import com.top_logic.dob.DataObject;
import com.top_logic.tool.boundsec.BoundObject;


/** 
 * Checking instance for access to knowledge objects and knowledge associations.
 *
 * @deprecated Security in TopLogic in TL is done via {@link BoundObject} and
 *             the {@link SecurityManager} so the concept of SecurityCheckers
 *             is obsolete now.
 *
 * Implementing classes are only responsible to take care of one aspect
 * to be checked.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
@Deprecated
public interface SecurityChecker {

    /**
     * Checks, wether the access to the given object is allowed.
     *
     * @param    anObject    The object to be inspected.
     * @return   true, if access allowed, false otherwise.
     */
    public boolean allow (DataObject anObject);

    /**
     * Checks, wether the access to the given attribute of the given object 
     * is allowed.
     *
     * @param    anObject       The object to be inspected.
     * @param    anAttribute    The name of the attribute to be accessed.
     * @return   true, if access allowed, false otherwise.
     */
    public boolean allow (DataObject anObject, String anAttribute);
}
