/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

import com.top_logic.common.webfolder.ui.WebFolderUIFactory;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;

/**
 * The ZipDownloadLabelProvider provides the file name for the zip download file
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ZipDownloadLabelProvider implements LabelProvider {

	@Override
	public String getLabel(Object object) {
		return getDownloadName((WebFolder) object);
	}

	private String getDownloadName(WebFolder folder) {
		// no folder, no name
		if (folder == null) {
			return null;
		}

		StringBuffer label = new StringBuffer();
		LabelProvider folderNames = WebFolderUIFactory.getInstance().getZipFolderNameProvider();

		label.append(folderNames.getLabel(folder));

		TLObject parent = folder.getOwner();
		// walk up the folder tree
		while (parent instanceof WebFolder) {
			label.insert(0, "_").insert(0, folderNames.getLabel(parent));
			parent = ((WebFolder) parent).getOwner();
		}

		TLObject owner = folder.getOwner();
		if (owner != null && !(owner instanceof WebFolder)) {
			label.insert(0, "_").insert(0, MetaLabelProvider.INSTANCE.getLabel(owner));
		}
		return label.toString();
	}
}

