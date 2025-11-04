/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import com.top_logic.knowledge.wrap.Document;
import com.top_logic.layout.PropertyAccessor;

/**
 * {@link PropertyAccessor} for the {@link Document#getSize()} property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WebFolderSizeProperty extends WebFolderPropertyAccessor<Object> {
	
	/**
	 * Singleton {@link WebFolderSizeProperty} instance.
	 */
	public static final WebFolderSizeProperty INSTANCE = new WebFolderSizeProperty();

	private WebFolderSizeProperty() {
		// Singleton constructor.
	}
	
	@Override
	protected Object getValidValue(Object anObject) {
		if (anObject instanceof Document) {
			return ((Document) anObject).getSize();
		}
		else {
			return null;
		}
	}
	
}