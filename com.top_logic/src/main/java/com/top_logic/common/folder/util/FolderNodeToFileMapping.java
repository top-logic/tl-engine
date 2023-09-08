/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder.util;

import java.io.File;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.func.Function1;
import com.top_logic.common.folder.impl.FileDocument;
import com.top_logic.common.folder.impl.FileFolderDefinition;
import com.top_logic.common.folder.model.FolderNode;

/**
 * A {@link Mapping} from {@link FolderNode} to the underlying {@link File}.
 * <p>
 * The {@link FolderNode#getBusinessObject() business object} of the {@link FolderNode} has to be
 * either a {@link FileDocument} or a {@link FileFolderDefinition}.
 * </p>
 * <p>
 * Maps to null, if input is null, not one of the mentioned types or the file itself is null.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class FolderNodeToFileMapping extends Function1<File, FolderNode> {

	/**
	 * The instance of {@link FolderNodeToFileMapping}.
	 */
	public static final FolderNodeToFileMapping INSTANCE = new FolderNodeToFileMapping();

	@Override
	public File apply(FolderNode folderNode) {
		if (folderNode == null) {
			return null;
		}
		Object businessObject = folderNode.getBusinessObject();
		if (businessObject instanceof FileFolderDefinition) {
			return ((FileFolderDefinition) businessObject).getFile();
		}
		if (businessObject instanceof FileDocument) {
			return ((FileDocument) businessObject).getFile();
		}
		return null;
	}

}
