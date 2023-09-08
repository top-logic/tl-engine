/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.util.Resources;

/**
 * {@link ResourceProvider} for {@link DocumentVersion}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DocumentVersionResourceProvider extends WrapperResourceProvider {

	/**
	 * Adds the version of the document of the label
	 * 
	 * @see com.top_logic.knowledge.gui.WrapperResourceProvider#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object anObject) {
		Document baseDocument = getDocument(anObject);
		String documentLabel = MetaResourceProvider.INSTANCE.getLabel(baseDocument);
		Integer version = baseDocument.getVersionNumber();
		Resources resources = Resources.getInstance();
		return resources.getString(I18NConstants.DOC_VERSION_LABEL__DOCLABEL_VERSION.fill(documentLabel, version));
	}

	private Document getDocument(Object documentVersion) {
		return ((DocumentVersion) documentVersion).getDocument();
	}

}

