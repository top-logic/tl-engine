/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.webfolder;

import java.util.Collection;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.knowledge.gui.layout.webfolder.WebFolderAware;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * The ExtendedWebfolderAware interface enables the support of
 * OCR and multiple folders on components.
 * 
 * Does not extend {@link WebFolderAware} because single WebFolders are not supported ion parallel 
 * 
 * @author    <a href=mailto:kha@top-logic.com>Karsten Buch</a>
 */
public interface ExtendedWebfolderAware {

	/**
	 * Get the currently relevant WebFolderMetaAttributes
	 * 
	 * @param nonEmptyOnly	if true only delivers attributes that have a WebFolder with children as value
	 * @param aBCG			the BoundCommandGroup that must be granted on an attribute to include it
	 * @return the WebFolderMetaAttributes
	 */
	public Collection<TLStructuredTypePart> getRelevantWebFolderAttributes(boolean nonEmptyOnly, BoundCommandGroup aBCG);
	
	/**
	 * Get the folder context for the given one, i.e. the top-level folder
	 * 
	 * @param aWebFolder	a folder
	 * @return the folder context
	 */
	public WebFolder getWebFolder(WebFolder aWebFolder);
	
	/**
	 * the holder of the WebFolders
	 */
	public Wrapper getWebFolderHolder();
	
	/**
	 * true if OCR should be used to documents when uploading
	 */
	public boolean useOCR();
	
}
