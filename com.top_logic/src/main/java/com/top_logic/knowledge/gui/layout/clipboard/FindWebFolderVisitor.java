/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.clipboard;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.knowledge.gui.layout.webfolder.WebFolderAware;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The FindWebFolderVisitor searches for visible {@link WebFolderAware} components and fetch the
 * contained {@link WebFolder}s.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class FindWebFolderVisitor extends DefaultDescendingLayoutVisitor {
    
	/** the found {@link WebFolder}s */
	Set<WebFolder> folders;
    
    /**
     * Constructor
     */
    public FindWebFolderVisitor() {
		this.folders = new HashSet<>();
    }
    
	/**
	 * @see com.top_logic.mig.html.layout.LayoutComponentVisitor#visitLayoutComponent(com.top_logic.mig.html.layout.LayoutComponent)
	 */
    @Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
        if (aComponent instanceof WebFolderAware && aComponent.isVisible()) {
			WebFolder theWrapper = ((WebFolderAware) aComponent).getWebFolder();
            if (theWrapper != null) {
				this.folders.add(theWrapper);
            }
         }
        return true;
    }
    
	/**
	 * all {@link WebFolder}s contained in visible {@link WebFolderAware} components.
	 */
	public Set<WebFolder> getWebFolders() {
		return this.folders;
    }

}

