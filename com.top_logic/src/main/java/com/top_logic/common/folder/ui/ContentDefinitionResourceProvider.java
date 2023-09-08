/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder.ui;

import com.top_logic.basic.Named;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * Resource provider for ContentDefinitions
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ContentDefinitionResourceProvider extends DefaultResourceProvider {

	public static ContentDefinitionResourceProvider INSTANCE = new ContentDefinitionResourceProvider();

	@Override
	public String getLabel(Object object) {
		return asContentDefinition(object).getName();
	}

	private Named asContentDefinition(Object object) {
		if (object instanceof FolderNode) {
			object = ((FolderNode) object).getBusinessObject();
		}
		return (Named) object;
	}

	@Override
	public String getType(Object object) {
		if (asContentDefinition(object) instanceof FolderDefinition) {
			return FolderDefinition.class.getName();
		}
		String theName = this.getLabel(object);

		if (theName != null) {
			return MimeTypes.getInstance().getMimeType(theName);
		}

		return null;
	}

}
