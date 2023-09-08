/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.Map;
import java.util.Set;

/**
 * Logging during security update.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface LogHandler {

    void logSecurityUpdate(Map aSomeNew, Map aSomeRemoved, Map aRulesToObjectsMap,
            Set aInvalidObjects);

}

