/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.LabelProvider;

/**
 * Return the name of a webfolder used inside a zip file
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ZipFolderNameProvider implements LabelProvider {

	public static final LabelProvider INSTANCE = new ZipFolderNameProvider();

	@Override
	public String getLabel(Object object) {
		WebFolder folder = (WebFolder) object;
		if (folder.getOwner() instanceof WebFolder) {
			return folder.getName();
		}
		return WebFolderUtils.DEFAULT_WEBFOLDER_TABLE_RESOURCES.getStringResource("firstNode");
	}

}
