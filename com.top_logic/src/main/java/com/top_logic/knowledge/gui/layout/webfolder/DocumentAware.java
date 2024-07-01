/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.webfolder;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.knowledge.wrap.WebFolder;

/**
 * Interface for objects being document aware.
 * 
 * That is Object that in someway contain a WebFolder and want its content displayed.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 * 
 * @deprecated getting a WebFolder and writing it, does not fit in one interface. use
 *             WebFolderAware.
 */
@Deprecated
public interface DocumentAware {

    /**
     * Return the current web folder of the implementor.
     * 
     * @return    The current web folder to be used, must not be <code>null</code>.
     */
    public WebFolder getWebFolder();

    /**
     * Set the given folder as the new one for this component.
     * 
     * @param    aFolder    The folder to be displayed now.
     * @return   <code>true</code>, if the folder is not <code>null</code>
     *           and calling the method changes the current folder.s
     */
    public boolean setWebFolder(WebFolder aFolder);

    /**
     * Define the current web folder as dirty.
     * i.e. some model not up to date.
     */
    public void setWebFolderDirty();
    
    /** Write the actual WebFolder (as Table) in some HTML Page */
    public void writeFolder(HttpServletRequest aRequest, HttpServletResponse aResponse) 
        throws IOException, ServletException;
    
}
