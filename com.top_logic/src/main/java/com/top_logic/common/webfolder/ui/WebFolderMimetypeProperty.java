/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import java.io.File;

import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.PropertyAccessor;
import com.top_logic.layout.ReadOnlyPropertyAccessor;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * {@link PropertyAccessor} for the {@link Document#getContentType()} property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WebFolderMimetypeProperty extends ReadOnlyPropertyAccessor<Object> {
	
	/**
	 * Singleton {@link WebFolderMimetypeProperty} instance.
	 */
	public static final WebFolderMimetypeProperty INSTANCE = new WebFolderMimetypeProperty();

	private WebFolderMimetypeProperty() {
		// Singleton constructor.
	}
	
	@Override
	public Object getValue(Object anObject) {
		MimeTypes theTypes = MimeTypes.getInstance();

		if (anObject instanceof Document) {
			String theType = ((Document) anObject).getContentType();

			return theTypes.getDescription(theType);
		} else if (anObject instanceof Wrapper) {
			return MetaLabelProvider.INSTANCE.getLabel(((Wrapper) anObject).tType());
		} else if (anObject instanceof File) {
			String theType = theTypes.getMimeType(((File) anObject).getName());

			return theTypes.getDescription(theType);
		} else {
			return null;
		}
	}
}