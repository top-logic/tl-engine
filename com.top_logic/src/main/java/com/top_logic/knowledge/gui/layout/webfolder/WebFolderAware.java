/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.webfolder;

import com.top_logic.knowledge.wrap.WebFolder;

/**
 * This interface may be implemented by a class knowing a webFolder to 
 * let others access it.
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public interface WebFolderAware {

    /**
     * Enable access to the implementors webfolder.
     * 
     * @return webfolder
     */
    public WebFolder getWebFolder();

}
