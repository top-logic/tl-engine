/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.PropertyAccessor;
import com.top_logic.layout.ValidObjectPropertyAccessor;

/**
 * {@link PropertyAccessor} for the {@link DocumentVersion#getDescription()} property.
 * 
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
public class WebFolderDescriptionProperty extends ValidObjectPropertyAccessor<Object> {
	
	/**
	 * Singleton {@link WebFolderDescriptionProperty} instance.
	 */
	public static final WebFolderDescriptionProperty INSTANCE = new WebFolderDescriptionProperty();

	private WebFolderDescriptionProperty() {
		// Singleton constructor.
	}
	
	@Override
	protected Object getValidValue(Object object) {
		if (object instanceof Document) {
			return ((Document) object).getDocumentVersion().getDescription();
		} else if (object instanceof WebFolder) {
			return ((WebFolder) object).getDescription();
		}
		else {
			return null;
		}
	}
	
}