/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder;

import java.util.Collection;

import com.top_logic.basic.Named;

/**
 * Represents a folder
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface FolderDefinition extends Named {

	/**
	 * all content elements of this
	 */
	public Collection<Named> getContents();

	/**
	 * Check if the given object is linked to this folder.
	 * 
	 * @param content
	 *        the wrapper to check, may be <code>null</code>
	 */
	public abstract boolean isLinkedContent(Named content);

}
