/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.table;

import javax.servlet.http.HttpServletRequest;

/**
 * Interim solution to untie the WebFolderTable from its componenet.
 * TODO TSA Interim since may 2004 ... and no explanation what the interface 
 *          is doing. 
 * 
 * 
 * @author tsa / kha
 */
public interface AnchorSupport {

    /**
     * Get the goto command TODO MGA what IS the goto command
     * DKH: what about let the renderer use GotoHandler to create goto-link?!
     *
     * @param    anObject       The object to be used for goto.
     */
    public String getGotoStart(HttpServletRequest aRequest, Object anObject);

}
