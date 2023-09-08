/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.layout.ReadOnlyPropertyAccessor;

/**
 * Access to the document description of a {@link DocumentVersion}.
 * 
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
final class VersionDescriptionProperty extends ReadOnlyPropertyAccessor<DocumentVersion> {
	
	/**
	 * Singleton {@link VersionDescriptionProperty} instance.
	 */
	public static final VersionDescriptionProperty INSTANCE = new VersionDescriptionProperty();

	private VersionDescriptionProperty() {
		// Singleton constructor.
	}
	
	@Override
	public Object getValue(DocumentVersion docVersion) {
        try {
			return docVersion.getDescription();
		}
		catch (Exception ex) {
			Logger.error("Failed to get description of " + docVersion, ex, this);
		    
			return null;
		}
	}
}