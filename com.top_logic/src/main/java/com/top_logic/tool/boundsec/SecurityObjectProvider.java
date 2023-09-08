/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.io.Serializable;

/**
 * The DefaultSecurityObjectProvider provides the object at which the security should be
 * checked.
 * 
 * SecurityObjectProviders must be stateless as a single instance is cached by the 
 * {@link SecurityObjectProviderManager}
 * 
 * @extends {@link Serializable} in order to allow serialization of components
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface SecurityObjectProvider extends Serializable {

    /**
	 * Gets the object on which the security should be checked.
	 *
	 * @param aChecker
	 *        the bound checker which should check the security
	 * @param model
	 *        The model of the given {@link BoundChecker} or a potential model.
	 * @param aCommandGroup
	 *        the command group to get the security object for
	 * @return the object on which the security should be checked
	 */
	BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup);

}
