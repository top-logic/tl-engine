/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import java.util.Date;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.PropertyAccessor;
import com.top_logic.layout.ReadOnlyPropertyAccessor;

/**
 * {@link PropertyAccessor} for the {@link Wrapper#getLastModified()} property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WebFolderDateProperty extends ReadOnlyPropertyAccessor<Object> {

	/**
	 * Singleton {@link WebFolderDateProperty} instance.
	 */
	public static final WebFolderDateProperty INSTANCE = new WebFolderDateProperty();

	private WebFolderDateProperty() {
		// Singleton constructor.
	}
	
	@Override
	public Object getValue(Object anObject) {
		{
			if (anObject instanceof Wrapper) {
				return new Date(((Wrapper) anObject).getLastModified());
			}
			else {
				return null;
			}
		}
	}
}