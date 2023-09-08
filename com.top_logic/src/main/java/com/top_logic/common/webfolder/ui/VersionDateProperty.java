/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.layout.ReadOnlyPropertyAccessor;

/**
 * Access to the document date of a {@link DocumentVersion}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class VersionDateProperty extends ReadOnlyPropertyAccessor<DocumentVersion> {
	
	/**
	 * Singleton {@link VersionDateProperty} instance.
	 */
	public static final VersionDateProperty INSTANCE = new VersionDateProperty();

	private VersionDateProperty() {
		// Singleton constructor.
	}
	
	@Override
	public Object getValue(DocumentVersion docVersion) {
        try {
			return docVersion.getDocument().getUpdateTime();
		}
		catch (Exception ex) {
			Logger.error("Failed to get date of " + docVersion, ex, this);
		    
			return null;
		}
	}
}